package tor.secure.chat.protocol.packets;

import static tor.secure.chat.common.byteutils.ByteUtils.*;

import tor.secure.chat.protocol.Protocol;

public class ServeNonRepudiationProofPacket {
    
    private byte[] signedNonce;

    public ServeNonRepudiationProofPacket(byte[] data) {
        this.signedNonce = readBlob(data, 1);
    }

    public byte[] getSignedNonce() {
        return signedNonce;
    }

    public static byte[] create(byte[] signedNonce) {
        byte[] packet = new byte[signedNonce.length + 5];

        writeByte(packet, Protocol.SERVE_NON_REPUDIATION_PROOF, 0);
        writeBlob(packet, signedNonce, 1);

        return packet;
    }

}