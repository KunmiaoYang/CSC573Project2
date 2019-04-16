package arq;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static arq.Util.*;

public class SimpleFTPClient {
    public static void main(String[] args) throws IOException {
        String host = args[0];
        int serverPort = Integer.parseInt(args[1]);
        String filePath = args[2];
        int N = Integer.parseInt(args[3]);
        int MSS = Integer.parseInt(args[4]);

        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName(host);
        byte[] data = new byte[BUFF_SIZE];
        data[6] = DATA_PACKET;
        data[7] = DATA_PACKET;

        try (InputStream is = new FileInputStream(filePath)) {
            int checksum;
            for (int seq = 0, readSize = is.read(data, HEADER_SIZE, MSS);
                 readSize != -1;
                 seq++, readSize = is.read(data, HEADER_SIZE, MSS)) {
                // create packet
                DatagramPacket packet = createDataPacket(seq, data, readSize, address, serverPort);

                // send packet
                socket.send(packet);
                format("----------- Send data packet [%d] -----------\r\n", seq);
                println(new String(data, HEADER_SIZE, readSize));
            }
        }

        // Send end packet
        socket.send(createEndPacket(address, serverPort));

//        byte[] data2 = new byte[1024];
//        DatagramPacket packet2 = new DatagramPacket(data2, data2.length);
//        System.out.println("Waiting for server response...");
//        socket.receive(packet2);
//        String reply = new String(data2, 0, packet2.getLength());
//        System.out.println("<Server>: " + reply);
        socket.close();
    }
}
