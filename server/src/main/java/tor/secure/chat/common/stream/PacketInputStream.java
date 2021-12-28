package tor.secure.chat.common.stream;

import java.io.IOException;
import java.io.InputStream;

public class PacketInputStream {

    private InputStream in;
    private boolean hasEnded = false;

    public PacketInputStream(InputStream in) {
        this.in = in;
    }

    public byte[] nextPacket() throws IOException {
        int b0 = in.read();
        int b1 = in.read();
        int b2 = in.read();
        int b3 = in.read();
        
        int packetLength = b0 | (b1 << 8) | (b2 << 16) | (b3 << 24);
        
        byte[] packet = in.readNBytes(packetLength);

        if (b3 == -1) {
            hasEnded = true;
        }

        return packet;
    }

    public boolean hasEnded() {
        return hasEnded;
    }

    public void close() throws IOException {
        hasEnded = true;
        in.close();
    }

}