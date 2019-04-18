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

public class Task1Client {
    public static void main(String[] args) throws IOException {
        CHANNEL_LIST &= (~CHANNEL_VERBOSE);
        CHANNEL_LIST |= CHANNEL_CONCISE;

        String host = args[0];
        Task task = getTask(args[5]);
        args[4] = "500";
        int avgTime;
        ArrayList<Integer> NList = new ArrayList<>();
        ArrayList<Integer> TimeList = new ArrayList<>();

        try(
                Socket socket = new Socket(host, TASK_SERVER_PORT);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()))
        ) {
            for (int N = 1; N <= 1024; N *= 2) {
                args[3] = Integer.toString(N);
                avgTime = test(writer, reader, task, args, "0.05");
                NList.add(N);
                TimeList.add(avgTime);
            }
            writer.println(CODE_END);
            writer.flush();
        }
        System.out.println("N = " + NList);
        System.out.println("time = " + TimeList);
    }

}
