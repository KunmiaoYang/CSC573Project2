package arq;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import static arq.SimpleFTPClient.ack;
import static arq.Util.*;

public class SimpleFTPClientThread extends Thread {
    DatagramSocket socket;
    DatagramPacket[] outgoing;

    public SimpleFTPClientThread(DatagramSocket socket, DatagramPacket[] outgoing) {
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
                format(CHANNEL_CLIENT_RECEIVE, "----------- Waiting for ACK [%d] ... -----------", ack);
                socket.receive(packet);

                ack = decodeNum(4, data, 0);

                format(CHANNEL_CLIENT_RECEIVE | CHANNEL_CONTENT, "<Server>: %s\r\n", bytes2Binary(data, 0, 4));
                format(CHANNEL_CLIENT_RECEIVE | CHANNEL_CONTENT, "<Server>: %s\r\n", bytes2Binary(data, 4, 4));
            }catch (SocketException e) {
                println(CHANNEL_CLIENT_RECEIVE, "Thread interrupted!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
