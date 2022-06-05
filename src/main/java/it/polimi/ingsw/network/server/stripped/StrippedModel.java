package it.polimi.ingsw.network.server.stripped;

import it.polimi.ingsw.view.UI;
import it.polimi.ingsw.exceptions.LocalModelNotLoadedException;
import it.polimi.ingsw.model.deck.assistantcard.AssistantCardDeck;
import it.polimi.ingsw.model.enumerations.Colors;

import java.beans.PropertyChangeEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Optional;

public class StrippedModel implements Serializable {
    final private ArrayList<StrippedBoard> boards;
    final private ArrayList<StrippedCharacter> characters;
    final private ArrayList<StrippedCloud> clouds;
    final private ArrayList<StrippedIsland> islands;
    private String currentPlayer;
    private String winnerTeam;
    private UI ui;

    public ArrayList<AssistantCardDeck> getAssistantDecks() {
        return assistantDecks;
    }

    final private ArrayList<AssistantCardDeck> assistantDecks;

    public StrippedModel(ArrayList<StrippedBoard> boards, ArrayList<StrippedCharacter> characters,
                         ArrayList<StrippedCloud> clouds, ArrayList<StrippedIsland> islands, ArrayList<AssistantCardDeck> assistantDecks) {
        this.boards = boards;
        this.characters = characters;
        this.clouds = clouds;
        this.islands = islands;
        this.assistantDecks = assistantDecks;
    }

    public void updateModel(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "entrance":
            case "dining":
            case "coins":
            case "professorTable":
            case "towers":
                setBoard(evt);
                break;
            case "character":
                changePriceCharacterCard(evt);
                break;
            case "island":
            case "island-conquest":
            case "island-merged":
                changeIsland(evt);
                break;
            case "cloud":
                changeCloud(evt);
                break;
            case "assistant":
                changeAssistantDeck(evt);
                break;
            case "current-player":
            case "first-player":
                ui.currentPlayer((String)evt.getNewValue());
                setCurrentPlayer((String)evt.getNewValue()); //todo remove
                break;
            case "game-over":
                winnerTeam = (String) evt.getNewValue();
                ui.gameOver(winnerTeam);
                break;
            default:
                System.out.println("scrivere una exception sensata"); //TODO
                break;
        }
    }

    private void changeAssistantDeck(PropertyChangeEvent evt) {
        String ownerDeck = currentPlayer;
        Optional<AssistantCardDeck> optionalDeckToModify = assistantDecks.stream().filter(d -> d.getOwner().equals(ownerDeck)).findFirst();
        AssistantCardDeck deckToModify= optionalDeckToModify.get();
        if (optionalDeckToModify.isPresent()) {
            assistantDecks.remove(deckToModify);
            assistantDecks.add((AssistantCardDeck) evt.getNewValue());
            String playedCard=(String) evt.getOldValue();
            ui.deckChange(playedCard);
        }
    }

    private void setBoard(PropertyChangeEvent evt) {
        String ownerBoard = (String) evt.getOldValue();
        Optional<StrippedBoard> boardToModify = boards.stream().filter(b -> ownerBoard.equals(b.getOwner())).findFirst();
        if (boardToModify.isPresent()) {
            switch (evt.getPropertyName()) {
                case "entrance":
                    boardToModify.get().setEntrance((EnumMap<Colors, Integer>) evt.getNewValue());
                    ui.entranceChanged(evt);
                    break;
                case "dining":
                    boardToModify.get().setDining((EnumMap<Colors, Integer>) evt.getNewValue());
                    ui.diningChange(evt);
                    break;
                case "towers":
                    boardToModify.get().setNumberOfTowers((int) evt.getNewValue());
                    ui.towersEvent(evt);
                    break;
                case "coins":
                    boardToModify.get().setCoins((int) evt.getNewValue());
                    ui.coinsChanged(evt);
                    break;
                case "professorTable":
                    boardToModify.get().setProfessorsTable((ArrayList<Colors>) evt.getNewValue());
                    break;
                default:
                    System.out.println("exception da fare setBoard"); //todo
                    break;
            }
        } else { //todo
            System.out.println("exception da fare setBoard");
        }
    }

    private void changePriceCharacterCard(PropertyChangeEvent evt) {
        StrippedCharacter changedCard = (StrippedCharacter) evt.getOldValue();
        StrippedCharacter cardToUpdate = null;
        for (StrippedCharacter card : characters) {
            if (card.sameCard(changedCard)) {
                cardToUpdate = card;
            }
        }

        if (cardToUpdate != null) {
            if (cardToUpdate.getPrice() == changedCard.getPrice()) {
                int newPriceCard = (int) evt.getNewValue();
                cardToUpdate.setPrice(newPriceCard); //update
            } else {
                System.out.println("throws an exception because the old price of character is not the same"); //todo
            }
        } else {
            System.out.println("throws an exception not found card to update"); //todo
        }
    }

    private void changeIsland(PropertyChangeEvent evt) {
        StrippedIsland changedIsland = (StrippedIsland) evt.getOldValue();
        Optional<StrippedIsland> optionalIslandFound = islands.stream().filter(x -> x.getName().equals(changedIsland.getName())).findFirst();
        StrippedIsland islandFound= optionalIslandFound.get();
        if (optionalIslandFound.isPresent()) {
            islands.remove(islandFound); //IslandEvent Deletion
            if (evt.getNewValue() != null) {
                islands.add((StrippedIsland) evt.getNewValue());
                if(evt.getPropertyName().equals("island"))
                ui.islandChange(evt);
                else if(evt.getPropertyName().equals("island-conquest"))
                {
                    ui.islandConquest(evt);
                }
                else if(evt.getPropertyName().equals("island-merged"))
                {
                    ui.islandMerged(evt);
                }
            }
        } else {
            System.out.println("Exception changeIsland , strippedModel"); //todo
        }
    }

    private void changeCloud(PropertyChangeEvent evt) {
        StrippedCloud changedCloud;
        if (evt.getOldValue() != null) {
            changedCloud = (StrippedCloud) evt.getOldValue();
            ui.notifyCloud(evt);
        } else {
            changedCloud = (StrippedCloud) evt.getNewValue();
            ui.notifyCloud(evt);
        }
        Optional<StrippedCloud> optionalCloudFound = clouds.stream().filter(x -> x.getName().equals(changedCloud.getName())).findFirst();
        StrippedCloud cloudFound= optionalCloudFound.get();
        clouds.remove(cloudFound);
        clouds.add(changedCloud);
    }

    private void setCurrentPlayer(String currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public String getCurrentPlayer() {
        return currentPlayer;
    }

    public ArrayList<StrippedCharacter> getCharacters() {
        return characters;
    }

    public ArrayList<StrippedCloud> getClouds() {
        return clouds;
    }

    public ArrayList<StrippedIsland> getIslands() {
        return islands;
    }

    public ArrayList<StrippedBoard> getBoards() {
        return boards;
    }

    public StrippedBoard getBoardOf(String owner) throws LocalModelNotLoadedException {
        for (StrippedBoard b : boards){
            if(b.getOwner().equals(owner))return b;
        }
        throw new LocalModelNotLoadedException(); //todo change the name of this exception with something more specific
    }

    public void setUi(UI ui) {
        this.ui = ui;
    }
}