package it.polimi.ingsw.view.GUI.controllerFX;

import it.polimi.ingsw.view.GUI.GUILauncher;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * @author Amrit
 */
public interface Controller {
    @FXML
    void initialize();

    static void load(String sceneName, Controller controller) {
        String filePath = ResourcesPath.FXML_FILE_PATH + sceneName + ResourcesPath.FILE_EXTENSION;
        FXMLLoader loader = new FXMLLoader(Controller.class.getResource(filePath));
        loader.setController(controller);

        Stage mainWindow = GUILauncher.mainWindow;
        mainWindow.setMinHeight(720);
        mainWindow.setMinWidth(1280);

        try {
            Scene sceneRooms = new Scene(loader.load(), mainWindow.getScene().getWidth(), mainWindow.getScene().getHeight());
            mainWindow.setScene(sceneRooms);
            mainWindow.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void showErrorDialogBox(String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initStyle(StageStyle.DECORATED);
        alert.setTitle(null);
        alert.setHeaderText(null);
        alert.setContentText(content);

        alert.showAndWait();
    }
}
