package tor.secure.chat.common;

import tor.secure.chat.common.byteutils.Offset;

import static tor.secure.chat.common.byteutils.ByteUtils.*;

public record Message(String sender, String receiver, long timestamp, byte[] message) {
    
    public void write(byte[] data, Offset offset) {
        writeString(data, sender, offset);
        writeString(data, receiver, offset);
        writeLongLE(data, timestamp, offset);
        writeBlob(data, message, offset);
    }

    public static void write(Message message, byte[] data, Offset offset) {
        message.write(data, offset);
    }

    public static Message read(byte[] data, Offset offset) {
        String sender = readString(data, offset);
        String receiver = readString(data, offset);
        long timestamp = readLongLE(data, offset);
        byte[] message = readBlob(data, offset);

        return new Message(sender, receiver, timestamp, message);
    }

    public int computeSpace() {
        return sender.length() + receiver.length() + message.length + 11;
    }

    public static int computeSpace(Message message) {
        return message.computeSpace();
    }

}
