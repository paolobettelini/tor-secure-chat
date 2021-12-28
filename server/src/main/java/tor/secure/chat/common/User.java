package tor.secure.chat.common;

public record User(String username, byte[] password, byte[] publicKey, byte[] privateKey) {
    
}
