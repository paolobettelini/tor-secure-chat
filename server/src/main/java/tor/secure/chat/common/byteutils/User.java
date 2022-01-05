package tor.secure.chat.common.byteutils;

public record User(String username, byte[] password, byte[] publicKey, byte[] privateKey) {
    
}
