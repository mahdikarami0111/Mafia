package com.company;

import java.util.ArrayList;

public class List {
    private static List listInstance = null;
    private ArrayList<PlayerHandler> players;

    private List(ArrayList<PlayerHandler> players){
        this.players = players;
    }

    public static void init(ArrayList<PlayerHandler> players){
        if(listInstance == null){
            listInstance = new List(players);
        }
    }

    public static ArrayList<PlayerHandler> list(){
        return listInstance.players;
    }
}
