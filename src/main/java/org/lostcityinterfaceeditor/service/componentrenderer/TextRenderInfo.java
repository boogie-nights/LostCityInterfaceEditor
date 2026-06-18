package org.lostcityinterfaceeditor.service.componentrenderer;

import org.lostcityinterfaceeditor.helpers.FontHelper;

import java.util.ArrayList;

public class TextRenderInfo {

	private ArrayList<String> lines;
	private ArrayList<Integer> lineStartXPositions;
	private ArrayList<Double> lineYPositions;
	private FontHelper font;
	private String text;
	private boolean shadowed;
	private boolean centered;
	private double containerWidth;

	public TextRenderInfo(FontHelper font, String text, boolean shadowed, boolean centered, double containerWidth)
	{
		this.lines = new ArrayList<>();
		this.lineStartXPositions = new ArrayList<>();
		this.lineYPositions = new ArrayList<>();
		this.font = font;
		this.text = text;
		this.shadowed = shadowed;
		this.centered = centered;
		this.containerWidth = containerWidth;
	}

	public void addLines(String string) {
		lines.add(string);
	}

	public void addLineStartXPosition(int startX) {
		lineStartXPositions.add(startX);
	}

	public void addLineYPosition(double yPosition) {
		lineYPositions.add(yPosition);
	}

	public ArrayList<String> getLines() {
		return lines;
	}

	public ArrayList<Integer> getLineStartXPositions() {
		return lineStartXPositions;
	}

	public ArrayList<Double> getLineYPositions() {
		return lineYPositions;
	}

	public FontHelper getFont() {
		return font;
	}

	public boolean isShadowed() {
		return shadowed;
	}
}