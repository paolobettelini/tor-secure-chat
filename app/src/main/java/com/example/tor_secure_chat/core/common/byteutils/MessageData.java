package com.example.tor_secure_chat.core.common.byteutils;

import static com.example.tor_secure_chat.core.common.byteutils.ByteUtils.*;

public class MessageData {

    private final String sender;
    private final String receiver;
    private final long timestamp;
    private final byte[] key;
    private final byte[] message;
    private final byte[] signature;

    public MessageData(String sender, String receiver, long timestamp, byte[] key, byte[] message, byte[] signature) {
        this.sender = sender;
        this.receiver = receiver;
        this.timestamp = timestamp;
        this.key = key;
        this.message = message;
        this.signature = signature;
    }

    public String sender() {
        return sender;
    }

    public String receiver() {
        return receiver;
    }

    public long timestamp() {
        return timestamp;
    }

    public byte[] key() {
        return key;
    }

    public byte[] message() {
        return message;
    }

    public byte[] signature() {
        return signature;
    }

    // key = RSA(random_password, receiver_publicKey)
    // message = AES/CBC(random_password, plain_message)
    // signature = SHA256withRSA(message, sender_privateKey)

    public void write(byte[] data, Offset offset) {
        writeString(data, sender, offset);
        writeString(data, receiver, offset);
        writeLongLE(data, timestamp, offset);
        writeBlob(data, key, offset);
        writeBlob(data, message, offset);
        writeBlob(data, signature, offset);
    }

    public static void write(MessageData message, byte[] data, Offset offset) {
        message.write(data, offset);
    }

    public static MessageData read(byte[] data, Offset offset) {
        String sender = readString(data, offset);
        String receiver = readString(data, offset);
        long timestamp = readLongLE(data, offset);
        byte[] key = readBlob(data, offset);
        byte[] message = readBlob(data, offset);
        byte[] signature = readBlob(data, offset);

        return new MessageData(sender, receiver, timestamp, key, message, signature);
    }

    public int computeSpace() {
        return sender.length() + receiver.length() + key.length + message.length + signature.length + 22;
    }

    public static int computeSpace(MessageData message) {
        return message.computeSpace();
    }

}
