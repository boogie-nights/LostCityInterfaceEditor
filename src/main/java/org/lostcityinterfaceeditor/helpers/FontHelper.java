package org.lostcityinterfaceeditor.helpers;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.lostcityinterfaceeditor.fileUtils.FontPacker;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class FontHelper {
    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"£$%^&*()-_=+[{]};:'@#~,<.>/?\\| ";
    private static final int[] CHAR_LOOKUP = new int[256];

    static {
        for (int i = 0; i < 256; i++) {
            int c = CHARSET.indexOf((char) i);
            CHAR_LOOKUP[i] = (c == -1) ? 94 : c;
        }
    }

    private final byte[][] charMask = new byte[256][];
    private final int[] charMaskWidth = new int[256];
    private final int[] charMaskHeight = new int[256];
    private final int[] charOffsetX = new int[256];
    private final int[] charOffsetY = new int[256];
    private final int[] charAdvance = new int[256];
    private final int[] glyphWidths = new int[256];

    public final Map<Integer, WritableImage> charImages = new HashMap<>();
    private Image fontImage;
    private int gridSize;
    private int charsPerRow;
    public int height;

    private final boolean isFull;

    public FontHelper(String imagePngFile) throws IOException {
        File fontFile = new File(imagePngFile);
        String fontName = fontFile.getName().toLowerCase();
        
        this.isFull = fontName.contains("_full");

        fontImage = new Image(fontFile.toURI().toString());

        if (fontName.lastIndexOf('.') > 0) {
            fontName = fontName.substring(0, fontName.lastIndexOf('.'));
        }

        String baseDir = fontFile.getParent();
        File optFile = new File(baseDir, "meta/" + fontName + ".opt");

        byte[] datBuffer = FontPacker.pack(imagePngFile, optFile.getPath());
        loadFontData(optFile.getPath(), datBuffer);

        createCharacterImagesFromFontImage();
    }

    private void loadFontData(String fontDataFile, byte[] dat) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fontDataFile))) {
            String firstLine = reader.readLine();
            if (firstLine == null) throw new IOException("Font opt file is empty");

            String[] dimensions = firstLine.split("x");
            gridSize = Integer.parseInt(dimensions[0].trim());
            charsPerRow = (int) fontImage.getWidth() / gridSize;

            int maxGlyphs = isFull ? 256 : 94;

            int datPos = 2;

            for (int i = 0; i < maxGlyphs; i++) {
                String line = reader.readLine();
                if (line == null || line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                charOffsetY[i] = Integer.parseInt(parts[1].trim());
                int w = charMaskWidth[i] = Integer.parseInt(parts[2].trim());
                int h = charMaskHeight[i] = Integer.parseInt(parts[3].trim());
                int pixelOrder = (parts.length > 4 && parts[4].trim().equalsIgnoreCase("row")) ? 1 : 0;

                int len = w * h;
                charMask[i] = new byte[len];

                if (len > 0) {
                    if (pixelOrder == 0) {
                        for (int j = 0; j < len; j++) charMask[i][j] = dat[datPos++];
                    } else {
                        for (int x = 0; x < w; x++)
                            for (int y = 0; y < h; y++)
                                charMask[i][x + y * w] = dat[datPos++];
                    }
                }

                if (h > height && (isFull ? i < 128 : true)) height = h;

                charOffsetX[i] = 1;
                charAdvance[i] = w + 2;
                
                int spaceLeft = 0;
                for (int j = h / 7; j < h; j++) spaceLeft += charMask[i][j * w];
                if (spaceLeft <= h / 7) { charAdvance[i]--; charOffsetX[i] = 0; }

                int spaceRight = 0;
                for (int j = h / 7; j < h; j++) spaceRight += charMask[i][w - 1 + j * w];
                if (spaceRight <= h / 7) charAdvance[i]--;
            }

            if (isFull) {
                boolean isQuill = fontDataFile.toLowerCase().contains("q8");
                if (isQuill) {
                    charAdvance[32] = charAdvance[73];
                } else {
                    charAdvance[32] = charAdvance[105];
                }
            } else {
                charAdvance[94] = charAdvance[8];
            }

            for (int c = 0; c < 256; c++) {
                if (isFull) {
                    glyphWidths[c] = charAdvance[c];
                } else {
                    glyphWidths[c] = charAdvance[CHAR_LOOKUP[c]];
                }
            }
        }
    }

    private void createCharacterImagesFromFontImage() {
        int count = isFull ? 256 : 94;

        for (int i = 0; i < count; i++) {
            if (charMaskWidth[i] <= 0 || charMaskHeight[i] <= 0) continue;

            int gridRow = i / charsPerRow;
            int gridCol = i % charsPerRow;

            WritableImage img = extractCharacter(fontImage, gridCol * gridSize, gridRow * gridSize,
                    charMaskWidth[i], charMaskHeight[i], i);
            
            if (isFull) {
                charImages.put(i, img);
            } else {
                for (int ascii = 0; ascii < 256; ascii++) {
                    if (CHAR_LOOKUP[ascii] == i) {
                        charImages.put(ascii, img);
                    }
                }
            }
        }
    }

    private WritableImage extractCharacter(Image source, int xOff, int yOff, int w, int h, int index) {
        WritableImage charImage = new WritableImage(w, h);
        PixelWriter pw = charImage.getPixelWriter();
        PixelReader pr = source.getPixelReader();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (charMask[index][x + y * w] != 0) {
                    pw.setColor(x, y, pr.getColor(xOff + x, yOff + y));
                } else {
                    pw.setColor(x, y, Color.TRANSPARENT);
                }
            }
        }
        return charImage;
    }

    public int getTextWidth(String str) {
        if (str == null) return 0;
        int size = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '@' && i + 4 < str.length() && str.charAt(i + 4) == '@') {
                i += 4;
            } else {
                int c = str.charAt(i);
                size += glyphWidths[c < 256 ? c : 32];
            }
        }
        return size;
    }

    public void drawTextWithTags(GraphicsContext gc, String text, double x, double y, Color defaultColor, boolean shadowed) {
        if (text == null) return;
        double currentX = x;
        double offY = y - height;
        Color currentColor = defaultColor;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '@' && i + 4 < text.length() && text.charAt(i + 4) == '@') {
                currentColor = evaluateColorTag(text.substring(i + 1, i + 4));
                i += 4;
                continue;
            }

            int ascii = c < 256 ? c : 32;
            int glyphIndex = isFull ? ascii : CHAR_LOOKUP[ascii];

            WritableImage image = charImages.get(ascii);
            if (image != null && ascii != 32) {
                if (shadowed) {
                    gc.drawImage(createColoredCharacter(image, Color.BLACK),
                            currentX + charOffsetX[glyphIndex] + 1, offY + charOffsetY[glyphIndex] + 1);
                }
                gc.drawImage(createColoredCharacter(image, currentColor),
                        currentX + charOffsetX[glyphIndex], offY + charOffsetY[glyphIndex]);
            }
            currentX += glyphWidths[ascii];
        }
    }

    private Color evaluateColorTag(String tag) {
        switch (tag) {
            case "red":
                return Color.web("#ff0000");
            case "gre":
                return Color.web("#00ff00");
            case "blu":
                return Color.web("#0000ff");
            case "yel":
                return Color.web("#ffff00");
            case "cya":
                return Color.web("#00ffff");
            case "mag":
                return Color.web("#ff00ff");
            case "whi":
                return Color.web("#ffffff");
            case "bla":
                return Color.web("#000000");
            case "lre":
                return Color.web("#ff9040");
            case "dre":
                return Color.web("#800000");
            case "dbl":
                return Color.web("#000080");
            case "or1":
                return Color.web("#ffb000");
            case "or2":
                return Color.web("#ff7000");
            case "or3":
                return Color.web("#ff3000");
            case "gr1":
                return Color.web("#c0ff00");
            case "gr2":
                return Color.web("#80ff00");
            case "gr3":
                return Color.web("#40ff00");
            default:
                return Color.web("#000000");
        }
    }

    private WritableImage createColoredCharacter(Image source, Color color) {
        int width = (int) source.getWidth();
        int height = (int) source.getHeight();
        WritableImage coloredImage = new WritableImage(width, height);

        PixelReader reader = source.getPixelReader();
        PixelWriter writer = coloredImage.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixelColor = reader.getColor(x, y);

                if (pixelColor.getOpacity() > 0) {
                    Color newColor = new Color(
                            color.getRed(),
                            color.getGreen(),
                            color.getBlue(),
                            pixelColor.getOpacity()
                    );
                    writer.setColor(x, y, newColor);
                } else {
                    writer.setColor(x, y, Color.TRANSPARENT);
                }
            }
        }
        return coloredImage;
    }
}