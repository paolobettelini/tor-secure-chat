package tor.secure.chat.protocol.packets;

import static tor.secure.chat.common.byteutils.ByteUtils.*;

import tor.secure.chat.common.byteutils.Offset;
import tor.secure.chat.protocol.Protocol;

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
        byte[] packet = new byte[receiver.length() + message.length + 6];
        Offset offset = new Offset();

        writeByte(packet, Protocol.SEND_MESSAGE, offset);
        writeString(packet, receiver, offset);
        writeBlob(packet, message, offset);

        return packet;
    }

}