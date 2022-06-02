package it.polimi.ingsw.client.GUI;

import it.polimi.ingsw.client.Client;
import it.polimi.ingsw.client.GUI.controller.*;
import it.polimi.ingsw.client.View;
import javafx.application.Application;
import javafx.application.Platform;

import java.beans.PropertyChangeEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class GUI implements View {
    public static Client client;

    public static Controller controller;
    public static String view;
    public RoomListController roomListController;

    private final AtomicBoolean isDoing;

    public GUI(Client client) {
        this.isDoing = new AtomicBoolean(false);
        GUI.client = client;
        GUI.client.setUI(this);
        view = "launcher";
    }

    /**
     * Starts the GUI
     */
    public void start() {
        Application.launch(GUILauncher.class);
    }

    public void startAction() {
        if (!isDoing.get()) {
            isDoing.set(true);
        }
    }

    public void stopAction() {
        if (isDoing.get()) {
            isDoing.set(false);
        }
    }

    public boolean isDoing() {
        return this.isDoing.get();
    }

    @Override
    public void startGame() throws RemoteException {

    }

    @Override
    public void currentPlayer(String s) {

    }

    @Override
    public void notifyCloud(PropertyChangeEvent e) {

    }

    @Override
    public void deckChange(String input) {

    }

    @Override
    public void assistantCardPlayed(PropertyChangeEvent e) {

    }

    @Override
    public void islandChange(PropertyChangeEvent e) {

    }

    @Override
    public void islandMerged(PropertyChangeEvent e) {

    }

    @Override
    public void islandConquest(PropertyChangeEvent e) {

    }

    @Override
    public void diningChange(PropertyChangeEvent e) {

    }

    @Override
    public void towersEvent(PropertyChangeEvent e) {

    }

    @Override
    public void gameOver(String winner) {

    }

    @Override
    public void coinsChanged(PropertyChangeEvent e) {

    }

    @Override
    public void entranceChanged(PropertyChangeEvent e) {

    }

    @Override
    public void removedProfessors(PropertyChangeEvent e) {

    }

    /**
     * Starts the window that shows the list of rooms available
     *
     * @param rooms list of rooms
     */
    public void roomsAvailable(ArrayList<String> rooms) {
        if (view.equals("lobby")) {
            if (RoomListController.isOpened()) {
                Platform.runLater(() -> {
                    roomListController.update(rooms);
                });
            } else {
                Platform.runLater(() -> {
                    roomListController = new RoomListController(this);
                    roomListController.setRoomsList(rooms);

                    Controller.startStage(ResourcesPath.ROOM_LIST, roomListController);
                    controller.closeStage();
                });
            }
        }
    }

    public void roomJoin(ArrayList<String> players) {
        if (view.equals("room")) {
            Platform.runLater(() -> {
                RoomController roomController = new RoomController(this);
                roomController.setPlayersList(players);
                Controller.startStage(ResourcesPath.ROOM_VIEW, roomController);
                controller.closeStage();
            });
        }
    }
}