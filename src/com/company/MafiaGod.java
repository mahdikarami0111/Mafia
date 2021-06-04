package com.company;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MafiaGod {

    private ArrayList<PlayerHandler> players;

    public MafiaGod(ArrayList<Socket> clients){
        players = new ArrayList<>();
        distributeTest(clients);
        List.init(players);
    }

    public void  distribute(ArrayList<Socket> sockets,int count){
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
            sockets.remove(s);
        }
    }

    public void distributeTest(ArrayList<Socket> sockets){
        players.add(new PlayerHandler(sockets.get(0),Role.CITIZEN));
        PlayerHandler p = new Mafia(sockets.get(1));
        players.add(p);
        players.add(new Godfather(sockets.get(2)));
    }


    public void run() {
        ArrayList<Future<?>> done = new ArrayList<>();
        for(PlayerHandler p : players){
            done.add(p.intro());
        }
        while (!allDone(done)){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        done.clear();
        ((Mafia)players.get(1)).mafiaIntro();
        ((Godfather)players.get(2)).mafiaIntro();
        done.add(((Mafia)players.get(1)).kill());
        done.add(((Godfather)players.get(2)).kill());
        while (! allDone(done)){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            System.out.println(((PlayerHandler)done.get(1).get()).getName());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
//        System.out.println("starting chatting");
//        done.clear();
//        for(PlayerHandler p : players){
//            done.add(p.chat());
//        }
//        try {
//            Thread.sleep(15000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        for(Future<?> f : done){
//            System.out.println("cancelling tasks");
//            f.cancel(true);
//        }
    }

    public boolean allDone(ArrayList<Future<?>> futures){
        for (Future<?> f : futures){
            if(!f.isDone()){return false;}
        }
        return true;
    }
}
