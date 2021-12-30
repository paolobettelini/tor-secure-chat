package tor.secure.chat.protocol.packets;

import static tor.secure.chat.common.byteutils.ByteUtils.*;

import tor.secure.chat.protocol.Protocol;

public class ErrorPacket {
    
    public static final byte CONNECTION_ERROR           = 0;
    public static final byte USER_NOT_FOUND             = 1;
    public static final byte USERNAME_ALREADY_EXISTS    = 2;
    public static final byte WRONG_PASSWORD             = 3;

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