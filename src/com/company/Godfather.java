package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Godfather extends PlayerHandler{
    private PlayerStatus state;
    private BufferedReader bufferReader;
    private PrintWriter printWriter;
    private ExecutorService action;
    private String name;

    public Godfather(Socket s){
        super(s,Role.GODFATHER);
        this.state = super.getState();
        this.name = super.getName();
        this.bufferReader = super.getBufferReader();
        this.printWriter = super.getPrintWriter();
        this.action = super.getAction();
    }

    public Future<PlayerHandler> kill(){
        Callable kill = new Callable() {
            @Override
            public PlayerHandler call() {
                printAlive();
                printWriter.println("who do you want to kill ?");
                String s="";
                try {
                    while (true){
                        s = bufferReader.readLine();
                        if(getPlayer(s) != null && getPlayer(s).getState().status == Status.ALIVE &&
                                getPlayer(s).getState().role != Role.GODFATHER &&
                                getPlayer(s).getState().role != Role.MAFIA &&
                                getPlayer(s).getState().role != Role.MAFIA_DOCTOR){
                            break;
                        }
                        printWriter.println("invalid name try agin");
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
                return getPlayer(s);
            }
        };
        return action.submit(kill);
    }

    public void mafiaIntro(){
        Runnable intro = new Runnable() {
            @Override
            public void run() {
                printWriter.println("you are godfather");
                String mafia = "";
                for(PlayerHandler p : List.list()){
                    if(p.getState().role == Role.MAFIA){
                        mafia += p.getName()+" ";
                    }
                    if(p.getState().role == Role.MAFIA_DOCTOR){
                        printWriter.println("Doctor Lectur : "+p.getName());
                    }
                }
                printWriter.println("other mafia are : "+mafia);
            }
        };
        action.execute(intro);
    }
}
