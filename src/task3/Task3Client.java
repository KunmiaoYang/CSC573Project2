package task3;

import arq.SimpleFTPClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static arq.Util.*;
import static task3.Task3Server.N_EXP;

public class Task3Client {
    private static int WAIT_TIME = 5000;
    public static void main(String[] args) throws IOException {
        CHANNEL_LIST &= (~CHANNEL_VERBOSE);
        CHANNEL_LIST |= CHANNEL_CONCISE;

        args[3] = "64";
        args[4] = "500";
        long start;
        int totalTime, runTime, avgTime;
        ArrayList<Double> pList = new ArrayList<>();
        ArrayList<Integer> TimeList = new ArrayList<>();
        for (double p = 0.01; p <= 0.1; p += 0.01) {
            totalTime = 0;
            for (int i = 0; i < N_EXP; i++) {
                System.out.println("------------------------------------------------------");
                System.out.format("Test start p = %f, N = %s, MSS = %s, #%d\r\n", p, args[3], args[4], i);
                try {
                    TimeUnit.MILLISECONDS.sleep(WAIT_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                start = System.currentTimeMillis();

                // Run client
                SimpleFTPClient.main(args);

                runTime = (int) (System.currentTimeMillis() - start);
                totalTime += runTime;
                System.out.format("\r\nTest result p = %f, N = %s, MSS = %s, #%d, time = %d ms\r\n", p, args[3], args[4], i, runTime);
            }
            avgTime = totalTime/N_EXP;
            System.out.format("Group result p = %f, N = %s, MSS = %s, average time = %d ms\r\n", p, args[3], args[4], avgTime);
            pList.add(p);
            TimeList.add(avgTime);
        }
        System.out.println("p = " + pList);
        System.out.println("time = " + TimeList);
    }
}
