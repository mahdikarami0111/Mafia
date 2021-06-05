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
        players = new ArrayList<>();
        distributeTest(clients);
        List.init(players);
    }

    public void  distribute(ArrayList<Socket> sockets,int count){
        Random r = new Random();
        int i = 0;
        int index = 0;
        for(i = 0 ; i<(count/3)-2 ; i++){
            index = r.nextInt(sockets.size()-1);
            Mafia p = new Mafia(sockets.get(index));
            mafia.add(p);
            sockets.remove(index);
            players.add(p);
        }

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
            sockets.remove(s);
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
        sendToAll("Ok it is night now");
        mafiaKill();
        sniperShoot();
        doctorLectur();
        doctorHeal();
        psychology();
        bulletDetect();
        concludeNight();
        voting();
    }

    public void mayorVote(){
        if(mayor.getState().status == Status.ALIVE){
            sendToAll("Mayor wake up and to yor thing");
            wait(mayor.verify());
        }
    }

    public void concludeNight(){
        for (PlayerHandler p : players){
            if(p.getState().status == Status.SHOT){
                p.getState().status = Status.DEAD;
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
            Future<?> result = mayor.verify();
            try {
                if ((boolean)result.get()){
                    System.out.println("Yes");
                }
            }catch (ExecutionException | InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    public void bulletDetect(){
        if(bulletproof.getState().status == Status.ALIVE){
            sendToAll("Bulletproof wake up");
            wait(bulletproof.deadInvestigate());
        }
    }

    public void psychology(){
        if(psychologist.getState().status == Status.ALIVE){
            sendToAll("Psychologist wake up");
            wait(psychologist.silence());
        }
    }

    public void detectiveDetect(){
        if(detective.getState().status == Status.ALIVE){
            sendToAll("Detective wake up and investigate");
            wait(detective.investigate());
        }
    }

    public void doctorHeal(){
        if(doctor.getState().status == Status.ALIVE){
            sendToAll("Doctor wake up and heal someone");
            wait(doctor.heal());
        }
    }

    public void doctorLectur(){
        if(mafiaDoctor.getState().status == Status.ALIVE){
            sendToAll("Doctor lectur wake up and heal someone");
            wait(mafiaDoctor.heal());
        }
    }

    public void sniperShoot(){
        if(sniper.getState().status == Status.ALIVE){
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
        ArrayList<Future<?>> done = new ArrayList<>();
        System.out.println("starting chatting");;
        for(PlayerHandler p : players){
            done.add(p.chat());
        }
        try {
            Thread.sleep(15000);
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
