package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * playerHandler class  is the main way of communication with clients through a socket which is one of the class fields
 * this class has a readr and writer to read from and write to client through the socket data streams
 * one instance of this class is assigned to each player and keeps track of their status throughout the game
 * has a single thread executive service to manage and switch between threads since a player is onl doing one specific task at a time
 * we dont need a thread pool with more than one thread also hast functionalities that are common between all roles such as chatting or voting
 */
public class PlayerHandler {
    private Socket client;
    private PlayerStatus state;
    private BufferedReader bufferReader;
    private PrintWriter printWriter;
    private ExecutorService action;
    private String name;

    /**
     * constructor for the class
     * @param s Socket  client's socket
     * @param role Role and enum for player's role
     */
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

    /**
     * used to send a message to the player
     * @param m String  message to be sent
     */
    public void receiveMessage(String m){
        printWriter.println(m);
    }

    /**
     * sends a message to all other players other players will know who sent the message
     * @param m String  message to be sent
     */
    public void sendMessage(String m){
        for (PlayerHandler p : List.list()){
            if (p == this)continue;
            p.receiveMessage("["+name+"] "+m);
        }
    }

    /**
     * sends a message to all other players other players will not know who sent the message
     * @param m String  message to be sent
     */
    public void sendAnonymousMessage(String m){
        for (PlayerHandler p : List.list()){
            if (p== this)continue;
            p.receiveMessage(m);
        }
    }

    /**
     * prints all alive players for the player
     */
    public void printAlive(){
        StringBuilder s = new StringBuilder();
        for(PlayerHandler p : List.list()){
            if(p.getState().status == Status.ALIVE || p.getState().status == Status.SHOT){
                s.append(p.getName());
            }
        }
        printWriter.println("[ "+s+" ]");
    }

    /**
     * takes name of a player as input and returns the player if player was not found returns nll
     * @param name String  name of the player to be found
     * @return  PlayerHandler player whose name was given to the function
     */
    public PlayerHandler getPlayer(String name){
        for(PlayerHandler p :List.list()){
            if(p.getName().equals(name)){
                return p;
            }
        }
        return null;
    }

    /**
     * returns status of the player
     * @return PlayerStatus  current state of the player
     */
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

    /**
     * overloaded version of the sendMessage method, sends the message only top specific given player
     * @param m String  message to be sent
     * @param p PlayerHandler  player whom message will be sent to
     */
    public void sendMessage(String m,PlayerHandler p){
        p.receiveMessage(m);
    }

    /**
     * Starts voting process for the player
     * @return Future<?>  a future object to keep track of the voting thread
     */
    public Future<?> vote(){
        Runnable vote = new Runnable() {
            @Override
            public void run() {
                printAlive();
                printWriter.println("who do you want to vote ?, if you don't want to vote type -1");
                try {
                    String name = bufferReader.readLine();
                    if(!name.equals("-1")){
                        PlayerHandler p = getPlayer(name);
                        while (p == null || p.getState().status == Status.DEAD){
                            printWriter.println("Invalid name try again");
                            p = getPlayer(bufferReader.readLine());
                        }
                        sendMessage("voted for : "+p.getName());
                        p.getState().votes +=1;

                        printWriter.println("[Server] your voting will end in 10 seconds");
                        printWriter.println("[Server] do you want to change your vote ?");
                        Thread.sleep(10000);
                        if(bufferReader.ready()){
                            p.getState().votes -= 1;
                            name = bufferReader.readLine();
                            p = getPlayer(name);
                            while (p == null || p.getState().status == Status.DEAD){
                                printWriter.println("Invalid name try again");
                                p = getPlayer(bufferReader.readLine());
                            }
                            sendMessage("changed his vote for : "+p.getName());
                            p.getState().votes +=1;
                        }
                    }
                }catch (IOException | InterruptedException e){
                    System.out.println("[Server] Client "+getName()+" connection is lost");
                    getState().status = Status.DEAD;
                    getState().silence = true;
                }
            }
        };
        return action.submit(vote);
    }

    /**
     * starts chatting process for the player
     * @return Future<?>  a future object to keep track of the chat thread
     */
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
                        }catch (IOException | InterruptedException e){
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
                        }catch (IOException | InterruptedException e){
                            break;
                        } finally {
                            if(getState().status == Status.ALIVE){
                                getState().silence = false;
                            }
                        }
                    }
                    if(getState().status == Status.ALIVE){
                        getState().silence = false;
                    }
                }
            }
        };

        return action.submit(chat);
    }

    /**
     * starts the introduction process for the player
     * @return Future<?>  a future object to keep track of the intro thread
     */
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

    /**
     * checks for repetitive username
     * @param name String  new username
     * @return boolean  true if username is valid false if not
     */
    public boolean isNameValid(String name){
        for(PlayerHandler p : List.list()){
            if(p.name.equals(name))return false;
        }
        return true;
    }

    /**
     * a class to group everything related to player inGame status
     */
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
