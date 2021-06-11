package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * a class for Mayor role in mafia game inherits from PlayerHandler also has some role specific features
 */
public class Mayor extends PlayerHandler{

    private boolean cancel;

    /**
     * constructor for the class calls the superclass constructor with the specified role
     * @param s Socket  player's socket
     */
    public Mayor(Socket s){
        super(s,Role.MAYOR);
        this.cancel = false;
    }

    /**
     * starts a thread running the vote cancellation task for the mayor player
     * @return Future  a future object to keep track of the threads status
     */
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
