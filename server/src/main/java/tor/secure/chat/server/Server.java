package tor.secure.chat.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import tor.secure.chat.protocol.database.DatabaseConnection;
import tor.secure.chat.protocol.database.DatabaseManager;

public class Server {
    
	private static final String databaseHost = "192.168.1.111";
	private static final String databasePort = "3306";
	private static final String databaseUser = "admin";
	private static final String databaseName = "chat";
	private static final String databasePassword = "Password&1";

    private int port;
    private Map<String, Connection> users;
    DatabaseManager databaseManager;

    public Server(int port) {
        this.port = port;
        this.users = new HashMap<>();
        this.databaseManager = new DatabaseManager(
            new DatabaseConnection(databaseHost, databasePort, databaseUser, databaseName, databasePassword));
    }

    public void start() {
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

    void forwardPacket(byte[] packet, String receiver) {
        if (users.containsKey(receiver)) {
            users.get(receiver).sendPacket(packet);
        }
    }

    public Set<String> getUsers() {
        return users.keySet();
    }

}
