package com.company;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

public class MafiaGod {

    public MafiaGod(ArrayList<Socket> clients){

    }

    public void distribute(ArrayList<Socket> sockets,int count){
        ArrayList<PlayerHandler> players = new ArrayList<>();
        Random r = new Random();
        int i = 0;
        int index = 0;
        for(i = 0 ; i<(count/3)-2 ; i++){
            index = r.nextInt(sockets.size()-1);
            PlayerHandler p = new PlayerHandler(sockets.get(index),Role.MAFIA);
            sockets.remove(index);
            players.add(p);
        }

        index = r.nextInt(sockets.size()-1);
        players.add(new PlayerHandler(sockets.get(index),Role.MAFIA_DOCTOR));
        sockets.remove(index);

        index = r.nextInt(sockets.size()-1);
        players.add(new PlayerHandler(sockets.get(index),Role.GODFATHER));
        sockets.remove(index);

        index = r.nextInt(sockets.size()-1);
        players.add(new PlayerHandler(sockets.get(index),Role.DETECTIVE));
        sockets.remove(index);

        index = r.nextInt(sockets.size()-1);
        players.add(new PlayerHandler(sockets.get(index),Role.DOCTOR));
        sockets.remove(index);

        index = r.nextInt(sockets.size()-1);
        players.add(new PlayerHandler(sockets.get(index),Role.SNIPER));
        sockets.remove(index);

        index = r.nextInt(sockets.size()-1);
        players.add(new PlayerHandler(sockets.get(index),Role.BULLETPROOF));
        sockets.remove(index);

        index = r.nextInt(sockets.size()-1);
        players.add(new PlayerHandler(sockets.get(index),Role.MAYOR));
        sockets.remove(index);

        index = r.nextInt(sockets.size()-1);
        players.add(new PlayerHandler(sockets.get(index),Role.PSYCHOLOGIST));
        sockets.remove(index);

        for(Socket s : sockets){
            players.add(new PlayerHandler(s,Role.CITIZEN));
        }

    }
}
