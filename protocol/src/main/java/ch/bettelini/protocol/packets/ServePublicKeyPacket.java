package ch.bettelini.protocol.packets;

import static ch.bettelini.common.byteutils.ByteUtils.*;

import ch.bettelini.common.byteutils.Offset;
import ch.bettelini.protocol.Protocol;

public class ServePublicKeyPacket {
    
    private String username;
    private byte[] publicKey;

    public ServePublicKeyPacket(byte[] packet) {
        Offset offset = new Offset(1);
        
        this.username = readString(packet, offset);
        this.publicKey = readBlob(packet, offset);
    }

    public String getUsername() {
        return username;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public static byte[] create(String username, byte[] publicKey) {
        byte[] packet = new byte[publicKey.length + username.length() + 6];

        Offset offset = new Offset();

        writeByte(packet, Protocol.SERVE_PUB_KEY, offset);
        writeString(packet, username, offset);
        writeBlob(packet, publicKey, offset);

        return packet;
    }

}