package Middleware;

import Server.Common.Trace;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class MiddlewareRunner implements Runnable {
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

    public MiddlewareRunner(Socket client,
                            String flightHost, int flightPort,
                            String carHost, int carPort,
                            String roomHost, int roomPort,
                            String customerHost, int customerPort) {
        try {
            this.client = client;

            // Establish dedicated downstream TCP connections for this specific client thread
            this.flightSocket = new Socket(flightHost, flightPort);
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

    public synchronized boolean bundle(int customerID, Vector<String> flightNumbers,
                                       String location, boolean car, boolean room) {
        try {
            // 1. Check customer existence via customer socket
            toCustomer.println("QUERY_CUSTOMER," + customerID);
            String custResp = fromCustomer.readLine();
            if (custResp == null || custResp.isEmpty() || custResp.startsWith("ERROR")) {
                Trace.warn("Middleware::bundle failed--customer " + customerID + " does not exist");
                return false;
            }

            if (flightNumbers == null || flightNumbers.isEmpty()) {
                Trace.warn("Middleware::bundle failed--at least one flight is required");
                return false;
            }

            // 2. Count duplicate flight numbers requested in the bundle
            Map<Integer, Integer> requestedFlights = new HashMap<>();
            try {
                for (String flight : flightNumbers) {
                    int flightNum = Integer.parseInt(flight.trim());
                    requestedFlights.put(flightNum, requestedFlights.getOrDefault(flightNum, 0) + 1);
                }
            } catch (NumberFormatException e) {
                Trace.warn("Middleware::bundle failed--invalid flight number format");
                return false;
            }

            // 3. Validate flight availability via flight socket
            for (Map.Entry<Integer, Integer> entry : requestedFlights.entrySet()) {
                toFlight.println("QUERY_FLIGHT," + entry.getKey());
                String resp = fromFlight.readLine();
                if (resp == null) return false;
                int available = Integer.parseInt(resp);
                if (available < entry.getValue()) {
                    return false;
                }
            }

            // 4. Validate car availability via car socket
            if (car) {
                toCar.println("QUERY_CARS," + location);
                String resp = fromCar.readLine();
                if (resp == null || Integer.parseInt(resp) < 1) return false;
            }

            // 5. Validate room availability via room socket
            if (room) {
                toRoom.println("QUERY_ROOMS," + location);
                String resp = fromRoom.readLine();
                if (resp == null || Integer.parseInt(resp) < 1) return false;
            }

            // 6. Perform reservations and update customer records
            for (String flight : flightNumbers) {
                int flightNum = Integer.parseInt(flight.trim());
                toFlight.println("RESERVE_FLIGHT," + customerID + "," + flightNum);
                String flightReserveResp = fromFlight.readLine();
                if (!Boolean.parseBoolean(flightReserveResp)) {
                    return false;
                }
                toCustomer.println("ADD_CUSTOMER_RES," + customerID + ",FLIGHT," + flightNum);
                fromCustomer.readLine();
            }

            if (car) {
                toCar.println("RESERVE_CAR," + customerID + "," + location);
                String carReserveResp = fromCar.readLine();
                if (!Boolean.parseBoolean(carReserveResp)) {
                    return false;
                }
                toCustomer.println("ADD_CUSTOMER_RES," + customerID + ",CAR," + location);
                fromCustomer.readLine();
            }

            if (room) {
                toRoom.println("RESERVE_ROOM," + customerID + "," + location);
                String roomReserveResp = fromRoom.readLine();
                if (!Boolean.parseBoolean(roomReserveResp)) {
                    return false;
                }
                toCustomer.println("ADD_CUSTOMER_RES," + customerID + ",ROOM," + location);
                fromCustomer.readLine();
            }

            return true;

        } catch (IOException | NumberFormatException e) {
            System.err.println("Error during bundle execution: " + e.getMessage());
            return false;
        }
    }

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
                    case "QUERY_FLIGHT":
                    case "QUERY_FLIGHT_PRICE":
                        toFlight.println(request);
                        finalResponse = fromFlight.readLine();
                        break;

                    case "ADD_CARS":
                    case "DELETE_CARS":
                    case "QUERY_CARS":
                    case "QUERY_CARS_PRICE":
                        toCar.println(request);
                        finalResponse = fromCar.readLine();
                        break;

                    case "ADD_ROOMS":
                    case "DELETE_ROOMS":
                    case "QUERY_ROOMS":
                    case "QUERY_ROOMS_PRICE":
                        toRoom.println(request);
                        finalResponse = fromRoom.readLine();
                        break;

                    case "ADD_CUSTOMER":
                    case "ADD_CUSTOMER_ID":
                    case "QUERY_CUSTOMER":
                        toCustomer.println(request);
                        finalResponse = fromCustomer.readLine();
                        break;

                    case "RESERVE_FLIGHT":
                        toFlight.println(request);
                        String flightResp = fromFlight.readLine();
                        if (Boolean.parseBoolean(flightResp)) {
                            toCustomer.println("ADD_CUSTOMER_RES," + tokens[1] + ",FLIGHT," + tokens[2]);
                            finalResponse = fromCustomer.readLine();
                        } else {
                            finalResponse = "false";
                        }
                        break;

                    case "DELETE_CUSTOMER":
                        finalResponse = "true";
                        break;

                    case "BUNLDE":
                        try {
                            int customerID = Integer.parseInt(tokens[1]);
                            Vector<String> flightList = new Vector<>();
                            String[] flightsArr = tokens[2].split(";");
                            for (String f : flightsArr) {
                                if (!f.trim().isEmpty()) {
                                    flightList.add(f.trim());
                                }
                            }
                            String location = tokens[3];
                            boolean wantCar = Boolean.parseBoolean(tokens[4]);
                            boolean wantRoom = Boolean.parseBoolean(tokens[5]);

                            boolean success = bundle(customerID, flightList, location, wantCar, wantRoom);
                            finalResponse = Boolean.toString(success);
                        } catch (Exception e) {
                            finalResponse = "false";
                        }
                        break;

                    default:
                        finalResponse = "ERROR: Unknown command " + command;
                }

                to_client.println(finalResponse);
            }

        } catch (IOException e) {
            System.err.println("Middleware runner connection closed or error: " + e.getMessage());
        }
    }
}