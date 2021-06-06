package com.company;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Future;

public class Bulletproof extends PlayerHandler{

    private int deathCount;
    private int investigateCount;

    public Bulletproof(Socket s){
        super(s,Role.BULLETPROOF);
        investigateCount = 0;
        deathCount = 1;
    }

    public Future<?> deadInvestigate(){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                getPrintWriter().println("do you want to investigate dead players ?");
                try {
                    if(getBufferReader().readLine().toUpperCase().equals("YES") && investigateCount<2){
                        for(PlayerHandler p : List.list()){
                            if(p.getState().status == Status.DEAD){
                                getPrintWriter().println("dead roles are :");
                                getPrintWriter().println(p.getState().role);
                            }
                        }
                        investigateCount++;
                    }else if(investigateCount<2) {
                        getPrintWriter().println("Ok maybe next time");
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        };
        return getAction().submit(r);
    }

    public int getDeathCount() {
        return deathCount;
    }

    public void setDeathCount(int deathCount) {
        this.deathCount = deathCount;
    }
}
