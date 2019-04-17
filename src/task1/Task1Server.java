package task1;

import arq.SimpleFTPServer;

import java.io.IOException;

public class Task1Server {
    static final int N_EXP = 5;
    public static void main(String[] args) throws IOException, InterruptedException {
        args[2] = "0.05";
        for (int N = 1; N <= 1024; N *= 2) {
            for (int i = 0; i < N_EXP; i++) {
                System.out.format("Test N = %d, p = %s, #%d\r\n", N, args[2], i);
                SimpleFTPServer.main(args);
            }
        }
    }
}
