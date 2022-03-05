package ch.bettelini.app.application.views;

import ch.bettelini.app.application.ChatBinding;
import ch.bettelini.app.terminal.TerminalView;

public class LoginView extends TerminalView {

    private String username;
    private String password;

    private ChatBinding client;

    public LoginView(ChatBinding client) {
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
            render();
        } else if (password == null) {
            password = input;
            
            // login
        }
    }

    private void auth() {
    }

    private void printCursor() {
        super.newLine();
        super.print("> ");
    }
    
}
