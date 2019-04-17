package task3;

import arq.SimpleFTPClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static task3.Task3Server.N_EXP;

public class Task3Client {
    private static int WAIT_TIME = 100;
    public static void main(String[] args) throws IOException {
        args[3] = "64";
        args[4] = "500";
        long start;
        int totalTime, runTime, avgTime;
        ArrayList<Double> pList = new ArrayList<>();
        ArrayList<Integer> TimeList = new ArrayList<>();
        for (double p = 0.01; p <= 0.1; p += 0.01) {
            totalTime = 0;
            for (int i = 0; i < N_EXP; i++) {
                System.out.format("Test start p = %f, N = %s, MSS = %s, #%d\r\n", p, args[3], args[4], i);
                start = System.currentTimeMillis();

                // Run client
                SimpleFTPClient.main(args);

                runTime = (int) (System.currentTimeMillis() - start);
                totalTime += runTime;
                System.out.format("Test result p = %f, N = %s, MSS = %s, #%d, time = %d ms\r\n", p, args[3], args[4], i, runTime);
                try {
                    TimeUnit.MILLISECONDS.sleep(WAIT_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            avgTime = totalTime/N_EXP;
            System.out.format("Group result p = %f, N = %s, MSS = %s, average time = %d ms\r\n", p, args[3], args[4], avgTime);
            pList.add(p);
            TimeList.add(avgTime);
        }
        System.out.println("MSS = " + pList);
        System.out.println("time = " + TimeList);
    }
}
