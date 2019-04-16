package arq;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Deque;

import static arq.Util.*;

public class SimpleFTPClientThread extends Thread {
    DatagramSocket socket;
    Deque<DatagramPacket> outgoing;

    public SimpleFTPClientThread(DatagramSocket socket, Deque<DatagramPacket> outgoing) {
        this.socket = socket;
        this.outgoing = outgoing;
    }

    @Override
    public void run() {
//        super.run();
        byte[] data = new byte[1024];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        while (!socket.isClosed()) {
            try {
                println(CHANNEL_CLIENT_RECEIVE, "----------- Waiting for ACK ... -----------");
                socket.receive(packet);
                format(CHANNEL_CLIENT_RECEIVE, "<Server>: %s\r\n", bytes2Binary(data, 0, 4));
                format(CHANNEL_CLIENT_RECEIVE, "<Server>: %s\r\n", bytes2Binary(data, 4, 4));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
