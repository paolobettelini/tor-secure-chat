package com.example.tor_secure_chat.core.protocol.packets;

import static com.example.tor_secure_chat.core.common.byteutils.ByteUtils.writeByte;

import com.example.tor_secure_chat.core.protocol.Protocol;

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