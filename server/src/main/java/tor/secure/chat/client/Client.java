package tor.secure.chat.client;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import tor.secure.chat.common.byteutils.MessageData;
import tor.secure.chat.common.concurrency.BlockingMap;
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

public abstract class Client extends Thread {

    // Connection
    private String address;
    private int port;
    private PacketInputStream in;
    private PacketOutputStream out;

    // Account
    private String username;
    private byte[] password;
    private KeyPair keyPair;

    // Cache
    private Map<String, PublicKey> keysCache;

    // Wait for public keys
    private BlockingMap<String, byte[]> blockingMap;

    public Client(String address, int port) {
        this.address = address;
        this.port = port;
        this.keysCache = new HashMap<>();
        this.blockingMap = new BlockingMap<>();
    }

    abstract void onError(int statusCode);
    abstract void onMessage(String sender, String message, long timestamp);

    @Override
    public void run() {
        try (Socket socket = new Socket(address, port)) {
            in = new PacketInputStream(socket.getInputStream());
            out = new PacketOutputStream(socket.getOutputStream());

            while (!in.hasEnded()) {
                if (isInterrupted()) {
                    break;
                }

                byte[] packet = in.nextPacket();

                if (packet != null) {
                    processPacket(packet);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.gc(); // Nothing happened :)
    }

    private void processPacket(byte[] packet) {
        switch (packet[0]) {
            case Protocol.ERROR -> processErrorPacket(packet);
            case Protocol.SERVE_MESSAGES -> processServeMessagesPacket(packet);
            case Protocol.SERVE_KEY_PAIR -> processServeKeyPairPacket(packet);
            case Protocol.SERVE_PUB_KEY -> processServePublicKeyPacket(packet);
            case Protocol.REQUEST_NON_REPUDIATION_PROOF -> processRequestNonRepudiationProofPacket(packet);
        }
    }

    private void processRequestNonRepudiationProofPacket(byte[] data) {
        RequestNonRepudiationProofPacket packet = new RequestNonRepudiationProofPacket(data);

        byte[] signature = Protocol.Crypto.sign(packet.getNonce(), keyPair.getPrivate());

        sendPacket(ServeNonRepudiationProofPacket.create(signature));
    }

    private void processServeKeyPairPacket(byte[] data) {
        ServeKeyPairPacket packet = new ServeKeyPairPacket(data);

        PublicKey publicKey = Protocol.Crypto.getPublicKey(packet.getPublicKey());
        PrivateKey privateKey = Protocol.Crypto.getPrivateKey(
            Protocol.Crypto.decryptSymmetrically(packet.getPrivateKey(), password)
        );

        this.keyPair = new KeyPair(publicKey, privateKey);
        
        if (!Protocol.Crypto.isKeyPairValid(keyPair)) {
            System.out.println("Invalid key pair! The server might be trying a MITM attack");            
            return;
        }

        System.out.println("Successfully logged in");
    }

    private void processServePublicKeyPacket(byte[] data) {
        ServePublicKeyPacket packet = new ServePublicKeyPacket(data);

        blockingMap.put(packet.getUsername(), packet.getPublicKey());
    }

    public CompletableFuture<PublicKey> retrievePublicKey(String username) {
        // take from cache
        if (keysCache.containsKey(username)) {
            return CompletableFuture.completedFuture(keysCache.get(username));
        }

        // send request
        sendPacket(RequestPublicKeyPacket.create(username));

        return CompletableFuture.supplyAsync(() -> {
            try {
                byte[] bytes = blockingMap.get(username);

                PublicKey key = Protocol.Crypto.getPublicKey(bytes);
                
                keysCache.put(username, key);
                return key;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    
            return null;
        });
    }

    private void processServeMessagesPacket(byte[] data) {
        ServeMessagesPacket packet = new ServeMessagesPacket(data);

        MessageData[] messages = packet.getMessages();
        
        for (MessageData message : messages) {
            // Verify signature
            retrievePublicKey(message.sender()).thenAccept(senderPublicKey -> {
                if (!Protocol.Crypto.verify(message.message(), message.signature(), senderPublicKey)) {
                    System.out.println("Message with invalid signature from " + message.sender());
                    return;
                }
                
                byte[] key = Protocol.Crypto.decryptAsimmetrically(message.key(), keyPair.getPrivate());
                byte[] content = Protocol.Crypto.decryptSymmetrically(message.message(), key);
                onMessage(message.sender(), new String(content), message.timestamp());
            });
        }
    }

    private void processErrorPacket(byte[] data) {
        ErrorPacket packet = new ErrorPacket(data);

        onError(packet.statusCode());
    }

    private void sendLoginPacket(String username, byte[] password) {
        sendPacket(LoginPacket.create(username, password));
    }

    public void login(String username, String password) {
        byte[] passwordBytes = password.getBytes();
        sendLoginPacket(username, Protocol.Crypto.hash(Protocol.Crypto.salt(passwordBytes, username.getBytes())));

        this.username = username;
        this.password = passwordBytes;
    }

    public void register(String username, String password) {
        KeyPair pair = Protocol.Crypto.generateKeyPair();

        byte[] passwordBytes = password.getBytes();
        byte[] passwordHash = Protocol.Crypto.hash(Protocol.Crypto.salt(passwordBytes, username.getBytes()));
        byte[] publicKey = pair.getPublic().getEncoded();
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
        if (keyPair == null || username == null) {
            return; // not logged in
        }

        retrievePublicKey(receiver).thenAccept(receiverPublicKey -> {
            byte[] randomPassword = Protocol.generateSecurePassword();
            byte[] encryptedKey = Protocol.Crypto.encryptAsimmetrically(randomPassword, receiverPublicKey);
            byte[] encryptedMessage = Protocol.Crypto.encryptSymmetrically(message.getBytes(), randomPassword);
            byte[] signature = Protocol.Crypto.sign(encryptedMessage, keyPair.getPrivate());
            sendSendMessagePacket(receiver, encryptedKey, encryptedMessage, signature);
        });
    }

    private void sendSendMessagePacket(String receiver, byte[] key, byte[] message, byte[] signature) {
        sendPacket(SendMessagePacket.create(receiver, key, message, signature));
    }

    public CompletableFuture<String> getChatFingerprint(String interlocutor) {
        return CompletableFuture.supplyAsync(() -> {
            PublicKey interlocutorPublicKey;
            try {
                interlocutorPublicKey = retrievePublicKey(interlocutor).get();
            } catch (InterruptedException | ExecutionException e) {
                return null;
            }
            return Protocol.Crypto.computeFingerprint(
                keyPair.getPublic().getEncoded(), interlocutorPublicKey.getEncoded());
        });
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }
    
    // username, SHA256(SHA256(pass)), publicKey, AES_CBC(128left(SHA256(pass)), 128right(SHA256(pass)), privateKey)
}
