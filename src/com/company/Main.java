package com.company;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        try {
            Socket s = new Socket("localhost",8080);
            PlayerHandler p = new PlayerHandler(s,Role.MAFIA);
        }catch (IOException e){
            e.printStackTrace();
        }
        ArrayList<PlayerHandler> x = new ArrayList<>();
        List.init(x);
    }
}
