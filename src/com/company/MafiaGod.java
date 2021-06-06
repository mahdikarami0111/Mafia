package com.company;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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

    public MafiaGod(ArrayList<Socket> clients){
        mafia = new ArrayList<>();
        citizens = new ArrayList<>();
        players = new ArrayList<>();
        distribute(clients,10);
        List.init(players);
    }

    public void  distribute(ArrayList<Socket> sockets,int count){
        Random r = new Random();
        int i = 0;
        int index = 0;
        index = r.nextInt(sockets.size()-1);
        Mafia m = new Mafia(sockets.get(index));
        mafia.add(m);
        players.add(m);
        sockets.remove(index);

        index = r.nextInt(sockets.size()-1);
        players.add(mafiaDoctor = new MafiaDoctor(sockets.get(index)));
        sockets.remove(index);

        index = r.nextInt(sockets.size()-1);
        players.add(godfather = new Godfather(sockets.get(index)));
        sockets.remove(index);

        index = r.nextInt(sockets.size()-1);
        players.add(detective = new Detective(sockets.get(index)));
        sockets.remove(index);

        index = r.nextInt(sockets.size()-1);
        players.add(doctor = new Doctor(sockets.get(index)));
        sockets.remove(index);

        index = r.nextInt(sockets.size()-1);
        players.add(sniper = new Sniper(sockets.get(index)));
        sockets.remove(index);

        index = r.nextInt(sockets.size()-1);
        players.add(bulletproof = new Bulletproof(sockets.get(index)));
        sockets.remove(index);

        index = r.nextInt(sockets.size()-1);
        players.add(mayor = new Mayor(sockets.get(index)));
        sockets.remove(index);

        index = r.nextInt(sockets.size()-1);
        players.add(psychologist = new Psychologist(sockets.get(index)));
        sockets.remove(index);

        for(Socket s : sockets){
            PlayerHandler p = new PlayerHandler(s,Role.CITIZEN);
            players.add(p);
            citizens.add(p);
        }
    }

    public void distributeTest(ArrayList<Socket> sockets){
        players.add(new PlayerHandler(sockets.get(0),Role.CITIZEN));
        players.add(new PlayerHandler(sockets.get(1),Role.CITIZEN));
        players.add(new PlayerHandler(sockets.get(2),Role.CITIZEN));
    }


    public void run() {
        intro();
        roleDeclaration();
        introduceMafia();
        sendToAll("GAME STARTED");
        while (!gameOver()){
            sendToAll("Ok it is night now");
            mafiaKill();
            sendToAll("mafia killed someone");
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

    public void mayorVote(){
        if(mayor.getState().status == Status.ALIVE){
            sendToAll("Mayor wake up and to yor thing");
            wait(mayor.verify());
        }
    }

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

    public void concludeNight(){
        for (PlayerHandler p : players){
            if(p.getState().status == Status.SHOT){
                p.getState().status = Status.DEAD;
                p.getState().silence = true;
                sendToAll("Last night " +p.getName()+" died, rest in peace my nigga");
            }
        }
    }

    public void voting(){
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
            sendToAll("Mayor wake up and to yor thing");
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

    public void bulletDetect(){
        if(bulletproof.getState().status != Status.DEAD){
            sendToAll("Bulletproof wake up");
            wait(bulletproof.deadInvestigate());
        }
    }

    public void psychology(){
        if(psychologist.getState().status != Status.DEAD){
            sendToAll("Psychologist wake up");
            wait(psychologist.silence());
        }
    }

    public void detectiveDetect(){
        if(detective.getState().status != Status.DEAD){
            sendToAll("Detective wake up and investigate");
            wait(detective.investigate());
        }
    }

    public void doctorHeal(){
        if(doctor.getState().status != Status.DEAD){
            sendToAll("Doctor wake up and heal someone");
            wait(doctor.heal());
        }
    }

    public void doctorLectur(){
        if(mafiaDoctor.getState().status != Status.DEAD){
            sendToAll("Doctor lectur wake up and heal someone");
            wait(mafiaDoctor.heal());
        }
    }

    public void sniperShoot(){
        if(sniper.getState().status != Status.DEAD){
            sendToAll("Sniper wake up");
            wait(sniper.shoot());
        }
    }

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
    }

    public void introduceMafia(){
        for (Mafia m :mafia){
            m.mafiaIntro();
        }
        godfather.mafiaIntro();
        mafiaDoctor.mafiaIntro();
    }

    public void sendToAll(String m){
        for(PlayerHandler p :players){
            p.receiveMessage("[Server] "+m);
        }
    }

    public void roleDeclaration(){
        for (PlayerHandler p : players){
            p.receiveMessage("[Server} Your role is : " + p.getState().role);
        }
    }

    public void intro(){
        ArrayList<Future<?>> done = new ArrayList<>();
        for(PlayerHandler p : players){
            done.add(p.intro());
        }
        wait(done);
    }

    public void startChatting(){
        sendToAll("chatting started");
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

    public boolean allDone(ArrayList<Future<?>> futures){
        for (Future<?> f : futures){
            if(!f.isDone()){return false;}
        }
        return true;
    }

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
