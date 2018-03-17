/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.httpserverheroku;

import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HttpServer extends Thread {
    
    private static ServerSocket serverSocket;
    
    public static void main(String[] args) throws IOException {
        serverSocket = null;
        Integer port;
        try { 
            port = new Integer(System.getenv("PORT"));
        } catch (Exception e) {
            port = 35000;
        }
        serverSocket = new ServerSocket(port);
        ExecutorService executor = Executors.newFixedThreadPool(1);
        while (true){
            executor.execute(new HttpServer());           
        }
    }
    
    public void run() {
        Socket clientSocket = null;
        byte[] bytes;
        try {
            System.out.println("Listo para recibir ...");
            clientSocket = serverSocket.accept();
        } catch (IOException e) {
            System.err.println("Accept failed.");
            System.exit(1);
        }
        try {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException ex) {
            Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException ex) {
            Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        String inputLine, outputLine, f, d;
        try {
            if ((inputLine = in.readLine()) != null) {
                inputLine = inputLine.split(" ")[1];
                Path path;
                if(inputLine.contains(".html")){
                    path = new File("./" + inputLine).toPath();
                } else {
                    path = new File("./index.html").toPath();                                       
                }
                bytes = Files.readAllBytes(path);
                d = "" + bytes.length;
                f = "text/html";
            } else {
                bytes = Files.readAllBytes(new File("./index.html").toPath());
                d = "" + bytes.length;
                f = "text/html";
                //System.out.println("Recibí: " + inputLine);
                //if (!in.ready()) {break; }
            }
            outputLine = "HTTP/1.1 200 OK\r\n" 
                                + "Content-Type: " + f + "\r\n"
                                + "Content-Length: " + d
                                + "\r\n\r\n";
            byte[] outputBytes = outputLine.getBytes();
            byte[] h = new byte[bytes.length + outputBytes.length];
        for (int i = 0; i < outputBytes.length; i++) {
            h[i] = outputBytes[i];
        }
        for (int i = outputBytes.length; i < outputBytes.length + bytes.length; i++) {
            h[i] = bytes[i - outputBytes.length];
        }           
            clientSocket.getOutputStream().write(h);
            clientSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(HttpServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }  
    
}