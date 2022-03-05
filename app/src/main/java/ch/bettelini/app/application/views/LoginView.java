package ch.bettelini.app.application.views;

import java.util.function.Consumer;

import ch.bettelini.app.application.ChatBinding;
import ch.bettelini.app.terminal.TerminalView;

public class LoginView extends TerminalView {

    private String username;
    private String password;

    private ContactView contactView;

    private ChatBinding client;

    private boolean invalidUsername;
    private String error;

    public LoginView(ChatBinding client, ContactView contactView) {
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
        } else if (password == null) {
            super.println("Enter password:");
            printCursor();
        }
    }

    @Override
    protected void input(String input) {
        // Error handling
        if (invalidUsername) {
            if (input.toLowerCase().equals("y")) {
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
        } else if (password == null) {
            password = input;
            
            auth();
        }
    }

    private void auth() {
        super.clear();
        super.println("Authenticating...");
        printCursor();

        client.login(username, password);
    }

    private void printCursor() {
        super.newLine();
        super.print("> ");
    }

    private Consumer<Integer> onStatusCode = code -> {
        if (code != ChatBinding.SUCCESSFUL_LOGIN_CODE) {
            error = ChatBinding.statusCodeToString(code);
            super.clear();
            super.println("Error: " + error);
            super.newLine();
            super.println("Do you want to retry? [y/N]");
            printCursor();
            username = password = null;
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
        client.removeStatusCodeListener(onStatusCode); // Remove listener
        invalidUsername = false;
        username = password = error = null;
    }
    
}
