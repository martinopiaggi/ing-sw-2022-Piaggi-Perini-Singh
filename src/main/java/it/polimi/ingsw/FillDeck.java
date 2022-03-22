package it.polimi.ingsw;


import com.google.gson.stream.JsonReader;
import it.polimi.ingsw.model.FilePaths;
import it.polimi.ingsw.model.exceptions.GetPaths;
import netscape.javascript.JSObject;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import com.google.gson.*;

public class FillDeck {

private ArrayList<AssistantCard> deck;


   public void newDeck(String pathToJSON, ArrayList<AssistantCard> deck){


        this.deck= new ArrayList<AssistantCard>();
        loadFromJSON();

    }

    private void loadFromJSON() {

        Gson gson = new Gson();


        try {
            InputStreamReader streamReader = new InputStreamReader(FillDeck.class.getResourceAsStream(GetPaths.ASSISTANT_CARDS_LOCATION), StandardCharsets.UTF_8);
            JsonReader jsonReader = new JsonReader(streamReader);
            String fileContent = new String(Files.readAllBytes(Paths.get(GetPaths.ASSISTANT_CARDS_LOCATION)));
            deck = gson.fromJson(jsonReader, AssistantCard.class);

        } catch (Exception e) {
            e.printStackTrace();

        }


    }
    public ArrayList<AssistantCard> getDeck(){
        return deck;
    }









}
