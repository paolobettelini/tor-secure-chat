package ch.bettelini.app.application.views;

import java.util.function.Consumer;

import ch.bettelini.app.application.ChatBinding;
import ch.bettelini.app.terminal.TerminalView;

public class RegisterView extends TerminalView {

    private String username;
    private String password1;
    private String password2;

    private ContactView contactView;

    private ChatBinding client;

    private boolean differentPasswords;
    private boolean invalidUsername;
    private String error;

    public RegisterView(ChatBinding client, ContactView contactView) {
        this.client = client;
        this.contactView = contactView;
    }

    public void setContactView(ContactView contactView) {
        this.contactView = contactView;
    }

    public void setChatBiding(ChatBinding client) {
        this.client = client;
    }

    @Override
    protected void render() {
        super.clear();
        
        if (username == null) {
            super.println("Enter username:");
            printCursor();
        } else if (password1 == null) {
            super.println("Enter password:");
            printCursor();
        } else if (password2 == null) {
            super.println("Repeat password:");
            printCursor();
        }
    }

    @Override
    protected void input(String input) {
        // Error handling
        if (differentPasswords || invalidUsername) {
            if (input.toLowerCase().equals("y")) {
                differentPasswords = false;
                invalidUsername = false;
                render();
            } else if (input.toLowerCase().equals("n")) {
                // Go back
                super.setView(parentView);
            }
            return;
        }

        if (error != null) {
            if (input.toLowerCase().equals("y")) {
                error = null;
                render();
            } else if (input.toLowerCase().equals("n")) {
                // Go back
                super.setView(parentView);
            }
            return;
        }

        // Input handling
        if (username == null) {
            username = input;

            if (!ChatBinding.isUsernameValid(username)) {
                super.clear();
                super.println("Invalid username!");
                super.newLine();
                super.println("Do you want to retry? [y/N]");
                printCursor();
                username = null;
                invalidUsername = true;
                return;
            }
            render();

        } else if (password1 == null) {
            password1 = input;
            render();
        } else if (password2 == null) {
            password2 = input;

            if (!password2.equals(password1)) {
                super.clear();
                super.println("Passwords are not the same");
                super.newLine();
                super.println("Do you want to retry? [y/N]");
                printCursor();
                password1 = password2 = null;
                differentPasswords = true;
                return;
            }

            auth();
        }
    }

    private void auth() {
        super.clear();
        super.println("Generating keys...");
        printCursor();

        // Send register packet
        client.register(username, password1);
    }

    private void printCursor() {
        super.newLine();
        super.print("> ");
    }

    private Consumer<Integer> onStatusCode = code -> {
        if (code != ChatBinding.SUCCESSFUL_LOGIN_CODE) {
            error = ChatBinding.statusCodeToString(code);
            super.clear();
            super.println("Server Error: " + error);
            super.newLine();
            super.println("Do you want to retry? [y/N]");
            printCursor();
            username = password1 = password2 = null;
        } else {
            super.setView(contactView, parentView); // Keep same parent
        }
    };

    @Override
    public void onDisplay() {
        client.addStatusCodeListener(onStatusCode);
    }

    @Override
    protected void onConceal() {
        differentPasswords = invalidUsername = false;
        username = password1 = password2 = error = null;
        client.removeStatusCodeListener(onStatusCode); // Remove listener
    }
    
}
