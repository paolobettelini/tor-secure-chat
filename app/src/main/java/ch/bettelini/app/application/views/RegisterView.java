package ch.bettelini.app.application.views;

import ch.bettelini.app.application.ChatBinding;
import ch.bettelini.app.terminal.TerminalView;

public class RegisterView extends TerminalView {

    private String username;
    private String password1;
    private String password2;

    private ChatBinding client;

    private boolean differentPasswords;
    private boolean invalidUsername;
    private String error;

    public RegisterView(ChatBinding client) {
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
        if (differentPasswords || invalidUsername) {
            differentPasswords = false;
            invalidUsername = false;
            render();
            return;
        }

        if (error != null) {
            error = null;
            render();
            return;
        }

        if (input.toLowerCase().equals("exit")) {
            if (parentView == null) {
                System.exit(0);
            } else {
                super.setView(parentView);
                return;
            }
        }

        if (username == null) {
            username = input;

            if (!ChatBinding.isUsernameValid(username)) {
                super.clear();
                super.println("Invalid username!");
                super.newLine();
                super.println("Do you want to retry? [y]");
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
                super.println("Do you want to retry? [y]");
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
        
        client.setOnStatusCode(code -> {
            if (code != ChatBinding.SUCCESSFUL_LOGIN_CODE) {
                error = ChatBinding.statusCodeToString(code);
                super.clear();
                super.println("Error: " + error);
                super.newLine();
                super.println("Do you want to retry? [y]");
                printCursor();
                username = password1 = password2 = null;
            } else {
                System.out.println("registered");
            }
        });

        client.register(username, password1);
    }

    private void printCursor() {
        super.newLine();
        super.print("> ");
    }
    
}
