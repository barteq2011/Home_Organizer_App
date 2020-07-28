package homeOrganiserApp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import homeOrganiserApp.alerting.Alerts;
import homeOrganiserApp.dataOperating.TaskData;

import java.io.IOException;


public final class App extends Application {
    double XOffset, YOffset;
    @Override
    public void start(Stage primaryStage) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("mainWindow.fxml"));
        try {
            Parent root = loader.load();
            primaryStage.setTitle("Organizer Domowy 1.0");
            primaryStage.setScene(new Scene(root, 600, 500));
            MainWindowController controller = loader.getController();
            primaryStage.setResizable(false);
            primaryStage.setOpacity(0.92);
            primaryStage.getIcons().add(new Image(getClass().getResource("styles/icon.png").toExternalForm()));
            // Adding handlers to menu bar, which will make windows draggable
            controller.getMenuBar().setOnMousePressed(e -> {
                XOffset = e.getSceneX();
                YOffset = e.getSceneY();
            });
            controller.getMenuBar().setOnMouseDragged(e -> {
                primaryStage.setX(e.getScreenX() - XOffset);
                primaryStage.setY(e.getScreenY() - YOffset);
            });
            primaryStage.setOnCloseRequest(e -> {
                controller.handleExitMenuItem();
                e.consume();
            });
            primaryStage.show();
        } catch (IOException e) {
            Alerts.showError("Nie można uruchomić programu, ponieważ nie udało się otworzyć odpowiedniego dokumentu FXML\n" +
                    "Spróbuj ponownie zainstalować program aby naprawić ten problem");
           e.printStackTrace();
        }
    }

    @Override
    public void init() {
        // Connect to database
        if(!TaskData.getInstance().open())
            Platform.exit();
    }

    @Override
    public void stop() {
        // Close connection with database
        TaskData.getInstance().close();
    }

    public static void main(String[] args) { launch(); }

}