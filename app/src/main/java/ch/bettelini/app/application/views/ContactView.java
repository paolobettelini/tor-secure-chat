package ch.bettelini.app.application.views;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import ch.bettelini.app.application.ChatBinding;
import ch.bettelini.app.application.Message;
import ch.bettelini.app.terminal.TerminalView;

/*
TODO:
fingerprint is null after you register
when you register messages are double
*/

public class ContactView extends TerminalView {

    private ChatBinding client;

    private ChatView chatView;

    private Map<String, ContactData> contacts;

    private boolean newUser = true;

    private String error;
    private boolean showErrorMessage;

    public ContactView(ChatBinding client, ChatView chatView) {
        this.client = client;
        this.chatView = chatView;
    }

    public void setChatView(ChatView chatView) {
        this.chatView = chatView;
    }

    public void setChatBiding(ChatBinding client) {
        this.client = client;
    }

    @Override
    protected void render() {
        super.clear();
        
        super.println("Type a username or an index to chat");
        super.newLine();
        int i = 0;
        for (String contact : contacts.keySet()) {
            super.println("\t" + i++ + ". " + contact);
        }
        printCursor();
    }

    private Consumer<Message> onMessage = message -> {
        if (message.sender().equals(client.getUsername())) {
            if (!contacts.containsKey(message.receiver())) {
                ContactData data = new ContactData();
                computeFingerprint(data, message.receiver());
                contacts.put(message.receiver(), data);
            }
            contacts.get(message.receiver()).addMessage(message);
        } else {
            if (!contacts.containsKey(message.sender())) {
                ContactData data = new ContactData();
                computeFingerprint(data, message.sender());
                contacts.put(message.sender(), data);
            }
            contacts.get(message.sender()).addMessage(message);
        }

        // Update if chatView is visible and has the same sender
        if (message.sender().equals(chatView.getSender())) {
            super.update(chatView);
        }
        super.update(this);
    };

    private Consumer<Message> onMessageSent = message -> {
        contacts.get(message.receiver()).addMessage(message);
        super.update(chatView);
    };

    private Consumer<Integer> onStatusCode = code -> {
        showErrorMessage = true;
        error = ChatBinding.statusCodeToString(code);
        super.setView(this);
    };

    @Override
    protected void input(String input) {
        // Error handling
        if (showErrorMessage) {
            super.println("Server error: " + error);
            super.newLine();
            super.println("Do you want to continue? [y/N]");
            printCursor();
            showErrorMessage = false;
            return;
        }

        if (error != null) {
            if (input.toLowerCase().equals("y")) {
                error = null;
                render();
            } else if (input.toLowerCase().equals("n")) {
                stopListening();
                super.setView(parentView);
            }
            return;
        }

        // Input handling
        if (input.toLowerCase().equals("exit")) {
            stopListening();
            super.setView(parentView);
            return;
        }

        try {
            // Input is index
            int index = Integer.parseInt(input);
            if (index < 0 || index >= contacts.size()) {
                return;
            }

            int i = 0;
            for (String contact : contacts.keySet()) {
                if (i++ == index) {
                    var data = contacts.get(contact);
                    chatView.setReceiver(client.getUsername());
                    chatView.setSender(contact);
                    chatView.setMessages(data.getMessages());
                    chatView.setFingerprint(data.getFingerprint());
                    super.setView(chatView, this);
                    return;
                }
            }
        } catch (NumberFormatException e) {}

        // Input is username

        if (input.equals(client.getUsername())) {
            // Message to myself
            return;
        }

        if (!contacts.containsKey(input)) {
            ContactData data = new ContactData();
            computeFingerprint(data, input);
            contacts.put(input, data);
        }

        var data = contacts.get(input);
        chatView.setReceiver(client.getUsername());
        chatView.setSender(input);
        chatView.setMessages(data.getMessages());
        chatView.setFingerprint(data.getFingerprint());
        super.setView(chatView, this);
    }

    @Override
    protected void onDisplay() {
        if (newUser) {
            this.contacts = new LinkedHashMap<>();
            newUser = false;
            client.addMessageListener(onMessage);
            chatView.addMessageSentListener(onMessageSent);
            client.addStatusCodeListener(onStatusCode);
        }
        
    }

    @Override
    protected void onConceal() {
        
    }

    private void printCursor() {
        super.newLine();
        super.print("> ");
    }
    
    private void stopListening() {
        newUser = true;
        client.removeMessageListener(onMessage);
        client.removeStatusCodeListener(onStatusCode);
    }

    private void computeFingerprint(ContactData data, String username) {
        client.getChatFingerprint(username).thenAccept(fingerprint -> {
            data.setFingerprint(fingerprint);
        });
    }

    private static class ContactData {
        
        private List<Message> messages = new LinkedList<>();

        private String fingerprint;
        
        //private int unread;

        public List<Message> getMessages() {
            return messages;
        }

        public void setFingerprint(String fingerprint) {
            this.fingerprint = fingerprint;
        }

        public String getFingerprint() {
            return fingerprint;
        }

        public void addMessage(Message message) {
            messages.add(message);

            Collections.sort(messages);
        }

    }

}
