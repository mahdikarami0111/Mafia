package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Mayor extends PlayerHandler{

    private boolean cancel;

    public Mayor(Socket s){
        super(s,Role.MAYOR);
        this.cancel = false;
    }

    public Future<?> verify(){
        Runnable verify = new Runnable() {
            @Override
            public void run() {
                try {
                    cancel = false;
                    getPrintWriter().println("[Server] do you want to cancel voting");
                    String answer = getBufferReader().readLine();
                    if(answer.toUpperCase().equals("YES")){
                        cancel = true;
                    }
                }catch (IOException e){
                    System.out.println("Client "+getName()+" connection is lost");
                    getState().status = Status.DEAD;
                    getState().silence = true;
                }
            }
        };
        return getAction().submit(verify);
    }

    public boolean isCancel() {
        return cancel;
    }
}
