package com.example.tor_secure_chat.core.protocol.packets;

import static com.example.tor_secure_chat.core.common.byteutils.ByteUtils.readBlob;
import static com.example.tor_secure_chat.core.common.byteutils.ByteUtils.writeBlob;
import static com.example.tor_secure_chat.core.common.byteutils.ByteUtils.writeByte;

import com.example.tor_secure_chat.core.protocol.Protocol;

import java.util.Random;

public class RequestNonRepudiationProofPacket {
    
    private static Random random;

    private byte[] nonce;

    static {
        random = new Random();
    }

    public RequestNonRepudiationProofPacket(byte[] data) {
        this.nonce = readBlob(data, 1);
    }

    public byte[] getNonce() {
        return nonce;
    }

    public static byte[] randomNonce(int length) {
        byte[] nonce = new byte[length];
        random.nextBytes(nonce);
        return nonce;
    }

    public static byte[] randomNonce() {
        return randomNonce(128);
    }

    public static byte[] create(byte[] nonce) {
        byte[] packet = new byte[nonce.length + 5];

        writeByte(packet, Protocol.REQUEST_NON_REPUDIATION_PROOF, 0);
        writeBlob(packet, nonce, 1);

        return packet;
    }

}