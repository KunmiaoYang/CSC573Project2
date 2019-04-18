package task;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

import static arq.Util.*;
import static task.ClientUtil.getTask;
import static task.ClientUtil.test;
import static task.TaskServer.CODE_END;
import static task.TaskServer.TASK_SERVER_PORT;

public class Task3Client {
    public static void main(String[] args) throws IOException {
        CHANNEL_LIST &= (~CHANNEL_VERBOSE);
        CHANNEL_LIST |= CHANNEL_CONCISE;

        String host = args[0];
        Task task = getTask(args[5]);
        args[3] = "64";
        args[4] = "500";
        int avgTime;
        ArrayList<Double> pList = new ArrayList<>();
        ArrayList<Integer> TimeList = new ArrayList<>();

        try(
                Socket socket = new Socket(host, TASK_SERVER_PORT);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()))
        ) {
            for (double p = 0.01; p <= 0.1; p += 0.01) {
                avgTime = test(writer, reader, task, args, String.valueOf(p));
                pList.add(p);
                TimeList.add(avgTime);
            }
            writer.println(CODE_END);
            writer.flush();
        }
        System.out.println("p = " + pList);
        System.out.println("time = " + TimeList);
    }

}
