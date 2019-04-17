package task2;

import arq.SimpleFTPClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static task2.Task2Server.N_EXP;

public class Task2Client {
    private static int WAIT_TIME = 100;
    public static void main(String[] args) throws IOException {
        args[3] = "64";
        long start;
        int totalTime, runTime, avgTime;
        ArrayList<Integer> MSSList = new ArrayList<>();
        ArrayList<Integer> TimeList = new ArrayList<>();
        for (int MSS = 100; MSS <= 1000; MSS += 100) {
            args[4] = Integer.toString(MSS);
            totalTime = 0;
            for (int i = 0; i < N_EXP; i++) {
                System.out.format("Test start N = %s, MSS = %s, #%d\r\n", args[3], args[4], i);
                start = System.currentTimeMillis();

                // Run client
                SimpleFTPClient.main(args);

                runTime = (int) (System.currentTimeMillis() - start);
                totalTime += runTime;
                System.out.format("Test result N = %s, MSS = %s, #%d, time = %d ms\r\n", args[3], args[4], i, runTime);
                try {
                    TimeUnit.MILLISECONDS.sleep(WAIT_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            avgTime = totalTime/N_EXP;
            System.out.format("Group result N = %s, MSS = %s, average time = %d ms\r\n", args[3], args[4], avgTime);
            MSSList.add(MSS);
            TimeList.add(avgTime);
        }
        System.out.println("MSS = " + MSSList);
        System.out.println("time = " + TimeList);
    }
}
