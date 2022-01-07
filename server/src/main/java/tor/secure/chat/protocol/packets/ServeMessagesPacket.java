package tor.secure.chat.protocol.packets;

import static tor.secure.chat.common.byteutils.ByteUtils.*;

import tor.secure.chat.common.byteutils.MessageData;
import tor.secure.chat.common.byteutils.Offset;
import tor.secure.chat.protocol.Protocol;

public class ServeMessagesPacket {
    
    private MessageData[] messages;

    public ServeMessagesPacket(byte[] packet) {
        Offset offset = new Offset(1);

        int n = readByte(packet, offset) & 0xFF;
        messages = new MessageData[n];

        for (int i = 0; i < n; i++) {
            messages[i] = MessageData.read(packet, offset);
        }
    }

    public MessageData[] getMessages() {
        return messages;
    }

    public static byte[] create(MessageData... messages) {
        int packetLength = 2;

        for (int i = 0; i < messages.length; i++) {
            packetLength += messages[i].computeSpace();
        }

        byte[] packet = new byte[packetLength];
        Offset offset = new Offset();

        writeByte(packet, Protocol.SERVE_MESSAGES, offset);
        writeByte(packet, (byte) messages.length, offset);

        for (int i = 0; i < messages.length; i++) {
            messages[i].write(packet, offset);
        };

        return packet;
    }

}