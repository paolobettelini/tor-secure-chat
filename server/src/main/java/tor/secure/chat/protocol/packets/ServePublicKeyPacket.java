package tor.secure.chat.protocol.packets;

import static tor.secure.chat.common.byteutils.ByteUtils.*;

import tor.secure.chat.protocol.Protocol;

public class ServePublicKeyPacket {
    
    private byte[] publicKey;

    public ServePublicKeyPacket(byte[] packet) {
        this.publicKey = readBlob(packet, 1);
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public static byte[] create(byte[] publicKey) {
        byte[] packet = new byte[publicKey.length + 5];

        writeByte(packet, Protocol.SERVE_PUB_KEY, 0);
        writeBlob(packet, publicKey, 1);

        return packet;
    }

}