package org.lostcityinterfaceeditor.helpers;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import java.util.*;
    
public class LayoutHelper {
    private final Map<String, Rectangle2D> bounds = new HashMap<>();
    private final Map<String, List<Point2D>> iconOffsets = new HashMap<>();
    private double frameWidth, frameHeight;

    public LayoutHelper setFrame(double w, double h) {
        this.frameWidth = w;
        this.frameHeight = h;
        return this;
    }

    public double getFrameWidth() { return frameWidth; }
    public double getFrameHeight() { return frameHeight; }

    public LayoutHelper set(String key, double x, double y, double w, double h) {
        bounds.put(key, new Rectangle2D(x, y, w, h));
        return this;
    }

    public LayoutHelper setIcons(String key, Point2D... points) {
        iconOffsets.put(key, Arrays.asList(points));
        return this;
    }

    public Rectangle2D get(String key) {
        return bounds.getOrDefault(key, Rectangle2D.EMPTY);
    }

    public List<Point2D> getIcons(String key) {
        return iconOffsets.getOrDefault(key, Collections.emptyList());
    }

    public static final LayoutHelper Legacy = new LayoutHelper()
            .setFrame(789, 532)
            .set("mapback",   561, 5, 168, 160)
            .set("chatback",  22, 375, 479, 96)
            .set("viewport",  8, 11, 512, 334)
            .set("sidebar",   562, 231, 190, 261)
            .set("backbase1", 0, 471, 501, 61)
            .set("backbase2", 501, 492, 288, 40)
            .set("backhmid1", 520, 165, 269, 66)
            .set("backleft1", 0, 11, 0, 0)
            .set("backleft2", 0, 375, 0, 0)
            .set("backright1", 729, 5, 0, 0)
            .set("backright2", 752, 231, 0, 0)
            .set("backvmid1", 520, 11, 0, 0)
            .set("backvmid2", 520, 231, 0, 0)
            .set("backvmid3", 501, 375, 0, 0)
            .set("backhmid2", 0, 345, 0, 0)
            .setIcons("hmidIcons", new Point2D(35,34), new Point2D(59,32), new Point2D(86,32), new Point2D(121,33), new Point2D(157,34), new Point2D(185,32), new Point2D(212,34))
            .setIcons("baseIcons", new Point2D(80,2), new Point2D(107,3), new Point2D(142,4), new Point2D(179,2), new Point2D(206,2), new Point2D(230,2));

    public static final LayoutHelper Standard = new LayoutHelper()
            .setFrame(765, 503)
            .set("viewport",  4, 4, 512, 334)
            .set("mapback",   550, 4, 172, 156)
            .set("chatback",  17, 357, 479, 96)
            .set("sidebar",   553, 205, 190, 261)
            .set("backbase1", 0, 453, 496, 50)
            .set("backbase2", 496, 466, 269, 37)
            .set("backhmid1", 516, 160, 249, 45)
            .set("backhmid2", 0, 338, 0, 0)
            .set("backleft1", 0, 4, 0, 0)
            .set("backleft2", 0, 357, 0, 0)
            .set("backright1", 722, 4, 0, 0)
            .set("backright2", 743, 205, 0, 0)
            .set("backtop1",  0, 0, 0, 0)
            .set("backtop2",  550, 0, 0, 0)
            .set("backvmid1", 516, 4, 0, 0)
            .set("backvmid2", 516, 205, 0, 0)
            .set("backvmid3", 496, 357, 0, 0)
            .setIcons("hmidIcons", new Point2D(29,13), new Point2D(53,11), new Point2D(82,11), new Point2D(115,12), new Point2D(153,13), new Point2D(180,11), new Point2D(208,13))
            .setIcons("baseIcons", new Point2D(74,0), new Point2D(102,0), new Point2D(135,1), new Point2D(173,0), new Point2D(201,0), new Point2D(229,0));
}