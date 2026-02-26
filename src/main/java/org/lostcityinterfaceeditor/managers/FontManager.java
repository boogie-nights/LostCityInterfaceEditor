package org.lostcityinterfaceeditor.managers;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.lostcityinterfaceeditor.helpers.FontHelper;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FontManager {

    private final Map<String, FontHelper> fonts = new HashMap<>();

    public List<String> getLoadedFontNames() {
        List<String> names = new ArrayList<>(fonts.keySet());
        Collections.sort(names);
        return names;
    }

    public void loadFont(String fontImageFile) throws IOException {
        File fontFile = new File(fontImageFile);
        String fontName = fontFile.getName();
        int dotIndex = fontName.lastIndexOf('.');
        if (dotIndex > 0) {
            fontName = fontName.substring(0, dotIndex);
        }

        FontHelper font = new FontHelper(fontImageFile);
        fonts.put(fontName, font);
    }

    public FontHelper getFont(String fontName) {
        return fonts.get(fontName);
    }

    public void drawTaggableText(GraphicsContext gc, String fontName, String text,
                                 double x, double y, int rgb, boolean shadow) {
        FontHelper font = getFont(fontName);
        Color color = Color.rgb((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);

        if (font != null) {
            font.drawTextWithTags(gc, text, x, y, color, shadow);
        } else {
            System.err.println("Font not found: " + fontName);
        }
    }
}