package tor.secure.chat.protocol.packets;

import static tor.secure.chat.common.byteutils.ByteUtils.*;

import tor.secure.chat.common.byteutils.Offset;
import tor.secure.chat.protocol.Protocol;

public class ServePGPKeysPacket {
    
    private byte[] publicKey;
    private byte[] privateKey;

    public ServePGPKeysPacket(byte[] packet) {
        Offset offset = new Offset(1);

        this.publicKey = readBlob(packet, offset);
        this.privateKey = readBlob(packet, offset);
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public static byte[] create(byte[] publicKey, byte[] privateKey) {
        byte[] packet = new byte[publicKey.length + privateKey.length + 3];
        Offset offset = new Offset();

        writeByte(packet, Protocol.SERVE_PGP_KEYS, offset);
        writeBlob(packet, publicKey, offset);
        writeBlob(packet, privateKey, offset);

        return packet;
    }

}
