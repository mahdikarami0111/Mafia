package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class MafiaDoctor extends PlayerHandler {
    private PlayerStatus state;
    private BufferedReader bufferReader;
    private PrintWriter printWriter;
    private ExecutorService action;
    private String name;
    private int selfCounter;
    private MafiaDoctor doc;

    public MafiaDoctor(Socket s){
        super(s,Role.MAFIA);
        this.state = super.getState();
        this.name = super.getName();
        this.bufferReader = super.getBufferReader();
        this.printWriter = super.getPrintWriter();
        this.action = super.getAction();
        selfCounter = 0;
        doc = this;
    }

    public void mafiaIntro(){
        Runnable intro = new Runnable() {
            @Override
            public void run() {
                printWriter.println("you are Mafia Doctor");
                String mafia = "";
                for(PlayerHandler p : List.list()){
                    if(p.getState().role == Role.GODFATHER){
                        printWriter.println("Godfather : "+p.getName());
                    }
                    else if(p.getState().role == Role.MAFIA){
                        mafia += p.getName()+" ";
                    }
                }
                printWriter.println("other mafia are : "+mafia);
            }
        };
        action.execute(intro);
    }

    public Future<?> heal(){
        Runnable heal = new Runnable() {
            @Override
            public void run() {
                printAlive();
                try {
                    printWriter.println("Who do you want to heal ?");
                    String s= bufferReader.readLine();
                    while (getPlayer(s) == null || getPlayer(s).getState().status == Status.DEAD || (getPlayer(s) == doc && selfCounter == 1)){
                        printWriter.println("invalid name try again");
                        s = bufferReader.readLine();
                    }
                    if(getPlayer(s).getState().status == Status.SHOT){
                        getPlayer(s).getState().status = Status.ALIVE;
                    }
                    if(getPlayer(s) == doc){
                        selfCounter = 1;
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        };
        return action.submit(heal);
    }
}
