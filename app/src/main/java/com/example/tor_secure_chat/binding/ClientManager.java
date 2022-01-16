package com.example.tor_secure_chat.binding;

import com.example.tor_secure_chat.core.client.Client;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ClientManager {

    private static Map<String, List<Message>> messages;

    private static Client client;

    static {
        messages = new HashMap<>();

        // Test messages
        List<Message> temp1 = new LinkedList<>();
        temp1.add(new Message("Marco", "Ciao!", System.currentTimeMillis(), false));
        temp1.add(new Message("Marco", "<3", System.currentTimeMillis(), false));
        messages.put("Marco", temp1);

        List<Message> temp2 = new LinkedList<>();
        temp2.add(new Message("Giorgio", "Ciao sono Giogio", System.currentTimeMillis(), false));
        temp2.add(new Message("Giorgio", "rispondi", System.currentTimeMillis(), false));
        temp2.add(new Message("Giorgio", "pls :(", System.currentTimeMillis(), false));
        messages.put("Giorgio", temp2);

        if (client == null) {
            initClient();
        }
    }

    private static BlockingQueue<Integer> nextCode;
    public static BlockingQueue<Integer> waitNextCode() {
        return nextCode = new ArrayBlockingQueue<>(1);
    }

    private static Runnable onNewMessage;
    public static void setOnNewMessage(Runnable onNewMessage) {
        ClientManager.onNewMessage = onNewMessage;
    }

    public static void initClient() {
        client = new Client("192.168.1.115", 6666) {
            @Override
            protected void onCode(int statusCode) {
                if (nextCode != null && !nextCode.isEmpty()) {
                    nextCode.add(statusCode);
                }
            }

            @Override
            protected void onMessage(String sender, String message, long timestamp) {
                getMessagesFor(sender).add(new Message(sender, message, timestamp, false));
                if (onNewMessage != null) {
                    onNewMessage.run();
                }
            }
        };

        //client.start();
    }

    public static boolean isConnected() {
        return client.isConnected();
    }

    public static Map<String, List<Message>> getMessages() {
        return messages;
    }

    public static List<Message> getMessagesFor(String username) {
        if (!messages.containsKey(username)) {
            messages.put(username, new LinkedList<>());
        }

        return messages.get(username);
    }

    public static void sendMessage(String receiver, String message) {
        client.sendMessage(receiver, message);
        getMessagesFor(receiver).add(new Message(receiver, message, System.currentTimeMillis(), true));
    }

    public static void login(String username, String password) {
        client.login(username, password);
    }

    public static void register(String username, String password) {
        client.register(username, password);
    }

}
