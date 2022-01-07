package tor.secure.chat.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import tor.secure.chat.protocol.database.DatabaseConnection;
import tor.secure.chat.protocol.database.DatabaseManager;

public class Server extends Thread {
    
	private static final String databaseHost = "192.168.1.111";
	private static final String databasePort = "3306";
	private static final String databaseUser = "serveruser";
	private static final String databaseName = "chat";
	private static final String databasePassword = "pass";

    private int port;
    private Map<String, Connection> users;
    DatabaseManager databaseManager;

    public Server(int port) {
        this.port = port;
        this.users = new HashMap<>();
        this.databaseManager = new DatabaseManager(
            new DatabaseConnection(databaseHost, databasePort, databaseUser, databaseName, databasePassword));
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket client = serverSocket.accept();
                Connection connection = new Connection(this, client);
                connection.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void addUser(String username, Connection connection) {
        users.put(username, connection);
    }

    void removeUser(String username) {
        users.remove(username);
    }

    boolean isUserOnline(String username) {
        return users.containsKey(username);
    }

    boolean forwardPacket(byte[] packet, String receiver) {
        boolean sent = isUserOnline(receiver);
        
        if (sent) {
            var user = users.get(receiver);

            if (sent &= user.isAuthenticated()) {
                users.get(receiver).sendPacket(packet);
                System.out.println("Forwarding message ");
            } else {
                System.out.println("Not forwaring :)");
            }
        }

        return sent;
    }

    public Set<String> getUsers() {
        return users.keySet();
    }

}
