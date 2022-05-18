package it.polimi.ingsw.model;

import it.polimi.ingsw.model.enumerations.Colors;
import it.polimi.ingsw.model.enumerations.State;
import it.polimi.ingsw.model.enumerations.Towers;
import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.server.ClientConnection;
import it.polimi.ingsw.server.Room;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumMap;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {
    @Test
    public void testPlayersInit() throws IncorrectArgumentException, NegativeValueException {
        ArrayList<String> nicknames = new ArrayList<>();
        nicknames.add("Amrit");
        nicknames.add("Lore");
        nicknames.add("Tino");
        nicknames.add("Tony Stark");
        ArrayList<ClientConnection> emptyClientListsTest = new ArrayList<>();
        Room roomTest = new Room("testRoom",emptyClientListsTest);
        Game game = new Game(roomTest,false, 4, nicknames);
        ArrayList<Player> players = game.getPlayers();
        assertTrue(nicknames.contains(game.getCurrentPlayer().getNickname()));
        assertEquals(game.getCurrentState(), State.PLANNINGPHASE);
        assertEquals(players.size(), 4);
    }

    @Test
    public void testDrawFromBag() throws IncorrectArgumentException, IncorrectPlayerException, NegativeValueException {
        Game game = initGame2players();
        game.drawFromBag(game.getCurrentPlayer().getNickname());
        assertTrue(game.getPlayerDrawnOut());
    }

    @Test
    public void testDrawFromBagFalse() throws IncorrectArgumentException, NegativeValueException {
        Game game = initGame2players();
        assertThrows(IncorrectPlayerException.class, () -> game.drawFromBag("randomPlayer"));
    }

    public Game initGame2players() throws IncorrectArgumentException, NegativeValueException {
        ArrayList<String> nicknames = new ArrayList<>();
        nicknames.add("Luke Skywalker");
        nicknames.add("Dark Fener");
        ArrayList<ClientConnection> emptyClientListsTest = new ArrayList<>();
        Room roomTest = new Room("testRoom",emptyClientListsTest);
        return new Game(roomTest,true, 2, nicknames);
    }

    public Game initGame3players() throws IncorrectArgumentException, NegativeValueException {
        ArrayList<String> nicknames = new ArrayList<>();
        nicknames.add("Bruce Wayne");
        nicknames.add("Joker");
        nicknames.add("Bane");
        ArrayList<ClientConnection> emptyClientListsTest = new ArrayList<>();
        Room roomTest = new Room("testRoom",emptyClientListsTest);
        return new Game(roomTest,false, 3, nicknames);
    }

    public Game initGame4players() throws IncorrectArgumentException, NegativeValueException {
        ArrayList<String> nicknames = new ArrayList<>();
        nicknames.add("Michelangelo");
        nicknames.add("Raffaello");
        nicknames.add("Donatello");
        nicknames.add("Leonardo");
        ArrayList<ClientConnection> emptyClientListsTest = new ArrayList<>();
        Room roomTest = new Room("testRoom",emptyClientListsTest);
        return new Game(roomTest,true, 4, nicknames);
    }

    @Test
    public void testNextPlayer3Players() throws IncorrectArgumentException, IncorrectStateException, NegativeValueException {
        Game game = initGame3players();

        String oldPlayerNickname = game.getCurrentPlayer().getNickname();
        game.nextPlayer();
        String newPlayerNickname = game.getCurrentPlayer().getNickname();
        assertNotEquals(oldPlayerNickname, newPlayerNickname);
    }

    @Test
    public void testNextPlayer2and4Players() throws IncorrectArgumentException, IncorrectStateException, NegativeValueException {
        Game game = initGame4players();
        String oldPlayerNickname = game.getCurrentPlayer().getNickname();
        game.nextPlayer();
        assertNotEquals(oldPlayerNickname, game.getCurrentPlayer().getNickname());
    }

    @Test
    public void testFindPlayerFromTeam() throws IncorrectArgumentException, NegativeValueException {
        Game game = initGame3players();
        assertEquals(game.findPlayerFromTeam(Towers.GREY).size(), 1);
    }

    @Test
    public void testFindPlayerFromTeam4players() throws IncorrectArgumentException, NegativeValueException {
        Game game = initGame4players();
        assertEquals(game.findPlayerFromTeam(Towers.GREY).size(), 0);
        assertEquals(game.findPlayerFromTeam(Towers.WHITE).size(), 2);
        assertEquals(game.findPlayerFromTeam(Towers.BLACK).size(), 2);
    }

    @Test
    void testPlayAssistantCard() throws IncorrectArgumentException, IncorrectPlayerException, IncorrectStateException,AssistantCardNotFound, NegativeValueException {
        Game game = initGame4players();
        game.drawFromBag(game.getCurrentPlayer().getNickname());
        String oldPlayer = game.getCurrentPlayer().getNickname();
        game.playAssistantCard(game.getCurrentPlayer().getNickname(), "Assistente1");
        assertNotEquals(oldPlayer, game.getCurrentPlayer().getNickname());
    }

    @Test
    void testPlanningPhase() throws IncorrectArgumentException, IncorrectStateException, AssistantCardNotFound, IncorrectPlayerException, NegativeValueException {
        Game game = initGame4players();
        String oldPlayer = "null";
        for (int i = 0; i < 4; i++) {
            oldPlayer = game.getCurrentPlayer().getNickname();
            game.drawFromBag(oldPlayer);
            game.playAssistantCard(game.getCurrentPlayer().getNickname(), "Assistente3");
            String newPlayer = game.getCurrentPlayer().getNickname();
            assertNotEquals(oldPlayer, newPlayer);
        }
        assertEquals(game.getCurrentState(), State.ACTIONPHASE_1);
    }

    Game planningPhaseComplete() throws IncorrectArgumentException, AssistantCardNotFound, IncorrectPlayerException, IncorrectStateException, NegativeValueException {
        Game game = initGame4players();
        for (int i = 0; i < 4; i++) {
            game.drawFromBag(game.getCurrentPlayer().getNickname());
            game.playAssistantCard(game.getCurrentPlayer().getNickname(), "Assistente1");
        }
        return game;
    }

    @Test
    void testMoveStudents() throws IncorrectArgumentException, AssistantCardNotFound,IncorrectPlayerException, IncorrectStateException, NegativeValueException, ProfessorNotFoundException {
        Game g = planningPhaseComplete();
        assertEquals(g.getCurrentState(), State.ACTIONPHASE_1);
        EnumMap<Colors, Integer> entrance = g.getCurrentPlayer().getSchoolBoard().getEntrance();
        assertEquals(7, g.valueOfEnum(entrance));
        EnumMap<Colors, ArrayList<String>> movingStudents = new EnumMap<>(Colors.class);
        int countNumStudents = 3;
        for (Colors c : Colors.values()) {
            ArrayList<String> tmp = new ArrayList<>();
            movingStudents.put(c, tmp);
            for (int x = entrance.get(c); countNumStudents > 0 && x > 0; x--) {
                tmp = movingStudents.get(c);
                if (Math.random() > 0.5) {
                    tmp.add("dining");
                } else tmp.add("island4");
                movingStudents.put(c, tmp);
                countNumStudents--;
            }
        }
        g.moveStudents(g.getCurrentPlayer().getNickname(), movingStudents);
        assertEquals(4, g.valueOfEnum(entrance));
    }

    @Test
    void moveMotherNature() throws IncorrectArgumentException, AssistantCardNotFound,IncorrectStateException, IncorrectPlayerException, MotherNatureLostException, NegativeValueException, ProfessorNotFoundException {
        Game g = planningPhaseComplete();
        EnumMap<Colors, Integer> entrance = g.getCurrentPlayer().getSchoolBoard().getEntrance();
        EnumMap<Colors, ArrayList<String>> movingStudents = new EnumMap<>(Colors.class);
        int countNumStudents = 3;
        for (Colors c : Colors.values()) {
            ArrayList<String> tmp = new ArrayList<>();
            movingStudents.put(c, tmp);
            for (int x = entrance.get(c); countNumStudents > 0 && x > 0; x--) {
                tmp = movingStudents.get(c);
                if (Math.random() > 0.5) {
                    tmp.add("dining");
                } else tmp.add("island4");
                movingStudents.put(c, tmp);
                countNumStudents--;
            }
        }
        g.moveStudents(g.getCurrentPlayer().getNickname(), movingStudents);
        int oldPosMN = g.getMotherNaturePosition();
        g.moveMotherNature(g.getCurrentPlayer().getNickname(), 1);
        assertNotEquals(g.getMotherNaturePosition(), oldPosMN);
    }

    @Test
    void testTakeStudentsFromCloud() throws IncorrectArgumentException,AssistantCardNotFound, IncorrectPlayerException, IncorrectStateException, MotherNatureLostException, NegativeValueException, ProfessorNotFoundException {
        Game g = planningPhaseComplete();
        EnumMap<Colors, Integer> entrance = g.getCurrentPlayer().getSchoolBoard().getEntrance();
        EnumMap<Colors, ArrayList<String>> movingStudents = new EnumMap<>(Colors.class);
        int countNumStudents = 3;
        for (Colors c : Colors.values()) {
            ArrayList<String> tmp = new ArrayList<>();
            movingStudents.put(c, tmp);
            for (int x = entrance.get(c); countNumStudents > 0 && x > 0; x--) {
                tmp = movingStudents.get(c);
                if (Math.random() > 0.5) {
                    tmp.add("dining");
                } else tmp.add("island4");
                movingStudents.put(c, tmp);
                countNumStudents--;
            }
        }
        g.moveStudents(g.getCurrentPlayer().getNickname(), movingStudents);
        g.moveMotherNature(g.getCurrentPlayer().getNickname(), 1);
        EnumMap<Colors, Integer> s = g.getCloudTile(0).getStudents();
        g.takeStudentsFromCloud(g.getCurrentPlayer().getNickname(), 0);
        s = g.getCloudTile(0).getStudents();
        for (Colors c : Colors.values()) {
            assertEquals(s.get(c), 0);
        }
    }

    @Test
    void testNextRound() throws IncorrectArgumentException, IncorrectPlayerException,AssistantCardNotFound, IncorrectStateException, MotherNatureLostException, NegativeValueException, ProfessorNotFoundException {
        Game g = planningPhaseComplete();
        for (int playersTurn = 0; playersTurn < 4; playersTurn++) {
            EnumMap<Colors, Integer> entrance = g.getCurrentPlayer().getSchoolBoard().getEntrance();
            EnumMap<Colors, ArrayList<String>> movingStudents = new EnumMap<>(Colors.class);
            int countNumStudents = 3;
            for (Colors c : Colors.values()) {
                ArrayList<String> tmp = new ArrayList<>();
                movingStudents.put(c, tmp);
                for (int x = entrance.get(c); countNumStudents > 0 && x > 0; x--) {
                    tmp = movingStudents.get(c);
                    if (Math.random() > 0.5) {
                        tmp.add("dining");
                    } else tmp.add("island4");
                    movingStudents.put(c, tmp);
                    countNumStudents--;
                }
            }
            g.moveStudents(g.getCurrentPlayer().getNickname(), movingStudents);
            g.moveMotherNature(g.getCurrentPlayer().getNickname(), 1);
            EnumMap<Colors, Integer> s = g.getCloudTile(0).getStudents();
            g.takeStudentsFromCloud(g.getCurrentPlayer().getNickname(), playersTurn);

        }
        assertEquals(State.PLANNINGPHASE, g.getCurrentState());
    }

    @Test
    void testTakeStudentsFromCloudException() throws IncorrectArgumentException,AssistantCardNotFound, IncorrectPlayerException, IncorrectStateException, NegativeValueException {
        Game g = planningPhaseComplete();
        assertThrows(IncorrectStateException.class, () -> g.takeStudentsFromCloud("wrong player", 0));
    }


    @Test
    void testMoveTowersFromTeam() throws IncorrectArgumentException, NegativeValueException {
        Game game = initGame4players();
        ArrayList<Player> team = game.findPlayerFromTeam(Towers.WHITE);
        int initialTowers = -1;
        for (Player p : team) {
            if (initialTowers == -1) initialTowers = p.getPlayerTowers();
            else assertEquals(initialTowers, p.getPlayerTowers());
        }
        int endingTowers = -1;
        game.moveTowersFromTeam(team, -4);
        for (Player p : team) {
            if (endingTowers == -1) endingTowers = p.getPlayerTowers();
            else assertEquals(endingTowers, p.getPlayerTowers());
        }
        assertNotEquals(initialTowers, endingTowers);
    }

    @Test
    void checkAndPlaceProfessor() throws IncorrectArgumentException,AssistantCardNotFound, IncorrectPlayerException, IncorrectStateException, NegativeValueException, ProfessorNotFoundException {
        Game g = planningPhaseComplete();
        g.checkAndPlaceProfessor();
    }

    @Test
    void checkUnificationIslands() {

    }

    @Test
    void testIsGameOver() throws IncorrectArgumentException, NegativeValueException {
        Game game = initGame4players();
        assertFalse(game.isGameOver());
    }

    @Test
    void testCheckWinner() throws IncorrectArgumentException, NegativeValueException {
        Game game = initGame4players();
        assertNull(game.checkWinner());
    }


    @Test
    void testIsGameOver3players() throws IncorrectArgumentException, NegativeValueException {
        Game game = initGame3players();
        assertFalse(game.isGameOver());
    }
}