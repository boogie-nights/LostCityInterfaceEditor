package org.lostcityinterfaceeditor.ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Builder;
import org.lostcityinterfaceeditor.loaders.AssetLoader;
import org.lostcityinterfaceeditor.models.ApplicationState;
import org.lostcityinterfaceeditor.models.InterfaceComponent;
import org.lostcityinterfaceeditor.ui.widget.Widgets;

import java.util.List;
import java.util.Optional;

public class InterfaceComponentsBuilder implements Builder<Region> {

	private final AssetLoader assetLoader;
	private final ApplicationState applicationState;

	private TreeView<String> componentTreeView;

	public InterfaceComponentsBuilder(AssetLoader assetLoader, ApplicationState applicationState) {
		this.applicationState = applicationState;
		this.assetLoader = assetLoader;
	}

	@Override
	public Region build() {
		VBox vbox = new VBox();

		vbox.setPrefWidth(200);
		vbox.setPadding(new Insets(0, 10, 10, 10));

		Label treeViewLabel = new Label("Interface Components");

		componentTreeView = new TreeView<>();
		componentTreeView.setRoot(new TreeItem<>("Interface Components"));
		componentTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				String componentName = newValue.getValue().split(" ")[0];
				InterfaceComponent selectedComponent = null;
				for (InterfaceComponent component : applicationState.getInterfaceComponents()) {
					if (component.getName().equals(componentName)) {
						selectedComponent = component;
						break;
					}
				}
//				populatePropertiesSidebar(selectedComponent);
			} else {
//				populatePropertiesSidebar(null);
			}
		});

		Label selectionHintLabel = new Label("Select a component to view properties and enable dragging");
		selectionHintLabel.setWrapText(true);
		selectionHintLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #555555;");

		Button addComponentButton = new Button("Add New Component");
		addComponentButton.setMaxWidth(Double.MAX_VALUE);
		addComponentButton.setOnAction(e -> showAddComponentDialog());

		vbox.getChildren().addAll(treeViewLabel, selectionHintLabel, addComponentButton);

		return vbox;
	}

	private void showAddComponentDialog() {
		Dialog<String> dialog = new Dialog<>();
		dialog.setTitle("Add New Component");
		dialog.setHeaderText("Select component type");
		ButtonType confirmButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
		ComboBox<String> componentTypeCombo = new ComboBox<>();
		componentTypeCombo.getItems().addAll("text", "graphic", "layer", "rect", "model", "inv", "invtext");
		componentTypeCombo.setPromptText("Component Type");
		componentTypeCombo.getSelectionModel().selectFirst();
		TextField nameField = new TextField();
		nameField.setPromptText("Component Name");
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));
		grid.add(new Label("Component Type:"), 0, 0);
		grid.add(componentTypeCombo, 1, 0);
		grid.add(new Label("Component Name:"), 0, 1);
		grid.add(nameField, 1, 1);

		dialog.getDialogPane().setContent(grid);

		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == confirmButtonType) {
				if (nameField.getText().isEmpty()) {
					Widgets.showAlert("Invalid Input", "Component name cannot be empty");
					return null;
				}
				return componentTypeCombo.getValue() + ":" + nameField.getText();
			}
			return null;
		});

		Optional<String> result = dialog.showAndWait();
		result.ifPresent(typeAndName -> {
			String[] parts = typeAndName.split(":");
			if (parts.length == 2) {
				String type = parts[0];
				String name = parts[1];
				createNewComponent(type, name);
			}
		});
	}

	private void createNewComponent(String type, String name) {
		for (InterfaceComponent component : applicationState.getInterfaceComponents()) {
			if (component.getName().equals(name)) {
				Widgets.showAlert("Duplicate Name", "A component with this name already exists");
				return;
			}
		}

		InterfaceComponent newComponent = new InterfaceComponent(name, type, 250, 250, 32, 32, null, null, null, 0, null, null);

		switch (type) {
			case "text":
				newComponent.setText("New Text");
				newComponent.setColour("0x000000");
				List<String> availableFonts = assetLoader.getFontManager().getLoadedFontNames();
				if (!availableFonts.isEmpty()) {
					String defaultFont = availableFonts.contains("p12_full") ? "p12_full" : availableFonts.get(0);
					newComponent.setFont(defaultFont);
				}
				break;
			case "rect":
				newComponent.setColour("0x000000");
				newComponent.setFill(true);
				break;
			case "graphic":
				newComponent.setGraphicName("miscgraphics");
				newComponent.setGraphicIndex(0);
				break;
			case "inv":
				newComponent.setDraggable(false);
				newComponent.setInteractable(false);
				newComponent.setUsable(false);
				newComponent.setMargin("0,0");
				break;
			case "invtext":
				newComponent.setDraggable(false);
				newComponent.setInteractable(false);
				newComponent.setMargin("0,0");
				newComponent.setColour("0x000000");
				newComponent.setFont("p12");
				newComponent.setCenter(false);
				newComponent.setShadowed(false);
				break;
			case "model":
				newComponent.setModel("model_55_idk_head");
				newComponent.setZoom(1200);
				newComponent.setXan(0);
				newComponent.setYan(200);
				break;
		}

		applicationState.getInterfaceComponents().add(newComponent);
//		clearRenderedComponents();
//		renderInterfaceComponents();
//		populateComponentTreeView();
//
//		selectComponentInTreeView(name);
	}
}
