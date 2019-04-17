package task3;

import arq.SimpleFTPServer;

import java.io.IOException;

public class Task3Server {
    static final int N_EXP = 5;
    public static void main(String[] args) throws IOException, InterruptedException {
        for (double p = 0.01; p <= 0.1; p += 0.01) {
            args[2] = Double.toString(p);
            for (int i = 0; i < N_EXP; i++) {
                System.out.println("\r\n------------------------------------------------------");
                System.out.format("Test p = %f, #%d\r\n", p, i);
                SimpleFTPServer.main(args);
            }
        }
    }
}
