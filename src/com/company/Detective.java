package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Detective extends PlayerHandler{


    public Detective(Socket s){
        super(s,Role.DETECTIVE);
    }

    public Future<?> investigate(){
        Runnable investigate = new Runnable() {
            @Override
            public void run() {
                printAlive();
                try{
                    getPrintWriter().println("[Server] whom do you want to investigate ?");
                    String s = getBufferReader().readLine();
                    while (getPlayer(s).getState().status == Status.DEAD){
                        getPrintWriter().println("[Server] invalid name try again");
                        s = getBufferReader().readLine();
                    }
                    Role role = getPlayer(s).getState().role;
                    if(role == Role.MAFIA || role == Role.MAFIA_DOCTOR){
                        getPrintWriter().println("[Server] investigation result is positive");
                    }else {
                        getPrintWriter().println("[Server] investigation result is negative");
                    }

                }catch (IOException e){
                    System.out.println("[Server] Client "+getName()+" connection is lost");
                    getState().status = Status.DEAD;
                    getState().silence = true;
                }
            }
        };
        return getAction().submit(investigate);
    }
}