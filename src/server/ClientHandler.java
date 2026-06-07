package server;

import model.*;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {

    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private UserData userData;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public UserData getUserData() {
        return userData;
    }

    public void sendObject(Object obj) {
        try {
            out.writeObject(obj);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object obj = in.readObject();
                if (obj instanceof UserData) {
                    userData = (UserData) obj;
                    ExchangeServer.registry.addClient(userData.getUsername(), this);
                    System.out.println("Registrovan: " + userData.getUsername());
                }

                else if (obj instanceof String) {
                    String request = (String) obj;
                    if (request.equals("GET_EXCHANGES")) {
                        List<ExchangeInfo> exchanges = ExchangeServer.registry.getPossibleExchanges(userData);
                        sendObject(exchanges);
                    }
                }
                else if (obj instanceof ExchangeRequest) {
                    ExchangeRequest req = (ExchangeRequest) obj;
                    if (req.isAccepted()) {
                        ClientHandler sender = ExchangeServer.registry.getClient(req.getFromUser());
                        if (sender != null) {
                            sender.sendObject(req);
                        }
                    } else {
                        ClientHandler other = ExchangeServer.registry.getClient(req.getToUser());
                        if (other != null) {
                            other.sendObject(req);
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Klijent diskonektovan.");
            if (userData != null) {
                ExchangeServer.registry.removeClient(userData.getUsername());
            }
        }
    }
}