package homeOrganiserApp.alerting;

import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.stage.Screen;
import javafx.stage.StageStyle;

public final class Alerts {
    public static void showError(String content) {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setTitle("Błąd");
        error.setHeaderText(null);
        error.setContentText(content);
        error.showAndWait();
    }
    public static void showInformation(String content) {
        Alert information = new Alert(Alert.AlertType.INFORMATION);
        information.setTitle("Info");
        information.setHeaderText(null);
        information.setContentText(content);
        information.showAndWait();
    }
    public static void showTrayWarning(String content) {
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        double xCoordinate = screenBounds.getWidth() - 400;
        double yCoordinate = screenBounds.getHeight() - (screenBounds.getHeight()/4);
        Alert warning = new Alert(Alert.AlertType.WARNING);
        warning.setX(xCoordinate);
        warning.setY(yCoordinate);
        warning.initStyle(StageStyle.TRANSPARENT);
        warning.setTitle("Uwaga");
        warning.setHeaderText("Organizer domowy");
        warning.setContentText(content);
        warning.showAndWait();
    }
}
