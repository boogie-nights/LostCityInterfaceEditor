package org.lostcityinterfaceeditor.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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

		VBox root = new VBox();

		Label componentPropertiesLabel = new Label("Component Properties");

		root.getChildren().addAll(addButtons(root), componentPropertiesLabel);

		return root;
	}

	private Node addButtons(Region root) {
		HBox hBox = new HBox();

		hBox.setPrefHeight(50);

		Button addLayerButton = new Button("Add Layer");
		addLayerButton.setMaxWidth(Double.MAX_VALUE);
		addLayerButton.setMaxHeight(Double.MAX_VALUE);
		//addComponentButton.setOnAction(e -> showAddComponentDialog());

		Button addGraphicButton = new Button("Add Graphic");
		addGraphicButton.setMaxWidth(Double.MAX_VALUE);
		addGraphicButton.setMaxHeight(Double.MAX_VALUE);
		//addComponentButton.setOnAction(e -> showAddComponentDialog());

		Button addModelButton = new Button("Add Model");
		addModelButton.setMaxWidth(Double.MAX_VALUE);
		addModelButton.setMaxHeight(Double.MAX_VALUE);
		//addComponentButton.setOnAction(e -> showAddComponentDialog());

		Button addTextButton = new Button("Add Text");
		addTextButton.setMaxWidth(Double.MAX_VALUE);
		addTextButton.setMaxHeight(Double.MAX_VALUE);
		//addComponentButton.setOnAction(e -> showAddComponentDialog());

		Button addRectangleButton = new Button("Add Rectangle");
		addRectangleButton.setMaxWidth(Double.MAX_VALUE);
		addRectangleButton.setMaxHeight(Double.MAX_VALUE);
		//addComponentButton.setOnAction(e -> showAddComponentDialog());

		Button addLineButton = new Button("Add Line");
		addLineButton.setMaxWidth(Double.MAX_VALUE);
		addLineButton.setMaxHeight(Double.MAX_VALUE);
		//addComponentButton.setOnAction(e -> showAddComponentDialog());

		hBox.getChildren().addAll(addLayerButton, addGraphicButton, addModelButton,
				addTextButton, addRectangleButton, addLineButton);

		HBox.setHgrow(addLayerButton, Priority.ALWAYS);
		HBox.setHgrow(addGraphicButton, Priority.ALWAYS);
		HBox.setHgrow(addModelButton, Priority.ALWAYS);
		HBox.setHgrow(addTextButton, Priority.ALWAYS);
		HBox.setHgrow(addRectangleButton, Priority.ALWAYS);
		HBox.setHgrow(addLineButton, Priority.ALWAYS);

		hBox.setPrefWidth(root.getWidth());
		return hBox;
	}
}
