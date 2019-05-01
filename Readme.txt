Team:
Name 1: Kunmiao Yang, Student ID 1: 200200409
Name 2: Mengyan Dong, Student ID 2: 200284681

************************* Code compilation and running *************************

Compile:
This code is developed by Java + IntelliJ. To compile it, you need to install JDK 1.7 and IntelliJ on your machine. The projects contains many artifacts, among them only 4 are for delivery: Simple_ftp_client.jar, Simple_ftp_server.jar, SRP_client.jar, and SRP_server.jar. After compilation, you will find them under the out/artifacts directory.

Simple FTP server:
use the following command line to run the server:
java -jar Simple_ftp_server.jar <port#> <file-name> <p>
where <port#> is the port number to which the server is listening (for this project, this port number is always 7735), <file-name> is the name of the file where the data will be written, and <p> is the packet loss probability discussed above.

Simple FTP client:
use the following command line to run the client:
java -jar Simple_ftp_client.jar <server-host-name> <server-port#> <file-name> <N> <MSS>
where <server-host-name> is the host name where the server runs, <server-port#> is the port number of the server (i.e., 7735), <file-name> is the name of the file to be transferred, <N> is the window size, and <MSS> is the maximum segment size.

Extra credit:
Selective Repeat ARQ server:
use the following command line to run the server:
java -jar SRP_server.jar <port#> <file-name> <p> <N>
where <port#> is the port number to which the server is listening (for this project, this port number is always 7735), <file-name> is the name of the file where the data will be written, and <p> is the packet loss probability discussed above, <N> is the window size.
In Selective Repeat ARQ, since server and client must have the same window size, please make sure <N> is using the same window size as the client.

Selective Repeat ARQ client:
use the following command line to run the client:
java -jar SRP_client.jar <server-host-name> <server-port#> <file-name> <N> <MSS>
where <server-host-name> is the host name where the server runs, <server-port#> is the port number of the server (i.e., 7735), <file-name> is the name of the file to be transferred, <N> is the window size, and <MSS> is the maximum segment size.
In Selective Repeat ARQ, since server and client must have the same window size, please make sure <N> is using the same window size as the client.

************************* Tasks *************************
Testing Environment:
I reserved a VCL for the test. The VCL I reserved was "CSC573+jdk", I guess this machine is just for this class, and it also come with JDK 1.7, that's why I used 1.7 version for this project.
The traceroute testing result shows that the RTT was about 40 ms, so I used 80 ms for the retransmission timer. You can find the traceroute result in traceroute.PNG file under this directory. I ran the server on this VCL machine, and client on my own laptop.

Task results:
You can find the task results for both Go back N and Selective Repeat ARQ in excel file data.xlsx in this directory. There are also 3 diagram comparing the 2 protocols for all the 3 tasks.




