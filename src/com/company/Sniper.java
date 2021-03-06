package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * a class for sniper role in mafia game inherits from PlayerHandler also has some role specific features
 */
public class Sniper extends PlayerHandler{

    private Sniper snip;

    /**
     * constructor for the class class superclass constructor witha  specified role
     * @param s Socket  player's socket
     */
    public Sniper(Socket s){
        super(s,Role.SNIPER);
        snip = this;
    }

    /**
     * starts a thread running the shooting task for sniper player
     * @return Future  a future object to keep track of thetask
     */
    public Future<?> shoot(){
        Runnable shoot = new Runnable() {
            @Override
            public void run() {
                try {
                    getPrintWriter().println("[Server] do you want to shoot someone ?");
                    String answer = getBufferReader().readLine();
                    if(answer.toUpperCase().equals("YES")){
                        printAlive();
                        getPrintWriter().println("[Server] who do you want to kill");
                        String s = getBufferReader().readLine();
                        while (getPlayer(s) == null || getPlayer(s).getState().status == Status.DEAD){
                            getPrintWriter().println("[Server] invalid name try again");
                            s = getBufferReader().readLine();
                        }
                        if(getPlayer(s).getState().role == Role.MAFIA || getPlayer(s).getState().role == Role.MAFIA_DOCTOR || getPlayer(s).getState().role ==Role.GODFATHER){
                            getPlayer(s).getState().status = Status.SHOT;
                        }else {
                            snip.getState().status = Status.SHOT;
                        }
                    }
                }catch (IOException e){
                    System.out.println("Client "+getName()+" connection is lost");
                    getState().status = Status.DEAD;
                    getState().silence = true;
                }
            }
        };
        return getAction().submit(shoot);
    }
}
