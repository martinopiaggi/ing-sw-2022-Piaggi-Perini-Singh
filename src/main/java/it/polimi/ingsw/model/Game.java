package it.polimi.ingsw.model;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import it.polimi.ingsw.model.cards.AssistantCard;
import it.polimi.ingsw.model.cards.CharacterCard;
import it.polimi.ingsw.model.enumerations.Colors;
import it.polimi.ingsw.model.enumerations.State;
import it.polimi.ingsw.model.enumerations.Towers;
import it.polimi.ingsw.model.exceptions.IncorrectArgumentException;
import it.polimi.ingsw.model.exceptions.IncorrectPlayerException;
import it.polimi.ingsw.model.exceptions.IncorrectStateException;
import it.polimi.ingsw.model.exceptions.MotherNatureLostException;
import it.polimi.ingsw.model.tiles.CloudTile;
import it.polimi.ingsw.model.tiles.IslandTile;


import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Game {
    private State state;
    private Bag bag;
    private boolean expertMode;
    private final int numOfPlayer;
    private Player currentPlayer;
    private int playerPlanPhase;
    private final ArrayList<Player> players;
    private PriorityQueue<Player> orderPlayers;
    private LinkedList<IslandTile> islands;
    private CloudTile[] clouds;
    private int motherNaturePosition;
    private int numRounds;
    private int numDrawnStudents;
    private int counterPlanningPhase;
    private boolean playerDrawnOut;
    private Player winner;
    private ArrayList<String> importingIslands;
    private ArrayList<String> importingClouds;
    private ArrayList<CharacterCard> listOfCharacters;
    private String jsoncontent;

    /**
     * Constructor it initializes everything following the rules of the game. It finishes initialize the first (random)
     * player of the Plan Phase, initializing the specific counters for the phase like 'counter' and 'playerPlanPhase'
     * @param expertMode
     * @param numOfPlayer
     * @param nicknames
     * @throws IncorrectArgumentException in case of bad arguments
     */
    public Game(boolean expertMode, int numOfPlayer, ArrayList<String> nicknames) throws IncorrectArgumentException {
        this.expertMode = expertMode;
        this.numOfPlayer = numOfPlayer;
        numRounds = 0;
        if (numOfPlayer == 3) numDrawnStudents = 4;
        else numDrawnStudents = 3;

        //Initialization Players
        players = new ArrayList<>();
        int indexColorTeam = 0;
        for (String nickname : nicknames) {
            int colorTeam;
            if (numOfPlayer == 3) {
                colorTeam = indexColorTeam;
            } else {
                if (indexColorTeam % 2 == 1) {
                    colorTeam = 0; //black tower
                } else {
                    colorTeam = 2; //white tower
                }
            }
            Player newPlayer = new Player(nickname, Towers.values()[colorTeam], numOfPlayer);
            players.add(newPlayer);
            indexColorTeam++;
        }

        importingTilesJson();

        //Initialization clouds
        clouds = new CloudTile[numOfPlayer];
        for (int i = 0; i < numOfPlayer; i++) {
            CloudTile cloud = new CloudTile(importingClouds.get(i)); //LORE:
            clouds[i] = cloud;
        }

        // initialization islands;
        islands = new LinkedList<>();
        for (int i = 0; i < 12; i++) {
            IslandTile island = new IslandTile(importingIslands.get(i));
            islands.add(island);
        }

        // place MotherNature on a random island
        motherNaturePosition = (int) (Math.random() * numOfPlayer);
        islands.get(motherNaturePosition).moveMotherNature();

        // create Bag and students
        EnumMap<Colors, Integer> students = new EnumMap(Colors.class);
        for (Colors studentColor : Colors.values()) {
            students.put(studentColor, 2);
        }
        bag = new Bag(students);

        //calculate opposite MotherNature's Island
        int oppositeMotherNaturePos = 0;
        if (motherNaturePosition >= islands.size() / 2)
            oppositeMotherNaturePos = motherNaturePosition - islands.size() / 2 + 1;
        else oppositeMotherNaturePos = motherNaturePosition + islands.size() / 2 - 1;
        // placing students except MotherNature's Island and the opposite one
        IslandTile islandOppositeMN = islands.get(oppositeMotherNaturePos);
        for (IslandTile island : islands) {
            if (!island.hasMotherNature() && !(island.getName().equals(islandOppositeMN.getName()))) {
                island.addStudents(bag.drawStudents(1));
            }
        }

        //Re-populate the Bag after 'placing Islands and Students phase'
        students = new EnumMap(Colors.class);
        for (Colors studentColor : Colors.values()) {
            students.put(studentColor, 24); //26  (total discStudents) -2 (used before) for each color
        }
        bag = new Bag(students);

        //initialization LinkedList<Player>
        playerPlanPhase = (int) (Math.random() * numOfPlayer - 1); //random init player
        counterPlanningPhase = numOfPlayer - 1; //used to 'count' during the Planning Phase
        playerDrawnOut = false; //used on drawBag and playAssistantCard
        state = State.PLANNINGPHASE;
        orderPlayers = new PriorityQueue<>(numOfPlayer);
        currentPlayer = players.get(playerPlanPhase);
        //playerPlanPhase++;
    }

    /**
     * Method use to import the textures (that we used as name id) of clouds and islands from the Json
     */
    public void importingTilesJson(){
        Gson gson = new Gson();

        //Loading IslandTiles Json file
        try {
            InputStreamReader streamReader = new InputStreamReader(Objects.requireNonNull(AssistantCard.class.getResourceAsStream(FilePaths.ISLAND_TILES_LOCATION)), StandardCharsets.UTF_8);
            Scanner s = new Scanner(streamReader).useDelimiter("\\A");
            jsoncontent = s.hasNext() ? s.next() : "";
        } catch (Exception FileNotFound) {
            FileNotFound.printStackTrace();
        }
        importingIslands = gson.fromJson(jsoncontent, new TypeToken<List<String>>() {
        }.getType());


        // initialization clouds;
        try { //Loading CloudTiles JSON file
            InputStreamReader streamReader = new InputStreamReader(Objects.requireNonNull(CloudTile.class.getResourceAsStream(FilePaths.CLOUD_TILES_LOCATION)), StandardCharsets.UTF_8);
            Scanner s = new Scanner(streamReader).useDelimiter("\\A");
            jsoncontent = s.hasNext() ? s.next() : "";
        } catch (Exception FileNotFound) {
            FileNotFound.printStackTrace();
        }
        importingClouds = gson.fromJson(jsoncontent, new TypeToken<List<String>>() {
        }.getType());
    }

    /**
     * the first action of the PlanPhase, it takes a randomize set of students from bag
     * numDrawnStudent is a variable that depends on the rules and it changes according to the number of
     * players. playerDrawnOut is a boolean variable that it's necessary to block possible calles from
     * the callerPlayer to the method 'playAssistantCard' without drawing from bag before. playerDrawnOut it's
     * initialized in the Game() constructor
     * @param nicknameCaller
     * @throws IncorrectArgumentException
     * @throws IncorrectPlayerException
     */
    public void drawFromBag(String nicknameCaller) throws IncorrectArgumentException, IncorrectPlayerException {
        if (state == State.PLANNINGPHASE && !playerDrawnOut) {
            if (nicknameCaller.equals(currentPlayer.getNickname())) {
                for (CloudTile cloud : clouds) {
                    cloud.addStudents(bag.drawStudents(numDrawnStudents));
                }
                playerDrawnOut = true;
            } else throw new IncorrectPlayerException();
        } else throw new IncorrectArgumentException();
    }

    /**
     * Action used to play a card. Notice that it calls 'nextPlayer() at the end and creates
     * the correct order of player for the ActionPhase. It is based using a PriorityQueue and
     * taking advantage of the comparable interface of Player
     * @param nicknameCaller
     * @param indexCard
     * @throws IncorrectPlayerException
     * @throws IncorrectStateException
     * @throws IncorrectArgumentException
     */
    public void playAssistantCard(String nicknameCaller, int indexCard) throws IncorrectPlayerException, IncorrectStateException, IncorrectArgumentException {
        if (state == State.PLANNINGPHASE) {
            if (nicknameCaller.equals(currentPlayer.getNickname()) && playerDrawnOut) {  //playerDrawnOut = player has drawn from bag
                currentPlayer.playAssistantCard(indexCard);

            } else {
                throw new IncorrectPlayerException();
            }
        } else {
            throw new IncorrectStateException();
        }
        orderPlayers.add(currentPlayer);
        nextPlayer();
    }

    /**
     * This is the function that correctly switch the player during the phases. It is called at the end
     * of the last action of each phase: so at the end of playAssistantCard for the Planning Phase and
     * at the end of takeStudentsFromCloud for the Action Phase. It also switch the state of the game.
     * @throws IncorrectStateException
     */
    public void nextPlayer() throws IncorrectStateException {
        if (state == State.PLANNINGPHASE) {
            if (counterPlanningPhase > 0) {
                counterPlanningPhase--;
                if (playerPlanPhase >= numOfPlayer - 1) {
                    playerPlanPhase = -1;
                }
                playerPlanPhase++;
                currentPlayer = players.get(playerPlanPhase);
                playerDrawnOut = false;
            } else {
                state = State.ACTIONPHASE;
                playerPlanPhase = players.indexOf(orderPlayers.peek());
                currentPlayer = players.get(playerPlanPhase);
            }
        } else if (state == State.ACTIONPHASE) {
            if (!orderPlayers.isEmpty()) currentPlayer = orderPlayers.poll();
            else {
                state = State.ENDTURN;
                nextRound();
            }
        } else {
            throw new IncorrectStateException();
        }
    }

    /**
     * Supporting method to nextPlayer() , it's call at the end of the turn of the last player of the action
     * phase. It checks if there is a gameOver and a winner, otherwise it starts the planning phase assigning
     * the correct currentPlayer, initializing the counter for the PlanningPhase and increasing the num of rounds
     * counter (that is one of the gameOver conditions.
     * @throws IncorrectStateException
     */
    public void nextRound() throws IncorrectStateException {
        if (state == State.ENDTURN) {
            if (isGameOver()) {
                state = State.END;
                checkWinner();
            } else {
                state = State.PLANNINGPHASE;
                numRounds++;
                currentPlayer = players.get(playerPlanPhase); //This is decided with the Assistant Card values and is assign in nextPlayer()
                counterPlanningPhase = numOfPlayer - 1;
            }
        } else throw new IncorrectStateException();
    }

    /**
     * It takes the students from a cloud and throw them to the entrance using a method of the player. It checks
     * that is the correct moment and the correct plaer to perform the action.
     * @param nicknameCaller
     * @param index
     * @throws IncorrectStateException
     * @throws IncorrectPlayerException
     * @throws IncorrectArgumentException
     */
    public void takeStudentsFromCloud(String nicknameCaller, int index) throws IncorrectStateException, IncorrectPlayerException, IncorrectArgumentException {
        if (state == State.ACTIONPHASE) {
            if (nicknameCaller.equals(currentPlayer.getNickname())) {
                currentPlayer.addStudents(clouds[index].drawStudents());
                nextPlayer();
            } else throw new IncorrectPlayerException();
        } else {
            throw new IncorrectStateException();
        }
    }

    /**
     * It sends the student to one of the destination: 0 for the dining room, 1 to the islands.
     * It makes a split of the students, checking which of them go to islands or to the Player that controls the dining room
     * In case the destination is the islands, it is used an array of islandDestination that is used by the game to send
     * the students in the correct place (the array uses the unique name of each island).
     * @param students
     * @param destinations
     * @param islandDestinations
     * @throws IncorrectArgumentException
     */
    public void moveStudents(EnumMap<Colors, Integer> students, ArrayList<Integer> destinations, ArrayList<String> islandDestinations) throws IncorrectArgumentException {
        int DestCounter = 0;
        EnumMap<Colors, Integer> studentsToMoveToIsland = new EnumMap<>(Colors.class);
        if (students.size() == destinations.size()) {
            for (Map.Entry<Colors, Integer> set : students.entrySet()) {
                if (destinations.get(DestCounter) == 1) {
                    studentsToMoveToIsland.put(set.getKey(), set.getValue());
                }
                DestCounter++;
            }
        } else {
            throw new IncorrectArgumentException();
        }
        //Sending ALL the students (including the islands ones) so that Player remove them from the entrance
        currentPlayer.moveStudents(students, destinations);

        if (!studentsToMoveToIsland.isEmpty() && studentsToMoveToIsland.size() == islandDestinations.size()) {
            boolean islandNameFound = true;
            for (String dest : islandDestinations) {
                if (islandNameFound) { //I check that the Island that I was searching in the last iteration it's found
                    islandNameFound = false;
                    for (IslandTile island : islands) {
                        if (island.getName().equals(dest)) {
                            islandNameFound = true;
                            island.addStudents(studentsToMoveToIsland);
                            break;
                        }
                    }
                } else {
                    throw new IncorrectArgumentException("The island is not found");
                }
            }
        }
        checkAndPlaceProfessor(); //maybe some students have arrived in the dining table
    }

    /**
     * It uses a method in player to check if the distance choosen by the player is legal. After the
     * control it moves the Mother Nature and it eventually moves the towers and unify islands.
     * @param distanceChoosen
     * @throws IncorrectArgumentException
     * @throws MotherNatureLostException
     */
    public void moveMotherNature(int distanceChoosen) throws IncorrectArgumentException, MotherNatureLostException {
        int destinationMotherNature = motherNaturePosition + distanceChoosen;
        if (islands.get(motherNaturePosition).hasMotherNature()) {
            if (currentPlayer.moveMotherNature(distanceChoosen)) {
                islands.get(motherNaturePosition).removeMotherNature();
                islands.get(destinationMotherNature).moveMotherNature();
                checkAndPlaceTower(islands.get(destinationMotherNature));
                checkUnificationIslands();

            } else {
                throw new IncorrectArgumentException();
            }
        } else {
            throw new MotherNatureLostException();
        }
    }

    /**
     * It is called if new students are added to the dining room and it checks if new professor are placed.
     * It is only called in moveStudents method in the Action Phase.
     * @throws IncorrectArgumentException
     */
    public void checkAndPlaceProfessor() throws IncorrectArgumentException {
        int max = 0;
        Player maxPlayer = null;
        for (Colors studentColor : Colors.values()) {
            for (Player player : players) {
                if (player.getNumOfStudent(studentColor) > max) {
                    maxPlayer = player;
                    max = player.getNumOfStudent(studentColor);
                } else if (player.getNumOfStudent(studentColor) == max) {
                    maxPlayer = null; //in case of ties noone should have assign the professor
                }
            }
            if (maxPlayer != null) {
                for (Player player : players) { //eventually remove all the players that had that professor
                    if (player.hasProfessorOfColor(studentColor)) player.removeProfessor(studentColor);
                }
                maxPlayer.addProfessor(studentColor);
            }
        }
    }

    /**
     * It computes the influence of each team on a given island. If it finds a team with more influence
     * than another assign the ownership and a new tower only if the island hasn't any owner or has an owner
     * different from the new team. It works with 2 players, 3 players and 4 players.
     * @param island
     * @throws IncorrectArgumentException
     */
    private void checkAndPlaceTower(IslandTile island) throws IncorrectArgumentException {
        HashMap<Towers, Integer> influenceScores = new HashMap<>();
        influenceScores.put(Towers.BLACK, 0);
        influenceScores.put(Towers.WHITE, 0);
        if (numOfPlayer % 2 == 1) influenceScores.put(Towers.GREY, 0);

        EnumMap<Colors, Integer> students = island.getStudents();

        for (Colors studentColor : Colors.values()) {
            if (students.get(studentColor) != 0) {
                for (Player p : players) {
                    if (p.hasProfessorOfColor(studentColor)) { //find the player with that professor
                        Towers teamColor = p.getTowerColor();
                        influenceScores.replace(teamColor, influenceScores.get(teamColor) + students.get(studentColor));
                        if (teamColor.equals(island.getTowersColor())) { //counting the towers if team owns the island
                            influenceScores.replace(teamColor, influenceScores.get(teamColor) + island.getNumOfTowers());
                        }
                    }
                }
            }
        }

        Towers newTeamOwner = null; //new owner
        int maxScore = 0;
        for (Map.Entry<Towers, Integer> team : influenceScores.entrySet()) {
            if (team.getValue() > maxScore) {
                newTeamOwner = team.getKey();
                maxScore = team.getValue();
            } else if (team.getValue() == maxScore) {
                newTeamOwner = null; // there is a TIE! -> no new Team owner
            }
        }

        if (newTeamOwner != null) {
            ArrayList<Player> newTeam = findPlayerFromTeam(newTeamOwner);
            if (island.getTowersColor() == null) {// The island was empty
                moveTowersFromTeam(newTeam, -island.getNumOfTowers());
            } else if (newTeamOwner != island.getTowersColor()) { //it means that there is a switch from team
                int switchedTowers = island.getNumOfTowers();
                moveTowersFromTeam(newTeam, -switchedTowers); //removing towers from new team player
                ArrayList<Player> oldTeam = findPlayerFromTeam(island.getTowersColor()); //oldTeamOwnerShip
                moveTowersFromTeam(oldTeam, switchedTowers); //adding towers to old team
                island.setTowersColor(newTeamOwner); //set ownership
            }
        }
    }

    public ArrayList<Player> findPlayerFromTeam(Towers teamColor) {
        Player firstPlayer = null;
        Player secondPlayer = null; // in case of 4 players I have to check both players of the team
        for (Player p : players) {
            if (p.getTowerColor() == teamColor) if (firstPlayer == null) {
                firstPlayer = p;
                if (numOfPlayer != 4) break;
            } else {
                secondPlayer = p;
            }
        }
        ArrayList<Player> returnedPlayers = new ArrayList<>();
        if (firstPlayer != null) returnedPlayers.add(firstPlayer);
        if (secondPlayer != null) returnedPlayers.add(secondPlayer);
        return returnedPlayers;
    }

    /**
     * It is used from checkAndPlaceTowers to add or remove towers from a team. The towers must be
     * removed from each player ONCE at time each. For example if playerA and playerB of same team have 3 and 4 towers
     * and 3 towers will be removed, it will leave this configuration: 2 and 2.
     * @param team
     * @param amount
     */
    public void moveTowersFromTeam(ArrayList<Player> team, int amount) {
        int numbersOfIterations = Math.abs(amount);
        int oneTowerSigned;
        if (amount < 0) oneTowerSigned = -1;
        else oneTowerSigned = 1;

        if (team.get(1) != null) { //It means we are 4 players game
            while (numbersOfIterations > 0) {
                if (oneTowerSigned > 0) { //this so that I always add/remove from the correct player
                    if (team.get(0).getPlayerTowers() <= team.get(1).getPlayerTowers())
                        team.get(0).moveTowers(oneTowerSigned);
                    else team.get(1).moveTowers(oneTowerSigned);
                } else {
                    if (team.get(0).getPlayerTowers() >= team.get(1).getPlayerTowers())
                        team.get(0).moveTowers(oneTowerSigned);
                    else team.get(1).moveTowers(oneTowerSigned);
                }
                numbersOfIterations--;
            }
        } else team.get(0).moveTowers(amount);
    }

    public void checkUnificationIslands() throws IncorrectArgumentException {
        boolean listChanged = false;
        ListIterator<IslandTile> it = islands.listIterator();
        IslandTile currentTile;
        IslandTile nextTile;
        while (it.hasNext()) {
            currentTile = it.next();
            if (it.hasNext()) nextTile = it.next();
            else nextTile = islands.getFirst();
            if (nextTile.getTowersColor().equals(currentTile.getTowersColor())) {
                currentTile.addStudents(nextTile.getStudents());
                currentTile.sumTowers(nextTile.getNumOfTowers());
                islands.remove(nextTile);
                listChanged = true;
            }
        }
        if (listChanged) checkUnificationIslands();
    }

    /**
     * Check the game over returning true if it is
     * @return
     */
    public boolean isGameOver() {
        for (Player p : players) {
            if (p.getPlayerTowers() <= 0) return true;
        }
        if (!bag.hasEnoughStudents(numDrawnStudents) || islands.size() <= 3 || numRounds >= 9 || checkWinner() != null)
            return true;
        else return false;
    }

    /**
     * It checks the winner and it is used as supporting method inside isGameOver() . It return the
     * winning team using an arrayList of Player (that could have size of 1 or 2 depending of numOfPLayers).
     * If it return a null team there isn't a winning team.
     * @return
     */
    public ArrayList<Player> checkWinner() {
        ArrayList<Player> team1 = findPlayerFromTeam(Towers.WHITE);
        ArrayList<Player> team2 = findPlayerFromTeam(Towers.BLACK);
        ArrayList<ArrayList<Player>> teams = new ArrayList<>();
        teams.add(team1);
        teams.add(team2);
        if (numOfPlayer % 2 == 1) {
            ArrayList<Player> team3 = findPlayerFromTeam(Towers.GREY);
            teams.add(team3);
        }

        for (ArrayList<Player> team : teams) {
            boolean teamWin = false;
            for (Player p : team) {
                if (p.getPlayerTowers() == 0) {
                    teamWin = true;
                } else {
                    teamWin = false;
                }
            }
            if (teamWin) return team;
        }

        return null;  //no win
    }

    public ArrayList<String> getimportingIslands() {
        return importingIslands;
    }

    public ArrayList<String> getImportingClouds() {
        return importingClouds;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public State getCurrentState() {
        return state;
    }

    public boolean getPlayerDrawnOut() {
        return playerDrawnOut;
    }

    public int getPlayerPlanPhase() {
        return playerPlanPhase;
    }

    public CloudTile getCloudTile(int index) {
        return clouds[index];
    }
}
