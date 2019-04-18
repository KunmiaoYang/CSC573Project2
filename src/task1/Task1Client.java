package task1;

import arq.SimpleFTPClient;
import arq.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static arq.Util.*;
import static task1.Task1Server.N_EXP;

public class Task1Client {
    private static int WAIT_TIME = 100;
    public static void main(String[] args) throws IOException {
        CHANNEL_LIST &= (~CHANNEL_VERBOSE);
        CHANNEL_LIST |= CHANNEL_CONCISE;

        args[4] = "500";
        long start;
        int totalTime, runTime, avgTime;
        ArrayList<Integer> NList = new ArrayList<>();
        ArrayList<Integer> TimeList = new ArrayList<>();
        for (int N = 1; N <= 1024; N *= 2) {
            args[3] = Integer.toString(N);
            totalTime = 0;
            for (int i = 0; i < N_EXP; i++) {
                System.out.println("------------------------------------------------------");
                System.out.format("Test start N = %d, MSS = %s, #%d\r\n", N, args[4], i);
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
                System.out.format("\r\nTest result N = %d, MSS = %s, #%d, time = %d ms\r\n", N, args[4], i, runTime);
            }
            avgTime = totalTime/N_EXP;
            System.out.format("Group result N = %d, MSS = %s, average time = %d ms\r\n", N, args[4], avgTime);
            NList.add(N);
            TimeList.add(avgTime);
        }
        System.out.println("N = " + NList);
        System.out.println("time = " + TimeList);
    }
}
