module mainPackage {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens homeOrganiserApp to javafx.fxml;
    exports homeOrganiserApp;
}