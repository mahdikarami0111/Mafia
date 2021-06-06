package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Psychologist extends PlayerHandler{
    private BufferedReader bufferReader;
    private PrintWriter printWriter;
    private ExecutorService action;
    private String name;

    public Psychologist(Socket s){
        super(s,Role.PSYCHOLOGIST);
        this.name = super.getName();
        this.bufferReader = super.getBufferReader();
        this.printWriter = super.getPrintWriter();
        this.action = super.getAction();
    }

    public Future<?> silence(){
        Runnable silence = new Runnable() {
            @Override
            public void run() {
                printWriter.println("do you want to silence anyone ?");
                try {
                    if(bufferReader.readLine().toUpperCase().equals("YES")){
                        printAlive();
                        printWriter.println("who do you want to silence ?");
                        String s = bufferReader.readLine();

                        while (getPlayer(s) == null || getPlayer(s).getState().status == Status.DEAD){
                            printWriter.println("Invalid name try again");
                            s = bufferReader.readLine();
                        }
                        getPlayer(s).getState().silence = true;
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        };
        return action.submit(silence);
    }
}
