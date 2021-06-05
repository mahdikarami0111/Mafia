package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Mayor extends PlayerHandler{

    private BufferedReader bufferReader;
    private PrintWriter printWriter;
    private ExecutorService action;
    private String name;
    private boolean cancel;

    public Mayor(Socket s){
        super(s,Role.GODFATHER);
        this.name = super.getName();
        this.bufferReader = super.getBufferReader();
        this.printWriter = super.getPrintWriter();
        this.action = super.getAction();
    }

    public Future<?> verify(){
        Runnable verify = new Runnable() {
            @Override
            public void run() {
                try {
                    printWriter.println("do you want to cancel voting");
                    String answer = bufferReader.readLine();
                    if(answer.toUpperCase().equals("YES")){
                        cancel = true;
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        };
        return action.submit(verify,cancel);
    }
}
