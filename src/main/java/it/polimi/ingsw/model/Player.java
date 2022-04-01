package it.polimi.ingsw.model;

import it.polimi.ingsw.model.cards.AssistantCard;
import it.polimi.ingsw.model.cards.AssistantCardDeck;
import it.polimi.ingsw.model.enumerations.Students;
import it.polimi.ingsw.model.enumerations.Towers;
import it.polimi.ingsw.model.exceptions.IncorrectArgumentException;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

/**
 * @author Amrit
 */
public class Player implements Comparable<Player> {
    private final String nickname;
    private SchoolBoard schoolBoard;
    private final Towers towerColor;
    private AssistantCardDeck assistantCardDeck;
    private AssistantCard playedCard;
    private int characterCard;

    public Player(String nickname, Towers towerColors, int numberOfPlayers) {
        this.nickname = nickname;
        this.schoolBoard = new SchoolBoard(numberOfPlayers);
        this.towerColor = towerColors;

        assistantCardDeck = new AssistantCardDeck();
    }

    public void playAssistantCard(int index) {
        playedCard = assistantCardDeck.getAssistantCard(index);
        assistantCardDeck.getAssistantCard(index).setHasPlayed(true);
    }

    public boolean moveMotherNature(int distance) {
        return distance <= playedCard.getValue();
    }

    public void addStudents(EnumMap<Students, Integer> students) throws IncorrectArgumentException {
        schoolBoard.addStudents(students);
    }

    public void moveStudents(EnumMap<Students, Integer> students, ArrayList<Integer> destinations) throws IncorrectArgumentException {
        int i = 0;
        EnumMap<Students, Integer> studentsToMove = new EnumMap(Students.class);
        EnumMap<Students, Integer> studentsToRemove = new EnumMap(Students.class);

        if (schoolBoard.hasEnoughStudents(students)) {
            if (students.size() == destinations.size()) {
                for (Map.Entry<Students, Integer> set : students.entrySet()) {
                    if (destinations.get(i) != 1 && destinations.get(i) != 0) {
                        throw new IncorrectArgumentException();
                    } else {
                        if (destinations.get(i) == 0) {
                            studentsToMove.put(set.getKey(), set.getValue());
                        } else {
                            studentsToRemove.put(set.getKey(), set.getValue());
                        }
                        i++;
                    }
                }
            } else {
                throw new IncorrectArgumentException();
            }
        } else {
            throw new IncorrectArgumentException();
        }
        if (studentsToMove.size() != 0) schoolBoard.moveStudents(studentsToMove);
        if (studentsToRemove.size() != 0) schoolBoard.removeStudents(studentsToRemove);
    }

    public void addProfessor(Students student) throws IncorrectArgumentException {
        schoolBoard.addProfessor(student);
    }

    public void removeProfessor(Students student) throws IncorrectArgumentException {
        schoolBoard.removeProfessor(student);
    }

    @Override
    public int compareTo(Player otherPlayer) {
        return Integer.compare(otherPlayer.getPlayedCard().getValue(), playedCard.getValue());
    }

    public SchoolBoard getSchoolBoard() {
        return schoolBoard;
    }

    public Towers getTowerColor() {
        return towerColor;
    }

    public AssistantCard getPlayedCard() {
        return playedCard;
    }

    public int getPlayerTowers() {
        return schoolBoard.getTowers();
    }

    public int getNumOfStudent(Students student) throws IncorrectArgumentException {
        return schoolBoard.getStudentsByColor(student);
    }

    public void moveTowers(int num) {
        schoolBoard.moveTowers(num);
    }

    public void setCharacterCard(int characterCard) {
        this.characterCard = characterCard;
    }

    public int getCharacterCard() {
        return characterCard;
    }

    public boolean hasProfessorOfColor(Students student) throws IncorrectArgumentException {
        return schoolBoard.hasProfessorOfColor(student);
    }

    public String getNickname() {
        return nickname;
    }
}
