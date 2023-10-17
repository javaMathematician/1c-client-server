package org.slovenly.task1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private static boolean morpheusWrote = false;

    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket("localhost", 12345)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            Thread receiverThread = new Thread(() -> {
                String message;

                try {
                    while ((message = in.readLine()) != null) {
                        System.out.printf("Received message from server: %s%n", message);
                        morpheusWrote = true;
                        out.println(MessageState.RECEIVED);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            receiverThread.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String line = reader.readLine();

                if (!morpheusWrote) {
                    System.err.println("You can't write a message until you get it from Morpheus.");
                    continue;
                }

                out.println(line);
            }
        }
    }
}
