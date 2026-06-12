package org.lostcityinterfaceeditor.service.componentrenderer;

import org.lostcityinterfaceeditor.helpers.FontHelper;

import java.util.ArrayList;

public class TextRenderInfo {

	private ArrayList<String> lines = new ArrayList<>();
	private ArrayList<Integer> lineStartXPositions = new ArrayList<>();
	private ArrayList<Double> lineYPositions = new ArrayList<>();
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
}