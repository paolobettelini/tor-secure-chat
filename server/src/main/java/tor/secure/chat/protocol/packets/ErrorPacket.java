package tor.secure.chat.protocol.packets;

import static tor.secure.chat.common.byteutils.ByteUtils.*;

import tor.secure.chat.protocol.Protocol;

public class ErrorPacket {
    
    private byte statusCode;

    public ErrorPacket(byte[] packet) {
        statusCode = packet[1];
    }

    public byte statusCode() {
        return statusCode;
    }

    public static byte[] create(byte code) {
        System.out.println("Sending error code");
        byte[] packet = new byte[2];

        writeByte(packet, Protocol.ERROR, 0);
        writeByte(packet, code, 1);

        return packet;
    }

}