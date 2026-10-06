package Middleware;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class TCPMiddleware {
    private static int middlewarePort = 1027;

    private static String serverFlightHost = "localhost";
    private static int serverFlightPort = 1027;
    private static String serverCarHost = "localhost";
    private static int serverCarPort = 10271;
    private static String serverRoomHost = "localhost";
    private static int serverRoomPort = 10272;
    private static String serverCustomerHost = "localhost";
    private static int serverCustomerPort = 10273;

    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            serverFlightHost = args[0];
            serverFlightPort = Integer.parseInt(args[1]);
            serverCarHost = args[2];
            serverCarPort = Integer.parseInt(args[3]);
            serverRoomHost = args[4];
            serverRoomPort = Integer.parseInt(args[5]);
            serverCustomerHost = args[6];
            serverCustomerPort = Integer.parseInt(args[7]);
            middlewarePort = Integer.parseInt(args[8]);
        }
        // Open socket for client to connect to
        ServerSocket middleware_socket = new ServerSocket(middlewarePort);

        while (true) {
            Socket client = middleware_socket.accept();

            new Thread(new MiddlewareRunner(
                    client,
                    serverFlightHost, serverFlightPort,
                    serverCarHost, serverCarPort,
                    serverRoomHost, serverRoomPort,
                    serverCustomerHost, serverCustomerPort
            )).start();
        }
    }
}
