package tor.secure.chat.client;

import java.io.IOException;
import java.net.Socket;
import java.security.Key;
import java.security.KeyPair;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.ArrayBlockingQueue;

import tor.secure.chat.common.Message;
import tor.secure.chat.common.byteutils.CryptoUtils;
import tor.secure.chat.common.stream.PacketInputStream;
import tor.secure.chat.common.stream.PacketOutputStream;
import tor.secure.chat.protocol.Protocol;
import tor.secure.chat.protocol.packets.LoginPacket;
import tor.secure.chat.protocol.packets.RegisterPacket;
import tor.secure.chat.protocol.packets.RequestPublicKeyPacket;
import tor.secure.chat.protocol.packets.SendMessagePacket;
import tor.secure.chat.protocol.packets.ServeMessagesPacket;
import tor.secure.chat.protocol.packets.ServePGPKeysPacket;
import tor.secure.chat.protocol.packets.ServePublicKeyPacket;

public class Client extends Thread {

    public static void main(String[] args) {
        var keyPair = CryptoUtils.generateKeyPair();
        
        CryptoUtils.getPublicKey(keyPair.getPublic().getEncoded());
        CryptoUtils.getPrivateKey(keyPair.getPrivate().getEncoded());
    }

    // Connection
    private String address;
    private int port;
    private PacketInputStream in;
    private PacketOutputStream out;

    // Account
    private String username;
    private byte[] password;
    private Key publicKey;
    private Key privateKey;

    // Wait for public PGP key
    private ArrayBlockingQueue<byte[]> blockingQueue;

    public Client(String address, int port) {
        this.address = address;
        this.port = port;
        this.blockingQueue = new ArrayBlockingQueue<>(1);
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(address, port)) {
            in = new PacketInputStream(socket.getInputStream());
            out = new PacketOutputStream(socket.getOutputStream());

            while (!in.hasEnded()) {
                byte[] packet = in.nextPacket();

                if (packet != null) {
                    processPacket(packet);
                }
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

    private void processServePGPKeysPacket(byte[] data) {
        ServePGPKeysPacket packet = new ServePGPKeysPacket(data);

        this.publicKey = CryptoUtils.getPublicKey(packet.getPublicKey());
        this.privateKey = CryptoUtils.getPrivateKey(
            //CryptoUtils.decryptAES(packet.getPrivateKey(), password)
            Protocol.Crypto.decryptSymmetrically(packet.getPrivateKey(), password)
        );

        System.out.println("Successfully logged in");

        // Login/register successful
    }

    private void processServePublicKeyPacket(byte[] data) {
        ServePublicKeyPacket packet = new ServePublicKeyPacket(data);

        blockingQueue.add(packet.getPublicKey());
    }

    public Key retrievePublicKey(String username) {
        sendPacket(RequestPublicKeyPacket.create(username));

        try {
            byte[] bytes = blockingQueue.take();
            return CryptoUtils.getPublicKey(bytes);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void processServeMessagesPacket(byte[] data) {
        ServeMessagesPacket packet = new ServeMessagesPacket(data);

        Message[] messages = packet.getMessages();

        for (Message message : messages) {
            //String content = new String(CryptoUtils.decryptRSA(message.message(), privateKey));
            String content = new String(Protocol.Crypto.decryptAsimmetrically(message.message(), privateKey));
            System.out.println(Instant.ofEpochMilli(message.timestamp()) + "[" + message.sender() + "] " + content);
        }
    }

    private void processErrorPacket(byte[] packet) {
        System.out.println("Received error packet");
    }

    private void sendLoginPacket(String username, byte[] password) {
        sendPacket(LoginPacket.create(username, password));
    }

    public void login(String username, String password) {
        byte[] passwordBytes = password.getBytes();
        sendLoginPacket(username, Protocol.Crypto.hash(passwordBytes));

        this.username = username;
        this.password = passwordBytes;
    }

    public void register(String username, String password) {
        KeyPair pair = CryptoUtils.generateKeyPair();

        byte[] passwordBytes = password.getBytes();
        byte[] passwordHash = Protocol.Crypto.hash(passwordBytes);
        byte[] publicKey = pair.getPublic().getEncoded();
        //byte[] privateKey = CryptoUtils.encryptAES(pair.getPrivate().getEncoded(), passwordBytes);
        byte[] privateKey = Protocol.Crypto.encryptSymmetrically(pair.getPrivate().getEncoded(), passwordBytes);

        sendRegisterPacket(username, passwordHash, publicKey, privateKey);
        this.username = username;
        this.password = passwordBytes;
    }

    private void sendRegisterPacket(String username, byte[] password, byte[] publicKey, byte[] privateKey) {
        sendPacket(RegisterPacket.create(username, password, publicKey, privateKey));
    }

    private void sendPacket(byte[] packet) {
        try {
            out.writePacket(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String receiver, String message) {
        if (privateKey == null || username == null) {
            return; // not logged in
        }

        Key publicKey = retrievePublicKey(receiver);

        //byte[] encryptedMessage = CryptoUtils.encryptRSA(message.getBytes(), publicKey);
        byte[] encryptedMessage = Protocol.Crypto.encryptAsimmetrically(message.getBytes(), publicKey);
        sendSendMessagePacket(receiver, encryptedMessage);
    }

    private void sendSendMessagePacket(String receiver, byte[] message) {
        sendPacket(SendMessagePacket.create(receiver, message));
    }
    
    // username, SHA256(SHA256(pass)), publicKey, AES_CBC(128left(SHA256(pass)), 128right(SHA256(pass)), privateKey)
}
