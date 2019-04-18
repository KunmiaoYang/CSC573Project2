package task;

import arq.SRPClient;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static arq.Util.*;
import static task.ClientUtil.getTask;
import static task.ClientUtil.test;
import static task.TaskServer.*;

public class Task2Client {
    public static void main(String[] args) throws IOException {
        CHANNEL_LIST &= (~CHANNEL_VERBOSE);
        CHANNEL_LIST |= CHANNEL_CONCISE;

        String host = args[0];
        Task task = getTask(args[5]);
        args[3] = "64";
        ArrayList<Integer> MSSList = new ArrayList<>();
        ArrayList<Integer> TimeList = new ArrayList<>();

        int avgTime;
        try(
            Socket socket = new Socket(host, TASK_SERVER_PORT);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()))
        ) {

            for (int MSS = 100; MSS <= 1000; MSS += 100) {
                args[4] = Integer.toString(MSS);
                avgTime = test(writer, reader, task, args, "0.05");
                MSSList.add(MSS);
                TimeList.add(avgTime);
            }
            writer.println(CODE_END);
            writer.flush();
        }
        System.out.println("MSS = " + MSSList);
        System.out.println("time = " + TimeList);
    }

}
