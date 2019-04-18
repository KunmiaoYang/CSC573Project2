package task;

import java.io.IOException;

public interface Task {
    void runServer(String[] args) throws IOException, InterruptedException;
    void runClient(String[] args) throws IOException;
}
