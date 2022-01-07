package tor.secure.chat.common.byteutils;

import static tor.secure.chat.common.byteutils.ByteUtils.*;

public record MessageData(String sender, String receiver, long timestamp, byte[] message) {
    
    public void write(byte[] data, Offset offset) {
        writeString(data, sender, offset);
        writeString(data, receiver, offset);
        writeLongLE(data, timestamp, offset);
        writeBlob(data, message, offset);
    }

    public static void write(MessageData message, byte[] data, Offset offset) {
        message.write(data, offset);
    }

    public static MessageData read(byte[] data, Offset offset) {
        String sender = readString(data, offset);
        String receiver = readString(data, offset);
        long timestamp = readLongLE(data, offset);
        byte[] message = readBlob(data, offset);

        return new MessageData(sender, receiver, timestamp, message);
    }

    public int computeSpace() {
        return sender.length() + receiver.length() + message.length + 14;
    }

    public static int computeSpace(MessageData message) {
        return message.computeSpace();
    }

}
