package org.lostcityinterfaceeditor.ui.widget;

import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.List;
import java.util.function.Consumer;

public class Widgets {

	static Node createTextInputField(StringProperty boundProperty) {
		TextField results = new TextField();
		results.setMinWidth(100);
		results.textProperty().bindBidirectional(boundProperty);
		return results;
	}

	static Node createPromptText(String prompt) {
		Text results = new Text(prompt);
		results.getStyleClass().add("label-text");
		return results;
	}

	public static void showAlert(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	public static void addReadOnlyProperty(VBox container, String name, String value) {
		HBox propertyRow = new HBox(5);
		propertyRow.setPadding(new Insets(2, 0, 2, 5));

		Label nameLabel = new Label(name + ":");
		nameLabel.setMinWidth(120);
		nameLabel.setMaxWidth(120);
		nameLabel.setStyle("-fx-font-weight: bold;");

		Label valueLabel = new Label(value != null ? value : "null");
		valueLabel.setWrapText(true);

		propertyRow.getChildren().addAll(nameLabel, valueLabel);
		container.getChildren().add(propertyRow);
	}

	public static void addEditableProperty(VBox container, String name, String value, Consumer<String> setter) {
		HBox propertyRow = new HBox(5);
		propertyRow.setPadding(new Insets(2, 0, 2, 5));

		Label nameLabel = new Label(name + ":");
		nameLabel.setMinWidth(120);
		nameLabel.setMaxWidth(120);
		nameLabel.setStyle("-fx-font-weight: bold;");

		TextField valueField = new TextField(value != null ? value : "");
		valueField.setPrefWidth(150);

		valueField.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue) {
				try {
					setter.accept(valueField.getText());
				} catch (NumberFormatException e) {
					valueField.setText(value != null ? value : "");
					Widgets.showAlert("Invalid input", "Please enter a valid number for " + name);
				}
			}
		});

		valueField.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.ENTER) {
				try {
					setter.accept(valueField.getText());
					valueField.getParent().requestFocus();
				} catch (NumberFormatException e) {
					valueField.setText(value != null ? value : "");
					Widgets.showAlert("Invalid input", "Please enter a valid number for " + name);
				}
			}
		});

		propertyRow.getChildren().addAll(nameLabel, valueField);
		container.getChildren().add(propertyRow);
	}

	public static void addDropdownProperty(VBox container, String name, String currentValue, List<String> options, Consumer<String> setter) {
		HBox propertyRow = new HBox(5);
		propertyRow.setPadding(new Insets(2, 0, 2, 5));

		Label nameLabel = new Label(name + ":");
		nameLabel.setMinWidth(120);
		nameLabel.setMaxWidth(120);
		nameLabel.setStyle("-fx-font-weight: bold;");

		ComboBox<String> valueComboBox = new ComboBox<>(FXCollections.observableArrayList(options));
		valueComboBox.setPrefWidth(150);

		if (currentValue != null && options.contains(currentValue)) {
			valueComboBox.setValue(currentValue);
		} else if (!options.isEmpty()) {
			valueComboBox.setPromptText("Select " + name);
		}

		valueComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				setter.accept(newValue);
			}
		});

		propertyRow.getChildren().addAll(nameLabel, valueComboBox);
		container.getChildren().add(propertyRow);
	}

	public static void addBooleanProperty(VBox container, String name, boolean value, Consumer<Boolean> setter) {
		HBox propertyRow = new HBox(5);
		propertyRow.setPadding(new Insets(2, 0, 2, 5));

		Label nameLabel = new Label(name + ":");
		nameLabel.setMinWidth(120);
		nameLabel.setMaxWidth(120);
		nameLabel.setStyle("-fx-font-weight: bold;");

		CheckBox checkBox = new CheckBox();
		checkBox.setSelected(value);

		checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
			setter.accept(newValue);
		});

		propertyRow.getChildren().addAll(nameLabel, checkBox);
		container.getChildren().add(propertyRow);
	}

	public static void addColorProperty(VBox container, String name, String value, Consumer<String> setter) {
		HBox propertyRow = new HBox(5);
		propertyRow.setPadding(new Insets(2, 0, 2, 5));

		Label nameLabel = new Label(name + ":");
		nameLabel.setMinWidth(120);
		nameLabel.setMaxWidth(120);
		nameLabel.setStyle("-fx-font-weight: bold;");

		ColorPicker colorPicker = new ColorPicker();

		if (value != null && !value.isEmpty()) {
			try {
				String colorValue = value;
				if (value.startsWith("0x")) {
					colorValue = "#" + value.substring(2);
				} else if (value.matches("\\d+")) {
					int decimalColor = Integer.parseInt(value);
					colorValue = String.format("#%06X", decimalColor);
				}
				colorPicker.setValue(Color.web(colorValue));
			} catch (Exception e) {
				System.err.println("Could not parse color value: " + value);
			}
		}

		colorPicker.setOnAction(event -> {
			Color color = colorPicker.getValue();
			String hexColor = String.format("0x%02X%02X%02X",
					(int) (color.getRed() * 255),
					(int) (color.getGreen() * 255),
					(int) (color.getBlue() * 255));
			setter.accept(hexColor);
		});

		propertyRow.getChildren().addAll(nameLabel, colorPicker);
		container.getChildren().add(propertyRow);
	}

	public static void addSectionHeader(VBox container, String sectionName) {
		Label sectionLabel = new Label(sectionName);
		sectionLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5 0 2 0;");
		container.getChildren().add(sectionLabel);

		Separator separator = new Separator();
		separator.setPrefWidth(container.getPrefWidth());
		container.getChildren().add(separator);
	}
}
