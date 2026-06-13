package org.lostcityinterfaceeditor.service.componentrenderer;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import org.lostcityinterfaceeditor.LostCityInterfaceEditor;
import org.lostcityinterfaceeditor.baseCode.Model;
import org.lostcityinterfaceeditor.baseCode.Pix3D;
import org.lostcityinterfaceeditor.helpers.FontHelper;
import org.lostcityinterfaceeditor.helpers.StringUtils;
import org.lostcityinterfaceeditor.loaders.AssetLoader;
import org.lostcityinterfaceeditor.models.ApplicationState;
import org.lostcityinterfaceeditor.models.InterfaceComponent;

import java.util.*;

public class ComponentRenderer {

	private final AssetLoader assetLoader;

	private Map<String, InterfaceComponent> componentMap = new HashMap<>();
	private Map<String, Pane> componentPaneMap = new HashMap<>();
	private Map<String, List<String>> layerChildrenMap = new HashMap<>();
	private Map<String, Boolean> layerVisibilityMap = new HashMap<>();
	private List<InterfaceComponent> interfaceComponents;

	public ComponentRenderer(AssetLoader assetLoader, List<InterfaceComponent> interfaceComponents) {
		this.assetLoader = assetLoader;
		this.interfaceComponents = interfaceComponents;
	}

	public void renderComponents() {
		//hideTooltip();

// Move to UI Builder for component
//		root.setOnMouseClicked(event -> {
//			if (activeComponentName != null && event.getTarget() == root) {
//				Pane activePane = componentPaneMap.get(activeComponentName);
//				InterfaceComponent activeComponent = componentMap.get(activeComponentName);
//
//				if (activePane != null && activeComponent != null &&
//						activeComponent.getActiveGraphicName() != null) {
//					ImageView activeImageView = (ImageView) activePane.getChildren().get(0);
//					activeImageView.setImage(assetLoader.getSpriteManager().getSprite(activeComponent.getGraphicName(),
//							activeComponent.getGraphicIndex()));
//				}
//
//				activeComponentName = null;
//			}
//		});

		for (InterfaceComponent component : interfaceComponents) {
			if (component == null) {
				continue;
			}

			componentMap.put(component.getName(), component);

			if ("layer".equals(component.getType())) {
				layerVisibilityMap.put(component.getName(), !component.isHide());
				layerChildrenMap.put(component.getName(), new ArrayList<>());
			}

			if (component.getLayer() == null || component.getLayer().isEmpty()) {
				continue;
			}

			layerChildrenMap.computeIfAbsent(component.getLayer(), k -> new ArrayList<>())
					.add(component.getName());
		}

		for (InterfaceComponent component : interfaceComponents) {
			if (component == null || component.getLayer() == null || component.getLayer().isEmpty()) {
				continue;
			}

			InterfaceComponent parentComponent = componentMap.get(component.getLayer());

			if (parentComponent == null) {
				System.err.println("Warning: Layer reference '" + component.getLayer() +
						"' not found for component '" + component.getName() + "'");
				continue;
			}

			double x = component.getX();
			double y = component.getY();

			x += parentComponent.getX();
			y += parentComponent.getY();

			RenderableJagComponent componentToRender = new RenderableJagComponent(component, x, y);

			Pane componentPane = null;

			if ("layer".equals(component.getType())) {
				componentPane = addComponentLayer(componentToRender);
			}
			else if ("graphic".equals(component.getType())) {
				componentPane = addComponentGraphic(componentToRender);
			}
			else if ("rect".equals(component.getType())) {
				componentPane = addComponentRect(componentToRender);
			}
			else if ("model".equals(component.getType())) {
				componentPane = addComponentModel(componentToRender);
			}
			else if ("text".equals(component.getType())) {
				try {
					FontHelper font = assetLoader.getFontManager().getFont(component.getFont());
					if (font == null) {
						System.err.println("Error: Font not found: " + component.getFont());
						return;
					}

					componentPane = new Pane();
					componentPane.setLayoutX(x);
					componentPane.setLayoutY(y);
					componentPane.setViewOrder(-1.0);
					componentPane.setId("text_" + component.getName());

					double containerWidth = component.getWidth();
					String originalText = component.getText();
					if (originalText == null) originalText = "";

					double maxTextWidth = 0;
					double totalHeight = 0;
					ArrayList<String> lines = new ArrayList<>();
					int lineHeight = font.height;

					TextRenderInfo renderInfo = new TextRenderInfo(font, originalText, component.isShadowed(), component.isCenter(), containerWidth);

					String remainingText = originalText;
					while (remainingText.length() > 0 || lines.isEmpty()) {
						int newlineIndex = remainingText.indexOf("\\n");
						String line;

						if (newlineIndex != -1) {
							line = remainingText.substring(0, newlineIndex);
							remainingText = remainingText.substring(newlineIndex + 2);
						} else {
							line = remainingText;
							remainingText = "";
						}

						if (originalText.isEmpty() && lines.isEmpty() && line.isEmpty()) {
							lines.add("");
						} else if (!originalText.isEmpty() || !line.isEmpty()){
							lines.add(line);
						}

						int currentLineWidth = font.getTextWidth(line);
						maxTextWidth = Math.max(maxTextWidth, currentLineWidth);
						totalHeight += lineHeight;

						if (originalText.isEmpty() && !lines.isEmpty()) break;

						if (newlineIndex == -1 && remainingText.isEmpty()) break;

					}
					if (lines.isEmpty()) {
						lines.add("");
						totalHeight = lineHeight;
					}

					double canvasWidth = component.isCenter() ? containerWidth : maxTextWidth;
					double canvasHeight = totalHeight;

					if (canvasWidth <= 0) canvasWidth = 1;
					if (canvasHeight <= 0) canvasHeight = lineHeight;

					Canvas textCanvas = new Canvas(canvasWidth + 5, canvasHeight + 5);
					GraphicsContext gc = textCanvas.getGraphicsContext2D();

					Color color = Color.BLACK;
					try {
						String colorStr = component.getColour();
						if (colorStr != null && colorStr.length() >= 2) {
							if (colorStr.startsWith("0x")) {
								colorStr = colorStr.substring(2);
							}
							if (colorStr.matches("[0-9a-fA-F]{6}")) {
								int rgb = Integer.parseInt(colorStr, 16);
								color = Color.rgb((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
							} else {
								System.err.println("Warning: Invalid color format for " + component.getName() + ": " + component.getColour());
							}
						}
					} catch (NumberFormatException nfe) {
						System.err.println("Error parsing color for " + component.getName() + ": " + component.getColour() + " - " + nfe.getMessage());
					} catch (Exception e) {
						System.err.println("Error processing color for " + component.getName() + ": " + e.getMessage());
					}
					double currentLineY = 0;
					for (String line : lines) {
						double baselineY = currentLineY + lineHeight;

						int lineStartX = 0;
						if (component.isCenter()) {
							int currentLineWidth = font.getTextWidth(line);
							lineStartX = ((int)containerWidth - currentLineWidth) / 2 + 1;
							if(lineStartX < 0) lineStartX = 0;
						}

						renderInfo.lines.add(line);
						renderInfo.lineStartXPositions.add(lineStartX);
						renderInfo.lineYPositions.add(baselineY);

						font.drawTextWithTags(
								gc,
								line,
								lineStartX,
								baselineY,
								color,
								component.isShadowed()
						);
						currentLineY += lineHeight;
					}

					textRenderInfoMap.put(component.getName(), renderInfo);
					componentPane.getChildren().add(textCanvas);

					componentPane.setPrefWidth(canvasWidth);
					componentPane.setPrefHeight(canvasHeight);

				} catch (Exception e) {
					System.err.println("Error creating text component " + component.getName() + ": " + e.getMessage());
					e.printStackTrace();
				}
			} else if ("inv".equals(component.getType())) {
				componentPane = addComponentInv(componentToRender);
			} else if ("invtext".equals(component.getType())) {
				componentPane = addComponentInvText(componentToRender);
			}
			if (componentPane != null && component.getScroll() > 0) {
				Pane scrollbarPane = new Pane();
				scrollbarPane.setLayoutX(x + component.getWidth());
				scrollbarPane.setLayoutY(y);
				scrollbarPane.setPrefWidth(16);
				scrollbarPane.setPrefHeight(component.getHeight());
				scrollbarPane.setId("scrollbar_" + component.getName());

				ImageView topButton = new ImageView(assetLoader.getSpriteManager().getSprite("scrollbar", 0));
				topButton.setFitWidth(16);
				topButton.setFitHeight(16);

				ImageView bottomButton = new ImageView(assetLoader.getSpriteManager().getSprite("scrollbar", 1));
				bottomButton.setFitWidth(16);
				bottomButton.setFitHeight(16);
				bottomButton.setLayoutY(component.getHeight() - 16);

				int gripSize = (component.getHeight() - 32) * component.getHeight() / component.getScroll();
				if (gripSize < 8) gripSize = 8;

				int gripY = (component.getHeight() - gripSize - 32) /
						(Math.max(1, component.getScroll() - component.getHeight()));

				Rectangle track = new Rectangle(16, component.getHeight() - 32);
				track.setLayoutY(16);
				track.setFill(Color.web("0x23201b"));

				Rectangle grip = new Rectangle(16, gripSize);
				grip.setLayoutY(0);
				grip.setFill(Color.web("0x4d4233"));

				Line highlight1 = new Line(0, 0, 0, gripSize);
				highlight1.setStroke(Color.web("0x766654"));
				highlight1.setLayoutX(0);
				highlight1.setLayoutY(0);

				Line highlight2 = new Line(0, 0, 0, gripSize);
				highlight2.setStroke(Color.web("0x766654"));
				highlight2.setLayoutX(1);
				highlight2.setLayoutY(0);

				Line highlight3 = new Line(0, 0, 16, 0);
				highlight3.setStroke(Color.web("0x766654"));
				highlight3.setLayoutY(0);

				Line highlight4 = new Line(0, 0, 16, 0);
				highlight4.setStroke(Color.web("0x766654"));
				highlight4.setLayoutY(1);

				Line lowlight1 = new Line(0, 0, 0, gripSize);
				lowlight1.setStroke(Color.web("0x332d25"));
				lowlight1.setLayoutX(15);
				lowlight1.setLayoutY(0);

				Line lowlight2 = new Line(0, 0, 0, gripSize - 1);
				lowlight2.setStroke(Color.web("0x332d25"));
				lowlight2.setLayoutX(14);
				lowlight2.setLayoutY(1);

				Line lowlight3 = new Line(0, 0, 16, 0);
				lowlight3.setStroke(Color.web("0x332d25"));
				lowlight3.setLayoutY(gripSize - 1);

				Line lowlight4 = new Line(0, 0, 15, 0);
				lowlight4.setStroke(Color.web("0x332d25"));
				lowlight4.setLayoutX(1);
				lowlight4.setLayoutY(gripSize - 2);

				Pane gripPane = new Pane();
				gripPane.getChildren().addAll(
						grip,
						highlight1, highlight2, highlight3, highlight4,
						lowlight1, lowlight2, lowlight3, lowlight4
				);
				gripPane.setLayoutY(16 + gripY);

				scrollbarPane.getChildren().addAll(track, topButton, bottomButton, gripPane);

				if (component.getLayer() != null && !component.getLayer().isEmpty()) {
					Pane parentPane = componentPaneMap.get(component.getLayer());
					if (parentPane != null) {
						parentPane.getChildren().add(scrollbarPane);
					} else {
						root.getChildren().add(scrollbarPane);
					}
				} else {
					applicationState.getInterfaceRenderArea().getChildren().add(scrollbarPane);
				}
			}

			if (componentPane != null) {
				final String overlayerName = component.getOverlayer();

				String tooltipText = null;
				if (component.getOption() != null && !component.getOption().isEmpty()) {
					tooltipText = component.getOption();
				} else if (component.getButtonType() != null && !component.getButtonType().isEmpty()) {
					tooltipText = component.getButtonType();
				}
				final String finalTooltipText = tooltipText != null ? StringUtils.capitalizeFirstLetter(tooltipText) : null;

				final Canvas textCanvas = component.getType().equals("text") ?
						(Canvas) componentPane.getChildren().get(0) : null;

				componentPane.setOnMouseEntered(event -> {
					event.consume();
					if (overlayerName != null && !overlayerName.isEmpty()) {
						toggleLayerVisibility(overlayerName, true, componentMap, componentPaneMap,
								layerVisibilityMap, layerChildrenMap);
					}

					if (finalTooltipText != null) {
						showTooltip(finalTooltipText);
					}

					if (component.getType().equals("text") && component.getOverColour() != null && textCanvas != null) {
						LostCityInterfaceEditor.TextRenderInfo renderInfo = textRenderInfoMap.get(component.getName());
						if (renderInfo != null) {
							GraphicsContext hoverGc = textCanvas.getGraphicsContext2D();
							hoverGc.clearRect(0, 0, textCanvas.getWidth(), textCanvas.getHeight());

							Color hoverColor = Color.BLACK;
							try {
								String colorStr = component.getOverColour();
								if (colorStr != null && colorStr.length() >= 2) {
									if (colorStr.startsWith("0x")) {
										colorStr = colorStr.substring(2);
									}
									if (colorStr.matches("[0-9a-fA-F]{6}")) {
										int rgb = Integer.parseInt(colorStr, 16);
										hoverColor = Color.rgb((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
									}
								}
							} catch (Exception e) {
								System.err.println("Error parsing hover color: " + e.getMessage());
							}

							for (int i = 0; i < renderInfo.lines.size(); i++) {
								renderInfo.font.drawTextWithTags(
										hoverGc,
										renderInfo.lines.get(i),
										renderInfo.lineStartXPositions.get(i),
										renderInfo.lineYPositions.get(i),
										hoverColor,
										renderInfo.shadowed
								);
							}
						}
					}

					event.consume();
				});

				componentPane.setOnMouseExited(event -> {
					event.consume();
					if (overlayerName != null && !overlayerName.isEmpty()) {
						InterfaceComponent overlayerComponent = componentMap.get(overlayerName);
						if (overlayerComponent != null && "layer".equals(overlayerComponent.getType())) {
							toggleLayerVisibility(overlayerName, !overlayerComponent.isHide(),
									componentMap, componentPaneMap, layerVisibilityMap, layerChildrenMap);
						}
					}

					if (finalTooltipText != null) {
						hideTooltip();
					}

					if (component.getType().equals("text") && component.getOverColour() != null && textCanvas != null) {
						LostCityInterfaceEditor.TextRenderInfo renderInfo = textRenderInfoMap.get(component.getName());
						if (renderInfo != null) {
							GraphicsContext exitGc = textCanvas.getGraphicsContext2D();
							exitGc.clearRect(0, 0, textCanvas.getWidth(), textCanvas.getHeight());

							Color originalColor = Color.BLACK;
							try {
								String colorStr = component.getColour();
								if (colorStr != null && colorStr.length() >= 2) {
									if (colorStr.startsWith("0x")) {
										colorStr = colorStr.substring(2);
									}
									if (colorStr.matches("[0-9a-fA-F]{6}")) {
										int rgb = Integer.parseInt(colorStr, 16);
										originalColor = Color.rgb((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
									}
								}
							} catch (Exception e) {
								System.err.println("Error parsing original color: " + e.getMessage());
							}

							for (int i = 0; i < renderInfo.lines.size(); i++) {
								renderInfo.font.drawTextWithTags(
										exitGc,
										renderInfo.lines.get(i),
										renderInfo.lineStartXPositions.get(i),
										renderInfo.lineYPositions.get(i),
										originalColor,
										renderInfo.shadowed
								);
							}
						}
					}

					event.consume();
				});

				componentPane.setPickOnBounds(true);
				componentPane.setMouseTransparent(false);

				componentPaneMap.put(component.getName(), componentPane);

				if (component.getLayer() != null && !component.getLayer().isEmpty() &&
						!("layer".equals(component.getType()))) {
					Pane parentPane = componentPaneMap.get(component.getLayer());
					if (parentPane != null) {
						if (parentComponent != null) {
							componentPane.setLayoutX(component.getX());
							componentPane.setLayoutY(component.getY());
						}
						parentPane.getChildren().add(componentPane);
					} else {
						applicationState.getInterfaceRenderArea().getChildren().add(componentPane);
						System.out.println("Warning: Parent layer pane not found for " + component.getName() + ", adding to root");
					}
				} else {
					applicationState.getInterfaceRenderArea().getChildren().add(componentPane);
				}
				if (component.getLayer() != null && !component.getLayer().isEmpty()) {
					boolean parentVisible = layerVisibilityMap.getOrDefault(component.getLayer(), true);
					componentPane.setVisible(parentVisible);
				}
			}
		}

		for (String componentName : componentPaneMap.keySet()) {
			Pane pane = componentPaneMap.get(componentName);
			InterfaceComponent component = componentMap.get(componentName);
			if (pane != null && component != null) {
				updateMouseTransparency(pane, component);
			}
		}
	}

	private Pane addComponentModel(RenderableJagComponent renderableComponent) {

		Pane componentPane = new Pane();
		componentPane.setLayoutX(renderableComponent.relativeX + ApplicationState.getApplicationState().getInterfaceRenderArea().getLayoutX());
		componentPane.setLayoutY(renderableComponent.relativeY + ApplicationState.getApplicationState().getInterfaceRenderArea().getLayoutY());
		componentPane.setId("model_" + renderableComponent.component.getName());

		Model model = assetLoader.getModel(renderableComponent.component.getModel());
		if (model == null) {
			System.out.println("No model found for: " + renderableComponent.component.getModel());
			return componentPane;
		}

		if (model.faceColorA == null) {
			model.calculateNormals(64, 768, -50, -10, -50, true);
		}
		int compWidth = renderableComponent.component.getWidth();
		int compHeight = renderableComponent.component.getHeight();

		if (compWidth <= 0 || compHeight <= 0) {
			System.err.println("Skipping model component with invalid dimensions: " + renderableComponent.component.getName());
			return componentPane;
		}

		int modelRadius = model.radius;
		int bufferWidth = Math.max(compWidth, compWidth + modelRadius * 2);
		int bufferHeight = Math.max(compHeight, compHeight + modelRadius * 2);

		WritableImage image = new WritableImage(bufferWidth, bufferHeight);
		PixelWriter pixelWriter = image.getPixelWriter();

		int[] pixelBuffer = new int[bufferWidth * bufferHeight];

		Pix3D.width2d = bufferWidth;
		Pix3D.boundBottom = bufferHeight;
		Pix3D.safeWidth = bufferWidth > 0 ? bufferWidth - 1 : 0;
		Pix3D.data = pixelBuffer;
		Pix3D.lineOffset = new int[bufferHeight];
		for (int i = 0; i < bufferHeight; i++) {
			Pix3D.lineOffset[i] = i * bufferWidth;
		}

		int originalCenterW = Pix3D.centerW3D;
		int originalCenterH = Pix3D.centerH3D;

		Pix3D.centerW3D = bufferWidth / 2;
		Pix3D.centerH3D = bufferHeight / 2;

		Arrays.fill(pixelBuffer, 0);

		int eyeY = Pix3D.sinTable[renderableComponent.component.getXan()] * renderableComponent.component.getZoom() >> 16;
		int eyeZ = Pix3D.cosTable[renderableComponent.component.getXan()] * renderableComponent.component.getZoom() >> 16;

		model.drawSimple(0, renderableComponent.component.getYan(), 0, renderableComponent.component.getXan(), 0, eyeY, eyeZ);

		Pix3D.centerW3D = originalCenterW;
		Pix3D.centerH3D = originalCenterH;

		for (int y2 = 0; y2 < bufferHeight; y2++) {
			for (int x2 = 0; x2 < bufferWidth; x2++) {
				int pixel = pixelBuffer[y2 * bufferWidth + x2];
				if (pixel == 0) {
					pixelWriter.setArgb(x2, y2, 0x00000000);
				} else {
					if ((pixel & 0xFF000000) == 0) {
						pixel = pixel | 0xFF000000;
					}
					pixelWriter.setArgb(x2, y2, pixel);
				}
			}
		}

		ImageView imageView = new ImageView(image);

		imageView.setLayoutX((compWidth - bufferWidth) / 2);
		imageView.setLayoutY((compHeight - bufferHeight) / 2);

		componentPane.getChildren().add(imageView);

		return componentPane;
	}

	private static Pane addComponentInvText(RenderableJagComponent renderableComponent) {
		Pane componentPane = new Pane();
		componentPane.setLayoutX(renderableComponent.relativeX);
		componentPane.setLayoutY(renderableComponent.relativeY);
		componentPane.setPrefWidth(renderableComponent.component.getWidth());
		componentPane.setPrefHeight(renderableComponent.component.getHeight());
		componentPane.setId("invtext_" + renderableComponent.component.getName());
		return componentPane;
	}

	private static Pane addComponentInv(RenderableJagComponent renderableComponent) {
		Pane componentPane = new Pane();
		componentPane.setLayoutX(renderableComponent.relativeX);
		componentPane.setLayoutY(renderableComponent.relativeY);
		componentPane.setPrefWidth(renderableComponent.component.getWidth());
		componentPane.setPrefHeight(renderableComponent.component.getHeight());
		componentPane.setId("inv_" + renderableComponent.component.getName());
		return componentPane;
	}

	private static Pane addComponentRect(RenderableJagComponent renderableComponent) {
		Pane componentPane = new Pane();
		componentPane.setLayoutX(renderableComponent.relativeX);
		componentPane.setLayoutY(renderableComponent.relativeY);
		componentPane.setId("rect_" + renderableComponent.component.getName());

		Rectangle rectangle = new Rectangle(renderableComponent.component.getWidth(), renderableComponent.component.getHeight());

		String colorStr = renderableComponent.component.getColour();
		if (colorStr != null && colorStr.startsWith("0x")) {
			int colorValue = Integer.parseInt(colorStr.substring(2), 16);
			// Spin this fun into it's own method in ColorUtils or something
			int red = (colorValue >> 16) & 0xFF;
			int green = (colorValue >> 8) & 0xFF;
			int blue = colorValue & 0xFF;
			Color color = Color.rgb(red, green, blue);
			if (renderableComponent.component.isFill()) {
				rectangle.setFill(color);
			} else {
				rectangle.setStroke(color);
				rectangle.setStrokeWidth(1);
			}
		} else {
			if (renderableComponent.component.isFill()) {
				rectangle.setFill(Color.BLACK);
			} else {
				rectangle.setFill(Color.TRANSPARENT);
			}
		}

		componentPane.getChildren().add(rectangle);

		return componentPane;
	}

	private Pane addComponentGraphic(RenderableJagComponent renderableComponent) {
		Pane componentPane = new Pane();
		componentPane.setLayoutX(renderableComponent.relativeX);
		componentPane.setLayoutY(renderableComponent.relativeY);
		componentPane.setPrefWidth(renderableComponent.component.getWidth() > 0 ? renderableComponent.component.getWidth() : 50);
		componentPane.setPrefHeight(renderableComponent.component.getHeight() > 0 ? renderableComponent.component.getHeight() : 50);
		componentPane.setId("graphic_" + renderableComponent.component.getName());

		ImageView imageView = new ImageView();

		if (renderableComponent.component.getGraphicName() != null && !renderableComponent.component.getGraphicName().isEmpty()) {
			WritableImage sprite = assetLoader.getSpriteManager().getSprite(renderableComponent.component.getGraphicName(), renderableComponent.component.getGraphicIndex());
			imageView.setImage(sprite);
		}
		componentPane.getChildren().add(imageView);

		if (renderableComponent.component.getActiveGraphicName() != null && !renderableComponent.component.getActiveGraphicName().isEmpty()) {
			final String componentName = renderableComponent.component.getName();

			final String activeGraphicName = renderableComponent.component.getActiveGraphicName();
			final int activeGraphicIndex = renderableComponent.component.getActiveGraphicIndex();

			double width = renderableComponent.component.getWidth() > 0 ? renderableComponent.component.getWidth() : (imageView.getImage() != null ? imageView.getImage().getWidth() : 50);
			double height = renderableComponent.component.getHeight() > 0 ? renderableComponent.component.getHeight() : (imageView.getImage() != null ? imageView.getImage().getHeight() : 50);

			Rectangle clickCaptureRect = new Rectangle(width, height);
			clickCaptureRect.setFill(Color.TRANSPARENT);
			componentPane.getChildren().add(clickCaptureRect);

			EventHandler<MouseEvent> clickHandler = event -> {

				if (activeComponentName != null && !componentName.equals(activeComponentName)) {
					Pane prevActivePane = componentPaneMap.get(activeComponentName);
					InterfaceComponent prevActiveComponent = componentMap.get(activeComponentName);

					if (prevActivePane != null && prevActiveComponent != null &&
							prevActiveComponent.getActiveGraphicName() != null) {
						Node firstChild = prevActivePane.getChildren().get(0);
						if (firstChild instanceof ImageView) {
							ImageView prevImageView = (ImageView) firstChild;
							if (prevActiveComponent.getGraphicName() != null && !prevActiveComponent.getGraphicName().isEmpty()) {
								prevImageView.setImage(assetLoader.getSpriteManager().getSprite(prevActiveComponent.getGraphicName(),
										prevActiveComponent.getGraphicIndex()));
							} else {
								prevImageView.setImage(null);
							}
						}
					}
				}

				activeComponentName = componentName;

				WritableImage activeImage = assetLoader.getSpriteManager().getSprite(activeGraphicName, activeGraphicIndex);
				if (activeImage != null) {
					imageView.setImage(activeImage);
				} else {
					System.err.println("Failed to get active sprite for: " + activeGraphicName);
				}

				event.consume();
			};
			originalClickHandlers.put(componentName, clickHandler);

			imageView.setOnMouseClicked(clickHandler);
			clickCaptureRect.setOnMouseClicked(clickHandler);

			componentPane.setPickOnBounds(true);
		}

		return componentPane;
	}

	private Pane addComponentLayer(RenderableJagComponent renderableComponent) {
		Pane componentPane = new Pane();
		componentPane.setLayoutX(renderableComponent.relativeX);
		componentPane.setLayoutY(renderableComponent.relativeY);
		componentPane.setPrefWidth(renderableComponent.component.getWidth());
		componentPane.setPrefHeight(renderableComponent.component.getHeight());
		componentPane.setId("layer_" + renderableComponent.component.getName());

		Rectangle clipRect = new Rectangle(renderableComponent.component.getWidth(), renderableComponent.component.getHeight());
		componentPane.setClip(clipRect);

		Rectangle eventCapture = new Rectangle(renderableComponent.component.getWidth(), renderableComponent.component.getHeight());
		eventCapture.setFill(Color.TRANSPARENT);
		componentPane.getChildren().add(eventCapture);

		boolean isVisible = layerVisibilityMap.getOrDefault(renderableComponent.component.getName(), true);
		componentPane.setVisible(isVisible);
		componentPane.setPickOnBounds(true);
		return componentPane;
	}

	private class RenderableJagComponent {
		InterfaceComponent component;
		double relativeX;
		double relativeY;

		public RenderableJagComponent(InterfaceComponent component, double xOffset, double yOffset) {
			this.component = component;
			this.relativeX = component.getX() + xOffset;
			this.relativeY = component.getY() + yOffset;
		}
	}
}
