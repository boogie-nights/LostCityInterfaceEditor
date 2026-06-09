module org.lostcityinterfaceeditor {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.desktop;

    opens org.lostcityinterfaceeditor to javafx.fxml;
    exports org.lostcityinterfaceeditor;
    exports org.lostcityinterfaceeditor.helpers;
    opens org.lostcityinterfaceeditor.helpers to javafx.fxml;
    exports org.lostcityinterfaceeditor.loaders;
    opens org.lostcityinterfaceeditor.loaders to javafx.fxml;
    exports org.lostcityinterfaceeditor.managers;
    opens org.lostcityinterfaceeditor.managers to javafx.fxml;
    exports org.lostcityinterfaceeditor.fileUtils;
    opens org.lostcityinterfaceeditor.fileUtils to javafx.fxml;
    exports org.lostcityinterfaceeditor.baseCode;
    opens org.lostcityinterfaceeditor.baseCode to javafx.fxml;
	exports org.lostcityinterfaceeditor.ui;
	opens org.lostcityinterfaceeditor.ui to javafx.fxml;
    exports org.lostcityinterfaceeditor.ui.widget;
    opens org.lostcityinterfaceeditor.ui.widget to javafx.fxml;
    exports org.lostcityinterfaceeditor.models;
    opens org.lostcityinterfaceeditor.models to javafx.fxml;
}