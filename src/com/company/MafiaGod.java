package com.company;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * mafia god class acts as mafia game handler the game loop method is used by the server and the game starts
 * keeps track of players and switches them between different threads when necessary
 */
public class MafiaGod {

    private ArrayList<PlayerHandler> players;
    private ArrayList<Mafia> mafia;
    private Godfather godfather;
    private Detective detective;
    private Doctor doctor;
    private MafiaDoctor mafiaDoctor;
    private Sniper sniper;
    private Bulletproof bulletproof;
    private Psychologist psychologist;
    private Mayor mayor;
    private ArrayList<PlayerHandler> citizens;

    /**
     * constructor for the class gets list of sockets as input and distributes roles between players
     * @param clients ArrayList<Socket>  list of sockets accepted by the server
     */
    public MafiaGod(ArrayList<Socket> clients){
        mafia = new ArrayList<>();
        citizens = new ArrayList<>();
        players = new ArrayList<>();
        distribute(clients,10);
        List.init(players);
    }

    /**
     * shuffles the lsit and distributes roles between players
     * @param sockets list of sockets
     * @param count players count
     */
    public void  distribute(ArrayList<Socket> sockets,int count){
        Collections.shuffle(sockets);
        int index = 0;
        for(int i = 0;i<(count/3)-2;i++){
            Mafia m = new Mafia(sockets.get(index));
            players.add(m);
            mafia.add(m);
            index++;
        }

        players.add(mafiaDoctor = new MafiaDoctor(sockets.get(index)));
        index++;

        players.add(godfather = new Godfather(sockets.get(index)));
        index++;

        players.add(detective = new Detective(sockets.get(index)));
        index++;

        players.add(doctor = new Doctor(sockets.get(index)));
        index++;

        players.add(sniper = new Sniper(sockets.get(index)));
        index++;

        players.add(bulletproof = new Bulletproof(sockets.get(index)));
        index++;

        players.add(mayor = new Mayor(sockets.get(index)));
        index++;

        players.add(psychologist = new Psychologist(sockets.get(index)));
        index++;

        for(int i = index;i<sockets.size();i++){
            PlayerHandler p = new PlayerHandler(sockets.get(index),Role.CITIZEN);
            players.add(p);
            citizens.add(p);
            index++;
        }
    }


    /**
     * game loop method keeps running until game is over
     */
    public void run() {
        intro();
        roleDeclaration();
        introduceMafia();
        while (!gameOver()){
            sendToAll("Ok it is night now");
            mafiaKill();
            sniperShoot();
            doctorLectur();
            doctorHeal();
            psychology();
            bulletDetect();
            detectiveDetect();
            sendToAll("OK its day now wake up");
            concludeNight();
            startChatting();
            voting();
        }
    }

    /**
     * chekcs weather the game is over or not
     * @return true if game is over false if not
     */
    public boolean gameOver(){
        int mafiaCount = 0;
        int citizenCount = 0;
        for(PlayerHandler p :players){
            if(p.getState().status == Status.ALIVE){
                Role r = p.getState().role;
                if(r == Role.MAFIA || r == Role.MAFIA_DOCTOR || r == Role.GODFATHER){
                    mafiaCount++;
                }else {
                    citizenCount ++;
                }
            }
        }
        return (citizenCount <= mafiaCount);
    }

    /**
     * concludes the night kills players who ahve been shot and not healed
     */
    public void concludeNight(){
        boolean nooneDied = true;
        for (PlayerHandler p : players){
            if(p.getState().status == Status.SHOT){
                p.getState().status = Status.DEAD;
                p.getState().silence = true;
                sendToAll("Last night " +p.getName()+" died, rest in peace my nigga");
                nooneDied = false;
            }
        }
        if(nooneDied){
            sendToAll("No one died last night");
        }
    }

    /**
     * starts voting process for all players and waits until everybody is done
     */
    public void voting(){
        sendToAll("Voting started");
        ArrayList<Future<?>> tasks= new ArrayList<>();
        for (PlayerHandler p : players){
            if(p.getState().status == Status.ALIVE){
                tasks.add(p.vote());
            }
        }
        wait(tasks);

        PlayerHandler max = players.get(0);
        for (PlayerHandler p : players){
            if (p.getState().votes > max.getState().votes &&p.getState().status == Status.ALIVE){
                max.getState().votes = 0;
                max = p;
            }else if(p.getState().status == Status.ALIVE) {
                p.getState().votes = 0;
            }
        }
        sendToAll("Result of voting is : " + max.getName());
        if(mayor.getState().status == Status.ALIVE){
            sendToAll("Mayor do your job");
            wait(mayor.verify());

            if(mayor.isCancel()){
                sendToAll("Mayor cancelled voting");
            }
            else {
                sendToAll("Mayor did not cancel voting "+max.getName()+" died");
                max.getState().status = Status.DEAD;
                max.getState().silence = true;
            }
        }else {
            sendToAll(max.getName()+" died");
            max.getState().status = Status.DEAD;
            max.getState().silence = true;
        }
    }

    /**
     * calls for Bulletproof role to do its job waits until it ends
     */
    public void bulletDetect(){
        if(bulletproof.getState().status != Status.DEAD){
            sendToAll("Bulletproof wake up");
            wait(bulletproof.deadInvestigate());
        }
    }

    /**
     * calls for psychologist role to his job waits until done
     */
    public void psychology(){
        if(psychologist.getState().status != Status.DEAD){
            sendToAll("Psychologist wake up");
            wait(psychologist.silence());
        }
    }

    /**
     * calls for detective to do his job waits till he is done
     */
    public void detectiveDetect(){
        if(detective.getState().status != Status.DEAD){
            sendToAll("Detective wake up and investigate");
            wait(detective.investigate());
        }
    }

    /**
     * calls for doctor to his job waits until done
     */
    public void doctorHeal(){
        if(doctor.getState().status != Status.DEAD){
            sendToAll("Doctor wake up and heal someone");
            wait(doctor.heal());
        }
    }

    /**
     * calls for doctor lectur to do his job waits untill done
     */
    public void doctorLectur(){
        if(mafiaDoctor.getState().status != Status.DEAD){
            sendToAll("Doctor Lectur wake up and heal someone");
            wait(mafiaDoctor.heal());
        }
    }

    /**
     * cals for sniper top do his job waits untill done
     */
    public void sniperShoot(){
        if(sniper.getState().status != Status.DEAD){
            sendToAll("Sniper wake up");
            wait(sniper.shoot());
        }
    }

    /**
     * calls for the mafias to do their job waits untill they are all done
     */
    public void mafiaKill(){
        sendToAll("Mafia wake up and kill someone");
        ArrayList<Future<?>> tasks = new ArrayList<>();
        for (Mafia m : mafia){
            if(m.getState().status == Status.ALIVE){
                tasks.add(m.kill());
            }
        }
        tasks.add(godfather.kill());
        wait(tasks);
        sendToAll("mafia killed someone");
    }

    /**
     * introduces the mafia to eachother
     */
    public void introduceMafia(){
        for (Mafia m :mafia){
            m.mafiaIntro();
        }
        godfather.mafiaIntro();
        mafiaDoctor.mafiaIntro();
    }

    /**
     * sends a message to all players from the server side
     * @param m String  message to be sent
     */
    public void sendToAll(String m){
        for(PlayerHandler p :players){
            p.receiveMessage("[Server] "+m);
        }
    }

    /**
     * declares every one of their roles
     */
    public void roleDeclaration(){
        for (PlayerHandler p : players){
            p.receiveMessage("[Server} Your role is : " + p.getState().role);
        }
    }

    /**
     * introduces each player role and situation only to himself
     */
    public void intro(){
        ArrayList<Future<?>> done = new ArrayList<>();
        for(PlayerHandler p : players){
            done.add(p.intro());
        }
        wait(done);
        sendToAll("Game will Start in 10 seconds");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * starts chatting process for all players ends it after a specific time
     */
    public void startChatting(){
        sendToAll("Chatting will start in 10 seconds");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        sendToAll("Chatting started");
        ArrayList<Future<?>> done = new ArrayList<>();
        System.out.println("starting chatting");;
        for(PlayerHandler p : players){
            done.add(p.chat());
        }
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for(Future<?> f : done){
            System.out.println("Ending chats");
            f.cancel(true);
        }
    }

    /**
     * gets a list of future objects and blocks the program until all their threads are done
     * @param futures future objects that we want the program to wait for them
     */
    public void wait(ArrayList<Future<?>> futures){
        for(Future<?> f : futures){
            try {
                f.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * overloaded version of the wait method only waits for one future object
     * @param f future object of the thread we want to wait for it to finish
     */
    public void wait(Future<?> f){
        try {
            f.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
