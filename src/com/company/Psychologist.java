package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Psychologist extends PlayerHandler{

    public Psychologist(Socket s){
        super(s,Role.PSYCHOLOGIST);

    }

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
