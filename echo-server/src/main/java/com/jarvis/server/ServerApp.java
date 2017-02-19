package com.jarvis.server;

import com.jarvis.server.PlainEchoServer;

import java.io.IOException;

/**
 * Hello world!
 */
public class ServerApp {
    public static void main(String[] args) {
        try {
            new PlainEchoServer().server(18443);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
