package org.lostcityinterfaceeditor.fileUtils;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FontPacker {
    private static final int TRANSPARENT_RGB = 0xFF00FF;

    private record Sprite(int x, int y, int w, int h, int pixelOrder) {}

    public static byte[] pack(String pngPath, String optPath) throws IOException {
        File file = new File(pngPath);
        Image img = new Image(file.toURI().toString());
        PixelReader reader = img.getPixelReader();

        int imgWidth = (int) img.getWidth();
        int imgHeight = (int) img.getHeight();

        List<Integer> colors = buildPalette(reader, imgWidth, imgHeight);

        List<Sprite> sprites = new ArrayList<>();
        int tileX = imgWidth;
        int tileY = imgHeight;

        try (BufferedReader br = new BufferedReader(new FileReader(optPath))) {
            String firstLine = br.readLine();

            if (firstLine.contains("x")) {
                String[] dims = firstLine.split("x");
                tileX = Integer.parseInt(dims[0].trim());
                tileY = Integer.parseInt(dims[1].trim());

                String line;
                while ((line = br.readLine()) != null && !line.isBlank()) {
                    sprites.add(parseSprite(line));
                }
            } else {
                sprites.add(parseSprite(firstLine));
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        dos.writeShort(0);

        if (sprites.size() > 1) {
            int tilesX = imgWidth / tileX;
            int tilesY = imgHeight / tileY;

            for (int ty = 0; ty < tilesY; ty++) {
                for (int tx = 0; tx < tilesX; tx++) {
                    Sprite meta = sprites.get(tx + ty * tilesX);
                    writeSprite(dos, reader, tx * tileX, ty * tileY, tileX, tileY, meta, colors);
                }
            }
        } else {
            Sprite meta = sprites.isEmpty() ? null : sprites.get(0);
            writeSprite(dos, reader, 0, 0, imgWidth, imgHeight, meta, colors);
        }

        return baos.toByteArray();
    }

    private static void writeSprite(
            DataOutputStream dos,
            PixelReader reader,
            int originX, int originY,
            int bitmapW, int bitmapH,
            Sprite meta,
            List<Integer> colors
    ) throws IOException {
        int left = 0, top = 0, right = bitmapW, bottom = bitmapH;
        int pixelOrder;

        if (meta != null && meta.w() != 0 && meta.h() != 0) {
            left = meta.x();
            top = meta.y();
            right = meta.w();
            bottom = meta.h();
            pixelOrder = meta.pixelOrder();
        } else {
            pixelOrder = generatePixelOrder(reader, originX, originY, bitmapW, bitmapH);
        }

        if (pixelOrder == 0) {
            for (int j = 0; j < bitmapW * bitmapH; j++) {
                int x = j % bitmapW;
                int y = j / bitmapW;
                if (x >= right || y >= bottom) continue;

                int px = originX + left + x;
                int py = originY + top + y;
                dos.writeByte(getPaletteIndex(reader, px, py, colors));
            }
        } else {
            for (int x = 0; x < bitmapW; x++) {
                for (int y = 0; y < bitmapH; y++) {
                    if (x >= right || y >= bottom) continue;

                    int px = originX + left + x;
                    int py = originY + top + y;
                    dos.writeByte(getPaletteIndex(reader, px, py, colors));
                }
            }
        }
    }

    private static int generatePixelOrder(PixelReader reader, int originX, int originY, int w, int h) {
        long rowScore = 0, colScore = 0;
        int prev;

        prev = 0;
        for (int j = 0; j < w * h; j++) {
            int x = originX + (j % w);
            int y = originY + (j / w);
            int current = getRGB(reader, x, y);
            rowScore += current - prev;
            prev = current;
        }

        prev = 0;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int current = getRGB(reader, originX + x, originY + y);
                colScore += current - prev;
                prev = current;
            }
        }

        return colScore < rowScore ? 0 : 1;
    }

    private static List<Integer> buildPalette(PixelReader reader, int width, int height) {
        List<Integer> colors = new ArrayList<>();
        colors.add(TRANSPARENT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = getRGB(reader, x, y);
                if (rgb == TRANSPARENT_RGB) continue;
                if (!colors.contains(rgb)) {
                    colors.add(rgb);
                }
            }
        }

        return colors;
    }

    private static int getPaletteIndex(PixelReader reader, int x, int y, List<Integer> colors) {
        int rgb = getRGB(reader, x, y);
        int idx = colors.indexOf(rgb);
        return idx == -1 ? 0 : idx;
    }

    private static int getRGB(PixelReader reader, int x, int y) {
        Color c = reader.getColor(x, y);
        int r = (int) Math.round(c.getRed() * 255);
        int g = (int) Math.round(c.getGreen() * 255);
        int b = (int) Math.round(c.getBlue() * 255);
        return (r << 16) | (g << 8) | b;
    }

    private static Sprite parseSprite(String line) {
        String[] parts = line.split(",");
        int x = Integer.parseInt(parts[0].trim());
        int y = Integer.parseInt(parts[1].trim());
        int w = Integer.parseInt(parts[2].trim());
        int h = Integer.parseInt(parts[3].trim());
        int pixelOrder = (parts.length > 4 && parts[4].trim().equalsIgnoreCase("row")) ? 1 : 0;
        return new Sprite(x, y, w, h, pixelOrder);
    }
}