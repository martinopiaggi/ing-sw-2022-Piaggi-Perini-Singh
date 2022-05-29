package it.polimi.ingsw.client;

import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.model.cards.assistantcard.AssistantCard;
import it.polimi.ingsw.model.deck.assistantcard.AssistantCardDeck;
import it.polimi.ingsw.model.enumerations.Colors;
import it.polimi.ingsw.model.stripped.StrippedBoard;
import it.polimi.ingsw.model.stripped.StrippedCharacter;
import it.polimi.ingsw.model.stripped.StrippedIsland;
import it.polimi.ingsw.model.stripped.StrippedModel;
import it.polimi.ingsw.server.commands.*;
import java.rmi.RemoteException;
import java.util.*;


public class ViewCLI {
    Client client;
    boolean hasGameStarted = false;
    boolean isMyTurn;
    String nickName;
    int playerNumber;
    String clientRoom = null;
    int action;
    int turnMoves;
    MoveMotherNature moveMotherNatureOrder;
    MoveStudents moveStudentsOrder;
    PickCloud pickCloudOrder;
    PlayAssistantCard playAssistantCardOrder;
    PlayCharacterCardA playCharacterCardAOrder;
    PlayCharacterCardB playCharacterCardBOrder;
    PlayCharacterCardC playCharacterCardCOrder;
    PlayCharacterCardD playCharacterCardDOrder;
    private final Scanner in = new Scanner(System.in);

    public ViewCLI(Client client) {
        this.client = client;
    }

    public void Start() throws RemoteException, UserNotInRoomException, NotLeaderRoomException, NotEnoughCoinsException, AssistantCardNotFoundException, NegativeValueException, IncorrectStateException, MotherNatureLostException, ProfessorNotFoundException, IncorrectPlayerException, IncorrectArgumentException, UserNotRegisteredException {

        System.out.println("Welcome to the lobby!\nWhat's your name?");
        while (true) {
            try {
                nickName = in.nextLine();
                client.registerClient(nickName);
                break;
            } catch (UserAlreadyExistsException e) {
                System.out.println("That username is already in the game! Try another.\n");
            }
        }
        System.out.println("Possible options: \n JOIN to join a room; \n CREATE to create a new room;\n ROOMS to list rooms;" +
                "\n PLAYERS to list players in current lobby; \n INFO to view your current room's information;\n CHANGE to toggle expert mode for the current lobby;\n " +
                "LEAVE to leave current lobby;\n" +
                "HELP to see this message again.\n" +
                "When you're ready to go and everyone is in the lobby type START to start the game!\n");
        //Main room loop
        while (!client.isInGame()) {

            //codice della lobby
            String command = in.nextLine().toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
            switch (command) {
                case "join":
                    requestRoomJoin(); //fatto
                    break;
                case "create":
                    requestRoomCreation(); //fatto
                    break;
                case "players":
                    getPlayersInRoom();//fatto
                    break;
                case "rooms":
                    getRooms();//fatto
                    break;
                case "info":
                    getLobbyInfo();//fatto
                    break;
                case "change":
                    setExpertMode();// TODO exception
                    break;
                case "leave":
                    leaveRoom();//fatto
                    break;
                case "start":
                    startGame();
                    break;
                case "help":
                    System.out.println("Possible options: \n JOIN to join a room; \n CREATE to create a new room;\n ROOMS to list rooms;" +
                            "\n PLAYERS to list players in current lobby; \n INFO to view your current room's information;\n " +
                            "CHANGE to toggle expert mode for the current lobby;\n " +
                            "LEAVE to leave current lobby;\n" +
                            "HELP to see this message again.\n" +
                            "When you're ready to go and everyone is in the lobby type START to start the game!\n");
                    break;
                default:
                    System.out.println("Command not recognized");
                    break;
            }
        }
        //Main game loop
        while (client.isInGame()) {

            //First comes the assistant card thingie
            System.out.println("These are your available assistant cards:\n");
            printAssistantCards();
            System.out.println("Wait for your turn then play one! Remember, you can't play a card that someone else this turn has already played.\n");

            //Not my turn

            //My turn
            while(true)
            {
                performActionInTurn();

            }
            //Not my turn
           /* while(true)
            {
                System.out.println("Uhhhh print some stuff I suppose");
            }*/

        }
    }

    //Room methods

    private void startGame() throws RemoteException {
        try {
            client.startGame();
        } catch (UserNotInRoomException e) {
            System.out.println("You're not in a room yet!\n");
        } catch (NotLeaderRoomException e) {
            System.out.println("You're not the leader of this room you can't start the game!\n");
        } catch (RoomNotExistsException | UserNotRegisteredException e) {
            throw new RuntimeException(e);
        }
    }

    private void getPlayersInRoom() throws RemoteException {
        if (clientRoom != null) {
            ArrayList<String> response;
            try {
                response = client.getNicknamesInRoom(clientRoom);
            } catch (RoomNotExistsException e) {
                throw new RuntimeException(e);
            }
            sendArrayString(response);
        } else
            System.out.println("You're not in a room, so there are no players to show\n");
    }

    private void getLobbyInfo() throws RemoteException {
        if (clientRoom != null) {
            ArrayList<String> result;
            try {
                result = client.requestLobbyInfo(clientRoom);
            } catch (RoomNotExistsException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Lobby name: " + result.get(0));
            System.out.println("Leader: " + result.get(1));
            System.out.println("Expert mode: " + result.get(2));
        } else
            System.out.println("You're not in a room yet\n");
    }

    private void leaveRoom() throws RemoteException {
        try {
            client.leaveRoom();
        } catch (UserNotInRoomException e) {
            System.out.println("You're not in a room yet\n");
        } catch (UserNotRegisteredException e) {
            throw new RuntimeException(e);
        }
    }

    public void getRooms() throws RemoteException {
        ArrayList<String> response = client.getRooms();
        if (response.isEmpty())
            System.out.println("There are no rooms yet\n");
        else
            sendArrayString(response);
    }

    public void setExpertMode() throws RemoteException, UserNotInRoomException, NotLeaderRoomException {
        boolean result = false;
        System.out.println("Do you want to play in expert mode? Y/N");
        String answer;
        answer = in.nextLine().toLowerCase(Locale.ROOT);
        switch (answer) {
            case "y": {
                result = true;
                break;
            }
            case "n": {
                result = false;
                break;
            }
            default:
                System.out.println("Command not recognized\n");
        }
        try {
            client.setExpertMode(result);
            if (result)
                System.out.println("Expert mode enabled!\n");
            else
                System.out.println("Expert mode disabled|\n");
            //TODO exception
        } catch (UserNotInRoomException e) {
            System.out.println("You're not in a room now!\n");
        } catch (NotLeaderRoomException e) {
            System.out.println("You're not this lobby's leader, you can't do that!\n");
        } catch (UserNotRegisteredException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void requestRoomCreation() throws RemoteException {
        System.out.println("Insert room name: \n");
        String nameRoom;
        nameRoom = in.nextLine();
        while (client.getRooms().contains(nameRoom)) {
            System.out.println("Ops, there is another room with the same name! Choose another one please. \n");
            nameRoom = in.nextLine();
        }
        try {
            client.createRoom(nameRoom);
        } catch (UserNotRegisteredException | RoomAlreadyExistsException e) {
            throw new RuntimeException(e);
        }
        clientRoom = nameRoom;
    }

    public synchronized void requestRoomJoin() throws RemoteException {
        String requestedRoom;
        System.out.println("Select the room: \n");
        if (client.getRooms().isEmpty()) System.out.println("There are no rooms, you can only create a new one");
        else {
            sendArrayString(client.getRooms());
            requestedRoom = in.nextLine().trim();
            while (!client.getRooms().contains(requestedRoom)) {
                System.out.println("Ops, there are no rooms with that name: try again. If you want to exit instead type EXIT.\n");
                requestedRoom = in.nextLine();
                if (requestedRoom.toLowerCase(Locale.ROOT).trim().equals("exit")) {
                    System.out.println("Gotcha, leaving room join!\n");
                    return;
                }
            }
            if (requestedRoom.equals(clientRoom)) {
                System.out.println("You're already in that room!\n");
            } else {
                try {
                    if (clientRoom==null)
                        leaveRoom();
                    client.requestRoomJoin(requestedRoom);
                    clientRoom=requestedRoom;
                } catch (RoomNotExistsException | UserNotRegisteredException e) {
                    throw new RuntimeException(e);
                }
                clientRoom = requestedRoom;
                System.out.println("You entered room " + clientRoom + " successfully \n");
                System.out.println("Players in this room:");
                try {
                    sendArrayString(client.getNicknamesInRoom(clientRoom));
                } catch (RoomNotExistsException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void printCommandHelp() {
        System.out.println("The commands available are the following:\n" +
                "Press 1 to view everyone's boards\n" +
                "Press 2 to view every player's name\n" +
                "Press 3 to view all the islands\n" +
                "Press 4 to move students across the islands and the dining room\n" +
                "Press 5 to move mother nature. This will end your turn\n" +
                "Press 6 to see the character cards in the game\n" +
                "Press 7 to play a character card\n" +
                "Press 8 to view this message again\n");
    }

    //Game methods

    public synchronized void playAssistantCard() throws NotEnoughCoinsException, AssistantCardNotFoundException, NegativeValueException, IncorrectStateException, MotherNatureLostException, ProfessorNotFoundException, IncorrectPlayerException, RemoteException, IncorrectArgumentException {
        System.out.println("It's your turn! Pick an assistant card to play. \n");
        printAssistantCards();
        int i = in.nextInt();
        while (i < 0 || i > client.getLocalModel().getAssistantDecks().size()) {
            System.out.println("Invalid number, try again\n");
            i = in.nextInt();
        }
        playAssistantCardOrder = new PlayAssistantCard(nickName, "Assistente(" + i + ")");
        try {
            client.performGameAction(playAssistantCardOrder);
        } catch (UserNotInRoomException | UserNotRegisteredException e) {
            throw new RuntimeException(e);
        }
    }


    public void printAssistantCards() {
        AssistantCardDeck myDeck = client.getLocalModel().getAssistantDecks().get(playerNumber);
        int i = 0;
        for (AssistantCard a : myDeck.getDeck()) {
            System.out.println("Card number " + i +" "+ a.getImageName().replaceAll("[^a-zA Z0-9]", "") +" " + a.getMove());
            i++;
        }
    }

    public void performActionInTurn() throws NotEnoughCoinsException, AssistantCardNotFoundException, NegativeValueException, IncorrectStateException, MotherNatureLostException, ProfessorNotFoundException, IncorrectPlayerException, RemoteException, IncorrectArgumentException, UserNotInRoomException, UserNotRegisteredException {
        do {
            printCommandHelp();
            System.out.println("Select an action: ");
            String s = in.nextLine();
            action = Integer.parseInt(s);
        } while (action < 1 || action > 7);
        switch (action) {
            case 1:
                printPlayerBoards();
                break;
            case 2:
                printPlayerNames();
                break;
            case 3:
                printIslands();
                break;
            case 4:
                moveStudents();
                break;
            case 5:
                moveMN();
                break;
            case 6:
                printCharacterCards();
                break;
            case 7:
                playCharacterCard();
                break;
            case 8:
                printCommandHelp();
                break;
            default:
                //TODO: add exception
        }

    }

    public void printCharacterCards()
    {
        int i=0;
        ArrayList<StrippedCharacter> temp= client.getLocalModel().getCharacters();
        for (StrippedCharacter c : temp) {
            System.out.println("Character " + i);
            System.out.println("Price: " + c.getPrice() + ", description:  " + c.getDescription());
            i++;
        }
    }

    public void playCharacterCard() throws NotEnoughCoinsException, AssistantCardNotFoundException, NegativeValueException, IncorrectStateException, MotherNatureLostException, ProfessorNotFoundException, IncorrectPlayerException, RemoteException, IncorrectArgumentException {
        StrippedCharacter tmp;
        System.out.println("Select the character you want to play! You currently have " + client.getLocalModel().getBoards().get(playerNumber).getCoins() + " coins \n");
        printCharacterCards();
        int i = in.nextInt();
        while (i < 0 || i > 2) {
            System.out.println("That's not right! Try again\n");
            i = in.nextInt();
        }
        tmp = client.getLocalModel().getCharacters().get(i);
        switch (tmp.getRequirements().getRequirements().toLowerCase(Locale.ROOT)) {
            //TODO: test if this actually works as intended
            case "islands":
                playCharacterB(i);
                break;
            case "colors,islands":
                playCharacterC(i, tmp);
                break;
            default:
                if (tmp.getRequirements().getRequirements().equals(""))
                    //Automatic action card
                    playCharacterA(i);
                else
                    //No resource used but input needed card
                    playCharacterD(i);
                break;
        }
    }

    public void playCharacterA(int id) throws NotEnoughCoinsException, AssistantCardNotFoundException, NegativeValueException, IncorrectStateException, MotherNatureLostException, ProfessorNotFoundException, IncorrectPlayerException, RemoteException, IncorrectArgumentException {
        System.out.println("You have chosen a no parameter character! Buckle up, the effects are on the way!\n");
        playCharacterCardAOrder = new PlayCharacterCardA(nickName, id);
        try {
            client.performGameAction(playCharacterCardAOrder);
        } catch (UserNotInRoomException | UserNotRegisteredException e) {
            throw new RuntimeException(e);
        }
    }

    public void playCharacterB(int id) throws NotEnoughCoinsException, AssistantCardNotFoundException, NegativeValueException, IncorrectStateException, MotherNatureLostException, ProfessorNotFoundException, IncorrectPlayerException, RemoteException, IncorrectArgumentException {
        System.out.println("You have chosen a student island card\n");
        int students = 0, island = 0;
        System.out.println(client.getLocalModel().getCharacters().get(id).getDescription());
        playCharacterCardBOrder = new PlayCharacterCardB(nickName, id, students, island);
        try {
            client.performGameAction(playCharacterCardBOrder);
        } catch (UserNotInRoomException | UserNotRegisteredException e) {
            throw new RuntimeException(e);
        }
    }

    public void playCharacterC(int id, StrippedCharacter card) throws NotEnoughCoinsException, AssistantCardNotFoundException, NegativeValueException, IncorrectStateException, MotherNatureLostException, ProfessorNotFoundException, IncorrectPlayerException, RemoteException, IncorrectArgumentException {
        System.out.println("You have chosen a card that requires two sets of students\n");
        System.out.println("These are the students on your card: \n");

        EnumMap<Colors, Integer> students1 = null, students2 = null;
        //TODO: add students on card implementation for StrippedCharacters
        for (Colors c : Colors.values()) {


        }
        playCharacterCardCOrder = new PlayCharacterCardC(nickName, id, students1, students2);
        try {
            client.performGameAction(playCharacterCardCOrder);
        } catch (UserNotInRoomException | UserNotRegisteredException e) {
            throw new RuntimeException(e);
        }
    }

    public void playCharacterD(int id) throws NotEnoughCoinsException, AssistantCardNotFoundException, NegativeValueException, IncorrectStateException, MotherNatureLostException, ProfessorNotFoundException, IncorrectPlayerException, RemoteException, IncorrectArgumentException {
        System.out.println("You have to make a choice\n");
        //There are two cards that only need a color to work, and the effects then take place
        //Globally.
        playCharacterCardDOrder = new PlayCharacterCardD(nickName, id, 0);
        try {
            client.performGameAction(playAssistantCardOrder);
        } catch (UserNotInRoomException | UserNotRegisteredException e) {
            throw new RuntimeException(e);
        }
    }

    public void moveMN() {
        System.out.println("Input the number of steps you want Mother Nature to move!\n ");
        int input = in.nextInt();
        while (input < 0 || input > turnMoves) {
            System.out.println("That number is not right! Try again.\n");
            input = in.nextInt();
        }
        //We now have a valid move for Mother Nature
    }

    public void moveStudents() throws NotEnoughCoinsException, AssistantCardNotFoundException, UserNotInRoomException, NegativeValueException, IncorrectStateException, MotherNatureLostException, ProfessorNotFoundException, UserNotRegisteredException, IncorrectPlayerException, RemoteException, IncorrectArgumentException {
        StrippedBoard myBoard;
        int i = 0;
        while (!client.getLocalModel().getBoards().get(i).getOwner().equals(nickName)) {
            i++;
        }
        myBoard = client.getLocalModel().getBoards().get(i);
        System.out.println("These are the students in your entrance: \n");
        System.out.println("\nEntrance configuration: ");
        for (Colors c : myBoard.getEntrance().keySet()) {
            System.out.println(c + " students: " + myBoard.getEntrance().get(c) + "\n");
        }

        String answer;
        String[] parts;
        String color;
        int value;
        int island;
        int movedStudents = 0;
        boolean isValidInputYN = false;
        boolean doItAgain;
        EnumMap<Colors, Integer> studentsToMove = null;
        ArrayList<StrippedIsland> myIslands = client.getLocalModel().getIslands();
        System.out.println("Do you want to move students to the dining room? Y\\N\n");
        do {
            answer = in.nextLine();
            answer = answer.toLowerCase(Locale.ROOT);
            if (answer.equals("y") || answer.equals("n"))
                isValidInputYN = true;
            else
                System.out.println("Whoops! That's not right. Try again: \n");
        } while (!isValidInputYN);

        //Move students to the dining room
        doItAgain = true;
        isValidInputYN = false;
        if (answer.equals("y")) {
            do {
                System.out.println("Type the students you want to move to the dining room as \"color, number\"");
                answer = in.nextLine();
                parts = answer.split(" ");
                color = parts[0];
                color = color.replaceAll("[^a-zA Z0-9]", "");
                value = Integer.parseInt(parts[1]);
                color = color.toUpperCase(Locale.ROOT);
                if (isValidColor(color)) {
                    if (myBoard.getEntrance().get(stringToColor(color)) <= value) {
                        studentsToMove = myBoard.getDining();
                        studentsToMove.put(stringToColor(color), studentsToMove.get(stringToColor(color)) + value);
                        movedStudents += value;

                        System.out.println("Do you want to move other students to the dining room?\n");
                        do {
                            answer = in.nextLine();
                            answer = answer.toLowerCase(Locale.ROOT);
                            if (answer.equals("y") || answer.equals("n"))
                                isValidInputYN = true;
                            else
                                System.out.println("Whoops! That's not right. Try again: \n");
                        } while (!isValidInputYN && movedStudents < 3);
                        //Since a player can only move 3 students in a turn there needs to be a check here too
                        if (answer.equals(("n"))) {
                            doItAgain = false;
                            myBoard.setDining(studentsToMove);
                        }
                    } else
                        System.out.println("You don't have enough students of that color! Try again.\n");
                } else
                    System.out.println("There is no such color as " + color + "! Try again. \n");
            } while (doItAgain && movedStudents < 3);
        }
        //End of dining room move segment
        moveStudentsOrder = new MoveStudents(nickName, strippedToDining(studentsToMove));
        client.performGameAction(moveStudentsOrder);
        //Move students to the islands if the player has moved less than 3 students already
        if (movedStudents < 3) {
            do {
                System.out.println("Type the students you want to move to the island as \"color, number, number of island\" (for example, \"RED, 1, 5)\"");
                answer = in.nextLine();
                parts = answer.split(" ");
                color = parts[0];
                color = color.replaceAll("[^a-zA Z0-9]", "");
                value = Integer.parseInt(parts[1]);
                island = Integer.parseInt(parts[2]);
                color = color.toUpperCase(Locale.ROOT);

                studentsToMove = myIslands.get(island).getStudents();

                if (isValidColor(color)) {
                    if (myBoard.getEntrance().get(stringToColor(color)) <= value) {
                        if (island > 0 && island < client.getLocalModel().getIslands().size()) {

                            studentsToMove.put(stringToColor(color), myIslands.get(island).getStudents().get(stringToColor(color)) + value);
                            movedStudents += value;

                            System.out.println("Do you want to move other students to the islands?\n");
                            do {
                                answer = in.nextLine();
                                answer = answer.toLowerCase(Locale.ROOT);
                                if (answer.equals("y") || answer.equals("n"))
                                    isValidInputYN = true;
                                else
                                    System.out.println("Whoops! That's not right. Try again: \n");
                            } while (!isValidInputYN && movedStudents < 3);
                            if (answer.equals("n") && movedStudents == 3) {
                                doItAgain = false;
                            } else
                                System.out.println("You still have " + (3 - movedStudents) + " students to move!\n");

                        } else
                            System.out.println("Invalid island number! Try again.\n");
                    } else
                        System.out.println("You don't have enough students of that color! Try again.\n");
                } else
                    System.out.println("There is no such color as " + color + "! Try again. \n");
            } while (doItAgain);
            moveStudentsOrder = new MoveStudents(nickName, strippedToDining(studentsToMove));
            client.performGameAction(moveStudentsOrder);
        } else
            System.out.println("You already moved three students this turn\n");
    }

//End of MoveStudents

    public void printPlayerBoards() {
        ArrayList<StrippedBoard> boards = client.getLocalModel().getBoards();
        System.out.println("Player boards:\n");
        for (StrippedBoard s : boards) {
            System.out.println(s.getOwner() + "'s board: ");
            System.out.println("Coins: " + s.getCoins());
            System.out.println("\nDining room configuration: ");
            for (Colors c : s.getDining().keySet()) {
                System.out.println(c + " students: " + s.getDining().get(c));
            }
            System.out.println("\nEntrance configuration: ");
            for (Colors c : s.getEntrance().keySet()) {
                System.out.println(c + " students: " + s.getEntrance().get(c) + "\n");
            }
            System.out.println("\nNumber of towers: " + s.getNumberOfTowers());
            System.out.println("\nProfessors table: ");
            for (Colors c : s.getProfessorsTable()) {
                System.out.println(c + "\n");
            }
        }
    }

    public void printPlayerBoard(String playerNickname) {
        ArrayList<StrippedBoard> boards = client.getLocalModel().getBoards();

        int i = 0;
        while (!client.getLocalModel().getBoards().get(i).getOwner().equals(playerNickname)) {
            i++;
        }
        StrippedBoard s = client.getLocalModel().getBoards().get(i);
        System.out.println(s.getOwner() + "'s board: ");
        System.out.println("Coins: " + s.getCoins());
        System.out.println("\nDining room configuration: ");
        for (Colors c : s.getDining().keySet()) {
            System.out.println(c + " students: " + s.getDining().get(c));
        }
        System.out.println("\nEntrance configuration: ");
        for (Colors c : s.getEntrance().keySet()) {
            System.out.println(c + " students: " + s.getEntrance().get(c) + "\n");
        }
        System.out.println("\nNumber of towers: " + s.getNumberOfTowers());
        System.out.println("\nProfessors table: ");
        for (Colors c : s.getProfessorsTable()) {
            System.out.println(c + "\n");
        }

        System.out.println("Player boards:\n");
    }

    public void printPlayerNames() {

        for (StrippedBoard board : client.getLocalModel().getBoards()) {
            System.out.println(board.getOwner() + "\n");
        }
    }

    public void printIslands() {
        ArrayList<StrippedIsland> islands = client.getLocalModel().getIslands();

        for (StrippedIsland island : islands) {
            System.out.println("Island name: " + island.getName() + "\n");
            System.out.println("Number of towers: " + island.getNumOfTowers() + "\n");
            System.out.println("Has no entry tile: " + island.isHasNoEnterTile() + "\n");
            System.out.println("Students on the island: ");
            for (Colors c : island.getStudents().keySet()) {
                System.out.println(c + " students: " + island.getStudents() + "\n");
            }
            System.out.println("Towers: " + island.getNumOfTowers() + island.getTowersColor() + "towers \n");
        }
    }

    public boolean isValidColor(String input) {
        input = input.toUpperCase(Locale.ROOT);
        for (Colors c : Colors.values()) {
            if (c.name().equals(input))
                return true;
        }
        return false;
    }

    public Colors stringToColor(String input) {
        input = input.toLowerCase(Locale.ROOT);
        switch (input) {
            case "red":
                return Colors.RED;
            case "blue":
                return Colors.BLUE;
            case "yellow":
                return Colors.YELLOW;
            case "green":
                return Colors.GREEN;
            case "pink":
                return Colors.PINK;
            default:
        }
        return Colors.BLUE;
    }

    public EnumMap<Colors, ArrayList<String>> strippedToDining(EnumMap<Colors, Integer> students) {
        EnumMap<Colors, ArrayList<String>> returnedStudents = null;
        ArrayList<String> destinations = new ArrayList<>();
        for (Colors c : students.keySet()) {
            //I have to count the number of students moved in the stripped class and build myself an enummap which Game can understand
            int i = students.get(c);
            while (i > 0) {
                destinations.add("dining");
                returnedStudents.put(c, destinations);
                //TODO: this doesn't really work, have to come back to this
            }

        }

        return returnedStudents;

    }

    private synchronized void sendArrayString(ArrayList<String> messageArray) {
        for (String message : messageArray) System.out.println(message);
    }
}
