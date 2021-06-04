package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Detective extends PlayerHandler{
    private PlayerStatus state;
    private BufferedReader bufferReader;
    private PrintWriter printWriter;
    private ExecutorService action;
    private String name;

    public Detective(Socket s){
        super(s,Role.MAFIA);
        this.state = super.getState();
        this.name = super.getName();
        this.bufferReader = super.getBufferReader();
        this.printWriter = super.getPrintWriter();
        this.action = super.getAction();
    }

    public Future<?> investigate(){
        Runnable investigate = new Runnable() {
            @Override
            public void run() {
                printAlive();
                try{
                    printWriter.println("whome do you want to investigate ?");
                    String s = bufferReader.readLine();
                    while (getPlayer(s).getState().status == Status.DEAD){
                        printWriter.println("invalid name try again");
                        s = bufferReader.readLine();
                    }
                    Role role = getPlayer(s).getState().role;
                    if(role == Role.MAFIA || role == Role.GODFATHER || role == Role.MAFIA_DOCTOR){
                        printWriter.println("investigation result is positive");
                    }else {
                        printWriter.println("investigation result is negative");
                    }

                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        };
        return action.submit(investigate);
    }
}
