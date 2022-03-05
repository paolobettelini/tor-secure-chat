package ch.bettelini.app.application;

public record Message(String sender, String receiver, String message, long timestamp) {
    
}
