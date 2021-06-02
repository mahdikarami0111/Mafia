package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    private class PlayerStatus{
        public boolean done;
        public Role role;
        public Status status;
        public int votes;

        public PlayerStatus(Role role){
            this.role = role;
            done = true;
            status = Status.ALIVE;
            votes = 0;
        }

    }
}
