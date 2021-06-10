package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Godfather extends PlayerHandler{


    public Godfather(Socket s){
        super(s,Role.GODFATHER);
    }

    public Future<?> kill(){
       Runnable kill = new Runnable() {
           @Override
           public void run() {
               printAlive();
               getPrintWriter().println("[Server] who do you want to kill ?");
               String s="";
               try {
                   while (true){
                       s = getBufferReader().readLine();
                       if(getPlayer(s) != null && getPlayer(s).getState().status == Status.ALIVE &&
                               getPlayer(s).getState().role != Role.GODFATHER &&
                               getPlayer(s).getState().role != Role.MAFIA &&
                               getPlayer(s).getState().role != Role.MAFIA_DOCTOR){
                           break;
                       }
                       getPrintWriter().println("[Server] invalid name try again");
                   }
               }catch (IOException e){
                   System.out.println("Client "+getName()+" connection is lost");
                   getState().status = Status.DEAD;
                   getState().silence = true;
               }
               if(getPlayer(s).getState().role == Role.BULLETPROOF){
                   Bulletproof bulletproof = (Bulletproof)getPlayer(s);
                   if(bulletproof.getDeathCount() == 1){
                       bulletproof.setDeathCount(0);
                   }else if(bulletproof.getDeathCount() == 0){
                       bulletproof.getState().status = Status.SHOT;
                   }
               }else {
                   getPlayer(s).getState().status = Status.SHOT;
               }
           }
       };
       return getAction().submit(kill);
    }

    public void mafiaIntro(){
        Runnable intro = new Runnable() {
            @Override
            public void run() {
                StringBuilder mafia = new StringBuilder();
                for(PlayerHandler p : List.list()){
                    if(p.getState().role == Role.MAFIA){
                        mafia.append(p.getName()).append(" ");
                    }
                    else if(p.getState().role == Role.MAFIA_DOCTOR){
                        getPrintWriter().println("Doctor Lectur : "+p.getName());
                    }
                }
                getPrintWriter().println("other mafia are : "+mafia);
            }
        };
        getAction().execute(intro);
    }
}
