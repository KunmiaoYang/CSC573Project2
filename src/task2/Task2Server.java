package task2;

import arq.SimpleFTPServer;

import java.io.IOException;

import static arq.Util.*;

public class Task2Server {
    static final int N_EXP = 5;
    public static void main(String[] args) throws IOException, InterruptedException {
        CHANNEL_LIST &= (~CHANNEL_VERBOSE);
        CHANNEL_LIST |= CHANNEL_CONCISE;

        args[2] = "0.05";
        for (int MSS = 100; MSS <= 1000; MSS += 100) {
            for (int i = 0; i < N_EXP; i++) {
                System.out.println("\r\n------------------------------------------------------");
                System.out.format("Test MSS = %d, p = %s, #%d\r\n", MSS, args[2], i);
                SimpleFTPServer.main(args);
            }
        }
    }
}
