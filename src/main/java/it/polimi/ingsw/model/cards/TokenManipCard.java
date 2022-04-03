package it.polimi.ingsw.model.cards;

import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.enumerations.Colors;

//This class of cards' power manipulates student tokens and professor behaviour connected to them. CharacterIDs range from 9 to 11
public class TokenManipCard extends CharacterCard {

    public TokenManipCard(int characterID, int startingPrice, int price, String powerDescription, int id) {
        super(characterID, startingPrice, price, powerDescription);
    }

    @Override
    public void callPower(Player playercaller, int id) {

        int newprice = this.getPrice();
        newprice++;
        this.setPrice(newprice);
        //Updating the price of the ability, which always happens.

        if (id == 9) {
            Colors student = Colors.GREEN;
            System.out.println("Choose the color to ignore in the calculation");
            DiscardOpponentStudents(student, playercaller);
        } else if (id == 10) {
            SwitchOwnStudents(playercaller);
        } else if (id == 11) {
            TieBreaker(playercaller);
        } else {
            throw new java.lang.Error("Card Mismatch");
        }
    }

    public void DiscardOpponentStudents(Colors student, Player playercaller) {
        System.out.println((this.getPowerDescription()));
    }

    public void SwitchOwnStudents(Player playercaller) {
        System.out.println((this.getPowerDescription()));
    }

    public void TieBreaker(Player playercaller) {
        System.out.println((this.getPowerDescription()));
    }
}
