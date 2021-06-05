package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Sniper extends PlayerHandler{
    private BufferedReader bufferReader;
    private PrintWriter printWriter;
    private ExecutorService action;
    private String name;
    private Sniper snip;

    public Sniper(Socket s){
        super(s,Role.GODFATHER);
        this.name = super.getName();
        this.bufferReader = super.getBufferReader();
        this.printWriter = super.getPrintWriter();
        this.action = super.getAction();
        snip = this;
    }

    public Future<?> shoot(){
        Runnable shoot = new Runnable() {
            @Override
            public void run() {
                printAlive();
                try {
                    printWriter.println("do you want to shoot someone ?");
                    String answer = bufferReader.readLine();
                    if(answer.toUpperCase().equals("YES")){
                        printAlive();
                        printWriter.println("who do you want to kill");
                        String s = bufferReader.readLine();
                        while (getPlayer(s) == null || getPlayer(s).getState().status == Status.DEAD){
                            printWriter.println("invalid name try again");
                            s = bufferReader.readLine();
                        }
                        if(getPlayer(s).getState().role == Role.MAFIA || getPlayer(s).getState().role == Role.MAFIA_DOCTOR || getPlayer(s).getState().role ==Role.GODFATHER){
                            getPlayer(s).getState().status = Status.SHOT;
                        }else {
                            snip.getState().status = Status.SHOT;
                        }
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        };
        return action.submit(shoot);
    }
}
