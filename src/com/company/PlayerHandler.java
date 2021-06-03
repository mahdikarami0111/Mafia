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
            p.receiveMessage("["+name+"] "+m);
        }
    }

    public void printAlive(){
        for(PlayerHandler p : List.list()){
            if(p.getState().status == Status.ALIVE){
                printWriter.println(p.getName());
            }
        }
    }

    public PlayerHandler getPlayer(String name){
        for(PlayerHandler p :List.list()){
            if(p.getName().equals(name) && p.getState().status == Status.ALIVE){
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

    public Future<?> vote(){
        return action.submit(new Vote(bufferReader,printWriter));
    }

    public Future<?> chat(){
        return action.submit(new Chat(bufferReader,printWriter));
    }

    public Future<?> intro(){return  action.submit(new Intro(bufferReader,printWriter)); }

    private class Chat implements Runnable{
        private BufferedReader reader;
        private PrintWriter writer;

        public Chat(BufferedReader reader, PrintWriter writer) {
            this.reader = reader;
            this.writer = writer;
        }

        @Override
        public void run() {
            while (true){
                try {
                    sendMessage(reader.readLine());
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private class Vote implements Runnable{
        private BufferedReader reader;
        private PrintWriter writer;

        public Vote(BufferedReader reader, PrintWriter writer) {
            this.reader = reader;
            this.writer = writer;
        }

        @Override
        public void run() {
            printAlive();
            try {
                String name = reader.readLine();
                PlayerHandler p = getPlayer(name);
                while (p == null){
                    writer.println("Invalid name try again");
                    p = getPlayer(reader.readLine());
                }
                p.getState().votes +=1;
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private class Intro implements Runnable{
        private BufferedReader reader;
        private PrintWriter writer;

        public Intro(BufferedReader reader, PrintWriter writer) {
            this.reader = reader;
            this.writer = writer;
        }

        @Override
        public void run() {
            try {
                writer.println("please enter your name :");
                writer.println("are you listening cunt ?");
                String name = reader.readLine();

                while (!isNameValid(name)){
                    writer.println("invalid name");
                    name = reader.readLine();
                }
                setName(name);
                writer.println("name accepted");

            }catch (IOException e){
                e.printStackTrace();
            }
        }
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

        public PlayerStatus(Role role){
            this.role = role;
            status = Status.ALIVE;
            votes = 0;
        }

    }
}
