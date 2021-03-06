package arq;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

import static arq.SRPClient.*;
import static arq.Util.*;

public class SRPClientThread extends Thread {
    DatagramSocket socket;
    DatagramPacket[] outgoing;

    public SRPClientThread(DatagramSocket socket, DatagramPacket[] outgoing) {
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
                format(CHANNEL_CLIENT_RECEIVE, "\r\n=========== Waiting for ACK [%d] ... ===========\r\n", ack + 1);
                socket.receive(packet);
                receiveTime = System.currentTimeMillis();
                ackNew = decodeNum(4, data, 0);
                println(CHANNEL_CLIENT_RECEIVE, "Receive time = " + receiveTime);
                println(CHANNEL_CLIENT_RECEIVE, Arrays.toString(timer));
                format(CHANNEL_CLIENT_RECEIVE, "=========== ACK packet [%d] received, RTT = %d ===========\r\n",
                        ackNew, receiveTime - timer[(int) ((ackNew + N - 1)%N)]);
                ack = ackNew;   // update ack after get the timer, to avoid timer update before output.
                if (p < ack) p = ack;

                format(CHANNEL_CLIENT_RECEIVE | CHANNEL_CONTENT, "<Server>: %s\r\n", bytes2Binary(data, 0, 4));
                format(CHANNEL_CLIENT_RECEIVE | CHANNEL_CONTENT, "<Server>: %s\r\n", bytes2Binary(data, 4, 4));

//                sendDataPacket(socket, outgoing[(int) (ackNew%N)]); // retransmit the ack packet
//                socket.send(outgoing[(int) (ackNew%N)]); // retransmit the ack packet
//                format(CHANNEL_CLIENT_RECEIVE,
//                        "----------- ACK = %d, Send data packet [%d], time = %d -----------\r\n",
//                        ack, ackNew, System.currentTimeMillis());
            }catch (SocketException e) {
                println(CHANNEL_CLIENT_RECEIVE, "Thread interrupted!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
