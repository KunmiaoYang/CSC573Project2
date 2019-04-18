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

public class SimpleFTPServer {
    private static int ARTIFICIAL_DELAY = 0;
    // Disable fire wall before use, or the server cannot receive UDP packet:
    // >sudo ufw disable
    public static void main(String[] args) throws IOException, InterruptedException {
        int serverPort = Integer.parseInt(args[0]);
        String filePath = args[1];
        float p = Float.parseFloat(args[2]);

        // Init
        int clientPort;
        DatagramSocket socket = new DatagramSocket(serverPort);
        byte[] dataReceived = new byte[BUFF_SIZE];
        byte[] dataSend = {
                0, 0, 0, 0,                 // the 32-bit sequence number that is being ACKed
                0, 0,                       // a 16-bit field that is all zeroes
                ACK_PACKET, ACK_PACKET};    // a 16-bit field that has the value 1010101010101010
        DatagramPacket packetReceived = new DatagramPacket(dataReceived, dataReceived.length);
        DatagramPacket packetSend;
        InetAddress address;

        int checksum, mark = 0, len, totalWriten = 0;
        Random rand = new Random();

        try (OutputStream os = new FileOutputStream(filePath)) {
            for (long ack = 0, seq; END_MARK != mark; ) {
                // Receive packet
                format(CHANNEL_SERVER, "\r\n----------- Waiting for client data packet [%d] ... -----------\r\n", ack);
                socket.receive(packetReceived);

                seq = decodeNum(4, dataReceived, 0);

                // probabilistic loss service
                if (rand.nextFloat() < p) {
                    format(CHANNEL_CONCISE, "\rACK = %d, Packet loss, sequence number = %d", ack, seq);
                    format(CHANNEL_VERBOSE, "Packet loss, sequence number = %d\r\n", seq);
                    continue;
                }

                checksum = (int) decodeNum(2, dataReceived, 4);
                mark = (int) decodeNum(2, dataReceived, 6);
                if (END_MARK == mark) {
                    ack++;
                } else if (seq == ack) {
                    format(CHANNEL_SERVER, "----------- Data packet [%d] received -----------\r\n", seq);
                    if (calcChecksum(checksum, dataReceived, HEADER_SIZE, packetReceived.getLength()) != 0)
                        continue;

                    // Output to local
                    len = packetReceived.getLength() - HEADER_SIZE;
                    os.write(dataReceived, HEADER_SIZE, len);
                    totalWriten += len;
                    os.flush();
                    println(CHANNEL_SERVER | CHANNEL_CONTENT,
                            new String(dataReceived, HEADER_SIZE, len));

                    // update ack
                    ack++;
                } else {
                    format(CHANNEL_SERVER, "*********** Data packet [%d] received ***********\r\n", seq);
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
