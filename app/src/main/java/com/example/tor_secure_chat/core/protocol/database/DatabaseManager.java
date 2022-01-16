package com.example.tor_secure_chat.core.protocol.database;

import com.example.tor_secure_chat.core.common.byteutils.MessageData;
import com.example.tor_secure_chat.core.common.byteutils.UserData;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

public class DatabaseManager {
    
    private DatabaseConnection database;

    private static final String TABLE1 = """
        CREATE TABLE IF NOT EXISTS user (
            username VARCHAR(25) PRIMARY KEY,
            pass BLOB,
            pub_key BLOB,
            priv_key BLOB
        );
    """;

    private static final String TABLE2 = """
        CREATE TABLE IF NOT EXISTS message (
            sender_username VARCHAR(25),
            receiver_username VARCHAR(25),
            datetime DATETIME,
            msg_key BLOB,
            msg BLOB,
            msg_sig BLOB,
            FOREIGN KEY (sender_username)
                REFERENCES user(username),
            FOREIGN KEY (receiver_username)
                REFERENCES user(username)
        );
    """;

    public DatabaseManager(DatabaseConnection database) {
        this.database = database;

        database.execute(TABLE1);
        database.execute(TABLE2);
    }

    public boolean isUsernameInUse(String username) {
        ResultSet result = database.query("SELECT username FROM user WHERE username='" + username + "' LIMIT 1;");
        try {
            return result.first();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void registerUser(String username, byte[] password, byte[] publicKey, byte[] privateKey) {
        PreparedStatement statement = database.prepareStatement(
            "INSERT INTO user VALUES (?,?,?,?);");

        try {
            statement.setString(1, username);
            statement.setBlob(2, database.createBlob(password));
            statement.setBlob(3, database.createBlob(publicKey));
            statement.setBlob(4, database.createBlob(privateKey));

            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void storeMessage(MessageData message) {
        PreparedStatement statement = database.prepareStatement(
            "INSERT INTO message VALUES (?,?,?,?,?,?);");

        try {
            statement.setString(1, message.sender());
            statement.setString(2, message.receiver());
            statement.setTimestamp(3, new Timestamp(message.timestamp()));
            statement.setBlob(4, database.createBlob(message.key()));
            statement.setBlob(5, database.createBlob(message.message()));
            statement.setBlob(6, database.createBlob(message.signature()));

            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public MessageData[] getMessagesFor(String username, boolean clear) {
        ResultSet result = database.query("SELECT * FROM message WHERE receiver_username='" + username + "';");
        
        try {
            List<MessageData> messages = new LinkedList<>();

            while (result.next()) {
                String sender = result.getString(1);
                String receiver = result.getString(2);
                long timestamp = result.getTimestamp(3).getTime();
                byte[] key = result.getBytes(4);
                byte[] message = result.getBytes(5);
                byte[] signature = result.getBytes(6);
                
                messages.add(new MessageData(sender, receiver, timestamp, key, message, signature));
            }

            if (clear) {
                database.execute("DELETE FROM message WHERE receiver_username='" + username + "';");
            }

            return messages.toArray(new MessageData[messages.size()]);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    
    public UserData getUser(String username) {
        ResultSet result = database.query("SELECT pass, pub_key, priv_key FROM user WHERE username='" + username + "' LIMIT 1;");
        
        try {
            if (!result.first()) {
                return null;
            }
            
            byte[] password = result.getBytes(1);
            byte[] publicKey = result.getBytes(2);
            byte[] privateKey = result.getBytes(3);

            return new UserData(username, password, publicKey, privateKey);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

}
