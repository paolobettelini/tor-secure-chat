package tor.secure.chat.common.byteutils;

public record UserData(String username, byte[] password, byte[] publicKey, byte[] privateKey) {
    
}
