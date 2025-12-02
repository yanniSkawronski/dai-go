package ch.heigvd.daigo.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class Server {
    private final int PORT;
    private final CopyOnWriteArrayList<Game> availableGames = new CopyOnWriteArrayList<>();

    public Server(int port) {
        this.PORT = port;
    }

    public void launch() {
        System.out.println("Server starting...");

        try(ServerSocket serverSocket = new ServerSocket(PORT);
            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor(); ) {
            while(!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                System.out.printf("Connection from %s" , clientSocket.getInetAddress());

                executor.submit(new ClientHandler(clientSocket));

            }
        } catch (IOException e) { System.out.println("Error : " + e); }
    }

}
