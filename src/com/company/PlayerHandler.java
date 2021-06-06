package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class PlayerHandler {
    private Socket client;
    private PlayerStatus state;
    private BufferedReader bufferReader;
    private PrintWriter printWriter;
    private ExecutorService action;
    private String name;

    public PlayerHandler(Socket s,Role role){
        state = new PlayerStatus(role);
        try {
            bufferReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
            printWriter = new PrintWriter(s.getOutputStream(),true);
            action = Executors.newSingleThreadExecutor();
            this.client = s;
            this.name = "";
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void receiveMessage(String m){
        printWriter.println(m);
    }

    public void sendMessage(String m){
        for (PlayerHandler p : List.list()){
            if (p == this)continue;
            p.receiveMessage("["+name+"] "+m);
        }
    }

    public void printAlive(){
        for(PlayerHandler p : List.list()){
            if(p.getState().status == Status.ALIVE || p.getState().status == Status.SHOT){
                printWriter.println(p.getName());
            }
        }
    }

    public PlayerHandler getPlayer(String name){
        for(PlayerHandler p :List.list()){
            if(p.getName().equals(name)){
                return p;
            }
        }
        return null;
    }

    public PlayerStatus getState() {
        return state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PlayerHandler getGodfather(){
        for(PlayerHandler p : List.list()){
            if(p.getState().role == Role.GODFATHER){
                return p;
            }
        }
        return null;
    }

    public void sendMessage(String m,PlayerHandler p){
        p.receiveMessage(m);
    }

    public Future<?> vote(){
        Runnable vote = new Runnable() {
            @Override
            public void run() {
                printAlive();
                printWriter.println("who do you want to vote ?");
                try {
                    String name = bufferReader.readLine();
                    PlayerHandler p = getPlayer(name);
                    while (p == null || p.getState().status == Status.DEAD){
                        printWriter.println("Invalid name try again");
                        p = getPlayer(bufferReader.readLine());
                    }
                    p.getState().votes +=1;
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        };
        return action.submit(vote);
    }

    public Future<?> chat(){
        Runnable chat = new Runnable() {
            @Override
            public void run() {

                if(!getState().silence){
                    boolean over = false;
                    while (!Thread.currentThread().isInterrupted()){
                        try {
                            while (!bufferReader.ready()){
                                if(Thread.currentThread().isInterrupted()){
                                    over = true;
                                    break;
                                }
                                Thread.sleep(100);
                            }
                            if(over){
                                break;
                            }
                            sendMessage(bufferReader.readLine());
                        }catch (IOException e){
                            e.printStackTrace();
                        }catch (InterruptedException x){
                            break;
                        }
                    }
                }

                else if(getState().silence){
                    boolean over = false;
                    while (!Thread.currentThread().isInterrupted()){
                        try {
                            while (!bufferReader.ready()){
                                if(Thread.currentThread().isInterrupted()){
                                    over = true;
                                    break;
                                }
                                Thread.sleep(100);
                            }
                            if(over){
                                break;
                            }
                            bufferReader.readLine();
                            printWriter.println("you are silenced you can not chat");
                        }catch (IOException e){
                            e.printStackTrace();
                        }catch (InterruptedException x){
                            break;
                        }finally {
                            if(getState().status == Status.ALIVE){
                                getState().silence = false;
                            }
                        }
                    }
                }
            }
        };

        return action.submit(chat);
    }

    public Future<?> intro(){
        Runnable intro = new Runnable() {
            @Override
            public void run() {
                try {
                    printWriter.println("please enter your name :");
                    String name = bufferReader.readLine();

                    while (!isNameValid(name)){
                        printWriter.println("invalid name");
                        name = bufferReader.readLine();
                    }
                    setName(name);
                    printWriter.println("name accepted");

                    printWriter.println("tell me when you are ready to start the game");
                    bufferReader.readLine();
                    printWriter.println("OK waiting for other players to ready up");

                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        };
        return  action.submit(intro);
    }

    public boolean isNameValid(String name){
        for(PlayerHandler p : List.list()){
            if(p.name.equals(name))return false;
        }
        return true;
    }

    public class PlayerStatus{
        public Role role;
        public Status status;
        public int votes;
        public boolean silence;

        public PlayerStatus(Role role){
            this.role = role;
            status = Status.ALIVE;
            votes = 0;
            silence = false;
        }

    }

    public BufferedReader getBufferReader() {
        return bufferReader;
    }

    public PrintWriter getPrintWriter() {
        return printWriter;
    }

    public ExecutorService getAction() {
        return action;
    }
}
