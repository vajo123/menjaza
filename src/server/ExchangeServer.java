package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ExchangeServer {

    public static final int PORT = 9000;
    public static ClientRegistry registry = new ClientRegistry();

    public static void main(String[] args) {

        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("SERVER POKRENUT");

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket);
                Thread thread = new Thread(handler);

                thread.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}