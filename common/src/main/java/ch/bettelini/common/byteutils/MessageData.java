package ch.bettelini.common.byteutils;

import static ch.bettelini.common.byteutils.ByteUtils.*;

public class MessageData {

    private final String sender;
    private final String receiver;
    private final long timestamp;
    private final byte[] keyForReceiver; // RSA(random_password1, receiver_publicKey)
    private final byte[] messageForReceiver; // AES/CBC(random_password1, plain_message)
    private final byte[] signature; // SHA256withRSA(messageForReceiver, sender_privateKey)
    private final byte[] keyForSender; // RSA(random_password2, sender_publicKey)
    private final byte[] messageForSender; // AES/CBC(random_password2, plain_message)

    public MessageData(String sender, String receiver, long timestamp, byte[] key, byte[] message, byte[] signature, byte[] keyForSender, byte[] messageForSender) {
        this.sender = sender;
        this.receiver = receiver;
        this.timestamp = timestamp;
        this.keyForReceiver = key;
        this.messageForReceiver = message;
        this.signature = signature;
        this.keyForSender = keyForSender;
        this.messageForSender = messageForSender;
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

    public byte[] keyForReceiver() {
        return keyForReceiver;
    }

    public byte[] messageForReceiver() {
        return messageForReceiver;
    }

    public byte[] signature() {
        return signature;
    }

    public byte[] keyForSender() {
        return keyForSender;
    }

    public byte[] messageForSender() {
        return messageForSender;
    }

    public void write(byte[] data, Offset offset) {
        writeString(data, sender, offset);
        writeString(data, receiver, offset);
        writeLongLE(data, timestamp, offset);
        writeBlob(data, keyForReceiver, offset);
        writeBlob(data, messageForReceiver, offset);
        writeBlob(data, signature, offset);
        writeBlob(data, keyForSender, offset);
        writeBlob(data, messageForSender, offset);
    }

    public static void write(MessageData message, byte[] data, Offset offset) {
        message.write(data, offset);
    }

    public static MessageData read(byte[] data, Offset offset) {
        String sender = readString(data, offset);
        String receiver = readString(data, offset);
        long timestamp = readLongLE(data, offset);
        byte[] keyForReceiver = readBlob(data, offset);
        byte[] messageForReceiver = readBlob(data, offset);
        byte[] signature = readBlob(data, offset);
        byte[] keyForSender = readBlob(data, offset);
        byte[] messageForSender = readBlob(data, offset);
        
        return new MessageData(sender, receiver, timestamp, keyForReceiver, messageForReceiver, signature, keyForSender, messageForSender);
    }

    public int computeSpace() {
        return sender.length() + receiver.length() + keyForReceiver.length + messageForReceiver.length + signature.length + keyForSender.length + messageForSender.length + 30;
    }

    public static int computeSpace(MessageData message) {
        return message.computeSpace();
    }

}
