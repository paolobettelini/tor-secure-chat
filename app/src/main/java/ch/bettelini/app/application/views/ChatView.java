package ch.bettelini.app.application.views;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import ch.bettelini.app.application.ChatBinding;
import ch.bettelini.app.application.Message;
import ch.bettelini.app.terminal.TerminalView;

public class ChatView extends TerminalView {

    private static int WIDTH = 50;

    private List<Message> messages;

    private String sender;
    private String receiver; // me

    private String fingerprint;

    private ChatBinding client;

    private List<Consumer<Message>> messageSentListeners = new LinkedList<>();

    public ChatView(ChatBinding client) {
        this.client = client;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setChatBiding(ChatBinding client) {
        this.client = client;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void addMessageSentListener(Consumer<Message> listener) {
        messageSentListeners.add(listener);
    }

    public void removeMessageSentListener(Consumer<Message> listener) {
        messageSentListeners.remove(listener);
    }

    @Override
    protected void render() {
        super.clear();
        super.println("\t[" + sender + "] - " + fingerprint);
        super.newLine();
        for (Message message : messages) {
            super.print(format(message.message(), !message.sender().equals(receiver)));
        }
        printCursor();
    }

    @Override
    protected void input(String input) {
        // Go back
        if (input.toLowerCase().equals("exit")) {
            super.setView(parentView);
            return;
        }

        // Send message
        messageSentListeners.forEach(
            consumer -> consumer.accept(new Message(receiver, sender, input, System.currentTimeMillis())));
        client.sendMessage(sender, input);
    }

    @Override
    protected void onDisplay() {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void onConceal() {
        
    }

    private void printCursor() {
        super.newLine();
        super.print("> ");
    }
    
    private static String center(String v) {
        return " ".repeat((WIDTH >> 1) + (v.length() >> 1)) + v;
    }

    private static String format(String v, boolean left) {
        StringBuilder builder = new StringBuilder();
        int thoThirds = WIDTH * 2 / 3;

        int pos = 0;
        while (v.length() - pos > 0) {
            int remaining = v.length() - pos;
            int length = Math.min(remaining, thoThirds);
            builder.append((left ? " " : " ".repeat(WIDTH - length))
                + v.substring(pos, pos += length));
            builder.append("\n\r");
        }

        return builder.toString();
    }

}