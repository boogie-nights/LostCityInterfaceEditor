package org.lostcityinterfaceeditor.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.*;
import javafx.util.Builder;
import org.lostcityinterfaceeditor.fileUtils.InterfaceFileParser;
import org.lostcityinterfaceeditor.loaders.AssetLoader;
import org.lostcityinterfaceeditor.models.ApplicationState;
import org.lostcityinterfaceeditor.models.InterfaceComponent;
import org.lostcityinterfaceeditor.ui.widget.Widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ComponentPropertiesBuilder implements Builder<Region> {

	private AssetLoader assetLoader;
	private ApplicationState applicationState;


	public ComponentPropertiesBuilder(AssetLoader assetLoader, ApplicationState applicationState) {
		this.assetLoader = assetLoader;
		this.applicationState = applicationState;
	}

	@Override
	public Region build() {

		VBox root = new VBox();

		Label componentPropertiesLabel = new Label("Component Properties");

		root.getChildren().addAll(addButtons(root), createPropertiesSection(null));

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

	private Node createPropertiesSection(InterfaceComponent component) {

		VBox root = new VBox();

		root.getChildren().clear();

		if (component == null) {
			Label noSelectionLabel = new Label("No component selected.");
			root.getChildren().add(noSelectionLabel);
			return root;

		}

		Widgets.addSectionHeader(root, "General Properties");

		Widgets.addReadOnlyProperty(root, "Name", component.getName());
		Widgets.addReadOnlyProperty(root, "Type", component.getType());
		Widgets.addEditableProperty(root, "X", String.valueOf(component.getX()), value -> component.setX(Integer.parseInt(value)));
		Widgets.addEditableProperty(root, "Y", String.valueOf(component.getY()), value -> component.setY(Integer.parseInt(value)));
		Widgets.addEditableProperty(root, "Width", String.valueOf(component.getWidth()), value -> component.setWidth(Integer.parseInt(value)));
		Widgets.addEditableProperty(root, "Height", String.valueOf(component.getHeight()), value -> component.setHeight(Integer.parseInt(value)));
		Widgets.addEditableProperty(root, "Layer", component.getLayer(), value -> component.setLayer(value));
		Widgets.addEditableProperty(root, "Overlayer", component.getOverlayer(), value -> component.setOverlayer(value));
		Widgets.addEditableProperty(root, "Option", component.getOption(), value -> component.setOption(value));
		Widgets.addEditableProperty(root, "Button Type", component.getButtonType(), value -> component.setButtonType(value));
		Widgets.addEditableProperty(root, "Client Code", String.valueOf(component.getClientCode()),
				value -> component.setClientCode(Integer.parseInt(value)));

		if (component.getButtonType() != null && component.getButtonType().contains("target")) {
			Widgets.addEditableProperty(root, "Action Verb", component.getActionVerb(), value -> component.setActionVerb(value));
			Widgets.addEditableProperty(root, "Action Target", component.getActionTarget(), value -> component.setActionTarget(value));
			Widgets.addEditableProperty(root, "Action", component.getAction(), value -> component.setAction(value));
		}

		String type = component.getType();
		if ("text".equals(type)) {
			Widgets.addSectionHeader(root, "Text Properties");
			Widgets.addEditableProperty(root, "Text", component.getText(), value -> component.setText(value));
			Widgets.addDropdownProperty(root, "Font", component.getFont(), assetLoader.getFontManager().getLoadedFontNames(), component::setFont);
			Widgets.addBooleanProperty(root, "Center", component.isCenter(), value -> component.setCenter(value));
			Widgets.addBooleanProperty(root, "Shadowed", component.isShadowed(), value -> component.setShadowed(value));
			Widgets.addColorProperty(root, "Colour", component.getColour(), value -> component.setColour(value));
			Widgets.addColorProperty(root, "Over Colour", component.getOverColour(), value -> component.setOverColour(value));
			Widgets.addColorProperty(root, "Active Colour", component.getActiveColour(), value -> component.setActiveColour(value));
		} else if ("graphic".equals(type)) {
			Widgets.addSectionHeader(root, "Graphic Properties");
			Widgets.addDropdownProperty(root, "Graphic Name", component.getGraphicName(), assetLoader.getSpriteManager().getAllSpriteNames(), component::setGraphicName);
			Widgets.addEditableProperty(root, "Graphic Index", String.valueOf(component.getGraphicIndex()),
					value -> component.setGraphicIndex(Integer.parseInt(value)));
			Widgets.addDropdownProperty(root, "Active Graphic Name", component.getActiveGraphicName(), assetLoader.getSpriteManager().getAllSpriteNames(), component::setActiveGraphicName);
			Widgets.addEditableProperty(root, "Active Graphic Index", String.valueOf(component.getActiveGraphicIndex()),
					value -> component.setActiveGraphicIndex(Integer.parseInt(value)));
		} else if ("layer".equals(type)) {
			Widgets.addSectionHeader(root, "Layer Properties");
			Widgets.addBooleanProperty(root, "Hide", component.isHide(), value -> component.setHide(value));
			Widgets.addEditableProperty(root, "Scroll", String.valueOf(component.getScroll()), value -> component.setScroll(Integer.parseInt(value)));
		} else if ("rect".equals(type)) {
			Widgets.addSectionHeader(root, "Rectangle Properties");
			Widgets.addColorProperty(root, "Colour", component.getColour(), value -> component.setColour(value));
			Widgets.addBooleanProperty(root, "Fill", component.isFill(), value -> component.setFill(value));
		} else if ("model".equals(type)) {
			Widgets.addSectionHeader(root, "Model Properties");
			Widgets.addEditableProperty(root, "Model", component.getModel(), value -> component.setModel(value));
			Widgets.addEditableProperty(root, "Active Model", component.getActiveModel(), value -> component.setActiveModel(value));
			Widgets.addEditableProperty(root, "Active Animation", component.getActiveAnim(), value -> component.setActiveAnim(value));
			Widgets.addEditableProperty(root, "Xan", String.valueOf(component.getXan()),
					value -> component.setXan(Integer.parseInt(value)));
			Widgets.addEditableProperty(root, "Yan", String.valueOf(component.getYan()),
					value -> component.setYan(Integer.parseInt(value)));
			Widgets.addEditableProperty(root, "Zoom", String.valueOf(component.getZoom()),
					value -> component.setZoom(Integer.parseInt(value)));
		} else if ("inv".equals(type)) {
			Widgets.addSectionHeader(root, "Inventory Properties");
			Widgets.addBooleanProperty(root, "Draggable", component.isDraggable(), value -> component.setDraggable(value));
			Widgets.addBooleanProperty(root, "Interactable", component.isInteractable(), value -> component.setInteractable(value));
			Widgets.addBooleanProperty(root, "Usable", component.isUsable(), value -> component.setUsable(value));
			Widgets.addEditableProperty(root, "Margin", component.getMargin(), value -> component.setMargin(value));
			List<String> invOptions = component.getInvOptions();
			if (invOptions != null) {
				for (int i = 0; i < invOptions.size(); i++) {
					final int optionIndex = i;
					String optionValue = invOptions.get(i);
					String optionLabel = "Option " + (i + 1);
					Widgets.addEditableProperty(root, optionLabel, optionValue, value -> {
						List<String> options = new ArrayList<>(component.getInvOptions());
						options.set(optionIndex, value);
						component.setInvOptions(options);
					});
				}
			}
		} else if ("invtext".equals(type)) {
			Widgets.addSectionHeader(root, "Inventory Text Properties");
			Widgets.addDropdownProperty(root, "Font", component.getFont(), assetLoader.getFontManager().getLoadedFontNames(), component::setFont);
			Widgets.addBooleanProperty(root, "Center", component.isCenter(), value -> component.setCenter(value));
			Widgets.addBooleanProperty(root, "Shadowed", component.isShadowed(), value -> component.setShadowed(value));
			Widgets.addColorProperty(root, "Colour", component.getColour(), value -> component.setColour(value));
			Widgets.addEditableProperty(root, "Margin", component.getMargin(), value -> component.setMargin(value));
			Widgets.addBooleanProperty(root, "Interactable", component.isInteractable(), value -> component.setInteractable(value));
			List<String> invOptions = component.getInvOptions();
			if (invOptions != null) {
				for (int i = 0; i < invOptions.size(); i++) {
					final int optionIndex = i;
					String optionValue = invOptions.get(i);
					String optionLabel = "Option " + (i + 1);
					Widgets.addEditableProperty(root, optionLabel, optionValue, value -> {
						List<String> options = new ArrayList<>(component.getInvOptions());
						options.set(optionIndex, value);
						component.setInvOptions(options);
					});
				}
			}
		}

		if (component.getScripts() != null && !component.getScripts().isEmpty()) {
			Widgets.addSectionHeader(root, "Scripts");
			for (Map.Entry<Integer, InterfaceFileParser.Script> scriptEntry : component.getScripts().entrySet()) {
				final int scriptKey = scriptEntry.getKey();
				String scriptValue = scriptEntry.getValue() != null ? scriptEntry.getValue().toString() : "null";
				Widgets.addReadOnlyProperty(root, "Script " + scriptKey, scriptValue);
			}
		}

		Button saveChangesButton = new Button("Apply Changes");
		saveChangesButton.setMaxWidth(Double.MAX_VALUE);
		saveChangesButton.setOnAction(e -> {
//			final InterfaceComponent editedComponent = component;
//
//			clearRenderedComponents();
//			renderInterfaceComponents();
//			populateComponentTreeView();
//			selectComponentInTreeView(editedComponent.getName());
//			highlightSelectedComponent(editedComponent);
//			if (editedComponent != null) {
//				Node componentNode = findComponentPane(editedComponent.getName());
//				if (componentNode instanceof Pane) {
//					Pane newComponentPane = (Pane) componentNode;
//					makeComponentDraggable(editedComponent, newComponentPane);
//
//					currentlyDraggablePane = newComponentPane;
//					currentlyDraggableComponent = editedComponent;
//				} else {
//					System.err.println("Warning: Could not find the new pane for " + editedComponent.getName() + " after applying changes.");
//					currentlyDraggablePane = null;
//					currentlyDraggableComponent = null;
//				}
//			}
		});

		return root;
	}
}
