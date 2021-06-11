package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * a class for bulletproof role in mafia game inherits from PlayerHandler also has some role specific features
 */
public class Mafia extends PlayerHandler{

    /**
     * constructor for the class calls superclass constructor witha specified role
     * @param s Socket  player's socket
     */
    public Mafia(Socket s){
        super(s,Role.MAFIA);

    }

    /**
     * starts a thread running the kill task for mafia player
     * @return Future  a future object to keep track of the task state
     */
    public Future<?> kill(){
        Runnable kill = new Runnable() {
            @Override
            public void run() {
                try {
                    printAlive();
                    getPrintWriter().println("[Server] who do you want to kill ?");
                    String s;
                    while (true){
                        s = getBufferReader().readLine();
                        if(getPlayer(s) != null && getPlayer(s).getState().status == Status.ALIVE &&
                                getPlayer(s).getState().role != Role.GODFATHER &&
                                getPlayer(s).getState().role != Role.MAFIA &&
                                getPlayer(s).getState().role != Role.MAFIA_DOCTOR){
                            break;
                        }
                        getPrintWriter().println("[Server] invalid name try again");
                    }
                    getPrintWriter().println("[Server] Your recommendation has been sent to Godfather");
                    String m = getName() + " suggests killing "+s;
                    sendMessage(m,getGodfather());
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        };
        return getAction().submit(kill);
    }

    /**
     * starts a thread and introduces other mafia to the player
     */
    public void mafiaIntro(){
        Runnable intro = new Runnable() {
            @Override
            public void run() {
                StringBuilder mafia = new StringBuilder();
                for(PlayerHandler p : List.list()){
                    if(p.getState().role == Role.GODFATHER){
                        getPrintWriter().println("Godfather : "+p.getName());
                    }
                    else if(p.getState().role == Role.MAFIA_DOCTOR){
                        getPrintWriter().println("Doctor Lectur : "+p.getName());
                    }
                    else if(p.getState().role == Role.MAFIA){
                        mafia.append(p.getName()).append(" ");
                    }
                }
                getPrintWriter().println("other mafia are : "+mafia);
            }
        };
        getAction().execute(intro);
    }
}
