package tor.secure.chat.server;

import java.io.IOException;
import java.net.Socket;
import java.security.PublicKey;

import tor.secure.chat.common.byteutils.CryptoUtils;
import tor.secure.chat.common.byteutils.MessageData;
import tor.secure.chat.common.byteutils.UserData;
import tor.secure.chat.common.stream.PacketInputStream;
import tor.secure.chat.common.stream.PacketOutputStream;
import tor.secure.chat.protocol.Protocol;
import tor.secure.chat.protocol.packets.ErrorPacket;
import tor.secure.chat.protocol.packets.LoginPacket;
import tor.secure.chat.protocol.packets.RegisterPacket;
import tor.secure.chat.protocol.packets.RequestNonRepudiationProofPacket;
import tor.secure.chat.protocol.packets.RequestPublicKeyPacket;
import tor.secure.chat.protocol.packets.SendMessagePacket;
import tor.secure.chat.protocol.packets.ServeMessagesPacket;
import tor.secure.chat.protocol.packets.ServeNonRepudiationProofPacket;
import tor.secure.chat.protocol.packets.ServeKeyPairPacket;
import tor.secure.chat.protocol.packets.ServePublicKeyPacket;

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
            case Protocol.SERVE_NON_REPUDIATION_PROOF -> processServeNonRepudiationProofPaclet(data);
        }
    }

    private void processRequestPublicKeyPacket(byte[] data) {
        RequestPublicKeyPacket packet = new RequestPublicKeyPacket(data);

        if (!server.databaseManager.isUsernameInUse(packet.getUsername())) {
            sendError(Protocol.USER_NOT_FOUND_ERROR);
            return;
        }

        UserData user = server.databaseManager.getUser(packet.getUsername());

        sendPacket(ServePublicKeyPacket.create(user.publicKey()));
    }

    private void processSendMessagePacket(byte[] data) {
        if (!authenticated) {
            return; // not logged in
        }

        System.out.println("Received msg send packet");
        SendMessagePacket packet = new SendMessagePacket(data);

        if (packet.getReceiver().equals(username)) {
            return;
        }

        if (!server.databaseManager.isUsernameInUse(packet.getReceiver())) {
            sendError(Protocol.USER_NOT_FOUND_ERROR);
            return;
        }

        MessageData message = new MessageData(username, packet.getReceiver(), System.currentTimeMillis(), packet.getMessage());

        if (!server.forwardPacket(ServeMessagesPacket.create(message), packet.getReceiver())) {
            server.databaseManager.storeMessage(message); // store if receiver is offline or isn't authenticated
        }
    }

    private void processLoginPacket(byte[] data) {
        LoginPacket packet = new LoginPacket(data);

        UserData user = server.databaseManager.getUser(packet.getUsername());

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
        
        this.publicKey = CryptoUtils.getPublicKey(user.publicKey());
        
        System.out.println("asking for authentication"); // ask for non-repudiation proof
        this.authenticationNonce = RequestNonRepudiationProofPacket.randomNonce();
        sendPacket(RequestNonRepudiationProofPacket.create(authenticationNonce));
    }
    
    private void processServeNonRepudiationProofPaclet(byte[] data) {
        ServeNonRepudiationProofPacket packet = new ServeNonRepudiationProofPacket(data);

        byte[] signature = packet.getSignedNonce();
        if (!CryptoUtils.verify(authenticationNonce, signature, publicKey)) {
            try {
                client.close();
            } catch (IOException e) {}
            System.out.println("False signature");
            return;
        }

        System.out.println("User authenticated");
        authenticated = true;

        // send unread messages
        MessageData[] messages = server.databaseManager.getMessagesFor(username, true);
        System.out.println("Messages to send; " + messages.length);
        if (messages.length != 0) {
            sendPacket(ServeMessagesPacket.create(messages));
        }
    }

    private void processRegisterPacket(byte[] data) {
        RegisterPacket packet = new RegisterPacket(data);

        if (server.databaseManager.isUsernameInUse(packet.getUsername())) {
            sendError(Protocol.USERNAME_ALREADY_EXISTS_ERROR);
            return;
        }
    
        if (packet.getUsername().length() > 25) {// && regex &&
            return;
        }

        login(packet.getUsername());
        
        // send key
        server.databaseManager.registerUser(username, packet.getPassword(), packet.getPublicKey(), packet.getPrivateKey());
        
        this.publicKey = CryptoUtils.getPublicKey(packet.getPublicKey());

        // send user data as confirmation
        System.out.println("sending data to user");
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

}

/**
 
    Send to server
    username, password, publicKey, privateKey

    Problem 0
    General security
    Solution
    Messages must not be persistent
    Messages must be stored by the server only if the receiver is offline

    Problem 1
    The server knows the ip of the interlocutors
    Solution:
    Send everything through the Tor network

    Problem 2
    The server can decrypt messages using the privateKey
    Solution:
    Simmetrically encrypt the privateKey with the password
    Problem 2.1
    The server can decrypt the privateKey using the password
    Solution:
    Instead of sending the password, send the hash

    Problem 3
    Two identical password result in the same hash
    Solution:
    Salt the password with the username

    Problem 4
    Man-in-the-middle: the server could generate a key pair,
    send his public key instead of request one and gain control over every message sent
    Solution:
    Out-of-band verification
    Sender and receiver must bith compute a value using their publicKey and the other end's publicKey
    such that f(publicKeyA, publicKeyB) = f(publicKeyB, publicKeyA)
    This value is then converted into something readable (e.g. 4 emojis)
    The interlocutors must then verify that they are the same
    A user must must check if the keypair is valid

    Problem 5
    The exit node can sniff traffic data (e.g. sniff hash(password)):
    - Can login with someone's elses account
        - Delete the user unread messages
        - Send messages pretending to be the user
        (can't read incoming messages, therefore deleting them from the server)
    Solution:
    The server must send the user some random data to sign to prove that he has the private key

    Problem 6
    The server can send messages pretending to be somebody
    Solution
    Every message sent must be signed with the privateKey (and then verified by the receiver)

    XXX authentication
    XXX servepgp -> servekeypair
    XXX salt password
    XXX Se l'utente è già online e qualcuno prova a loggarsi?
    Sign all messages
    regex username check
    usare Protocol.Crypto invece che cryptoutils
    encrypt messages with AES with random password
    prevent server exception

    Don't include your username in your password
    Don't send sensitive messages if the receiver is not online
 */