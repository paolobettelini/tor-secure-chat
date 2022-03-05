package ch.bettelini.app.application;

import ch.bettelini.app.application.views.LoginView;
import ch.bettelini.app.application.views.RegisterView;
import ch.bettelini.app.application.views.StartView;
import ch.bettelini.app.terminal.TerminalApplication;

public class ChatApplication {

    public void start(String address, int port) {
        var client = new ChatBinding(address, port);
        client.start();

        var app = new TerminalApplication("\t[SECURE CHAT] - " + address + ":" + port);

        var startView = new StartView();
        var loginView = new LoginView(client);
        var registerView = new RegisterView(client);

        app.addView(startView);
        app.addView(loginView);
        app.addView(registerView);

        startView.setLoginView(loginView);
        startView.setRegisterView(registerView);
        
        app.setView(startView);

        app.start();
    }
    
}
