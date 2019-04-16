package arq;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SimpleFTPServer {
    static final int BUFF_SIZE = 2*1024;
    static final int HEADER_SIZE = 8;
    static final byte ACK_PACKET = (byte) 0b10101010;
    static final int MASK16 = 0xFFFF;
    static final int MASK8 = 0xFF;

    static byte[] testData = {
            69, 0, 0, 28,
            0, 1, 0, 0,
            4, 17, 0, 0,
            10, 12, 14, 5,
            12, 6, 7, 9
    };
    public static int calcChecksum(int checksum, byte[] data, int start) {
        for (int i = start; i < data.length; i += 2) {
            checksum += (data[i] << 8);
            if (i + 1 < data.length)
                checksum += data[i + 1];
        }
        return  (checksum & MASK16)^MASK16;
    }
    public static void encodeNum(long num, int bytes, byte[] data, int start) {
        if (start + bytes >= data.length) return;
        for (int i = start + bytes - 1; i >= start; i--, num >>= 8) {
            data[i] = (byte) (num&MASK8);
        }
    }
    public static long decodeNum(int bytes, byte[] data, int start) {
        if (start + bytes >= data.length) return 0;
        long num = 0;
        for (int i = start; i < start + bytes; i++) {
            num = (num << 8) | (data[i] & MASK8);
//            System.out.println("data: " + Integer.toBinaryString(data[i] & MASK8));
//            System.out.println(num + ": " + Long.toBinaryString(num));
        }
        return num;
    }

    // Disable fire wall before use, or the server cannot receive UDP packet:
    // >sudo ufw disable
    public static void main(String[] args) throws IOException {
        int serverPort = Integer.parseInt(args[0]);
        String filePath = args[1];
        float p = Float.parseFloat(args[2]);

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
//        System.out.println("Server has started, waiting for client data...");
        int checksum, mark;
        for (long ack = 0, seq = 0; seq > -1; ) {
            socket.receive(packetReceived);
            seq = decodeNum(4, dataReceived, 0);
            checksum = (int) decodeNum(2, dataReceived, 4);
            mark = (int) decodeNum(2, dataReceived, 6);
            if (seq != ack) continue;
            if (calcChecksum(checksum, dataReceived, HEADER_SIZE) != 0) continue;

            encodeNum(++ack, 4, dataSend, 0);
            address = packetReceived.getAddress();
            clientPort = packetReceived.getPort();
            packetSend = new DatagramPacket(dataSend, HEADER_SIZE, address, clientPort);
            socket.send(packetSend);
        }
        socket.close();
    }
}
