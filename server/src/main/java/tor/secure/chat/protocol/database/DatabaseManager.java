package tor.secure.chat.protocol.database;

import java.sql.ResultSet;

public class DatabaseManager {
    
    private DatabaseConnection database;

    private static final String SQL = """
        CREATE SCHEMA IF NOT EXISTS chat;
        USE chat;

        CREATE TABLE IF NOT EXISTS user (
            username VARCHAR(25),
            pass BINARY(32),        -- SHA256(SHA256(pass))
            pub_key BLOB,
            priv_key BLOB           -- AES(SHA256(pass), priv_key)
        );

        CREATE TABLE IF NOT EXISTS message (
            'datetime' DATETIME,
            sender_username VARCHAR(25),
            receiver_username VARCHAR(25),
            msg BLOB                -- RSA(pub_key, msg)
        );
    """;

    public DatabaseManager(DatabaseConnection database) {
        this.database = database;

        database.execute(SQL);
    }

    public boolean isUsernameInUse(String username) {
        return false;
    }

    public void registerUser(String username, byte[] password, byte[] publicKey, byte[] privateKey) {

    }

    public ResultSet getUser(String username) {
        return null;        
    }

    public void addMessage(String sender, String receiver, byte[] msg) {

    }

    public ResultSet getMessagesFor(String username) {
        return null;
    }

    public byte[] getPublicKey(String username) {
        return null;
    }

}
