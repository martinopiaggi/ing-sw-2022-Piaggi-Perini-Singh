package it.polimi.ingsw.controller.commands;

import it.polimi.ingsw.controller.Controller;

public class PlayCharacterCardC implements Command {

    Controller controller;
    int assistantCardID;

    public PlayCharacterCardC(Controller controller, int assistantCardID) {
        this.controller = controller;
        this.assistantCardID = assistantCardID;
    }

    @Override
    public void execute() {
        controller.callPlayAssistantCard(assistantCardID); //TODO
    }
}
