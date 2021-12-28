package tor.secure.chat.protocol.packets;

import static tor.secure.chat.common.byteutils.ByteUtils.*;

import tor.secure.chat.common.byteutils.Offset;
import tor.secure.chat.protocol.Protocol;

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
        byte[] packet = new byte[username.length() + password.length + 3];
        Offset offset = new Offset();

        writeByte(packet, Protocol.LOGIN, offset);
        writeString(packet, username, offset);
        writeBlob(packet, password, offset);

        return packet;
    }

}
