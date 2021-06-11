package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * a class for psychologist role in mafia game inherits from PlayerHandler also has some role specific features
 */
public class Psychologist extends PlayerHandler{
    /**
     * constructor for the class class superclass constructor with a specified role (Psychologist)
     * @param s Socket  player's socket
     */
    public Psychologist(Socket s){
        super(s,Role.PSYCHOLOGIST);

    }

    /**
     * starts a thread running the silence task for the psychologist player
     * @return Future  a future object to keep track of the task status
     */
    public Future<?> silence(){
        Runnable silence = new Runnable() {
            @Override
            public void run() {
                getPrintWriter().println("[Server] do you want to silence anyone ?");
                try {
                    if(getBufferReader().readLine().toUpperCase().equals("YES")){
                        printAlive();
                        getPrintWriter().println("[Server] who do you want to silence ?");
                        String s = getBufferReader().readLine();

                        while (getPlayer(s) == null || getPlayer(s).getState().status == Status.DEAD){
                            getPrintWriter().println("[Server] Invalid name try again");
                            s = getBufferReader().readLine();
                        }
                        getPlayer(s).getState().silence = true;
                    }
                }catch (IOException e){
                    System.out.println("Client "+getName()+" connection is lost");
                    getState().status = Status.DEAD;
                    getState().silence = true;
                }
            }
        };
        return getAction().submit(silence);
    }
}
