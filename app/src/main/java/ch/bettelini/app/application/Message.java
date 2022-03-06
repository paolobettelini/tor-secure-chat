package ch.bettelini.app.application;

public record Message(String sender, String receiver, String message, long timestamp) implements Comparable<Message> {

    @Override
    public int compareTo(Message other) {
        return other.timestamp < timestamp ? 1 : -1;
    }

    
}
