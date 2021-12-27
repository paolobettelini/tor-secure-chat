package tor.secure.chat.protocol.packets;

import static tor.secure.chat.utils.byteutils.ByteUtils.*;

import tor.secure.chat.protocol.Protocol;
import tor.secure.chat.utils.byteutils.Offset;

public class SendMessagePacket {
    
    private String receiver;
    private byte[] message;

    public SendMessagePacket(byte[] packet) {
        Offset offset = new Offset(1);

        this.receiver = readString(packet, offset);
        this.message = readBlob(packet, offset);
    }

    public String getReceiver() {
        return receiver;
    }

    public byte[] getMessage() {
        return message;
    }

    public static byte[] create(String receiver, byte[] message) {
        byte[] packet = new byte[receiver.length() + message.length + 3];
        Offset offset = new Offset();

        writeByte(packet, Protocol.MESSAGE_SENT, offset);
        writeString(packet, receiver, offset);
        writeBlob(packet, message, offset);

        return packet;
    }

}