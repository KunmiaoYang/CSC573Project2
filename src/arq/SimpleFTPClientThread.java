package arq;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

import static arq.SimpleFTPClient.N;
import static arq.SimpleFTPClient.ack;
import static arq.SimpleFTPClient.timer;
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
        long receiveTime, ackNew;
        while (!socket.isClosed()) {
            try {
                format(CHANNEL_CLIENT_RECEIVE, "\r\n=========== Waiting for ACK [%d] ... ===========\r\n", ack);
                socket.receive(packet);
                receiveTime = System.currentTimeMillis();
                ackNew = decodeNum(4, data, 0);
                format(CHANNEL_CLIENT_RECEIVE, "=========== ACK packet [%d] received, RTT = %d ===========\r\n",
                        ackNew, receiveTime - timer[(int) ((ackNew + N - 1)%N)]);
                ack = ackNew;   // update ack after get the timer, to avoid timer update before output.

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
