package server;

import model.ExchangeInfo;
import model.UserData;

import java.util.*;

public class ClientRegistry {

    private final Map<String, ClientHandler> clients = Collections.synchronizedMap(new HashMap<>());

    public void addClient(String username, ClientHandler handler) {
        clients.put(username, handler);
    }

    public void removeClient(String username) {
        clients.remove(username);
    }

    public ClientHandler getClient(String username) {
        return clients.get(username);
    }

    public synchronized List<ExchangeInfo> getPossibleExchanges(UserData current) {
        List<ExchangeInfo> result = new ArrayList<>();

        for (String username : clients.keySet()) {
            if (username.equals(current.getUsername())) {
                continue;
            }

            ClientHandler handler = clients.get(username);
            UserData other = handler.getUserData();

            List<Integer> iGive = new ArrayList<>();
            List<Integer> heGives = new ArrayList<>();

            for (Integer d : current.getDuplicates()) {
                if (other.getMissing().contains(d)) {
                    iGive.add(d);
                }
            }

            for (Integer d : other.getDuplicates()) {
                if (current.getMissing().contains(d)) {
                    heGives.add(d);
                }
            }

            if (!iGive.isEmpty() && !heGives.isEmpty()) {
                result.add(new ExchangeInfo(other.getUsername(), iGive, heGives));
            }
        }
        return result;
    }
}