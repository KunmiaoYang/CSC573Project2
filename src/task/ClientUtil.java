package task;

import arq.SRPClient;
import arq.SRPServer;
import arq.SimpleFTPClient;
import arq.SimpleFTPServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import static task.TaskServer.CODE_READY;

public class ClientUtil {
    static final int N_EXP = 5;
    private static int WAIT_TIME = 2000;
    private static String TASK_ID_SIMPLE = "0";
    private static String TASK_ID_SRP = "1";

    static Task getTask(String arg) {
        if (arg.equals(TASK_ID_SRP)) return new TaskSRP();
        return new TaskSimpleFTP();
    }

    static int test(PrintWriter writer, BufferedReader reader, Task task, String[] args, String p) throws IOException {
        long start;
        int runTime, avgTime, totalTime = 0;
        for (int i = 0; i < N_EXP; i++) {
            System.out.println("------------------------------------------------------");
            System.out.format("Test start N = %s, MSS = %s, p = %s, #%d\r\n", args[3], args[4], p, i);

            writer.println(CODE_READY);
            writer.println(p);
            writer.println(args[3]);
            writer.format("Test start N = %s, MSS = %s, p = %s, #%d\r\n", args[3], args[4], p, i);
            writer.flush();
            reader.readLine();
            try {
                TimeUnit.MILLISECONDS.sleep(WAIT_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            start = System.currentTimeMillis();

            // Run client
            task.runClient(args);

            runTime = (int) (System.currentTimeMillis() - start);
            totalTime += runTime;
            System.out.format("\r\nTest result N = %s, MSS = %s, p = %s, #%d, time = %d ms\r\n", args[3], args[4], p, i, runTime);
        }
        avgTime = totalTime / N_EXP;
        System.out.format("Group result N = %s, MSS = %s, p = %s, average time = %d ms\r\n", args[3], args[4], p, avgTime);

        return avgTime;
    }

    static class TaskSimpleFTP implements Task {

        @Override
        public void runServer(String[] args) throws IOException, InterruptedException {
            SimpleFTPServer.main(args);
        }

        @Override
        public void runClient(String[] args) throws IOException {
            SimpleFTPClient.main(args);
        }
    }
    static class TaskSRP implements Task {

        @Override
        public void runServer(String[] args) throws IOException, InterruptedException {
            SRPServer.main(args);
        }

        @Override
        public void runClient(String[] args) throws IOException {
            SRPClient.main(args);
        }
    }
}
