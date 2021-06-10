package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Doctor extends PlayerHandler{


    private int selfCounter;
    private Doctor doc;

    public Doctor(Socket s){
        super(s,Role.DOCTOR);
        selfCounter = 0;
        doc = this;
    }

    public Future<?> heal(){
        Runnable heal = new Runnable() {
            @Override
            public void run() {
                printAlive();
                try {
                    getPrintWriter().println("[Server] Who do you want to heal ?");
                    String s= getBufferReader().readLine();
                    while (getPlayer(s) == null || getPlayer(s).getState().status == Status.DEAD || (getPlayer(s) == doc && selfCounter == 1)){
                        getPrintWriter().println("[Server] invalid name try again");
                        s = getBufferReader().readLine();
                    }
                    if(getPlayer(s).getState().status == Status.SHOT && getPlayer(s) != doc){
                        getPlayer(s).getState().status = Status.ALIVE;
                    }
                    if(getPlayer(s) == doc){
                        selfCounter = 1;
                        doc.getState().status = Status.ALIVE;
                    }
                }catch (IOException e){
                    System.out.println("Client "+getName()+"'s connection has been lost");
                    getState().status = Status.DEAD;
                    getState().silence = true;
                }
            }
        };
        return getAction().submit(heal);
    }
}
