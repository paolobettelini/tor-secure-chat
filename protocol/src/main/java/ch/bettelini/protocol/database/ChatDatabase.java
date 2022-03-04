package ch.bettelini.protocol.database;

import ch.bettelini.common.byteutils.MessageData;
import ch.bettelini.common.byteutils.UserData;

public interface ChatDatabase {
    
    public boolean isUsernameInUse(String username);

    public void registerUser(String username, byte[] password, byte[] publicKey, byte[] privateKey);
    
    public void storeMessage(MessageData message);
    
    public MessageData[] getMessagesFor(String username, boolean clear);
    
    public UserData getUser(String username);

}
