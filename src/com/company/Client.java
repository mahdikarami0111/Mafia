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
            Socket client = new Socket("localhost",8080);
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter writer = new PrintWriter(client.getOutputStream(),true);
            ClientReader clientReader = new ClientReader(reader);
            ClientWriter clientWriter = new ClientWriter(keyboard, writer);
            clientWriter.start();
            clientReader.start();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static class ClientReader extends Thread{
        private BufferedReader reader;

        public ClientReader(BufferedReader reader){
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

    public static class ClientWriter extends Thread{
        private BufferedReader keyboard;
        private PrintWriter writer;

        public ClientWriter(BufferedReader br,PrintWriter pw){
            keyboard = br;
            writer = pw;
        }

        @Override
        public void run(){
            try {
                while (true){
                    String request = keyboard.readLine();
                    writer.println(request);
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
