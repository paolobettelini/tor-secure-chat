package com.example.tor_secure_chat.core.protocol.packets;

import static com.example.tor_secure_chat.core.common.byteutils.ByteUtils.*;

import com.example.tor_secure_chat.core.common.byteutils.Offset;
import com.example.tor_secure_chat.core.protocol.Protocol;

public class RequestPublicKeyPacket {
    
    private String username;

    public RequestPublicKeyPacket(byte[] packet) {
        Offset offset = new Offset(1);

        this.username = readString(packet, offset);
    }

    public String getUsername() {
        return username;
    }

    public static byte[] create(String username) {
        byte[] packet = new byte[username.length() + 2];

        writeByte(packet, Protocol.REQUEST_PUB_KEY, 0);
        writeString(packet, username, 1);

        return packet;
    }

}
