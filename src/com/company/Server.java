package com.company;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Server {
    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(8080);
            ArrayList<Socket> sockets = new ArrayList<>();
            for(int i = 0; i<10;i++){
                sockets.add(server.accept());
                System.out.println("new player added");
            }
            MafiaGod mafiaGod = new MafiaGod(sockets);
            mafiaGod.run();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
