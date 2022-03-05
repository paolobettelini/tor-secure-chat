package ch.bettelini.app;

import ch.bettelini.app.application.ChatApplication;

public class Main {
    
    public static void main(String[] args) {
        new ChatApplication().start("127.0.0.1", 6666);
    }
    
}