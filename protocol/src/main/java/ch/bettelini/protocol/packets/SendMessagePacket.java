package ch.bettelini.protocol.packets;

import static ch.bettelini.common.byteutils.ByteUtils.*;

import ch.bettelini.common.byteutils.Offset;
import ch.bettelini.protocol.Protocol;

public class SendMessagePacket {
    
    private String receiver;
    private byte[] key;
    private byte[] message;
    private byte[] signature;

    public SendMessagePacket(byte[] packet) {
        Offset offset = new Offset(1);

        this.receiver = readString(packet, offset);
        this.key = readBlob(packet, offset);
        this.message = readBlob(packet, offset);
        this.signature = readBlob(packet, offset);
    }

    public String getReceiver() {
        return receiver;
    }

    public byte[] getKey() {
        return key;
    }

    public byte[] getMessage() {
        return message;
    }

    public byte[] getSignature() {
        return signature;
    }

    public static byte[] create(String receiver, byte[] key, byte[] message, byte[] signature) {
        byte[] packet = new byte[receiver.length() + key.length + message.length + signature.length + 14];
        Offset offset = new Offset();

        writeByte(packet, Protocol.SEND_MESSAGE, offset);
        writeString(packet, receiver, offset);
        writeBlob(packet, key, offset);
        writeBlob(packet, message, offset);
        writeBlob(packet, signature, offset);

        return packet;
    }

}