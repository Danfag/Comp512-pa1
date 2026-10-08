package Client;


/*
The client opens a tcp connection with the middleware, the middleware accepts
 it and hands the connection off to a thread to handle all the requests from
 that client connection

 How do we then communicate with the server resource managers?

 It seems each thread can be treated as a client to the server,
 so each thread can get its own connection with that resource manager, and
 the resource manager can accept then pass it on to a thread running the
 resource manager

 with multiple, each thread I guess needs a connection to each?

 or the middleware just makes 3 connections, one with each, and each thread uses
 that? but then how does the server know which thread to respond to?

 If a single client could send multiple requests at the same time, instead of
 the request loop thread doing the work directly, it would just pass it onto a
 worker pool that just does the actual work and doesn't run in any loops

 So middleware has an accept loop that accepts each client connection, then
 it passes each connection to a request loop which listens for request from
 the specific client it's responsible for.

 Each of these request loop threads will have one socket connection to each
 of the types of resource managers, the resource managers will have similar
 accept loop + request loop

 in the request loop, all the threads should share the same state
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TCPClient extends Client {

    // To connect to socket we need the name and port
    private static String middlewareHost = "localhost";
    private static int middlewarePort = 1027;

    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            middlewareHost = args[0];
        }
        if (args.length > 1) {
            middlewarePort = Integer.parseInt(args[1]);
        }

        TCPClient client = new TCPClient();

        System.out.println("Client connecting to server");
        client.connectServer();
        System.out.println("Client connection successful!");

        client.start();
    }

    public void connectServer() {
        try {
            this.connectServer(middlewareHost, middlewarePort);
        } catch (IOException e) {
            System.err.println("Exception encountered while trying to connect" +
                    " to server.");
        }
    }

    public void connectServer(String middlewareHost, int middlewarePort) throws IOException {
        Socket middlewareSocket = new Socket(middlewareHost, middlewarePort);
        PrintWriter out = new PrintWriter(middlewareSocket.getOutputStream(),
                true);
        BufferedReader in =
                new BufferedReader(new InputStreamReader(middlewareSocket.getInputStream()));

        m_resourceManager = new TCPResourceManager(middlewareSocket, out, in);
    }
}