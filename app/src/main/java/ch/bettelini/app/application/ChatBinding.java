package ch.bettelini.app.application;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import ch.bettelini.client.Client;

public class ChatBinding extends Client {

    private List<Consumer<Integer>> statusCodeListeners = new LinkedList<>();
    private List<Consumer<Message>> messageListeners = new LinkedList<>();

    public ChatBinding(String address, int port) {
        super(address, port);
    }

    @Override
    protected void onCode(int statusCode) {
        statusCodeListeners.forEach(consumer -> consumer.accept(statusCode));
    }

    @Override
    protected void onMessage(String sender, String receiver, String message, long timestamp) {
        messageListeners.forEach(consumer -> consumer.accept(new Message(sender, receiver, message, timestamp)));
    }

    public void addStatusCodeListener(Consumer<Integer> onStatusCode) {
        this.statusCodeListeners.add(onStatusCode);
    }

    public void addMessageListener(Consumer<Message> onMessage) {
        this.messageListeners.add(onMessage);
    }

    public void removeStatusCodeListener(Consumer<Integer> onStatusCode) {
        this.statusCodeListeners.remove(onStatusCode);
    }

    public void removeMessageListener(Consumer<Message> onMessage) {
        this.messageListeners.remove(onMessage);
    }

    public static String statusCodeToString(int code) {
        return switch (code) {
            case CONNECTION_ERROR               -> "CONNECTION_ERROR";
            case USER_NOT_FOUND_ERROR           -> "USER_NOT_FOUND_ERROR";
            case USERNAME_ALREADY_EXISTS_ERROR  -> "USERNAME_ALREADY_EXISTS_ERROR";
            case WRONG_PASSWORD_ERROR           -> "WRONG_PASSWORD_ERROR";
            case ALREADY_LOGGED_ERROR           -> "ALREADY_LOGGED_ERROR";
            case SUCCESSFUL_LOGIN_CODE          -> "SUCCESSFUL_LOGIN_CODE";
            case INVALID_USERNAME               -> "INVALID_USERNAME";
            default                             -> "Unknown";
        };
    }

}
