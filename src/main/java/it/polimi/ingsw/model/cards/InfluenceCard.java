package it.polimi.ingsw.model.cards;

import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.enumerations.Colors;

//This class of cards' power tampers in some way with the Influence calculations. CharacterIDs range from 5 to 8
public class InfluenceCard extends CharacterCard {

    int id = this.getCharacterID();

    @Override
    public void callPower(Player playercaller, int id) {

        int newprice = this.getPrice();
        newprice++;
        this.setPrice(newprice);
        //Updating the price of the ability, which always happens.

        switch (id) {

            case 5:
                IgnoreTowers(playercaller);
                break;

            case 6:
                AddTwo(playercaller);
                break;
            case 7:
                //TODO: Add way for user to input valid color
                Colors color = Colors.GREEN;
                System.out.println("Choose the color to ignore in the calculation");

                IgnoreStudent(playercaller, color);
                break;
            case 8:
                CallOnIsland(playercaller);

                break;
        }


    }

    //Every power call starts with a brief description of the power itself, the same the player saw when choosing it
    public void IgnoreTowers(Player playercaller) {
        System.out.println((this.getPowerDescription()));


    }

    public void AddTwo(Player playercaller) {
        System.out.println((this.getPowerDescription()));


    }

    public void IgnoreStudent(Player playercaller, Colors color) {
        System.out.println((this.getPowerDescription()));
    }

    //TODO: Add a way to select a specific island to call
    public void CallOnIsland(Player playercaller) {
        System.out.println((this.getPowerDescription()));

    }
}


