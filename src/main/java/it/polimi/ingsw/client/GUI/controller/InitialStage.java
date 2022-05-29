package it.polimi.ingsw.client.GUI.controller;

import it.polimi.ingsw.client.GUI.GUI;

/**
 * @author Amrit
 * Class that sets the attributes needed by the windows involved in the initial set up phase
 */
public class InitialStage {
    protected GUI gui;

    public InitialStage(GUI gui) {
        this.gui = gui;
    }

    public GUI getGui() {
        return gui;
    }

    public void setGui(GUI gui) {
        this.gui = gui;
    }
}
