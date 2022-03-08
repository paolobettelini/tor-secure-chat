package ch.bettelini.common.stream;

import java.io.IOException;
import java.io.InputStream;

public class PacketInputStream {

    private InputStream in;
    private boolean hasEnded = false;

    public PacketInputStream(InputStream in) {
        this.in = in;
    }

    public synchronized byte[] nextPacket() throws IOException {
        int b0 = ((byte) in.read()) & 0xFF;
        int b1 = ((byte) in.read()) & 0xFF;
        int b2 = ((byte) in.read()) & 0xFF;
        int b3 = ((byte) in.read()) & 0xFF;

        int packetLength = b0 | (b1 << 8) | (b2 << 16) | (b3 << 24);

        if (packetLength == -1) {
            hasEnded = true;
            return null;
        }

        //byte[] packet = in.readNBytes(packetLength);
        byte[] packet = new byte[packetLength];
        in.read(packet, 0, packet.length);

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
