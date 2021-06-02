package com.company;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(8080);
            ArrayList<Socket> sockets = new ArrayList<>();
            for(int i = 0; i<10 ; i++){
                sockets.add(server.accept());
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
