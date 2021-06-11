package com.company;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Future;

/**
 * a class for bulletproof role in mafia game inherits from PlayerHandler also has some role specific features
 */
public class Bulletproof extends PlayerHandler{

    private int deathCount;
    private int investigateCount;

    /**
     * constructor for the class gets a socket as parameter and calls PlayerHandler constructor also sets maximum number of deaths and investigations
     * @param s Socket  player's socket
     */
    public Bulletproof(Socket s){
        super(s,Role.BULLETPROOF);
        investigateCount = 0;
        deathCount = 1;
    }

    /**
     * runs a thread to complete investigation task for the player returns a future object to keep track of the task
     * @return Future<?>  a future object to keep track of the task
     */
    public Future<?> deadInvestigate(){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if(investigateCount<2){
                    getPrintWriter().println("[Server] do you want to investigate dead players ?");
                    try {
                        if(getBufferReader().readLine().toUpperCase().equals("YES")){
                            getPrintWriter().println("[Server] Dead roles are : ");
                            sendAnonymousMessage("[Server] Bulletproof asked for dead roles : ");
                            for(PlayerHandler p : List.list()){
                                if(p.getState().status == Status.DEAD){
                                    getPrintWriter().println(p.getState().role);
                                    sendAnonymousMessage((p.getState().role).toString());
                                }
                            }
                            investigateCount++;
                        }else {
                            getPrintWriter().println("[Server] Ok maybe next time");
                        }
                    }catch (IOException e){
                        System.out.println("Client "+getName()+" connection is lost");
                        getState().status = Status.DEAD;
                        getState().silence = true;
                    }
                }
            }
        };
        return getAction().submit(r);
    }

    public int getDeathCount() {
        return deathCount;
    }

    public void setDeathCount(int deathCount) {
        this.deathCount = deathCount;
    }
}
