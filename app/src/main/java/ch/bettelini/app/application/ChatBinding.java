package ch.bettelini.app.application;

import java.util.function.Consumer;

import ch.bettelini.client.Client;

public class ChatBinding extends Client {

    private Consumer<Integer> onStatusCode;
    private Consumer<Message> onMessage;

    public ChatBinding(String address, int port) {
        super(address, port);
    }

    @Override
    protected void onCode(int statusCode) {
        onStatusCode.accept(statusCode);
    }

    @Override
    protected void onMessage(String sender, String message, long timestamp) {
        onMessage.accept(new Message(sender, message, timestamp));
    }

    public void setOnStatusCode(Consumer<Integer> onStatusCode) {
        this.onStatusCode = onStatusCode;
    }

    public void setOnMessage(Consumer<Message> onMessage) {
        this.onMessage = onMessage;
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
