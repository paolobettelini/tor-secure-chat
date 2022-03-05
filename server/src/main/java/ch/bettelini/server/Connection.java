package ch.bettelini.server;

import ch.bettelini.common.byteutils.MessageData;
import ch.bettelini.common.byteutils.UserData;
import ch.bettelini.common.stream.PacketInputStream;
import ch.bettelini.common.stream.PacketOutputStream;
import ch.bettelini.protocol.Protocol;
import ch.bettelini.protocol.packets.ErrorPacket;
import ch.bettelini.protocol.packets.LoginPacket;
import ch.bettelini.protocol.packets.RegisterPacket;
import ch.bettelini.protocol.packets.RequestNonRepudiationProofPacket;
import ch.bettelini.protocol.packets.RequestPublicKeyPacket;
import ch.bettelini.protocol.packets.SendMessagePacket;
import ch.bettelini.protocol.packets.ServeKeyPairPacket;
import ch.bettelini.protocol.packets.ServeMessagesPacket;
import ch.bettelini.protocol.packets.ServeNonRepudiationProofPacket;
import ch.bettelini.protocol.packets.ServePublicKeyPacket;

import java.io.IOException;
import java.net.Socket;
import java.security.PublicKey;

public class Connection extends Thread {

    private final Socket client;
    private final Server server;
    private PacketInputStream in;
    private PacketOutputStream out;

    private String username;
    private boolean authenticated;
    private PublicKey publicKey;
    private byte[] authenticationNonce; // the user must sign this nonce for non-repudiation

    public Connection(Server server, Socket client) {
        this.client = client;
        this.server = server;
        this.authenticated = false;
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

    public boolean isAuthenticated() {
        return authenticated;
    }

    private void processPacket(byte[] data) {
        switch (data[0]) {
            case Protocol.REGISTER -> processRegisterPacket(data);
            case Protocol.LOGIN -> processLoginPacket(data);
            case Protocol.SEND_MESSAGE -> processSendMessagePacket(data);
            case Protocol.REQUEST_PUB_KEY -> processRequestPublicKeyPacket(data);
            case Protocol.SERVE_NON_REPUDIATION_PROOF -> processServeNonRepudiationProofPacket(data);
        }
    }

    private void processRequestPublicKeyPacket(byte[] data) {
        RequestPublicKeyPacket packet = new RequestPublicKeyPacket(data);

        if (!server.databaseManager.isUsernameInUse(packet.getUsername())) {
            sendError(Protocol.USER_NOT_FOUND_ERROR);
            return;
        }

        UserData user = server.databaseManager.getUser(packet.getUsername());
        sendPacket(ServePublicKeyPacket.create(packet.getUsername(), user.publicKey()));
    }

    private void processSendMessagePacket(byte[] data) {
        if (!authenticated) {
            System.out.println("NOT AUTH");
            return; // not logged in
        }

        SendMessagePacket packet = new SendMessagePacket(data);

        if (packet.getReceiver().equals(username)) {
            System.out.println("Writing to yourself buddy?");
            return;
        }

        if (!server.databaseManager.isUsernameInUse(packet.getReceiver())) {
            sendError(Protocol.USER_NOT_FOUND_ERROR);
            return;
        }

        MessageData message = new MessageData(username, packet.getReceiver(), System.currentTimeMillis(), packet.getKeyForReceiver(), packet.getMessageForReceiver(), packet.getSignature(), packet.getKeyForSender(), packet.getMessageForSender());

        // Try to contact
        server.forwardPacket(ServeMessagesPacket.create(message), packet.getReceiver());
        
        // Store in db
        server.databaseManager.storeMessage(message);
    }

    private void processLoginPacket(byte[] data) {
        LoginPacket packet = new LoginPacket(data);

        UserData user = server.databaseManager.getUser(packet.getUsername());

        if (user == null) {
            sendError(Protocol.USER_NOT_FOUND_ERROR);
            return;
        }

        byte[] inputPassword = packet.getPassword();
        byte[] actualPassword = user.password();

        if (inputPassword.length != actualPassword.length) {
            sendError(Protocol.WRONG_PASSWORD_ERROR);
            return;
        }

        for (int i = 0; i < actualPassword.length; i++) {
            if (inputPassword[i] != actualPassword[i]) {
                sendError(Protocol.WRONG_PASSWORD_ERROR);
                return;
            }
        }

        if (server.isUserOnline(packet.getUsername())) {
            sendError(Protocol.ALREADY_LOGGED_ERROR);
            return;
        }

        login(packet.getUsername());

        // send key
        sendPacket(ServeKeyPairPacket.create(user.publicKey(), user.privateKey()));
        
        this.publicKey = Protocol.Crypto.getPublicKey(user.publicKey());
        
        // ask for non-repudiation proof
        this.authenticationNonce = RequestNonRepudiationProofPacket.randomNonce();
        sendPacket(RequestNonRepudiationProofPacket.create(authenticationNonce));
    }
    
    private void processServeNonRepudiationProofPacket(byte[] data) {
        ServeNonRepudiationProofPacket packet = new ServeNonRepudiationProofPacket(data);

        byte[] signature = packet.getSignedNonce();
        if (!Protocol.Crypto.verify(authenticationNonce, signature, publicKey)) {
            try {
                client.close();
            } catch (IOException e) {}
            System.out.println("Wrong signature");
            return;
        }

        authenticate();

        // send messages
        MessageData[] messages = server.databaseManager.getMessagesFor(username);
        System.out.println("Messages to send; " + messages.length);
        // TODO read db piece by piece
        
        if (messages.length != 0) {
            // MAX 3 at a time
            final int N = 3;
            int remaining = messages.length;

            while (remaining > 0) {
                int amount = Math.min(N, remaining);
                
                MessageData[] group = new MessageData[amount];
                for (int j = 0; j < group.length; j++) {
                    group[j] = messages[j + messages.length - remaining];
                }

                sendPacket(ServeMessagesPacket.create(group));
                remaining -= amount;
            }
        }
    }

    private void processRegisterPacket(byte[] data) {
        RegisterPacket packet = new RegisterPacket(data);

        if (server.databaseManager.isUsernameInUse(packet.getUsername())) {
            sendError(Protocol.USERNAME_ALREADY_EXISTS_ERROR);
            return;
        }
    
        if (!Protocol.isUsernameValid(packet.getUsername())) {
            System.out.println("INVALID USERNAME");
            return;
        }

        login(packet.getUsername());

        authenticate();
        
        // send key
        server.databaseManager.registerUser(username, packet.getPassword(), packet.getPublicKey(), packet.getPrivateKey());
        
        this.publicKey = Protocol.Crypto.getPublicKey(packet.getPublicKey());

        // send user data as confirmation
        sendPacket(ServeKeyPairPacket.create(packet.getPublicKey(), packet.getPrivateKey()));
    }

    private void login(String username) {
        if (this.username != null) {
            server.removeUser(this.username);
            authenticated = false; // still need non-repudiation
        }

        this.username = username;
        server.addUser(username, this); // notify server
    }

    private void authenticate() {
        authenticated = true;
        // TODO: Rename to StatusCodePacket
        sendPacket(ErrorPacket.create(Protocol.SUCCESSFUL_LOGIN_CODE));
    }

}