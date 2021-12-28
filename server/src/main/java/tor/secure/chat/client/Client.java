package tor.secure.chat.client;

import java.io.IOException;
import java.net.Socket;

import tor.secure.chat.common.stream.PacketInputStream;
import tor.secure.chat.common.stream.PacketOutputStream;
import tor.secure.chat.protocol.Protocol;
import tor.secure.chat.protocol.packets.LoginPacket;
import tor.secure.chat.protocol.packets.RegisterPacket;
import tor.secure.chat.protocol.packets.RequestPublicKeyPacket;

public class Client extends Thread {

    private String address;
    private int port;

    private PacketInputStream in;
    private PacketOutputStream out;

    private String username;
    private byte[] password;
    private byte[] publicKey;
    private byte[] privateKey;

    public Client(String address, int port) {
        this.address = address;
        this.port = port;
    }

    @Override
    public void start() {
        try (Socket socket = new Socket(address, port)) {
            in = new PacketInputStream(socket.getInputStream());
            out = new PacketOutputStream(socket.getOutputStream());

            while (!in.hasEnded()) {
                byte[] packet = in.nextPacket();
                processPacket(packet);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processPacket(byte[] packet) {
        switch (packet[0]) {
            case Protocol.ERROR -> processErrorPacket(packet);
            case Protocol.SERVE_MESSAGES -> processServeMessagesPacket(packet);
            case Protocol.SERVE_PGP_KEYS -> processServePGPKeysPacket(packet);
            case Protocol.SERVE_PUB_KEY -> processServePublicKeyPacket(packet);
        }
    }

    private void processServePGPKeysPacket(byte[] packet) {
        
    }

    private void processServePublicKeyPacket(byte[] packet) {
        
    }

    private void processServeMessagesPacket(byte[] packet) {
        
    }

    private void processErrorPacket(byte[] packet) {
        
    }

    public void sendLoginPacket(String username, byte[] password) {
        sendPacket(LoginPacket.create(username, password));
    }

    public void sendRegisterPacket(String username, byte[] password, byte[] publicKey, byte[] privateKey) {
        sendPacket(RegisterPacket.create(username, password, publicKey, privateKey));
    }

    public void sendRequestPublicKeyPacket(String username) {
        sendPacket(RequestPublicKeyPacket.create(username));
    }

    private void sendPacket(byte[] packet) {
        try {
            out.writePacket(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // error -> status code
    // (+ register confirm)

}
