package tor.secure.chat.protocol.packets;

import static tor.secure.chat.common.byteutils.ByteUtils.*;

import tor.secure.chat.common.byteutils.Offset;
import tor.secure.chat.protocol.Protocol;

public class RegisterPacket {
    
    private String username;
    private byte[] password;
    private byte[] publicKey;
    private byte[] privateKey;

    public RegisterPacket(byte[] packet) {
        Offset offset = new Offset(1);

        this.username = readString(packet, offset);
        this.password = readBlob(packet, offset);
        this.publicKey = readBlob(packet, offset);
        this.privateKey = readBlob(packet, offset);
    }

    public String getUsername() {
        return username;
    }

    public byte[] getPassword() {
        return password;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public byte[] getPrivateKey() {
        return privateKey;
    }

    public static byte[] create(String username, byte[] password, byte[] publicKey, byte[] privateKey) {
        byte[] packet = new byte[username.length() + password.length + publicKey.length + privateKey.length + 14];
        Offset offset = new Offset();

        writeByte(packet, Protocol.REGISTER, offset);
        writeString(packet, username, offset);
        writeBlob(packet, password, offset);
        writeBlob(packet, publicKey, offset);
        writeBlob(packet, privateKey, offset);

        return packet;
    }

}
