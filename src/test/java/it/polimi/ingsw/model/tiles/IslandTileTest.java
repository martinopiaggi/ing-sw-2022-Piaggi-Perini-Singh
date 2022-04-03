package it.polimi.ingsw.model.tiles;

import it.polimi.ingsw.model.enumerations.Colors;
import it.polimi.ingsw.model.enumerations.Towers;
import it.polimi.ingsw.model.exceptions.IncorrectArgumentException;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;

import static org.junit.jupiter.api.Assertions.*;

class IslandTileTest {

    @Test
    void testgetName() {
        String test = "Test";
        IslandTile island = new IslandTile("Test");

        assertEquals(test, island.getName());
    }

    @Test
    void testgetTowersColor() {
        IslandTile island = new IslandTile("Test");
        for (Towers color : Towers.values()) {
            island.setTowersColor(color);
            assertEquals(color, island.getTowersColor());
        }
    }

    @Test
    void testsetTowersColor() {

        IslandTile island = new IslandTile("Test");

        for (Towers color : Towers.values()) {
            island.setTowersColor(color);
            assertEquals(island.getTowersColor(), color);
        }
    }

    @Test
    void testgetStudents() {
        EnumMap<Colors, Integer> students = new EnumMap<>(Colors.class);
        int i = 0;
        for (Colors color : Colors.values()) {
            students.put(color, i);
            i++;
        }
        IslandTile island = new IslandTile("Test");
        try {
            island.addStudents(students);
        } catch (IncorrectArgumentException e) {
            e.printStackTrace();
        }
        assertEquals(students, island.getStudents());

    }

    @Test
    void testgetNumOfTowers() throws IncorrectArgumentException {
        int towers = 3;
        IslandTile island = new IslandTile("Test");
        island.sumTowers(towers);
        assertEquals(towers, island.getNumOfTowers());
        island.sumTowers(towers);
        towers += towers;
        assertEquals(towers, island.getNumOfTowers());


    }

    @Test
    void testaddStudents() {
        EnumMap<Colors, Integer> students = new EnumMap<>(Colors.class);
        int i = 0;
        for (Colors color : Colors.values()) {
            students.put(color, i);
            i++;
        }
        IslandTile island = new IslandTile("Test");
        try {
            island.addStudents(students);
        } catch (IncorrectArgumentException e) {
            e.printStackTrace();
        }
        assertEquals(students, island.getStudents());
    }

    @Test
    void testAddStudentsException() {
        EnumMap<Colors, Integer> students = new EnumMap<>(Colors.class);
        int i = -1;
        for (Colors color : Colors.values()) {
            students.put(color, i);

        }
        IslandTile tile = new IslandTile("Test");

        IncorrectArgumentException e = assertThrows(IncorrectArgumentException.class, () -> tile.addStudents(students));


        String expectedMessage = "EnumMap is not correct";
        String actualMessage = e.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));

    }

    @Test
    void testhasMotherNature() {

        IslandTile island = new IslandTile("Test");
        assertFalse(island.hasMotherNature());
        island.moveMotherNature();
        assertTrue(island.hasMotherNature());
    }

    @Test
    void testmoveMotherNature() {
        IslandTile island = new IslandTile("Test");
        assertFalse(island.hasMotherNature());
        island.moveMotherNature();
        assertTrue(island.hasMotherNature());
    }

    @Test
    void testremoveMotherNature() {
        IslandTile island = new IslandTile("Test");
        island.moveMotherNature();
        assertTrue(island.hasMotherNature());
        island.removeMotherNature();
        assertFalse(island.hasMotherNature());
    }
}