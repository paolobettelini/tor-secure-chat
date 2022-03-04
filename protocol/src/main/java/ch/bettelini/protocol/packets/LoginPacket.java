package ch.bettelini.protocol.packets;

import static ch.bettelini.common.byteutils.ByteUtils.readBlob;
import static ch.bettelini.common.byteutils.ByteUtils.readString;
import static ch.bettelini.common.byteutils.ByteUtils.writeBlob;
import static ch.bettelini.common.byteutils.ByteUtils.writeByte;
import static ch.bettelini.common.byteutils.ByteUtils.writeString;

import ch.bettelini.common.byteutils.Offset;
import ch.bettelini.protocol.Protocol;

public class LoginPacket {
    
    private String username;
    private byte[] password;

    public LoginPacket(byte[] packet) {
        Offset offset = new Offset(1);

        this.username = readString(packet, offset);
        this.password = readBlob(packet, offset);
    }

    public String getUsername() {
        return username;
    }

    public byte[] getPassword() {
        return password;
    }

    public static byte[] create(String username, byte[] password) {
        byte[] packet = new byte[username.length() + password.length + 6];
        Offset offset = new Offset();

        writeByte(packet, Protocol.LOGIN, offset);
        writeString(packet, username, offset);
        writeBlob(packet, password, offset);

        return packet;
    }

}
