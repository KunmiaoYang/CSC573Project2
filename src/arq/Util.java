package arq;

import java.net.DatagramPacket;
import java.net.InetAddress;

class Util {
    static final int CHANNEL_SERVER = 1;
    static final int CHANNEL_CLIENT_SEND = 2;
    static final int CHANNEL_CLIENT_RECEIVE = 4;
    static final int CHANNEL_CONTENT = 8;
    private static int CHANNEL_LIST = CHANNEL_SERVER | CHANNEL_CLIENT_SEND;
//    private static int CHANNEL_LIST = CHANNEL_SERVER | CHANNEL_CLIENT_RECEIVE;
//    private static int CHANNEL_LIST = CHANNEL_SERVER | CHANNEL_CLIENT_RECEIVE | CHANNEL_CLIENT_SEND;
//    private static int CHANNEL_LIST = 0;

    static final int BUFF_SIZE = 2*1024;
    static final int HEADER_SIZE = 8;
    static final byte ACK_PACKET = (byte) 0b10101010;
    static final byte DATA_PACKET = (byte) 0b01010101;
    private static final byte END_PACKET = (byte) 0b11111111;
    static final int END_MARK = 0xFFFF;
    private static final int MASK16 = 0xFFFF;
    private static final int MASK8 = 0xFF;

    private static byte[] dataEnd = {
            0, 0, 0, 0,
            0, 0, END_PACKET, END_PACKET,
    };
    static byte[] testData = {
            69, 0, 0, 28,
            0, 1, 0, 0,
            4, 17, 0, 0,
            10, 12, 14, 5,
            12, 6, 7, 9
    };
    static void println(int channel, String s) {
        if ((channel & CHANNEL_LIST) == channel)
            System.out.println(s);
    }
    static void format(int channel, String s, Object... args) {
        if ((channel & CHANNEL_LIST) == channel)
            System.out.format(s, args);
    }
    static int calcChecksum(int checksum, byte[] data, int start, int end) {
        for (int i = start; i < end; i += 2) {
            checksum += (data[i] << 8);
            if (i + 1 < data.length)
                checksum += data[i + 1];
        }
        return  (checksum & MASK16)^MASK16;
    }
    static void encodeNum(long num, int bytes, byte[] data, int start) {
        if (start + bytes >= data.length) return;
        for (int i = start + bytes - 1; i >= start; i--, num >>= 8) {
            data[i] = (byte) (num&MASK8);
        }
    }
    static long decodeNum(int bytes, byte[] data, int start) {
        if (start + bytes >= data.length) return 0;
        long num = 0;
        for (int i = start; i < start + bytes; i++) {
            num = (num << 8) | (data[i] & MASK8);
//            System.out.println("data: " + Integer.toBinaryString(data[i] & MASK8));
//            System.out.println(num + ": " + Long.toBinaryString(num));
        }
        return num;
    }
    static DatagramPacket createDataPacket(
            long seq, byte[] data, int dataSize,
            InetAddress address, int port) {
        int checksum = calcChecksum(0, data, HEADER_SIZE, HEADER_SIZE + dataSize);
        encodeNum(seq, 4, data, 0);
        encodeNum(checksum, 2, data, 4);
        data[6] = DATA_PACKET;
        data[7] = DATA_PACKET;
        return new DatagramPacket(
                data, HEADER_SIZE + dataSize,
                address, port);
    }
    static DatagramPacket createEndPacket(
            long ack, InetAddress address, int port) {
        encodeNum(ack, 4, dataEnd, 0);
        return new DatagramPacket(
                dataEnd, HEADER_SIZE,
                address, port);
    }
    static String bytes2Binary(byte[] data, int start, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length && start + i < data.length; i++)
            sb.append(' ').append(Integer.toBinaryString(data[start + i]&MASK8));
        return sb.substring(1);
    }
}
