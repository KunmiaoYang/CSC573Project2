package arq;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import static arq.Util.*;

public class SimpleFTPClient {
    static long RTO = 5;
    static long ack;
    static long[] timer;
    static int N;
    public static void main(String[] args) throws IOException {
        String host = args[0];
        int serverPort = Integer.parseInt(args[1]);
        String filePath = args[2];
        N = Integer.parseInt(args[3]);
        int MSS = Integer.parseInt(args[4]);

        timer = new long[N];
        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName(host);

        int readSize = 0;
        long seq = 0;
        try (InputStream is = new FileInputStream(filePath)) {
            ack = 0;
            DatagramPacket[] outgoing = new DatagramPacket[N];
            SimpleFTPClientThread thread = new SimpleFTPClientThread(socket, outgoing);
            thread.start();

            byte[] data;
            long p = 0, maxSentSeq = -1, packetSeq, retransTimer, ackCur;
            for  (; ack < seq || readSize > -1; p++) {
                // create packet
                if (readSize > -1 && seq - ack < N) {
                    data = new byte[BUFF_SIZE];
                    if ((readSize = is.read(data, HEADER_SIZE, MSS)) > -1)
                        outgoing[(int) (seq%N)] = createDataPacket(
                                seq++, data, readSize, address, serverPort);
                }

                // select packet
                ackCur = ack;
                retransTimer = maxSentSeq < ackCur?
                        0 : System.currentTimeMillis() -  timer[(int) (ackCur%N)];
                if (p >= seq || retransTimer > RTO) {
                    format(CHANNEL_CLIENT_SEND, "p = %d, seq = %d, ACK = %d\r\n", p, seq, ackCur);
                    format(CHANNEL_CLIENT_SEND, "RT = %d, ACK = %d\r\n", retransTimer, ackCur);
                    p = ackCur;
                    // If ack also equals seq, it means this packet is not lost,
                    // all packets have been transmited, ack updated after loop condition check.
                    if (ack == seq) break;
                    System.out.println("Timeout, sequence number = " + p);
                }
                DatagramPacket packet = outgoing[(int) (p%N)];
                data = packet.getData();

                // send packet
                packetSeq = decodeNum(4, data, 0);
                timer[(int) (packetSeq%N)] = System.currentTimeMillis();
                maxSentSeq = packetSeq;
                socket.send(packet);
                format(CHANNEL_CLIENT_SEND,
                        "----------- ACK = %d, seq = %d, p = %d, RT = %d, Send data packet [%d] -----------\r\n",
                        ack, seq, p, retransTimer, packetSeq);
                println(CHANNEL_CLIENT_SEND | CHANNEL_CONTENT, new String(data, HEADER_SIZE, packet.getLength()));
            }
        }

        // Send end packet
        DatagramPacket packet = createEndPacket(seq, address, serverPort);
        while (ack == seq) {
            socket.send(packet);
            format(CHANNEL_CLIENT_SEND,
                    "----------- ACK = %d, Send data packet [%d] -----------\r\n",
                    ack, seq);
            try {
                TimeUnit.MILLISECONDS.sleep(RTO);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        format(CHANNEL_CLIENT_SEND,
                "----------- ACK = %d, seq = %d, readSize = %d, Transmission terminated! -----------\r\n",
                ack, seq, readSize);

        socket.close();
    }
}
