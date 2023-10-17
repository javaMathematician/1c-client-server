package org.slovenly.task1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Server {
    private static final Map<Integer, PrintWriter> clientWriters = new ConcurrentHashMap<>();
    private static final ExecutorService SERVICE = Executors.newVirtualThreadPerTaskExecutor();

    public static void main(String[] args) throws InterruptedException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("""
                Type in any moment:
                1, if you want to broadcast;\s
                2, if you want to list connected clients;\s
                3, if you want to write message to specific client;""");

        Callable<Object> clientHandlerCallable = () -> {
            try (ServerSocket serverSocket = new ServerSocket(12345, 5000)) {
                System.err.println("Server is running. Waiting for clients...");

                while (true) {
                    ClientHandler clientHandler = new ClientHandler(serverSocket.accept());
                    SERVICE.submit(clientHandler);
                }
            }
        };

        Callable<Object> clientInterface = () -> {
            while (true) {
                try {
                    String command = reader.readLine();

                    switch (command) {
                        case "1" -> {
                            System.out.print("Print message to broadcast: ");
                            broadcast(reader.readLine());
                        }

                        case "2" -> System.out.printf("Connected socket ids: %s%n", clientWriters.keySet());

                        case "3" -> {
                            System.out.printf("Print ID from list %s: ", clientWriters.keySet());
                            String id = reader.readLine();

                            if (!StringUtils.isNumeric(id) || !clientWriters.containsKey(Integer.parseInt(id))) {
                                System.err.println("Client is not connected");
                                break;
                            }

                            PrintWriter printWriter = clientWriters.get(Integer.parseInt(id));
                            System.out.printf("Print message to write to client %s: ", id);
                            printWriter.println(reader.readLine());
                        }

                        default -> System.out.println("Incorrect input. Try again");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        SERVICE.submit(clientHandlerCallable);
        SERVICE.submit(clientInterface);
        SERVICE.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    }

    private static class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
            System.err.printf("Log: connected user with port %s%n", socket.getPort());
        }

        @Override
        public void run() {
            int port = socket.getPort();

            try (
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
            ) {
                clientWriters.put(port, out);

                String message;
                while (!Objects.equals(message = in.readLine(), "null") && message != null) {
                    if (message.equals(MessageState.RECEIVED.toString())) {
                        System.err.printf("Log: message delivered to %s%n", port);
                        continue;
                    }

                    System.out.printf("Received message from port %s: %s%n", port, message);
                }
            } catch (IOException ignored) {
            } finally {
                PrintWriter remove = clientWriters.remove(port);

                if (remove != null) {
                    remove.close();
                }

                System.err.printf("Log: client %s disconnected%n", port);

                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.printf("Failed closing socket %s%n", socket);
                }
            }
        }
    }

    private static void broadcast(String message) {
        synchronized (clientWriters) {
            for (PrintWriter writer : clientWriters.values()) {
                writer.println(message);
            }
        }
    }
}
