package ch.heigvd.daigo.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * HeiGo implementation of a DAIGO server
 */
public class Server {
    private final int PORT;

    /**
     * @param port port to listen on
     */
    public Server(int port) {
        this.PORT = port;
    }

    /**
     * Start the server loop
     */
    public void launch() {
        System.out.println("Server starting...");

        try(ServerSocket serverSocket = new ServerSocket(PORT);
            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor(); ) {
            while(!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                System.out.printf("Connection from: %s\n" , clientSocket.getInetAddress());

                executor.submit(new ClientHandler(clientSocket));

            }
        } catch (IOException e) { System.out.println("Error : " + e); }
    }

}
