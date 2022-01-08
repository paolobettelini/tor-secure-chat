package tor.secure.chat.common.byteutils;

import static tor.secure.chat.common.byteutils.ByteUtils.*;

public record MessageData(String sender, String receiver, long timestamp, byte[] key, byte[] message, byte[] signature) {
    
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
