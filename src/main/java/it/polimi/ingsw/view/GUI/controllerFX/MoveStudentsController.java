package it.polimi.ingsw.view.GUI.controllerFX;

import it.polimi.ingsw.StringNames;
import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.model.StudentManager;
import it.polimi.ingsw.model.enumerations.Colors;
import it.polimi.ingsw.network.server.commands.MoveStudents;
import it.polimi.ingsw.network.server.commands.PlayAssistantCard;
import it.polimi.ingsw.view.GUI.GUI;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.text.Text;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.EnumMap;

public class MoveStudentsController extends InitialStage implements Controller {

    @FXML
    private Button cancelButton;

    @FXML
    private Button confirmButton;

    @FXML
    private Text totalYellow;
    @FXML
    private Text totalBlue;
    @FXML
    private Text totalGreen;
    @FXML
    private Text totalRed;
    @FXML
    private Text totalPink;

    @FXML
    private ComboBox islandNumber;

    @FXML
    private ComboBox totalDiningYellow;
    @FXML
    private ComboBox totalDiningBlue;
    @FXML
    private ComboBox totalDiningGreen;
    @FXML
    private ComboBox totalDiningRed;
    @FXML
    private ComboBox totalDiningPink;

    @FXML
    private ComboBox totalIslandYellow;
    @FXML
    private ComboBox totalIslandBlue;
    @FXML
    private ComboBox totalIslandGreen;
    @FXML
    private ComboBox totalIslandRed;
    @FXML
    private ComboBox totalIslandPink;

    public MoveStudentsController(GUI gui) {
        super(gui);
    }

    @Override
    public void initialize() {
        loadComboBox(islandNumber, GUI.client.getLocalModel().getIslands().size());

        ArrayList<Text> text = new ArrayList<>();
        text.add(totalYellow);
        text.add(totalBlue);
        text.add(totalGreen);
        text.add(totalRed);
        text.add(totalPink);

        ArrayList<ComboBox> diningComboBoxes = new ArrayList<>();
        diningComboBoxes.add(totalDiningYellow);
        diningComboBoxes.add(totalDiningBlue);
        diningComboBoxes.add(totalDiningGreen);
        diningComboBoxes.add(totalDiningRed);
        diningComboBoxes.add(totalDiningPink);

        ArrayList<ComboBox> islandsComboBoxes = new ArrayList<>();
        islandsComboBoxes.add(totalIslandYellow);
        islandsComboBoxes.add(totalIslandBlue);
        islandsComboBoxes.add(totalIslandGreen);
        islandsComboBoxes.add(totalIslandRed);
        islandsComboBoxes.add(totalIslandPink);

        for (ComboBox islandBox : islandsComboBoxes) {
            islandBox.getSelectionModel().selectFirst();
            islandBox.setDisable(true);
        }

        try {
            EnumMap<Colors, Integer> entrance = GUI.client.getLocalModel().getBoardOf(GUI.client.getNickname()).getEntrance();

            int i = 0;
            for (Text textEntrance : text) {
                textEntrance.setText(entrance.get(Colors.getStudent(i)).toString());
                i++;
            }
        } catch (LocalModelNotLoadedException e) {
            e.printStackTrace();
        }

        int k = 0;
        for (ComboBox diningBox : diningComboBoxes) {
            loadComboBox(diningBox, Integer.parseInt(text.get(k).getText()));
            diningBox.getSelectionModel().selectFirst();
            k++;
        }

        islandNumber.setOnAction((event) -> {
            int selectedIndex = islandNumber.getSelectionModel().getSelectedIndex();
            if (selectedIndex != 0) {
                int j = 0;
                for (ComboBox islandBox : islandsComboBoxes) {
                    loadComboBox(islandBox, Integer.parseInt(text.get(j).getText()));
                    islandBox.setDisable(false);
                    islandBox.getSelectionModel().selectFirst();
                    j++;
                }
            } else {
                for (ComboBox islandBox : islandsComboBoxes) {
                    islandBox.setDisable(true);
                }
            }
        });

        //button start
        //crea ciò che devi passare prendendo i valori dei select
        //conferma
        confirmButton.setOnAction((event) -> {
            EnumMap<Colors, ArrayList<String>> studentToMove = new EnumMap<>(Colors.class);
            ArrayList<String> emptyString = new ArrayList<>();
            for (Colors color : Colors.values()) {
                studentToMove.put(color, emptyString);
            }

            String dining = "dining";
            ArrayList<String> destinations;
            int value;
            for (int i = 0; i < Colors.values().length; i++) {
                destinations = new ArrayList<>();

                if (diningComboBoxes.get(i).getSelectionModel().getSelectedIndex() != 0) {
                    value = Integer.parseInt((String) diningComboBoxes.get(i).getSelectionModel().getSelectedItem());
                    for (int j = 0; j < value; j++) {
                        destinations.add(dining);
                    }
                }

                if (islandNumber.getSelectionModel().getSelectedIndex() != 0 && islandsComboBoxes.get(i).getSelectionModel().getSelectedItem() != null) {
                    value = Integer.parseInt((String) islandsComboBoxes.get(i).getSelectionModel().getSelectedItem());
                    for (int j = 0; j < value; j++) {
                        destinations.add(GUI.client.getLocalModel().getIslands().get(value).getName());
                    }
                }

                studentToMove.put(Colors.getStudent(i), destinations);
            }

            for (Colors colors : Colors.values()) {
                System.out.println(studentToMove.get(colors));
            }

            MoveStudents moveStudents = new MoveStudents(GUI.client.getNickname(), studentToMove);
            try {
                GUI.client.performGameAction(moveStudents);
            } catch (NotEnoughCoinsException e) {
                Controller.showErrorDialogBox(StringNames.NOT_ENOUGH_COINS);
            } catch (AssistantCardNotFoundException e) {
                Controller.showErrorDialogBox(StringNames.ASSISTANT_CARD_NOT_FOUND);
            } catch (NegativeValueException e) {
                Controller.showErrorDialogBox(StringNames.NEGATIVE_VALUE);
            } catch (IncorrectStateException e) {
                Controller.showErrorDialogBox(StringNames.INCORRECT_STATE);
            } catch (MotherNatureLostException e) {
                Controller.showErrorDialogBox(StringNames.MOTHER_NATURE_LOST);
            } catch (ProfessorNotFoundException e) {
                Controller.showErrorDialogBox(StringNames.PROFESSOR_NOT_FOUND);
            } catch (IncorrectPlayerException e) {
                Controller.showErrorDialogBox(StringNames.INCORRECT_PLAYER);
            } catch (RemoteException e) {
                Controller.showErrorDialogBox(StringNames.CONNECTION_ERROR);
            } catch (IncorrectArgumentException e) {
                Controller.showErrorDialogBox(StringNames.INCORRECT_ARGUMENT);
            } catch (UserNotInRoomException e) {
                Controller.showErrorDialogBox(StringNames.NOT_IN_ROOM);
            } catch (UserNotRegisteredException e) {
                Controller.showErrorDialogBox(StringNames.USER_NOT_REGISTERED);
            }

            Window window = ((Node) (event.getSource())).getScene().getWindow();
            window.fireEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSE_REQUEST));
        });

        //button cancel
        cancelButton.setOnAction((event) -> {
            Window window = ((Node) (event.getSource())).getScene().getWindow();
            window.fireEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSE_REQUEST));
        });
    }

    public void loadComboBox(ComboBox comboBox, int num) {
        ObservableList<String> choices = FXCollections.observableArrayList();

        choices.add("None");
        for (int i = 1; i <= num; i++)
            choices.add(Integer.toString(i));

        comboBox.setItems(choices);
    }
}