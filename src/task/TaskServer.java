package task;

import arq.SRPServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static arq.Util.*;
import static task.ClientUtil.getTask;

public class TaskServer {
    static int TASK_SERVER_PORT = 8835;
    static String CODE_READY = "ready";
    static String CODE_END = "end";
    public static void main(String[] args) throws IOException, InterruptedException {
        CHANNEL_LIST &= (~CHANNEL_VERBOSE);
        CHANNEL_LIST |= CHANNEL_CONCISE;

        Task task = getTask(args[4]);
        try(
            ServerSocket serverSocket = new ServerSocket(TASK_SERVER_PORT);
            Socket socket = serverSocket.accept();
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()))
        ) {
            for (String code = reader.readLine(); !code.equals(CODE_END); code = reader.readLine()) {
                System.out.println("\r\n------------------------------------------------------");
                args[2] = reader.readLine();
                args[3] = reader.readLine();
                System.out.println(reader.readLine());
                writer.println(CODE_READY);
                writer.flush();

                task.runServer(args);
            }
        }
    }
}
