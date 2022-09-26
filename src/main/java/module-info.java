module dictionary.app {
    requires java.logging;
    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;

    exports com.github.naiithink.app to javafx.graphics;

    opens com.github.naiithink.app to javafx.fxml;
    opens com.github.naiithink.app.controllers to javafx.fxml;
}
