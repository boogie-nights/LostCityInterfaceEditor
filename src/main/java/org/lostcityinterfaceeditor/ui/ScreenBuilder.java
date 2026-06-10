package org.lostcityinterfaceeditor.ui;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.util.Builder;

public class ScreenBuilder implements Builder<Region> {

	private final RuneScapeUiBuilder runeScapeUiBuilder;
	private final InterfaceComponentsBuilder interfaceComponentsBuilder;
	private final ComponentPropertiesBuilder componentPropertiesBuilder;

	public ScreenBuilder(RuneScapeUiBuilder runeScapeUiBuilder, InterfaceComponentsBuilder interfaceComponentsBuilder, ComponentPropertiesBuilder componentPropertiesBuilder) {
		this.runeScapeUiBuilder = runeScapeUiBuilder;
		this.interfaceComponentsBuilder = interfaceComponentsBuilder;
		this.componentPropertiesBuilder = componentPropertiesBuilder;
	}

	@Override
	public Region build() {

		BorderPane screen = new BorderPane();

		MenuBar menuBar = new MenuBar();
		// Keep this for all the fun sections and whatnot
		// menuBar.setStyle("-fx-border-color: red; -fx-border-width: 1;");

		Menu fileMenu = new Menu("File");
		Menu editMenu = new Menu("Edit");
		Menu viewMenu = new Menu("View");
		Menu helpMenu = new Menu("Help");

		menuBar.getMenus().addAll(fileMenu, editMenu, viewMenu, helpMenu);

		Region componentPropertiesRegion = componentPropertiesBuilder.build();
		componentPropertiesRegion.setPrefWidth(screen.getWidth());
//		componentPropertiesRegion.setStyle("-fx-border-color: red; -fx-border-width: 1;");

		screen.setLeft(runeScapeUiBuilder.build());
		screen.setTop(menuBar);
		screen.setRight(interfaceComponentsBuilder.build());
		screen.setBottom(componentPropertiesRegion);

		return screen;
	}
}
