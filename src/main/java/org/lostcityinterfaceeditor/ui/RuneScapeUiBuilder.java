package org.lostcityinterfaceeditor.ui;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Builder;
import org.lostcityinterfaceeditor.loaders.AssetLoader;
import org.lostcityinterfaceeditor.models.ApplicationState;

import java.util.List;

public class RuneScapeUiBuilder implements Builder<Region> {

	private final AssetLoader assetLoader;
	private final ApplicationState applicationState;

	private ImageView compassImageView;
	private Pane areaMinimap;
	private Pane areaCompass;
	private Pane areaBackleft1;
	private Pane areaBackleft2;
	private Pane areaBackright1;
	private Pane areaBackright2;
	private Pane areaBacktop1;
	private Pane areaBacktop2;
	private Pane areaBackvmid1;
	private Pane areaBackvmid2;
	private Pane areaBackvmid3;
	private Pane areaBackhmid2;
	private Pane areaChatback;
	private Pane areaMapback;
	private Pane areaBackbase1;
	private Pane areaBackbase2;
	private Pane areaBackhmid1;
	private Pane areaViewport;
	private Pane areaSidebar;
	private GraphicsContext gc;
	private Canvas backgroundCanvas;
	private Canvas tooltipCanvas;
	private Pane tooltipPane;

	public RuneScapeUiBuilder(AssetLoader assetLoader, ApplicationState applicationState) {
		this.assetLoader = assetLoader;
		this.applicationState = applicationState;
	}

	@Override
	public Region build() {

		AnchorPane anchorPane = new AnchorPane();

		double totalW = applicationState.getActiveLayout().getFrameWidth();
		double totalH = applicationState.getActiveLayout().getFrameHeight();

		backgroundCanvas = new Canvas(totalW, totalH);
		gc = backgroundCanvas.getGraphicsContext2D();
		gc.setFill(Color.GRAY);
		gc.fillRect(0, 0, totalW, totalH);
		anchorPane.getChildren().add(backgroundCanvas);
		backgroundCanvas.toBack();
		anchorPane.setBackground(Background.EMPTY);

		tooltipPane = new Pane();
		tooltipPane.setLayoutX(0);
		tooltipPane.setLayoutY(0);
		tooltipCanvas = new Canvas(300, 40);
		tooltipPane.getChildren().add(tooltipCanvas);
		tooltipPane.setVisible(false);
		tooltipPane.setMouseTransparent(true);
		tooltipPane.setViewOrder(-100);
		anchorPane.getChildren().add(tooltipPane);

		areaViewport = setupPane("viewport");

		areaChatback = setupPaneWithSprite("chatback", "chatback", 0);
		areaMapback  = setupPaneWithSprite("mapback", "mapback", 0);
		areaSidebar  = setupPaneWithSprite("sidebar", "invback", 0);

		areaBackbase1 = setupPaneWithSprite("backbase1", "backbase1", 0);
		areaBackhmid1 = setupPaneWithSprite("backhmid1", "backhmid1", 0);
		addIconsToPane(areaBackhmid1, "hmidIcons", 0);
		areaBackbase2 = setupPaneWithSprite("backbase2", "backbase2", 0);
		addIconsToPane(areaBackbase2, "baseIcons", 7);

		areaBackleft1  = setupPaneWithSprite("backleft1", "backleft1", 0);
		areaBackleft2  = setupPaneWithSprite("backleft2", "backleft2", 0);

		areaBackright1 = setupPaneWithSprite("backright1", "backright1", 0);
		areaBackright2 = setupPaneWithSprite("backright2", "backright2", 0);

		areaBackvmid1  = setupPaneWithSprite("backvmid1", "backvmid1", 0);
		areaBackvmid2  = setupPaneWithSprite("backvmid2", "backvmid2", 0);
		areaBackvmid3  = setupPaneWithSprite("backvmid3", "backvmid3", 0);
		areaBackhmid2  = setupPaneWithSprite("backhmid2", "backhmid2", 0);

		areaBacktop1 = setupPaneWithSprite("backtop1", "backtop1", 0);
		areaBacktop2 = setupPaneWithSprite("backtop2", "backtop2", 0);

		areaCompass = setupPane("mapback");
		compassImageView = new ImageView(assetLoader.getSpriteManager().getSprite("compass", 0));
		compassImageView.setLayoutX(-8);
		compassImageView.setLayoutY(-8);
		areaCompass.getChildren().add(compassImageView);

		areaMinimap = setupPane("mapback");
		Circle minimapCircle = new Circle(100);
		minimapCircle.setFill(Color.BLACK);
		minimapCircle.setCenterX(100);
		minimapCircle.setCenterY(100);
		areaMinimap.getChildren().add(minimapCircle);

		applicationState.setInterfaceRenderArea(areaViewport);

		anchorPane.getChildren().addAll(
				areaViewport, areaChatback, areaCompass, areaMinimap, areaMapback,
				areaBackbase1, areaBackbase2, areaBackhmid1,
				areaBackleft1, areaBackleft2, areaBackright1, areaBackright2,
				areaBacktop1, areaBacktop2, areaBackvmid1, areaBackvmid2, areaBackvmid3,
				areaBackhmid2, areaSidebar);

		return anchorPane;
	}

	private Pane setupPane(String key) {
		Rectangle2D rect = applicationState.getActiveLayout().get(key);
		Pane p = new Pane();
		p.setLayoutX(rect.getMinX());
		p.setLayoutY(rect.getMinY());
		if (rect.getWidth() > 0) p.setPrefSize(rect.getWidth(), rect.getHeight());
		return p;
	}

	private Pane setupPaneWithSprite(String key, String spriteName, int index) {
		Pane p = setupPane(key);
		ImageView iv = new ImageView(assetLoader.getSpriteManager().getSprite(spriteName, index));
		p.getChildren().add(iv);
		return p;
	}

	private void addIconsToPane(Pane parent, String iconKey, int startIdx) {
		List<Point2D> points = this.applicationState.getActiveLayout().getIcons(iconKey);
		for (int i = 0; i < points.size(); i++) {
			ImageView iv = new ImageView(assetLoader.getSpriteManager().getSprite("sideicons", startIdx + i));
			iv.setLayoutX(points.get(i).getX());
			iv.setLayoutY(points.get(i).getY());
			parent.getChildren().add(iv);
		}
	}
}
