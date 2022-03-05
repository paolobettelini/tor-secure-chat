package ch.bettelini.app.application.views;

import ch.bettelini.app.terminal.TerminalView;

public class StartView extends TerminalView {

    private RegisterView registerView;
    private LoginView loginView;
    
    public void setRegisterView(RegisterView registerView) {
        this.registerView = registerView;
    }

    public void setLoginView(LoginView loginView) {
        this.loginView = loginView;
    }

    @Override
    protected void render() {
        super.clear();

        super.newLine();
        super.println("SELECT CHOICE");
        super.newLine();
        super.println("\t1. Login");
        super.println("\t2. Register");
        super.newLine();
        super.print("> ");
    }

    @Override
    protected void input(String input) {
        switch (input.trim()) {
            case "1", "login" -> super.setView(loginView);
            case "2", "register" -> super.setView(registerView);
            case "exit" -> System.exit(0);
            default -> render();
        }
    }
    
}