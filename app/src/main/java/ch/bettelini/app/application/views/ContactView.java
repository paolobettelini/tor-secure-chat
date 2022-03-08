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
            int unread = contacts.get(contact).getUnread();
            super.println("\t" + i++ + ". " + contact + (unread != 0 ? " [" + unread + "]" : ""));
        }
        printCursor();
    }

    private Consumer<Message> onMessage = message -> {
        ContactData data; // TODO: simplify spaghetti code
        if (message.sender().equals(client.getUsername())) {
            if (!contacts.containsKey(message.receiver())) {
                data = new ContactData();
                computeFingerprint(data, message.receiver(), chatView);
                contacts.put(message.receiver(), data);
            } else {
                data = contacts.get(message.receiver());
            }
        } else {
            if (!contacts.containsKey(message.sender())) {
                data = new ContactData();
                computeFingerprint(data, message.sender(), chatView);
                contacts.put(message.sender(), data);
            } else {
                data = contacts.get(message.sender());
            }
        }
        data.addMessage(message);

        // Update chatView if it is visible and has the same sender
        // Otherwise try to update this view
        if (!message.sender().equals(chatView.getSender()) || !super.updateIfCurrent(chatView)) {
            // Mark as unread
            data.incrementUnread();

            super.updateIfCurrent(this);
        }
    };

    private Consumer<Message> onMessageSent = message -> {
        contacts.get(message.receiver()).addMessage(message);
        super.updateIfCurrent(chatView);
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
                    data.resetUnread();
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

        ContactData data;

        if (!contacts.containsKey(input)) {
            data = new ContactData();
            computeFingerprint(data, input, chatView);
            contacts.put(input, data);
        } else {
            data = contacts.get(input);
        }

        chatView.setReceiver(client.getUsername());
        chatView.setSender(input);
        chatView.setMessages(data.getMessages());
        chatView.setFingerprint(data.getFingerprint());
        data.resetUnread();
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

    /**
     * Computes the fingerprint asynchronously and adds it to
     * the given <code>ContactData</code> object.
     * A <code>ChatView</code> is also passed to be updated after computing
     * the fingerprint (if the view is visible).
     * 
     * @param data the <code>ContactData</code> object
     * @param username the username of the interlocutor
     * @param view the view to try to update
     */
    private void computeFingerprint(ContactData data, String username, ChatView view) {
        client.getChatFingerprint(username).thenAccept(fingerprint -> {
            data.setFingerprint(fingerprint);
            view.setFingerprint(fingerprint);
            super.updateIfCurrent(view);
        });
    }

    private static class ContactData {
        
        private List<Message> messages = new LinkedList<>();

        private String fingerprint;
        
        private int unread;

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

        public int getUnread() {
            return unread;
        }

        public void incrementUnread() {
            ++unread;
        }

        public void resetUnread() {
            unread = 0;
        }

    }

}
