package tor.secure.chat.server;

import java.io.IOException;
import java.net.Socket;

import tor.secure.chat.common.Message;
import tor.secure.chat.common.User;
import tor.secure.chat.common.stream.PacketInputStream;
import tor.secure.chat.common.stream.PacketOutputStream;
import tor.secure.chat.protocol.Protocol;
import tor.secure.chat.protocol.packets.ErrorPacket;
import tor.secure.chat.protocol.packets.LoginPacket;
import tor.secure.chat.protocol.packets.RegisterPacket;
import tor.secure.chat.protocol.packets.RequestPublicKeyPacket;
import tor.secure.chat.protocol.packets.SendMessagePacket;
import tor.secure.chat.protocol.packets.ServeMessagesPacket;
import tor.secure.chat.protocol.packets.ServePGPKeysPacket;
import tor.secure.chat.protocol.packets.ServePublicKeyPacket;

public class Connection extends Thread {

    private final Socket client;
    private final Server server;
    private PacketInputStream in;
    private PacketOutputStream out;

    private String username;

    public Connection(Server server, Socket client) {
        this.client = client;
        this.server = server;
    }

    @Override
    public void run() {
        try (client) {
            this.in = new PacketInputStream(client.getInputStream());
            this.out = new PacketOutputStream(client.getOutputStream());

            System.out.println("Listening to new connection");
            while (!in.hasEnded()) {
                byte[] packet = in.nextPacket();

                if (packet != null) {
                    processPacket(packet);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("No more listening connection");

        if (username != null) {
            server.removeUser(username);
        }
    }

    void sendPacket(byte[] packet) {
        try {
            out.writePacket(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void sendError(byte code) {
        sendPacket(ErrorPacket.create(code));
    }

    private void processPacket(byte[] data) {
        switch (data[0]) {
            case Protocol.REGISTER -> processRegisterPacket(data);
            case Protocol.LOGIN -> processLoginPacket(data);
            case Protocol.SEND_MESSAGE -> processSendMessagePacket(data);
            case Protocol.REQUEST_PUB_KEY -> processRequestPublicKeyPacket(data);
        }
    }

    private void processRequestPublicKeyPacket(byte[] data) {
        RequestPublicKeyPacket packet = new RequestPublicKeyPacket(data);

        if (!server.databaseManager.isUsernameInUse(packet.getUsername())) {
            sendError(ErrorPacket.USER_NOT_FOUND);
            return;
        }

        User user = server.databaseManager.getUser(packet.getUsername());

        sendPacket(ServePublicKeyPacket.create(user.publicKey()));
    }

    private void processSendMessagePacket(byte[] data) {
        if (username == null) {
            return; // not logged in
        }

        System.out.println("Received msg send packet");
        SendMessagePacket packet = new SendMessagePacket(data);

        Message message = new Message(username, packet.getReceiver(), System.currentTimeMillis(), packet.getMessage());

        server.forwardPacket(ServeMessagesPacket.create(message), packet.getReceiver());
        
        server.databaseManager.storeMessage(message); // only if receiver is not online
    }

    private void processLoginPacket(byte[] data) {
        LoginPacket packet = new LoginPacket(data);

        System.out.println("received login packet");

        User user = server.databaseManager.getUser(packet.getUsername());

        byte[] inputPassword = packet.getPassword();
        byte[] actualPassword = user.password();

        if (inputPassword.length != actualPassword.length) {
            sendError(ErrorPacket.WRONG_PASSWORD);
            return;
        }

        for (int i = 0; i < actualPassword.length; i++) {
            if (inputPassword[i] != actualPassword[i]) {
                sendError(ErrorPacket.WRONG_PASSWORD);
                return;
            }
        }

        login(packet.getUsername());
        System.out.println("sending pgp keys");
        sendPacket(ServePGPKeysPacket.create(user.publicKey(), user.privateKey()));

        // send messages
        Message[] messages = server.databaseManager.getMessagesFor(username);
        if (messages.length != 0) {
            sendPacket(ServeMessagesPacket.create(messages));
        }

        // delete messages from db
    }

    private void processRegisterPacket(byte[] data) {
        RegisterPacket packet = new RegisterPacket(data);
        System.out.println("Received register packet");
        if (server.databaseManager.isUsernameInUse(packet.getUsername())) {
            sendError(ErrorPacket.USERNAME_ALREADY_EXISTS);
            return;
        }
    
        if (packet.getUsername().length() > 25) {// && regex &&
            return;
        }

        login(packet.getUsername());
        
        // send pgp keys
        server.databaseManager.registerUser(username, packet.getPassword(), packet.getPublicKey(), packet.getPrivateKey());
        
        // send user data as confirmation
        System.out.println("sending data to user");
        sendPacket(ServePGPKeysPacket.create(packet.getPublicKey(), packet.getPrivateKey()));
    }

    private void login(String username) {
        if (this.username != null) {
            server.removeUser(this.username);
        }

        this.username = username;
        server.addUser(username, this); // notify server
    }

}