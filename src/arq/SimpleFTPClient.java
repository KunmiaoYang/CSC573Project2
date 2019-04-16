package arq;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import static arq.Util.*;

public class SimpleFTPClient {
    static long ack;
    public static void main(String[] args) throws IOException {
        String host = args[0];
        int serverPort = Integer.parseInt(args[1]);
        String filePath = args[2];
        int N = Integer.parseInt(args[3]);
        int MSS = Integer.parseInt(args[4]);

        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName(host);

        try (InputStream is = new FileInputStream(filePath)) {
            ack = 0;
            DatagramPacket[] outgoing = new DatagramPacket[N];
            SimpleFTPClientThread thread = new SimpleFTPClientThread(socket, outgoing);
            thread.start();

            byte[] data;
            int readSize = 0;
            long seq = 0, p = 0;
            for  (; ack < seq || readSize > -1; p++) {
                // create packet
                if (readSize > -1 && seq - ack < N) {
                    data = new byte[BUFF_SIZE];
                    if ((readSize = is.read(data, HEADER_SIZE, MSS)) > -1)
                        outgoing[(int) (seq%N)] = createDataPacket(
                                seq++, data, readSize, address, serverPort);
                }

                // select packet
                if (p >= seq) {
                    p = ack;
                    // If ack also equals seq, it means this packet is not lost,
                    // all packets have been transmited, ack updated after loop condition check.
                    if (p == seq) break;
                    System.out.println("Packet loss, sequence number = " + p);
                }
                DatagramPacket packet = outgoing[(int) (p%N)];
                data = packet.getData();

                // send packet
                socket.send(packet);
                format(CHANNEL_CLIENT_SEND,
                        "----------- ACK = %d, seq = %d, p = %d, Send data packet [%d] -----------\r\n",
                        ack, seq, p, decodeNum(4, data, 0));
                println(CHANNEL_CLIENT_SEND | CHANNEL_CONTENT, new String(data, HEADER_SIZE, packet.getLength()));
            }
            format(CHANNEL_CLIENT_SEND,
                    "----------- ACK = %d, seq = %d, readSize = %d, Transmission terminated! -----------\r\n",
                    ack, seq, readSize);
        }

        // Send end packet
        socket.send(createEndPacket(address, serverPort));

        socket.close();
    }
}
