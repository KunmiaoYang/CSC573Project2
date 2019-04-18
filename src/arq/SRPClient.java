package arq;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import static arq.Util.*;

public class SRPClient {
    static long RTO = 8;
    static long ack;
    static Long p;
    static long[] timer;
    static int N;
    static int MSS;
    static long sendDataPacket (DatagramSocket socket, DatagramPacket packet) throws IOException {
        byte[] data = packet.getData();
        long packetSeq = decodeNum(4, data, 0);
        long curTime = System.currentTimeMillis();
        timer[(int) (packetSeq%N)] = curTime;
        socket.send(packet);
        return packetSeq;
    }
    private static void rdt_send(String filePath, DatagramSocket socket, InetAddress address, int serverPort) throws IOException {
        int readSize = 0;
        long seq = 0;
        try (InputStream is = new FileInputStream(filePath)) {
            ack = 0;
            p = 0L;
            DatagramPacket[] outgoing = new DatagramPacket[N];
            SRPClientThread thread = new SRPClientThread(socket, outgoing);
            thread.start();

            byte[] data;
            long maxSentSeq = -1, packetSeq, retransTimer, ackCur;
            while  (ack < seq || readSize > -1) {
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
//                if (p >= seq || retransTimer > RTO) {
                if (retransTimer > RTO) {
                    format(CHANNEL_CLIENT_SEND, "p = %d, seq = %d, ACK = %d\r\n", p, seq, ackCur);
                    format(CHANNEL_CLIENT_SEND, "RT = %d, ACK = %d\r\n", retransTimer, ackCur);
                    p = ackCur;
                    // If ack also equals seq, it means this packet is not lost,
                    // all packets have been transmited, ack updated after loop condition check.
                    if (ack == seq) break;
                    format(CHANNEL_CONCISE, "\rTimeout, sequence number = %d", p);
                    format(CHANNEL_VERBOSE, "Timeout, sequence number = %d\r\n", p);
                } else if (p >= seq) {
                    TimeUnit.MILLISECONDS.sleep(1);
                    continue;
                }

                // send packet
                DatagramPacket packet;
                synchronized (p) {
                    packet = outgoing[(int) ((p++) % N)];
//                    format(CHANNEL_CLIENT_SEND, "$$$$$$ p increased to %d $$$$$$\r\n", p);
                }
                packetSeq = sendDataPacket(socket, packet);
                maxSentSeq = Math.max(maxSentSeq, packetSeq);
                format(CHANNEL_CLIENT_SEND,
                        "----------- ACK = %d, seq = %d, p = %d, RT = %d, Send data packet [%d], time = %d -----------\r\n",
                        ack, seq, p - 1, retransTimer, packetSeq, timer[(int) (packetSeq%N)]);
                println(CHANNEL_CLIENT_SEND | CHANNEL_CONTENT, new String(packet.getData(), HEADER_SIZE, packet.getLength()));
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

        // Send end packet
        DatagramPacket packet = createEndPacket(seq, address, serverPort);
        while (ack == seq) {
            socket.send(packet);
            format(CHANNEL_CLIENT_SEND,
                    "----------- Close ACK = %d, Send data packet [%d] -----------\r\n",
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
    }
    public static void main(String[] args) throws IOException {
        String host = args[0];
        int serverPort = Integer.parseInt(args[1]);
        String filePath = args[2];
        N = Integer.parseInt(args[3]);
        MSS = Integer.parseInt(args[4]);

        timer = new long[N];
        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName(host);

        rdt_send(filePath, socket, address, serverPort);

        socket.close();
    }
}
