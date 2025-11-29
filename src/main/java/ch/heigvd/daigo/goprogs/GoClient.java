package ch.heigvd.daigo.goprogs;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class GoClient {
    private static final int PORT = 1919;

    public static void main(String[] args) {
        System.out.println("Client starting...");

        try (Socket socket = new Socket("localhost", PORT);
             OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
             BufferedWriter out = new BufferedWriter(osw);
             InputStreamReader isr = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
             BufferedReader in = new BufferedReader(isr);
             Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);) {

            String serverOutput, userInput;

            userInput = scanner.nextLine();
            out.write(userInput + "\n");
            out.flush();

            while ((serverOutput = in.readLine()) != null) {
                System.out.println(serverOutput);
                userInput = scanner.nextLine();
                out.write(userInput + "\n");
                out.flush();
                if(userInput.equals("DISCONNECT"))
                    break;
            }
            if(serverOutput==null)
                System.out.println("Server has unexpectedly disconnected.");

        } catch (IOException e) {
            System.out.println("Error : " + e);
        }
        System.out.println("Client stopping...");

    }
}