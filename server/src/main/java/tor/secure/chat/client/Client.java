package tor.secure.chat.client;

import java.io.IOException;
import java.net.Socket;

import tor.secure.chat.utils.stream.PacketInputStream;
import tor.secure.chat.utils.stream.PacketOutputStream;

public class Client extends Thread {

    private String address;
    private int port;

    private PacketInputStream in;
    private PacketOutputStream out;

    public Client(String address, int port) {
        this.address = address;
        this.port = port;
    }

    @Override
    public void start() {
        try (Socket socket = new Socket(address, port)) {
            in = new PacketInputStream(socket.getInputStream());
            out = new PacketOutputStream(socket.getOutputStream());

            while (!in.hasEnded()) {
                byte[] packet = in.nextPacket();
                processPacket(packet);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processPacket(byte[] packet) {
    }

}
