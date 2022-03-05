package ch.bettelini.client;

import ch.bettelini.common.byteutils.MessageData;
import ch.bettelini.common.concurrency.BlockingMap;
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
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public abstract class Client extends Thread {

    // Protocol codes mirror for application usage
    public static final byte CONNECTION_ERROR              = Protocol.CONNECTION_ERROR;
    public static final byte USER_NOT_FOUND_ERROR          = Protocol.USER_NOT_FOUND_ERROR;
    public static final byte USERNAME_ALREADY_EXISTS_ERROR = Protocol.USERNAME_ALREADY_EXISTS_ERROR;
    public static final byte WRONG_PASSWORD_ERROR          = Protocol.WRONG_PASSWORD_ERROR;
    public static final byte ALREADY_LOGGED_ERROR          = Protocol.ALREADY_LOGGED_ERROR;
    public static final byte SUCCESSFUL_LOGIN_CODE         = Protocol.SUCCESSFUL_LOGIN_CODE;
    public static final byte INVALID_USERNAME              = Protocol.INVALID_USERNAME;

    // Connection
    private String address;
    private int port;
    private PacketInputStream in;
    private PacketOutputStream out;
    private boolean connected = false;

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

    protected abstract void onCode(int statusCode);
    protected abstract void onMessage(String sender, String receiver, String message, long timestamp);

    @Override
    public void run() {
        try (Socket socket = new Socket(address, port)) {
            in = new PacketInputStream(socket.getInputStream());
            out = new PacketOutputStream(socket.getOutputStream());
            connected = true;

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
            onCode(Protocol.CONNECTION_ERROR);
            connected = false;
        }
    }

    public boolean isConnected() {
        return connected;
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
            return;
        }

        //onCode(Protocol.SUCCESSFUL_LOGIN_CODE);
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

        // If request is already on the way
        if (blockingMap.contains(username)) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    while (true) {
                        if (keysCache.containsKey(username)) {
                            return keysCache.get(username);
                        }

                        Thread.sleep(25);
                    }
                } catch (InterruptedException e) {
                    return null;
                }
            });
        }

        blockingMap.createQueue(username);

        // send request
        sendPacket(RequestPublicKeyPacket.create(username));

        return CompletableFuture.supplyAsync(() -> {
            try {
                byte[] bytes = blockingMap.get(username);

                PublicKey key = Protocol.Crypto.getPublicKey(bytes);
                
                keysCache.putIfAbsent(username, key);
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
            if (message.sender().equals(username)) {
                // I am the sender
                // Check signature
                if (!Protocol.Crypto.verify(message.messageForReceiver(), message.signature(), keyPair.getPublic())) {
                    System.out.println("Bad signature1");
                    return;
                }
                byte[] key = Protocol.Crypto.decryptAsimmetrically(message.keyForSender(), keyPair.getPrivate());
                byte[] content = Protocol.Crypto.decryptSymmetrically(message.messageForSender(), key);
                onMessage(message.sender(), message.receiver(), new String(content), message.timestamp());
            } else {
                retrievePublicKey(message.sender()).thenAccept(senderPublicKey -> {
                    // Check signature
                    if (!Protocol.Crypto.verify(message.messageForReceiver(), message.signature(), senderPublicKey)) {
                        System.out.println("Bad signatur2e for " + message.sender());
                        return;
                    }
    
                    byte[] key = Protocol.Crypto.decryptAsimmetrically(message.keyForReceiver(), keyPair.getPrivate());
                    byte[] content = Protocol.Crypto.decryptSymmetrically(message.messageForReceiver(), key);
                
                    onMessage(message.sender(), message.receiver(), new String(content), message.timestamp());
                });

            }
        }
    }

    private void processErrorPacket(byte[] data) {
        ErrorPacket packet = new ErrorPacket(data);

        onCode(packet.statusCode());
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
        if (!Protocol.isUsernameValid(username)) {
            onCode(Protocol.INVALID_USERNAME);
            return;
        }
        
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
            byte[] randomPassword1 = Protocol.Crypto.generateSecurePassword();
            byte[] randomPassword2 = Protocol.Crypto.generateSecurePassword();
            byte[] encryptedKeyForReceiver = Protocol.Crypto.encryptAsimmetrically(randomPassword1, receiverPublicKey);
            byte[] encryptedMessageForReceiver = Protocol.Crypto.encryptSymmetrically(message.getBytes(), randomPassword1);
            byte[] signature = Protocol.Crypto.sign(encryptedMessageForReceiver, keyPair.getPrivate());
            byte[] encryptedKeyForSender = Protocol.Crypto.encryptAsimmetrically(randomPassword2, keyPair.getPublic());
            byte[] encryptedMessageForSender = Protocol.Crypto.encryptSymmetrically(message.getBytes(), randomPassword2);

            sendSendMessagePacket(receiver, encryptedKeyForReceiver, encryptedMessageForReceiver, signature, encryptedKeyForSender, encryptedMessageForSender);
        });
    }

    private void sendSendMessagePacket(String receiver, byte[] keyForReceiver, byte[] messageForReceiver, byte[] signature, byte[] keyForSender, byte[] messageForSender) {
        sendPacket(SendMessagePacket.create(receiver, keyForReceiver, messageForReceiver, signature, keyForSender, messageForSender));
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

    public String getUsername() {
        return username;
    }

    public static boolean isUsernameValid(String username) {
        return Protocol.isUsernameValid(username);
    }

}
