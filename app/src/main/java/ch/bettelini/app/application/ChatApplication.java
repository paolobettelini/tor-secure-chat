package ch.bettelini.app.application;

import ch.bettelini.app.application.views.ChatView;
import ch.bettelini.app.application.views.ContactView;
import ch.bettelini.app.application.views.LoginView;
import ch.bettelini.app.application.views.RegisterView;
import ch.bettelini.app.application.views.StartView;
import ch.bettelini.app.terminal.TerminalApplication;

public class ChatApplication {

    public void start(String address, int port) {
        var client = new ChatBinding(address, port);
        client.start();

        var app = new TerminalApplication("\t[SECURE CHAT] - " + address + ":" + port);

        var chatView = new ChatView(client);
        var contactView = new ContactView(client, chatView);
        var loginView = new LoginView(client, contactView);
        var registerView = new RegisterView(client, contactView);
        var startView = new StartView(registerView, loginView);

        app.addView(startView);
        app.addView(loginView);
        app.addView(registerView);
        app.addView(contactView);
        app.addView(chatView);

        app.setView(startView);

        app.start();
    }
    
}
