package ch.bettelini.protocol.packets;

import static ch.bettelini.common.byteutils.ByteUtils.*;

import ch.bettelini.common.byteutils.Offset;
import ch.bettelini.protocol.Protocol;

public class SendMessagePacket {
    
    private String receiver;
    private byte[] keyForReceiver;
    private byte[] messageForReceiver;
    private byte[] signature;
    private byte[] keyForSender;
    private byte[] messageForSender;

    public SendMessagePacket(byte[] packet) {
        Offset offset = new Offset(1);

        this.receiver = readString(packet, offset);
        this.keyForReceiver = readBlob(packet, offset);
        this.messageForReceiver = readBlob(packet, offset);
        this.signature = readBlob(packet, offset);
        this.keyForSender = readBlob(packet, offset);
        this.messageForSender = readBlob(packet, offset);
    }

    public String getReceiver() {
        return receiver;
    }

    public byte[] getKeyForReceiver() {
        return keyForReceiver;
    }

    public byte[] getMessageForReceiver() {
        return messageForReceiver;
    }

    public byte[] getSignature() {
        return signature;
    }

    public byte[] getKeyForSender() {
        return keyForSender;
    }

    public byte[] getMessageForSender() {
        return messageForSender;
    }

    public static byte[] create(String receiver, byte[] keyForReceiver, byte[] messageForReceiver, byte[] signature, byte[] keyForSender, byte[] messageForSender) {
        byte[] packet = new byte[receiver.length() + keyForReceiver.length + messageForReceiver.length + signature.length + keyForSender.length + messageForSender.length + 22];
        Offset offset = new Offset();

        writeByte(packet, Protocol.SEND_MESSAGE, offset);
        writeString(packet, receiver, offset);
        writeBlob(packet, keyForReceiver, offset);
        writeBlob(packet, messageForReceiver, offset);
        writeBlob(packet, signature, offset);
        writeBlob(packet, keyForSender, offset);
        writeBlob(packet, messageForSender, offset);

        return packet;
    }

}