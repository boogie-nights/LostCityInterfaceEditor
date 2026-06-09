package org.lostcityinterfaceeditor.ui;

import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Builder;
import org.lostcityinterfaceeditor.models.ApplicationState;
import org.lostcityinterfaceeditor.models.InterfaceComponent;

public class ComponentPropertiesBuilder implements Builder<Region> {

	private ApplicationState applicationState;


	public ComponentPropertiesBuilder(ApplicationState applicationState) {
		this.applicationState = applicationState;
	}

	@Override
	public Region build() {

		HBox hBox = new HBox();
		Label componentPropertiesLabel = new Label("Component Properties");

		hBox.getChildren().add(componentPropertiesLabel);

		return hBox;
	}
}
