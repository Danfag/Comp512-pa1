package Middleware;

import Server.Common.Trace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class MiddlewareRunner extends Middleware implements Runnable{
    private static final char RESPONSE_TERMINATOR = '\u001e';

    // this could have a middleware instance, and whenever it gets a request
    // it can decode which method it should go to, then call it
    // and the middleware and orchestrator can make the requests

    // this is what takes the incoming request, and handles it
    // if we define functions with the same interface as middleware, but
    // instead of just calling, it actually makes the request then returns
    // the response, that would make this seemless

    // So we need an orchestrator insteance and a middleware instance


    // We really just need an orchestrator, but an orchestrator needs a
    // middleware
    private static String name = "MiddlewareRunner";

    private Socket client;
    private Socket flightSocket;
    private Socket carSocket;
    private Socket roomSocket;
    private Socket customerSocket;

    private BufferedReader fromFlight;
    private PrintWriter toFlight;
    private BufferedReader fromCar;
    private PrintWriter toCar;
    private BufferedReader fromRoom;
    private PrintWriter toRoom;
    private BufferedReader fromCustomer;
    private PrintWriter toCustomer;

    private MiddlewareRunner(Socket client,
                            String flightHost, int flightPort,
                            String carHost, int carPort,
                            String roomHost, int roomPort,
                            String customerHost, int customerPort) {
        super(name);
        try {
            this.client = client;

            // Establish dedicated downstream TCP connections for this specific client thread
            flightSocket = new Socket(flightHost, flightPort);
            this.carSocket = new Socket(carHost, carPort);
            this.roomSocket = new Socket(roomHost, roomPort);
            this.customerSocket = new Socket(customerHost, customerPort);

            this.fromFlight = new BufferedReader(new InputStreamReader(flightSocket.getInputStream()));
            this.toFlight = new PrintWriter(flightSocket.getOutputStream(), true);

            this.fromCar = new BufferedReader(new InputStreamReader(carSocket.getInputStream()));
            this.toCar = new PrintWriter(carSocket.getOutputStream(), true);

            this.fromRoom = new BufferedReader(new InputStreamReader(roomSocket.getInputStream()));
            this.toRoom = new PrintWriter(roomSocket.getOutputStream(), true);

            this.fromCustomer = new BufferedReader(new InputStreamReader(customerSocket.getInputStream()));
            this.toCustomer = new PrintWriter(customerSocket.getOutputStream(), true);

        } catch (IOException e) {
            System.err.println("Failed to initialize backend socket streams for runner: " + e.getMessage());
        }
    }

    public static MiddlewareRunner create(Socket client,
                            String flightHost, int flightPort,
                            String carHost, int carPort,
                            String roomHost, int roomPort,
                            String customerHost, int customerPort) {
        MiddlewareRunner middlewareRunner = new MiddlewareRunner(client,
                flightHost, flightPort, carHost, carPort, roomHost, roomPort,
                customerHost, customerPort);

        middlewareRunner.flightStreams =
                new SocketStreams(middlewareRunner.fromFlight,
                middlewareRunner.toFlight);
        middlewareRunner.carStreams =
                new SocketStreams(middlewareRunner.fromCar, middlewareRunner.toCar);
        middlewareRunner.roomStreams = new SocketStreams(middlewareRunner.fromRoom, middlewareRunner.toRoom);
        middlewareRunner.customerStreams = new SocketStreams(middlewareRunner.fromCustomer,
                middlewareRunner.toCustomer);

        Orchestrator orchestrator = new Orchestrator(middlewareRunner);
        middlewareRunner.setOrchestratorRM(orchestrator);

        return middlewareRunner;
    };

    @Override
    public void run() {
        try (
                BufferedReader from_client = new BufferedReader(new InputStreamReader(client.getInputStream()));
                PrintWriter to_client = new PrintWriter(client.getOutputStream(), true)
        ) {
            String request;

            while ((request = from_client.readLine()) != null) {
                String[] tokens = request.split(",");
                String command = tokens[0].trim().toUpperCase();

                String finalResponse = "";

                switch (command) {
                    case "ADD_FLIGHT":
                    case "DELETE_FLIGHT":
                        finalResponse = flightRedirectionWithSynchro(request);
                        break;
                    case "QUERY_FLIGHT":
                    case "QUERY_FLIGHT_PRICE":
                        finalResponse =
                                flightRedirectionWithoutSynchro(request);
                        break;

                    case "ADD_CARS":
                    case "DELETE_CARS":
                        finalResponse = carRedirectionWithSynchro(request);
                        break;
                    case "QUERY_CARS":
                    case "QUERY_CARS_PRICE":
                        finalResponse = carRedirectionWithoutSynchro(request);
                        break;

                    case "ADD_ROOMS":
                    case "DELETE_ROOMS":
                        finalResponse = roomRedirectionWithSynchro(request);
                        break;
                    case "QUERY_ROOMS":
                    case "QUERY_ROOMS_PRICE":
                        finalResponse = roomRedirectionWithoutSynchro(request);
                        break;

                    case "ADD_CUSTOMER":
                    case "ADD_CUSTOMER_ID":
                    case "DELETE_CUSTOMER":
                        finalResponse = customerRedirectionWithSynchro(request);
                        break;
                    case "QUERY_CUSTOMER":
                        finalResponse =
                                customerRedirectionWithoutSynchro(request);
                        break;

                    case "RESERVE_FLIGHT":
                        finalResponse = String.valueOf(
                                reserveFlight(Integer.parseInt(tokens[1]),
                                Integer.parseInt(tokens[2])));
                        break;
                    case "RESERVE_CAR":
                        finalResponse = String.valueOf(
                                reserveCar(Integer.parseInt(tokens[1]),
                                        tokens[2]));
                        break;
                    case "RESERVE_ROOM":
                        finalResponse = String.valueOf(
                                reserveRoom(Integer.parseInt(tokens[1]),
                                        tokens[2]));
                        break;

                    case "BUNDLE":
                         String[] flightNumbers =
                                tokens[2].substring(1,
                                        tokens[2].length()-1).split(",");

                        finalResponse = String.valueOf(
                                bundle(Integer.parseInt(tokens[1]),
                                        flightNumbers, tokens[3],
                                        Boolean.parseBoolean(tokens[4]),
                                        Boolean.parseBoolean(tokens[5])));

                        break;

                    default:
                        finalResponse = "ERROR: Unknown command " + command;
                }

                if (command.equals("QUERY_CUSTOMER")) {
                    to_client.print(finalResponse);
                    to_client.print(RESPONSE_TERMINATOR);
                    to_client.flush();
                } else {
                    to_client.println(finalResponse);
                }
            }

        } catch (IOException e) {
            System.err.println("Middleware runner connection closed or error: " + e.getMessage());
        }
    }
}