package org.lostcityinterfaceeditor;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.lostcityinterfaceeditor.fileUtils.InterfaceFileParser;
import org.lostcityinterfaceeditor.fileUtils.InterfaceFileWriter;
import org.lostcityinterfaceeditor.helpers.FontHelper;
import org.lostcityinterfaceeditor.helpers.LayoutHelper;
import org.lostcityinterfaceeditor.loaders.AssetLoader;
import org.lostcityinterfaceeditor.models.ApplicationState;
import org.lostcityinterfaceeditor.models.InterfaceComponent;
import org.lostcityinterfaceeditor.service.UpdatePackFilesService;
import org.lostcityinterfaceeditor.service.componentrenderer.TextRenderInfo;
import org.lostcityinterfaceeditor.ui.ComponentPropertiesBuilder;
import org.lostcityinterfaceeditor.ui.InterfaceComponentsBuilder;
import org.lostcityinterfaceeditor.ui.RuneScapeUiBuilder;
import org.lostcityinterfaceeditor.ui.ScreenBuilder;
import org.lostcityinterfaceeditor.ui.widget.Widgets;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class LostCityInterfaceEditor extends Application {

    private Map<String, Double> originalViewOrderMap = new HashMap<>();

    private List<InterfaceComponent> interfaceComponents;
    private String activeComponentName = null;
    private Map<String, EventHandler<MouseEvent>> originalClickHandlers = new HashMap<>();
    private TreeView<String> componentTreeView;
    private VBox sidebarVBox;
    private VBox propertiesSidebarVBox;
    private Pane selectedComponentHighlight;
    private double dragStartX, dragStartY;
    private boolean isDragging = false;
    private Pane currentlyDraggablePane = null;
    private InterfaceComponent currentlyDraggableComponent = null;
    private String currentLoadedFile;
    private Map<String, TextRenderInfo> textRenderInfoMap = new HashMap<>();
    private AssetLoader assetLoader;
    private ScrollPane propertiesScrollPane;
    private ApplicationState applicationState;
    private Canvas tooltipCanvas;
    private Pane tooltipPane;

    private UpdatePackFilesService updatePackFilesService;

    private AnchorPane root;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.applicationState = ApplicationState.getApplicationState();

        try {
            applicationState.setServerDirectoryPath(AssetLoader.chooseServerDirectory(stage));
            if (applicationState.getServerDirectoryPath() == null) {
                System.err.println("No server directory selected. Exiting.");
                return;
            }
            if (!chooseLayoutVersion()) {
                System.err.println("No layout version selected. Using standard.");
            }
            assetLoader = new AssetLoader(applicationState.getServerDirectoryPath());
            assetLoader.loadAll();

            updatePackFilesService = new UpdatePackFilesService(applicationState);

            RuneScapeUiBuilder runeScapeUiBuilder = new RuneScapeUiBuilder(assetLoader, applicationState);
            InterfaceComponentsBuilder interfaceComponentsBuilder = new InterfaceComponentsBuilder(assetLoader, applicationState);
            ComponentPropertiesBuilder componentPropertiesBuilder = new ComponentPropertiesBuilder(assetLoader, applicationState);

            Region sceneRoot = new ScreenBuilder(runeScapeUiBuilder, interfaceComponentsBuilder, componentPropertiesBuilder).build();
            Scene scene = new Scene(sceneRoot);

            scene.getStylesheets().add("https://raw.githubusercontent.com/antoniopelusi/JavaFX-Dark-Theme/main/style.css");

//            initializePropertiesSidebar();
//            initializeSelectionHighlight();
//            populateComponentTreeView();
            stage.setScene(scene);
            stage.setTitle("Lost City Interface Editor");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean chooseLayoutVersion() {
        List<String> options = Arrays.asList("Standard (Modern)", "Legacy (225)");
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Standard up to 377", options);
        dialog.setTitle("Select Layout Version");
        dialog.setHeaderText("UI Version Selection");
        dialog.setContentText("Choose the layout version to use:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            if (result.get().equals("Legacy (225)")) {
                applicationState.setActiveLayout(LayoutHelper.Legacy);
            } else {
                applicationState.setActiveLayout(LayoutHelper.Standard);
            }
            return true;
        }
        return false;
    }

    private void initializePropertiesSidebar() {
        propertiesSidebarVBox = new VBox();
        propertiesSidebarVBox.setPrefWidth(300);
        propertiesSidebarVBox.setPadding(new Insets(10));
        propertiesSidebarVBox.setStyle("-fx-background-color: #f0f0f0;");

        propertiesScrollPane = new ScrollPane();
        propertiesScrollPane.setContent(propertiesSidebarVBox);
        propertiesScrollPane.setFitToWidth(true);
        propertiesScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        propertiesScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        //root.getChildren().add(propertiesScrollPane);

        AnchorPane.setTopAnchor(propertiesScrollPane, 0.0);
        AnchorPane.setBottomAnchor(propertiesScrollPane, 0.0);
        if (sidebarVBox != null) {
            AnchorPane.setRightAnchor(propertiesScrollPane, sidebarVBox.getPrefWidth());
        } else {
            AnchorPane.setRightAnchor(propertiesScrollPane, 200.0);
            System.err.println("Warning: sidebarVBox was null during properties sidebar initialization.");
        }
    }

    private void populatePropertiesSidebar(InterfaceComponent component) {
        propertiesSidebarVBox.getChildren().clear();

        if (component == null) {
            Label noSelectionLabel = new Label("No component selected.");
            propertiesSidebarVBox.getChildren().add(noSelectionLabel);
            return;
        }

        Widgets.addSectionHeader(propertiesSidebarVBox, "General Properties");

        Widgets.addReadOnlyProperty(propertiesSidebarVBox, "Name", component.getName());
        Widgets.addReadOnlyProperty(propertiesSidebarVBox, "Type", component.getType());
        Widgets.addEditableProperty(propertiesSidebarVBox, "X", String.valueOf(component.getX()), value -> component.setX(Integer.parseInt(value)));
        Widgets.addEditableProperty(propertiesSidebarVBox, "Y", String.valueOf(component.getY()), value -> component.setY(Integer.parseInt(value)));
        Widgets.addEditableProperty(propertiesSidebarVBox, "Width", String.valueOf(component.getWidth()), value -> component.setWidth(Integer.parseInt(value)));
        Widgets.addEditableProperty(propertiesSidebarVBox, "Height", String.valueOf(component.getHeight()), value -> component.setHeight(Integer.parseInt(value)));
        Widgets.addEditableProperty(propertiesSidebarVBox, "Layer", component.getLayer(), value -> component.setLayer(value));
        Widgets.addEditableProperty(propertiesSidebarVBox, "Overlayer", component.getOverlayer(), value -> component.setOverlayer(value));
        Widgets.addEditableProperty(propertiesSidebarVBox, "Option", component.getOption(), value -> component.setOption(value));
        Widgets.addEditableProperty(propertiesSidebarVBox, "Button Type", component.getButtonType(), value -> component.setButtonType(value));
        Widgets.addEditableProperty(propertiesSidebarVBox, "Client Code", String.valueOf(component.getClientCode()),
                value -> component.setClientCode(Integer.parseInt(value)));

        if (component.getButtonType() != null && component.getButtonType().contains("target")) {
            Widgets.addEditableProperty(propertiesSidebarVBox, "Action Verb", component.getActionVerb(), value -> component.setActionVerb(value));
            Widgets.addEditableProperty(propertiesSidebarVBox, "Action Target", component.getActionTarget(), value -> component.setActionTarget(value));
            Widgets.addEditableProperty(propertiesSidebarVBox, "Action", component.getAction(), value -> component.setAction(value));
        }

        String type = component.getType();
        if ("text".equals(type)) {
            Widgets.addSectionHeader(propertiesSidebarVBox, "Text Properties");
            Widgets.addEditableProperty(propertiesSidebarVBox, "Text", component.getText(), value -> component.setText(value));
            Widgets.addDropdownProperty(propertiesSidebarVBox, "Font", component.getFont(), assetLoader.getFontManager().getLoadedFontNames(), component::setFont);
            Widgets.addBooleanProperty(propertiesSidebarVBox, "Center", component.isCenter(), value -> component.setCenter(value));
            Widgets.addBooleanProperty(propertiesSidebarVBox, "Shadowed", component.isShadowed(), value -> component.setShadowed(value));
            Widgets.addColorProperty(propertiesSidebarVBox, "Colour", component.getColour(), value -> component.setColour(value));
            Widgets.addColorProperty(propertiesSidebarVBox, "Over Colour", component.getOverColour(), value -> component.setOverColour(value));
            Widgets.addColorProperty(propertiesSidebarVBox, "Active Colour", component.getActiveColour(), value -> component.setActiveColour(value));
        } else if ("graphic".equals(type)) {
            Widgets.addSectionHeader(propertiesSidebarVBox, "Graphic Properties");
            Widgets.addDropdownProperty(propertiesSidebarVBox, "Graphic Name", component.getGraphicName(), assetLoader.getSpriteManager().getAllSpriteNames(), component::setGraphicName);
            Widgets.addEditableProperty(propertiesSidebarVBox, "Graphic Index", String.valueOf(component.getGraphicIndex()),
                    value -> component.setGraphicIndex(Integer.parseInt(value)));
           Widgets. addDropdownProperty(propertiesSidebarVBox, "Active Graphic Name", component.getActiveGraphicName(), assetLoader.getSpriteManager().getAllSpriteNames(), component::setActiveGraphicName);
            Widgets.addEditableProperty(propertiesSidebarVBox, "Active Graphic Index", String.valueOf(component.getActiveGraphicIndex()),
                    value -> component.setActiveGraphicIndex(Integer.parseInt(value)));
        } else if ("layer".equals(type)) {
            Widgets.addSectionHeader(propertiesSidebarVBox, "Layer Properties");
            Widgets.addBooleanProperty(propertiesSidebarVBox, "Hide", component.isHide(), value -> component.setHide(value));
            Widgets.addEditableProperty(propertiesSidebarVBox, "Scroll", String.valueOf(component.getScroll()), value -> component.setScroll(Integer.parseInt(value)));
        } else if ("rect".equals(type)) {
            Widgets.addSectionHeader(propertiesSidebarVBox, "Rectangle Properties");
            Widgets.addColorProperty(propertiesSidebarVBox, "Colour", component.getColour(), value -> component.setColour(value));
            Widgets.addBooleanProperty(propertiesSidebarVBox, "Fill", component.isFill(), value -> component.setFill(value));
        } else if ("model".equals(type)) {
            Widgets.addSectionHeader(propertiesSidebarVBox, "Model Properties");
            Widgets.addEditableProperty(propertiesSidebarVBox, "Model", component.getModel(), value -> component.setModel(value));
            Widgets.addEditableProperty(propertiesSidebarVBox, "Active Model", component.getActiveModel(), value -> component.setActiveModel(value));
            Widgets.addEditableProperty(propertiesSidebarVBox, "Active Animation", component.getActiveAnim(), value -> component.setActiveAnim(value));
            Widgets.addEditableProperty(propertiesSidebarVBox, "Xan", String.valueOf(component.getXan()),
                    value -> component.setXan(Integer.parseInt(value)));
            Widgets.addEditableProperty(propertiesSidebarVBox, "Yan", String.valueOf(component.getYan()),
                    value -> component.setYan(Integer.parseInt(value)));
            Widgets.addEditableProperty(propertiesSidebarVBox, "Zoom", String.valueOf(component.getZoom()),
                    value -> component.setZoom(Integer.parseInt(value)));
        } else if ("inv".equals(type)) {
            Widgets.addSectionHeader(propertiesSidebarVBox, "Inventory Properties");
            Widgets.addBooleanProperty(propertiesSidebarVBox, "Draggable", component.isDraggable(), value -> component.setDraggable(value));
            Widgets.addBooleanProperty(propertiesSidebarVBox, "Interactable", component.isInteractable(), value -> component.setInteractable(value));
            Widgets.addBooleanProperty(propertiesSidebarVBox, "Usable", component.isUsable(), value -> component.setUsable(value));
            Widgets.addEditableProperty(propertiesSidebarVBox, "Margin", component.getMargin(), value -> component.setMargin(value));
            List<String> invOptions = component.getInvOptions();
            if (invOptions != null) {
                for (int i = 0; i < invOptions.size(); i++) {
                    final int optionIndex = i;
                    String optionValue = invOptions.get(i);
                    String optionLabel = "Option " + (i + 1);
                    Widgets.addEditableProperty(propertiesSidebarVBox, optionLabel, optionValue, value -> {
                        List<String> options = new ArrayList<>(component.getInvOptions());
                        options.set(optionIndex, value);
                        component.setInvOptions(options);
                    });
                }
            }
        } else if ("invtext".equals(type)) {
            Widgets.addSectionHeader(propertiesSidebarVBox, "Inventory Text Properties");
            Widgets.addDropdownProperty(propertiesSidebarVBox, "Font", component.getFont(), assetLoader.getFontManager().getLoadedFontNames(), component::setFont);
            Widgets.addBooleanProperty(propertiesSidebarVBox, "Center", component.isCenter(), value -> component.setCenter(value));
            Widgets.addBooleanProperty(propertiesSidebarVBox, "Shadowed", component.isShadowed(), value -> component.setShadowed(value));
            Widgets.addColorProperty(propertiesSidebarVBox, "Colour", component.getColour(), value -> component.setColour(value));
            Widgets.addEditableProperty(propertiesSidebarVBox, "Margin", component.getMargin(), value -> component.setMargin(value));
            Widgets.addBooleanProperty(propertiesSidebarVBox, "Interactable", component.isInteractable(), value -> component.setInteractable(value));
            List<String> invOptions = component.getInvOptions();
            if (invOptions != null) {
                for (int i = 0; i < invOptions.size(); i++) {
                    final int optionIndex = i;
                    String optionValue = invOptions.get(i);
                    String optionLabel = "Option " + (i + 1);
                    Widgets.addEditableProperty(propertiesSidebarVBox, optionLabel, optionValue, value -> {
                        List<String> options = new ArrayList<>(component.getInvOptions());
                        options.set(optionIndex, value);
                        component.setInvOptions(options);
                    });
                }
            }
        }

        if (component.getScripts() != null && !component.getScripts().isEmpty()) {
            Widgets.addSectionHeader(propertiesSidebarVBox, "Scripts");
            for (Map.Entry<Integer, InterfaceFileParser.Script> scriptEntry : component.getScripts().entrySet()) {
                final int scriptKey = scriptEntry.getKey();
                String scriptValue = scriptEntry.getValue() != null ? scriptEntry.getValue().toString() : "null";
                Widgets.addReadOnlyProperty(propertiesSidebarVBox, "Script " + scriptKey, scriptValue);
            }
        }

        Button saveChangesButton = new Button("Apply Changes");
        saveChangesButton.setMaxWidth(Double.MAX_VALUE);
        saveChangesButton.setOnAction(e -> {
            final InterfaceComponent editedComponent = component;

            clearRenderedComponents();
            renderInterfaceComponents();
            populateComponentTreeView();
            selectComponentInTreeView(editedComponent.getName());
            highlightSelectedComponent(editedComponent);
            if (editedComponent != null) {
                Node componentNode = findComponentPane(editedComponent.getName());
                if (componentNode instanceof Pane) {
                    Pane newComponentPane = (Pane) componentNode;
                    makeComponentDraggable(editedComponent, newComponentPane);

                    currentlyDraggablePane = newComponentPane;
                    currentlyDraggableComponent = editedComponent;
                } else {
                    System.err.println("Warning: Could not find the new pane for " + editedComponent.getName() + " after applying changes.");
                    currentlyDraggablePane = null;
                    currentlyDraggableComponent = null;
                }
            }
        });

        VBox.setMargin(saveChangesButton, new Insets(10, 0, 0, 0));
        propertiesSidebarVBox.getChildren().add(saveChangesButton);
    }

    private void initializeSidebar() {
        sidebarVBox = new VBox(10);
        sidebarVBox.setPrefWidth(200);
        sidebarVBox.setPadding(new Insets(10));
        sidebarVBox.setStyle("-fx-background-color: lightgray;");

        Label selectionHintLabel = new Label("Select a component to view properties and enable dragging");
        selectionHintLabel.setWrapText(true);
        selectionHintLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #555555;");

//        Button addComponentButton = new Button("Add New Component");
//        addComponentButton.setMaxWidth(Double.MAX_VALUE);
//        addComponentButton.setOnAction(e -> showAddComponentDialog());

        Button loadInterfaceButton = new Button("Load Interface");
        loadInterfaceButton.setMaxWidth(Double.MAX_VALUE);
        loadInterfaceButton.setOnAction(e -> loadInterfaceFile());

        Button saveInterfaceButton = new Button("Save Interface");
        saveInterfaceButton.setMaxWidth(Double.MAX_VALUE);
        saveInterfaceButton.setOnAction(e -> saveInterfaceFile());

        Button spriteEditorButton = new Button("Edit Sprite");
        spriteEditorButton.setMaxWidth(Double.MAX_VALUE);
        spriteEditorButton.setOnAction(event -> LostCitySpriteEditor.openSpriteEditor(assetLoader));

        String[] interfaceDisplayLocations = {
                "Chatbox",
                "Main Window",
                "Sidebar"
        };
        ComboBox interfaceDisplayLocation = new ComboBox(FXCollections.observableArrayList(interfaceDisplayLocations));
        interfaceDisplayLocation.setMaxWidth(Double.MAX_VALUE);
        interfaceDisplayLocation.getSelectionModel().select(1);
        interfaceDisplayLocation.setOnAction(event -> {

            if (interfaceDisplayLocation.getValue().equals("Chatbox")) {
                clearRenderedComponents();
               // interfaceRenderArea = areaChatback;
                renderInterfaceComponents();
                populateComponentTreeView();
            }

            if (interfaceDisplayLocation.getValue().equals("Main Window")) {
                clearRenderedComponents();
               // interfaceRenderArea = areaViewport;
                renderInterfaceComponents();
                populateComponentTreeView();
            }

            if (interfaceDisplayLocation.getValue().equals("Sidebar")) {
                clearRenderedComponents();
               // interfaceRenderArea = areaSidebar;
                renderInterfaceComponents();
                populateComponentTreeView();
            }
        });

        VBox buttonBox = new VBox(5);
        buttonBox.getChildren().addAll(loadInterfaceButton, saveInterfaceButton, spriteEditorButton, interfaceDisplayLocation);
        VBox.setMargin(buttonBox, new Insets(10, 0, 0, 0));

        sidebarVBox.getChildren().addAll(componentTreeView, selectionHintLabel, buttonBox);
        root.getChildren().add(sidebarVBox);
        AnchorPane.setTopAnchor(sidebarVBox, 0.0);
        AnchorPane.setBottomAnchor(sidebarVBox, 0.0);
        AnchorPane.setRightAnchor(sidebarVBox, 0.0);
    }




    private void selectComponentInTreeView(String componentName) {
        TreeItem<String> root = componentTreeView.getRoot();
        selectComponentInTreeView(root, componentName);
    }

    private boolean selectComponentInTreeView(TreeItem<String> item, String componentName) {
        if (item.getValue().startsWith(componentName + " ")) {
            componentTreeView.getSelectionModel().select(item);
            return true;
        }

        for (TreeItem<String> child : item.getChildren()) {
            if (selectComponentInTreeView(child, componentName)) {
                return true;
            }
        }

        return false;
    }

    private void loadInterfaceFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Interface File");

        if (applicationState.getServerDirectoryPath() != null && !applicationState.getServerDirectoryPath().isEmpty()) {
            File initialDirectory = new File(applicationState.getServerDirectoryPath() + "/scripts/");
            if (initialDirectory.exists() && initialDirectory.isDirectory()) {
                fileChooser.setInitialDirectory(initialDirectory);
            }
        }

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Interface Files", "*.if"));

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try {
                loadInterfaceComponents(selectedFile.getAbsolutePath());
                renderInterfaceComponents();
                populateComponentTreeView();
                currentLoadedFile = selectedFile.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Loading File");
                alert.setHeaderText(null);
                alert.setContentText("Error loading interface file: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void saveInterfaceFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Interface File");

        File initialDir = null;
        if (currentLoadedFile != null && !currentLoadedFile.isEmpty()) {
            File currentFile = new File(currentLoadedFile);
            initialDir = currentFile.getParentFile();
            fileChooser.setInitialFileName(currentFile.getName());
        } else if (applicationState.getServerDirectoryPath() != null && !applicationState.getServerDirectoryPath().isEmpty()) {
            initialDir = new File(applicationState.getServerDirectoryPath());
        }
        if (initialDir != null && initialDir.exists() && initialDir.isDirectory()) {
            fileChooser.setInitialDirectory(initialDir);
        }
        if(fileChooser.getInitialFileName() == null) {
            fileChooser.setInitialFileName("untitled.if");
        }


        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Interface Files", "*.if"));

        File selectedFile = fileChooser.showSaveDialog(null);

        if (selectedFile != null) {
            try {
                String filePath = selectedFile.getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".if")) {
                    selectedFile = new File(filePath + ".if");
                }

                InterfaceFileWriter.writeInterfaceFile(interfaceComponents, selectedFile.getAbsolutePath());
                currentLoadedFile = selectedFile.getAbsolutePath();

                updatePackFilesService.updateInterfacePackFiles(selectedFile, interfaceComponents);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("File Saved");
                alert.setHeaderText(null);
                alert.setContentText("Interface file was successfully saved to:\n" + selectedFile.getAbsolutePath());
                alert.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Saving File");
                alert.setHeaderText(null);
                alert.setContentText("Error saving interface file: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void populateComponentTreeView() {
        if (interfaceComponents == null || interfaceComponents.isEmpty()) {
            return;
        }

        Map<String, TreeItem<String>> itemMap = new HashMap<>();
        TreeItem<String> rootItem = new TreeItem<>("Interface Components");
        componentTreeView.setMaxHeight(300.0);
        itemMap.put("root", rootItem);

        for (InterfaceComponent component : interfaceComponents) {
            if (component == null) continue;
            TreeItem<String> newItem = new TreeItem<>(component.getName() + " (" + component.getType() + ")");
            itemMap.put(component.getName(), newItem);

            if (component.getLayer() != null && !component.getLayer().isEmpty()) {
                TreeItem<String> parentItem = itemMap.get(component.getLayer());
                if (parentItem != null) {
                    parentItem.getChildren().add(newItem);
                } else {
                    rootItem.getChildren().add(newItem);
                    System.out.println("Parent not found for component: " + component.getName());
                }
            } else {
                rootItem.getChildren().add(newItem);
            }
        }

        componentTreeView.setRoot(rootItem);
        rootItem.setExpanded(true);

        componentTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            if (currentlyDraggablePane != null) {
                removeDraggable(currentlyDraggablePane, currentlyDraggableComponent);
                currentlyDraggablePane = null;
                currentlyDraggableComponent = null;
            }

            if (newValue != null) {
                String componentName = newValue.getValue().split(" ")[0];
                InterfaceComponent selectedComponent = findComponentByName(componentName);
                Node componentNode = findComponentPane(componentName);

                if (selectedComponent != null && componentNode instanceof Pane) {
                    Pane componentPane = (Pane) componentNode;

                    populatePropertiesSidebar(selectedComponent);
                    highlightSelectedComponent(selectedComponent);

                    makeComponentDraggable(selectedComponent, componentPane);
                    currentlyDraggablePane = componentPane;
                    currentlyDraggableComponent = selectedComponent;

                } else {
                    System.err.println("Warning: Could not find component or pane for tree item: " + newValue.getValue());
                    populatePropertiesSidebar(null);
                    selectedComponentHighlight.setVisible(false);
                }
            } else {
                populatePropertiesSidebar(null);
                selectedComponentHighlight.setVisible(false);
            }
        });
    }

    private void highlightSelectedComponent(InterfaceComponent component) {
        if (component == null) {
            selectedComponentHighlight.setVisible(false);
            return;
        }

        Node componentNode = findComponentPane(component.getName());
        if (componentNode instanceof Pane) {
            Pane componentPane = (Pane) componentNode;

            Point2D sceneCoords = componentPane.localToScene(0, 0);
            Point2D rootCoords = root.sceneToLocal(sceneCoords);

            double x = rootCoords.getX();
            double y = rootCoords.getY();
            double width = component.getWidth();
            double height = component.getHeight();

            if ("graphic".equals(component.getType())) {

                if (width <= 0 || height <= 0) {
                    if (!componentPane.getChildren().isEmpty() && componentPane.getChildren().get(0) instanceof ImageView) {
                        ImageView imageView = (ImageView) componentPane.getChildren().get(0);
                        if (imageView.getImage() != null) {
                            width = width <= 0 ? imageView.getImage().getWidth() : width;
                            height = height <= 0 ? imageView.getImage().getHeight() : height;
                        }
                    }
                }
            } else if ("text".equals(component.getType())) {

                if (!componentPane.getChildren().isEmpty() && componentPane.getChildren().get(0) instanceof Canvas) {
                    Canvas canvas = (Canvas) componentPane.getChildren().get(0);
                    width = canvas.getWidth();
                    height = canvas.getHeight();
                }
            }

            selectedComponentHighlight.setLayoutX(x);
            selectedComponentHighlight.setLayoutY(y);
            selectedComponentHighlight.setPrefWidth(width);
            selectedComponentHighlight.setPrefHeight(height);
            selectedComponentHighlight.setVisible(true);
        } else {
            selectedComponentHighlight.setVisible(false);
        }
    }

    private InterfaceComponent findComponentByName(String name) {
        if (interfaceComponents == null || name == null) {
            return null;
        }
        for (InterfaceComponent component : interfaceComponents) {
            if (component != null && name.equals(component.getName())) {
                return component;
            }
        }
        return null;
    }

    private Node findComponentPane(String componentName) {
        for (Node node : root.getChildren()) {
            if (node.getId() != null &&
                    (node.getId().equals("layer_" + componentName) ||
                            node.getId().equals("graphic_" + componentName) ||
                            node.getId().equals("rect_" + componentName) ||
                            node.getId().equals("model_" + componentName) ||
                            node.getId().equals("inv_" + componentName) ||
                            node.getId().equals("invtext_" + componentName) ||
                            node.getId().equals("text_" + componentName))) {
                return node;
            }
        }

        for (Node node : root.getChildren()) {
            if (node instanceof Pane && node.getId() != null && node.getId().startsWith("layer_")) {
                Pane layerPane = (Pane) node;
                for (Node childNode : layerPane.getChildren()) {
                    if (childNode.getId() != null &&
                            (childNode.getId().equals("graphic_" + componentName) ||
                                    childNode.getId().equals("rect_" + componentName) ||
                                    childNode.getId().equals("model_" + componentName) ||
                                    node.getId().equals("inv_" + componentName) ||
                                    node.getId().equals("invtext_" + componentName) ||
                                    childNode.getId().equals("text_" + componentName))) {
                        return childNode;
                    }
                }
            }
        }
        for (Node node : applicationState.getInterfaceRenderArea().getChildren()) {
            if (node.getId() != null &&
                    (node.getId().equals("layer_" + componentName) ||
                            node.getId().equals("graphic_" + componentName) ||
                            node.getId().equals("rect_" + componentName) ||
                            node.getId().equals("model_" + componentName) ||
                            node.getId().equals("inv_" + componentName) ||
                            node.getId().equals("invtext_" + componentName) ||
                            node.getId().equals("text_" + componentName))) {
                return node;
            }
        }

        for (Node node : applicationState.getInterfaceRenderArea().getChildren()) {
            if (node instanceof Pane && node.getId() != null && node.getId().startsWith("layer_")) {
                Pane layerPane = (Pane) node;
                for (Node childNode : layerPane.getChildren()) {
                    if (childNode.getId() != null &&
                            (childNode.getId().equals("graphic_" + componentName) ||
                                    childNode.getId().equals("rect_" + componentName) ||
                                    childNode.getId().equals("model_" + componentName) ||
                                    node.getId().equals("inv_" + componentName) ||
                                    node.getId().equals("invtext_" + componentName) ||
                                    childNode.getId().equals("text_" + componentName))) {
                        return childNode;
                    }
                }
            }
        }

        return null;
    }

    private void makeComponentDraggable(InterfaceComponent component, Pane componentPane) {

        componentPane.setOnMousePressed(null);
        componentPane.setOnMouseDragged(null);
        componentPane.setOnMouseReleased(null);

        componentPane.setMouseTransparent(false);
        componentPane.setPickOnBounds(true);

        final InterfaceComponent parentComponent = component.getLayer() != null ?
                findComponentByName(component.getLayer()) : null;
        final Pane parentPane = parentComponent != null ?
                (Pane)findComponentPane(parentComponent.getName()) : null;

        if (parentPane != null) {
            parentPane.setPickOnBounds(false);
        }

        componentPane.setOnMousePressed(event -> {
            dragStartX = event.getSceneX();
            dragStartY = event.getSceneY();
            isDragging = true;

            componentPane.toFront();
            selectedComponentHighlight.toFront();

            event.consume();
        });

        componentPane.setOnMouseDragged(event -> {
            if (isDragging) {
                double deltaX = event.getSceneX() - dragStartX;
                double deltaY = event.getSceneY() - dragStartY;

                double newX = component.getX() + deltaX;
                double newY = component.getY() + deltaY;

                if (componentPane.getParent() == applicationState.getInterfaceRenderArea()) {
                    newX = Math.clamp(newX, 0, applicationState.getInterfaceRenderArea().getWidth() - component.getWidth());
                    newY = Math.clamp(newY, 0, applicationState.getInterfaceRenderArea().getHeight() - component.getHeight());
                }

                component.setX((int) newX);
                component.setY((int) newY);

                componentPane.setLayoutX(newX);
                componentPane.setLayoutY(newY);

                if (component == currentlyDraggableComponent) {
                    highlightSelectedComponent(component);
                }

                dragStartX = event.getSceneX();
                dragStartY = event.getSceneY();

                if ("layer".equals(component.getType())) {
                    updateChildrenPositions(component.getName());
                }

                event.consume();
            }
        });

        componentPane.setOnMouseReleased(event -> {
            if (isDragging) {
                isDragging = false;
                event.consume();
                populatePropertiesSidebar(component);
            }
        });

        if (parentPane != null) {
            parentPane.setPickOnBounds(false);

            parentPane.setOnMouseExited(event -> {
                if (isDragging) {
                    isDragging = false;
                    event.consume();
                }
            });
        }

        else if (componentPane.getParent() == applicationState.getInterfaceRenderArea()) {
            applicationState.getInterfaceRenderArea().setOnMouseExited(event -> {
                if (isDragging) {
                    isDragging = false;
                    event.consume();
                }
            });
        }
    }

    private void updateChildrenPositions(String layerName) {
        if (layerName == null || interfaceComponents == null) return;

        for (InterfaceComponent component : interfaceComponents) {
            if (component != null && layerName.equals(component.getLayer())) {
                Node childNode = findComponentPane(component.getName());
                if (childNode instanceof Pane) {
                    Pane childPane = (Pane) childNode;

                    TreeItem<String> selectedItem = componentTreeView.getSelectionModel().getSelectedItem();
                    if (selectedItem != null && selectedItem.getValue().startsWith(component.getName() + " ")) {
                        highlightSelectedComponent(component);
                    }
                }
            }
        }
    }

    private void initializeSelectionHighlight() {
        selectedComponentHighlight = new Pane();
        selectedComponentHighlight.setStyle("-fx-border-color: #ff6600; -fx-border-width: 2; -fx-border-style: dashed;");
        selectedComponentHighlight.setMouseTransparent(true);
        selectedComponentHighlight.setVisible(false);
        root.getChildren().add(selectedComponentHighlight);
        selectedComponentHighlight.setViewOrder(-10);
    }

    private void loadInterfaceComponents(String filePath) throws IOException {
        if (interfaceComponents != null) {
            interfaceComponents.clear();
        }
        clearRenderedComponents();

        applicationState.setInterfaceComponents(InterfaceFileParser.parseInterfaceFile(filePath));
    }

    private void clearRenderedComponents() {
        List<Node> nodesToRemove = new ArrayList<>();

        for (Node node : root.getChildren()) {
            if (node != sidebarVBox &&
                    node != propertiesSidebarVBox &&
//                    node != areaViewport &&
//                    node != areaSidebar &&
//                    node != tooltipPane &&
//                    node != backgroundCanvas &&
                    node != applicationState.getInterfaceRenderArea()) {

                if (node instanceof Pane) {
                    String id = node.getId();
                    if (id != null && (
                            id.startsWith("layer_") ||
                            id.startsWith("graphic_") ||
                            id.startsWith("rect_") ||
                            id.startsWith("model_") ||
                            id.startsWith("text_") ||
                            id.startsWith("scrollbar_")
                        )
                    ) {
                        nodesToRemove.add(node);
                    }
                }
            }
        }
        root.getChildren().removeAll(nodesToRemove);
        nodesToRemove.clear();

        for (Node node : applicationState.getInterfaceRenderArea().getChildren()) {
            if (node != sidebarVBox &&
                    node != propertiesSidebarVBox &&
//                    node != areaViewport &&
//                    node != areaSidebar &&
//                    node != tooltipPane &&
//                    node != backgroundCanvas &&
                    node != applicationState.getInterfaceRenderArea()) {

                if (node instanceof Pane) {
                    String id = node.getId();
                    if (id != null && (
                            id.startsWith("layer_") ||
                            id.startsWith("graphic_") ||
                            id.startsWith("rect_") ||
                            id.startsWith("model_") ||
                            id.startsWith("text_") ||
                            id.startsWith("scrollbar_")
                        )
                    ) {
                        nodesToRemove.add(node);
                    }
                }
            }
        }
        applicationState.getInterfaceRenderArea().getChildren().removeAll(nodesToRemove);

        activeComponentName = null;

        originalClickHandlers.clear();
        originalViewOrderMap.clear();
        textRenderInfoMap.clear();
    }

    private void renderInterfaceComponents() {
    }

    private void updateMouseTransparency(Pane pane, InterfaceComponent component) {
        if (pane == null || component == null) return;

        boolean isLayer = "layer".equals(component.getType());
        boolean hasActiveState = (component.getActiveGraphicName() != null && !component.getActiveGraphicName().isEmpty());
        boolean hasHoverState = component.getOverColour() != null;
        boolean isInteractiveLayer = isLayer && component.getOverlayer() != null && !component.getOverlayer().isEmpty();
        boolean hasTooltip = (component.getOption() != null && !component.getOption().isEmpty()) ||
                (component.getButtonType() != null && !component.getButtonType().isEmpty());

        boolean hasInteractiveChildren = false;
        if (isLayer) {
            for (InterfaceComponent child : interfaceComponents) {
                if (child != null && component.getName().equals(child.getLayer())) {
                    if ((child.getOption() != null && !child.getOption().isEmpty()) ||
                            (child.getButtonType() != null && !child.getButtonType().isEmpty()) ||
                            (child.getActiveGraphicName() != null && !child.getActiveGraphicName().isEmpty()) ||
                            child.getOverColour() != null) {
                        hasInteractiveChildren = true;
                        break;
                    }
                }
            }
        }

        boolean makeTransparent = !hasActiveState && !hasHoverState && !hasTooltip &&
                !isInteractiveLayer && !hasInteractiveChildren;

        pane.setMouseTransparent(makeTransparent);
        pane.setPickOnBounds(!makeTransparent);
    }

    private void removeDraggable(Pane pane, InterfaceComponent component) {
        if (pane == null) return;
        pane.setOnMousePressed(null);
        pane.setOnMouseDragged(null);
        pane.setOnMouseReleased(null);
        updateMouseTransparency(pane, component);
    }



    private void toggleLayerVisibility(
            String layerName,
            boolean visible,
            Map<String, InterfaceComponent> componentMap,
            Map<String, Pane> componentPaneMap,
            Map<String, Boolean> layerVisibilityMap,
            Map<String, List<String>> layerChildrenMap) {

        layerVisibilityMap.put(layerName, visible);

        Pane layerPane = componentPaneMap.get(layerName);
        if (layerPane != null) {
            layerPane.setVisible(visible);

            layerPane.setMouseTransparent(false);

            if (visible) {
                if (!originalViewOrderMap.containsKey(layerName)) {
                    originalViewOrderMap.put(layerName, layerPane.getViewOrder());
                }

                layerPane.setViewOrder(-10.0);
                layerPane.toFront();
            } else {
                Double originalViewOrder = originalViewOrderMap.getOrDefault(layerName, 0.0);
                layerPane.setViewOrder(originalViewOrder);
            }
        }

        List<String> children = layerChildrenMap.get(layerName);
        if (children != null) {
            for (String childName : children) {
                InterfaceComponent childComponent = componentMap.get(childName);
                Pane childPane = componentPaneMap.get(childName);

                if (childComponent != null && childPane != null) {
                    childPane.setVisible(visible);
                    if ("layer".equals(childComponent.getType())) {
                        toggleLayerVisibility(childName, visible, componentMap, componentPaneMap,
                                layerVisibilityMap, layerChildrenMap);
                    } else {
                        childPane.setVisible(visible);
                        if (visible) {
                            if (!originalViewOrderMap.containsKey(childName)) {
                                originalViewOrderMap.put(childName, childPane.getViewOrder());
                            }
                            childPane.setViewOrder(-9.0);
                            if (childPane.getParent() instanceof Pane) {
                                Pane parent = (Pane) childPane.getParent();
                                parent.getChildren().remove(childPane);
                                parent.getChildren().add(childPane);
                            }
                        } else {
                            Double originalViewOrder = originalViewOrderMap.getOrDefault(childName, 0.0);
                            childPane.setViewOrder(originalViewOrder);
                        }
                    }
                }
            }
        }
    }

    private void showTooltip(String text) {
        if (text == null || text.isEmpty()) return;
        tooltipCanvas.getGraphicsContext2D().clearRect(0, 0, tooltipCanvas.getWidth(), tooltipCanvas.getHeight());
        String fontName = "b12_full";
        FontHelper font = assetLoader.getFontManager().getFont(fontName);

        if (font == null) {
            fontName = "b12";
            font = assetLoader.getFontManager().getFont(fontName);
        }
        double width = Math.max(100,  font.getTextWidth(text) + 10);
        if (width > tooltipCanvas.getWidth()) {
            tooltipCanvas.setWidth(width);
        }
        assetLoader.getFontManager().drawTaggableText(
                tooltipCanvas.getGraphicsContext2D(),
                fontName,
                text,
                12.0, 26.0,
                16777215,
                true
        );
        tooltipPane.setVisible(true);
    }

    private void hideTooltip() {
        tooltipPane.setVisible(false);
        tooltipCanvas.getGraphicsContext2D().clearRect(0, 0, tooltipCanvas.getWidth(), tooltipCanvas.getHeight());
    }
}
