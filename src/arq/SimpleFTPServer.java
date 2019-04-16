package arq;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import static arq.Util.*;

public class SimpleFTPServer {
    private static int ARTIFICIAL_DELAY = 1;
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

        int checksum, mark;
        for (long ack = 0, seq; true; ) {
            // Receive packet
            format(CHANNEL_SERVER, "----------- Waiting for client data packet [%d] ... -----------\r\n", ack);
            socket.receive(packetReceived);
            seq = decodeNum(4, dataReceived, 0);
            checksum = (int) decodeNum(2, dataReceived, 4);
            mark = (int) decodeNum(2, dataReceived, 6);
            if (END_MARK == mark) break;
            if (seq != ack) {
                format(CHANNEL_SERVER, "*********** Data packet [%d] received ***********\r\n", seq);
                continue;
            }
            format(CHANNEL_SERVER, "----------- Data packet [%d] received -----------\r\n", seq);
            if (calcChecksum(checksum, dataReceived, HEADER_SIZE, packetReceived.getLength()) != 0)
                continue;

            // Output to local
            println(CHANNEL_SERVER_CONTENT, new String(
                    dataReceived,
                    HEADER_SIZE,
                    packetReceived.getLength() - HEADER_SIZE));

            // Artificial delay to simulate network delay
            if (ARTIFICIAL_DELAY > 0) TimeUnit.MILLISECONDS.sleep(ARTIFICIAL_DELAY);

            // Send ACK
            encodeNum(++ack, 4, dataSend, 0);
            address = packetReceived.getAddress();
            clientPort = packetReceived.getPort();
            packetSend = new DatagramPacket(dataSend, HEADER_SIZE, address, clientPort);
            socket.send(packetSend);
            format(CHANNEL_SERVER, "----------- ACK packet [%d] sent -----------\r\n", ack);
        }
        socket.close();

        println(CHANNEL_SERVER, "Server closed!");
    }
}
