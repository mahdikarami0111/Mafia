package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        try {
            BufferedReader keyboard =new BufferedReader(new InputStreamReader(System.in));
            Socket client = new Socket("localhost",Integer.parseInt(keyboard.readLine()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter writer = new PrintWriter(client.getOutputStream(),true);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public class ClientReader extends Thread{
        BufferedReader reader;

        public ClientReader(BufferedReader reader) {
            this.reader = reader;
        }

        @Override
        public void run(){
            try {
                while (true){
                    String response = reader.readLine();
                    System.out.println(response);
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
