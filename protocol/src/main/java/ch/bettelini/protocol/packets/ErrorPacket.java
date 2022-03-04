package ch.bettelini.protocol.packets;

import static ch.bettelini.common.byteutils.ByteUtils.writeByte;

import ch.bettelini.protocol.Protocol;

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