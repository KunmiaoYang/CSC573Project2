package arq;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SimpleFTPClient {
    static final int DATA_PACKET = 0b01010101;
    public static void main(String[] args) throws IOException {
        String host = args[0];
        int serverPort = Integer.parseInt(args[1]);

        InetAddress address = InetAddress.getByName(host);
        byte[] data = "Hello this is client".getBytes();
        DatagramPacket packet = new DatagramPacket(data, data.length, address, serverPort);
        DatagramSocket socket = new DatagramSocket();
        socket.send(packet);

        byte[] data2 = new byte[1024];
        DatagramPacket packet2 = new DatagramPacket(data2, data2.length);
        System.out.println("Waiting for server response...");
        socket.receive(packet2);
        String reply = new String(data2, 0, packet2.getLength());
        System.out.println("<Server>: " + reply);
        socket.close();
    }
}
