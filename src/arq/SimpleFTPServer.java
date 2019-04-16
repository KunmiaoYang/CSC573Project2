package arq;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class SimpleFTPServer {

    // Disable fire wall before use, or the server cannot receive UDP packet:
    // >sudo ufw disable
    public static void main(String[] args) throws IOException {
        int serverPort = Integer.parseInt(args[0]);
        String filePath = args[1];
        float p = Float.parseFloat(args[2]);

        DatagramSocket socket = new DatagramSocket(serverPort);
        byte[] data = new byte[1024];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        System.out.println("Server has started, waiting for client data...");
        socket.receive(packet);
        String info = new String(data, 0, packet.getLength());
        System.out.println("<Client>: " + info);

        InetAddress address = packet.getAddress();
        int port = packet.getPort();
        byte[] data2 = "Welcome".getBytes();
        DatagramPacket packet2 = new DatagramPacket(data2, data2.length, address, port);
        socket.send(packet2);
        socket.close();
    }
}
