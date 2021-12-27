package tor.secure.chat.server;

import java.io.IOException;
import java.net.Socket;

import tor.secure.chat.protocol.Protocol;
import tor.secure.chat.protocol.packets.LoginPacket;
import tor.secure.chat.protocol.packets.RegisterPacket;
import tor.secure.chat.utils.stream.PacketInputStream;
import tor.secure.chat.utils.stream.PacketOutputStream;

public class Connection extends Thread {

    private final Socket client;
    private Server server;
    private PacketInputStream in;
    private PacketOutputStream out;

    private String username;

    public Connection(Server server, Socket client) {
        this.client = client;
        this.server = server;
    }

    @Override
    public void start() {
        try (client) {
            this.in = new PacketInputStream(client.getInputStream());
            this.out = new PacketOutputStream(client.getOutputStream());

            while (!in.hasEnded()) {
                byte[] packet = in.nextPacket();
                processPacket(packet);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (username != null) {
            server.removeUser(username);
        }
    }

    public void processPacket(byte[] data) {
        switch (data[0]) {
            case Protocol.REGISTER -> {
                RegisterPacket packet = new RegisterPacket(data);
                
                // if already exists
                // send error code

                // if username is invalid
                // return

                // add to database
            }
            case Protocol.LOGIN -> {

            }
            case Protocol.MESSAGE_SENT -> {

            }
        }
    }

}
