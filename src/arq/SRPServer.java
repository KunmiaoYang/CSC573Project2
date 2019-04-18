package arq;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static arq.Util.*;

public class SRPServer {
    private static int ARTIFICIAL_DELAY = 0;
    // Disable fire wall before use, or the server cannot receive UDP packet:
    // >sudo ufw disable
    public static void main(String[] args) throws IOException, InterruptedException {
        int serverPort = Integer.parseInt(args[0]);
        String filePath = args[1];
        float p = Float.parseFloat(args[2]);
        int N = Integer.parseInt(args[3]);

        // Init
        int clientPort;
        DatagramPacket[] incoming = new DatagramPacket[N];
        DatagramSocket socket = new DatagramSocket(serverPort);
        byte[] dataSend = {
                0, 0, 0, 0,                 // the 32-bit sequence number that is being ACKed
                0, 0,                       // a 16-bit field that is all zeroes
                ACK_PACKET, ACK_PACKET};    // a 16-bit field that has the value 1010101010101010
        DatagramPacket packetSend;
        InetAddress address;

        int checksum, mark = 0, len, totalWriten = 0;
        Random rand = new Random();

        try (OutputStream os = new FileOutputStream(filePath)) {
            for (long ack = 0, seq; END_MARK != mark; ) {
                // new data and packet, so the packets in the incoming queue are not pointing at the same data
                byte[] dataReceived = new byte[BUFF_SIZE];
                DatagramPacket packetReceived = new DatagramPacket(dataReceived, dataReceived.length);
                // Receive packet
                format(CHANNEL_SERVER, "\r\n----------- Waiting for client data packet [%d] ... -----------\r\n", ack);
                socket.receive(packetReceived);

                seq = decodeNum(4, dataReceived, 0);

                // probabilistic loss service
                if (rand.nextFloat() < p) {
                    format(CHANNEL_CONCISE, "\rPacket loss, sequence number = %d", seq);
                    format(CHANNEL_VERBOSE, "Packet loss, sequence number = %d\r\n", seq);
                    continue;
                }

                checksum = (int) decodeNum(2, dataReceived, 4);
                mark = (int) decodeNum(2, dataReceived, 6);
                if (END_MARK == mark) {
                    ack++;
                    encodeNum(END_MARK, 2, dataSend, 6);
                } else if (calcChecksum(checksum, dataReceived, HEADER_SIZE, packetReceived.getLength()) != 0) {
                    continue;
                } else if (seq < ack + N) {
                    incoming[(int) (seq%N)] = packetReceived;
                    if (seq == ack) {
                        format(CHANNEL_SERVER, "----------- Data packet [%d] received -----------\r\n", seq);

                        // Output to local
                        DatagramPacket packet = packetReceived;
                        byte[] data = packet.getData();
                        long packetSeq = decodeNum(4, data, 0);
                        while (ack == packetSeq) {
                            len = packet.getLength() - HEADER_SIZE;
                            os.write(data, HEADER_SIZE, len);
                            totalWriten += len;
                            os.flush();
                            println(CHANNEL_SERVER | CHANNEL_CONTENT,
                                    new String(data, HEADER_SIZE, len));

                            if (null == (packet = incoming[(int) ((++ack)%N)])) break;
                            data = packet.getData();
                            packetSeq = decodeNum(4, data, 0);
                        }
                    } else {
                        format(CHANNEL_SERVER, "*********** Data packet [%d] received ***********\r\n", seq);
                    }
                }

                // Artificial delay to simulate network delay
//                println(CHANNEL_CLIENT_RECEIVE, "Receive time = " + System.currentTimeMillis());
                if (ARTIFICIAL_DELAY > 0) TimeUnit.MILLISECONDS.sleep(ARTIFICIAL_DELAY);
//                println(CHANNEL_CLIENT_RECEIVE, "ACK time = " + System.currentTimeMillis());

                // Send ACK
                encodeNum(ack, 4, dataSend, 0);
                address = packetReceived.getAddress();
                clientPort = packetReceived.getPort();
                packetSend = new DatagramPacket(dataSend, HEADER_SIZE, address, clientPort);
                socket.send(packetSend);
                format(CHANNEL_SERVER, "----------- ACK packet [%d] sent -----------\r\n", ack);
            }
        }
        println(CHANNEL_SERVER, "File total written: " + totalWriten);
        socket.close();

        println(CHANNEL_SERVER, "Server closed!");
    }
}
