module org.example.taskmanagerpersonaljfx { // IMPORTANT: Keep your actual module name here!
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics; // Add this
    requires javafx.base;    // Add this


    requires java.sql;       // For database connectivity
    requires java.desktop;   // For general utility (e.util.Date)

    // Open packages to javafx.fxml for UI loading
    opens gui to javafx.fxml;
    exports gui;

    // Open model and util packages for reflection if needed by JavaFX or other modules
    opens model to javafx.base;
    exports model;

    opens util to javafx.base;
    exports util;
}
    