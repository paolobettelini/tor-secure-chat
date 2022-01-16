package com.example.tor_secure_chat.binding;

public class Message {

    private String interlocutor, message;
    private long timestamp;
    private boolean sent;

    public Message(String interlocutor, String message, long timestamp, boolean sent) {
        this.interlocutor = interlocutor;
        this.message = message;
        this.timestamp = timestamp;
        this.sent = sent;
    }

    public String interlocutor() {
        return interlocutor;
    }

    public String message() {
        return message;
    }

    public long timestamp() {
        return timestamp;
    }

    public boolean sent() {
        return sent;
    }



}
