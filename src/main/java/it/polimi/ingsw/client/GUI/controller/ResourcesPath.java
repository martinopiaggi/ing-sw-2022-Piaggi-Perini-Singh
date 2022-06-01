package it.polimi.ingsw.client.GUI.controller;

/**
 * Contains all the paths and file names needed to load a FXML file
 */
public interface ResourcesPath {
    String FXML_FILE_PATH = "/fxml/"; // Equivalent to src/main/resources/fxml
    String FILE_EXTENSION = ".fxml";

    String MAIN_MENU = "MainMenu";
    String WAITING_WINDOW = "WaitingWindow";
    String ROOM_LIST = "RoomList";
    String ROOM_VIEW = "RoomController";
    String CREATE_NEW_GAME = "CreateNewGame";

    String IMAGES_PATH = "/img/";
    String IMAGES_EXTENSION = ".png";

    String CSS_PATH = "/css/";
    String CSS_EXTENSION = ".css";
}
