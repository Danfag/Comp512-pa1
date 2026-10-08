package Middleware;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

// instead of extending maybe just have a refernce to one?
// that's kind of what we want anyways
public class Orchestrator {
    private static Middleware middleware;

    private static final String p_name = "Orchestrator";

    public Orchestrator(Middleware middleware) {
        Orchestrator.middleware = middleware;
    }

    public boolean reserveFlight(int customerID, int flightNumber)
            throws IOException {
        synchronized (middleware.getCustomerStreams()) {
            synchronized (middleware.getFlightStreams()) {
                // Check if customer exists
                String payload = String.join(",", "QUERY_CUSTOMER",
                        String.valueOf(customerID));
                if (middleware.customerRedirectionWithoutSynchro(payload).isEmpty()) {
                    return false;
                }
                // Check if flight is available
                payload = String.join(",", "QUERY_FLIGHT",
                        String.valueOf(flightNumber));
                if (Integer.parseInt(middleware.flightRedirectionWithoutSynchro(payload)) <= 0) {
                    return false;
                }

                payload = String.join(",", "QUERY_FLIGHT_PRICE",
                        String.valueOf(flightNumber));
                int flightPrice =
                        Integer.parseInt(middleware.flightRedirectionWithoutSynchro(payload));

                // Reserve for customer
                payload = String.join(",", "RESERVE_FLIGHT_CUSTOMER",
                        String.valueOf(customerID),
                        String.valueOf(flightNumber),
                        String.valueOf(flightPrice));
                middleware.customerRedirectionWithSynchro(payload);

                // Reserve for flight
                payload = String.join(",", "RESERVE_FLIGHT",
                        String.valueOf(customerID),
                        String.valueOf(flightNumber));
                middleware.flightRedirectionWithSynchro(payload);
            }
        }

        return true;
    }

    public boolean reserveCar(int customerID, String location)
            throws IOException {
        synchronized (middleware.getCustomerStreams()) {
            synchronized (middleware.getCarStreams()) {
                // Check if customer exists
                String payload = String.join(",", "QUERY_CUSTOMER",
                        String.valueOf(customerID));
                if (middleware.customerRedirectionWithoutSynchro(payload).isEmpty()) {
                    return false;
                }

                // Check if car is available
                payload = String.join(",", "QUERY_CARS",
                        location);
                if (Integer.parseInt(middleware.carRedirectionWithoutSynchro(payload)) <= 0) {
                    return false;
                }

                payload = String.join(",", "QUERY_CARS_PRICE", location);
                int carPrice =
                        Integer.parseInt(middleware.carRedirectionWithoutSynchro(payload));

                // Reserve for customer
                payload = String.join(",", "RESERVE_CAR_CUSTOMER",
                        String.valueOf(customerID),
                        location, String.valueOf(carPrice));
                if (!Boolean.parseBoolean(middleware.customerRedirectionWithSynchro(payload))) {
                    System.out.println("reservation failed");
                    return false;
                }

                // Reserve for car
                payload = String.join(",", "RESERVE_CAR",
                        String.valueOf(customerID), location);
                if (!Boolean.parseBoolean(middleware.carRedirectionWithSynchro(payload))) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean reserveRoom(int customerID, String location)
            throws IOException {
        synchronized (middleware.getCustomerStreams()) {
            synchronized (middleware.getRoomStreams()) {
                // Check if customer exists
                String payload = String.join(",", "QUERY_CUSTOMER",
                        String.valueOf(customerID));
                if (middleware.customerRedirectionWithoutSynchro(payload).isEmpty()) {
                    return false;
                }

                // Check if car is available
                payload = String.join(",", "QUERY_ROOMS",
                        location);
                if (Integer.parseInt(middleware.roomRedirectionWithoutSynchro(payload)) <= 0) {
                    return false;
                }

                payload = String.join(",", "QUERY_ROOMS_PRICE", location);
                int roomPrice =
                        Integer.parseInt(middleware.roomRedirectionWithoutSynchro(payload));

                // Reserve for customer
                payload = String.join(",", "RESERVE_ROOM_CUSTOMER",
                        String.valueOf(customerID),
                        location, String.valueOf(roomPrice));
                if (!Boolean.parseBoolean(middleware.customerRedirectionWithSynchro(payload))) {
                    return false;
                }

                // Reserve for room
                payload = String.join(",", "RESERVE_ROOM",
                        String.valueOf(customerID), location);
                if (!Boolean.parseBoolean(middleware.roomRedirectionWithSynchro(payload))) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean deleteCustomer(int customerID) throws IOException {
        synchronized (middleware.getCustomerStreams()) {
            synchronized (middleware.getFlightStreams()) {
                synchronized (middleware.getCarStreams()) {
                    synchronized (middleware.getRoomStreams()) {
                        String request = String.join(",", "GET_CUSTOMER_RESERVATIONS",
                                String.valueOf(customerID));
                        String reservationsResponse =
                                middleware.customerRedirectionWithoutSynchro(request);
                        if ("NOT_FOUND".equals(reservationsResponse)) {
                            return false;
                        }

                        Map<String, Integer> reservations = new HashMap<>();
                        String[] lines = reservationsResponse.split("\\r?\\n");
                        try {
                            for (String reservationLine : lines) {
                                String line = reservationLine.trim();
                                if (line.isEmpty()) {
                                    continue;
                                }
                                String[] fields = line.split("\\t", 2);
                                if (fields.length < 2) {
                                    throw new IOException(
                                            "Invalid customer reservation line: " + line);
                                }
                                int count = Integer.parseInt(fields[1]);
                                if (count <= 0) {
                                    throw new IOException(
                                            "Invalid reservation count in line: " + line);
                                }
                                reservations.merge(fields[0].toLowerCase(), count,
                                        Integer::sum);
                            }
                        } catch (NumberFormatException e) {
                            throw new IOException("Invalid customer reservation bill", e);
                        }

                        for (Map.Entry<String, Integer> reservation :
                                reservations.entrySet()) {
                            String key = reservation.getKey();
                            int count = reservation.getValue();
                            String[] keyParts = key.split("-", 2);
                            if (keyParts.length != 2 || keyParts[1].isEmpty()) {
                                throw new IOException(
                                        "Invalid reservation key in customer bill: " + key);
                            }

                            String releaseRequest;
                            switch (keyParts[0]) {
                                case "flight":
                                    int flightNumber;
                                    try {
                                        flightNumber = Integer.parseInt(keyParts[1]);
                                    } catch (NumberFormatException e) {
                                        throw new IOException(
                                                "Invalid flight key in customer bill: " + key, e);
                                    }
                                    releaseRequest = String.join(",", "RELEASE_FLIGHT",
                                            String.valueOf(flightNumber),
                                            String.valueOf(count));
                                    if (!Boolean.parseBoolean(
                                            middleware.flightRedirectionWithSynchro(
                                                    releaseRequest))) {
                                        return false;
                                    }
                                    break;
                                case "car":
                                    releaseRequest = String.join(",", "RELEASE_CAR",
                                            keyParts[1], String.valueOf(count));
                                    if (!Boolean.parseBoolean(
                                            middleware.carRedirectionWithSynchro(
                                                    releaseRequest))) {
                                        return false;
                                    }
                                    break;
                                case "room":
                                    releaseRequest = String.join(",", "RELEASE_ROOM",
                                            keyParts[1], String.valueOf(count));
                                    if (!Boolean.parseBoolean(
                                            middleware.roomRedirectionWithSynchro(
                                                    releaseRequest))) {
                                        return false;
                                    }
                                    break;
                                default:
                                    throw new IOException(
                                            "Unknown reservation type in customer bill: " + key);
                            }
                        }

                        request = String.join(",", "DELETE_CUSTOMER",
                                String.valueOf(customerID));
                        return Boolean.parseBoolean(
                                middleware.customerRedirectionWithSynchro(request));
                    }
                }
            }
        }
    }

    public boolean bundle(int customerID, String[] flightNumbers,
                                       String location, boolean car, boolean room)
            throws IOException
    {
        if (flightNumbers == null || flightNumbers.length == 0) {
            return false;
        }

        // Count duplicate flight numbers because a bundle may request the same
        // flight more than once.
        Map<Integer, Integer> requestedFlights = new HashMap<>();
        try {
            for (String flight : flightNumbers) {
                int flightNum = Integer.parseInt(flight);
                requestedFlights.put(flightNum, requestedFlights.getOrDefault(flightNum, 0) + 1);
            }
        } catch (NumberFormatException e) {
            return false;
        }

        synchronized (middleware.getCustomerStreams()) {
            synchronized (middleware.getCustomerStreams()) {
                String payload = String.join(",", "QUERY_CUSTOMER",
                        String.valueOf(customerID));
                if (middleware.customerRedirectionWithoutSynchro(payload).isEmpty()) {
                    System.err.println("Customer not found");
                    return false;
                }

                // Validate the whole bundle before changing any RM state.
                for (Entry<Integer, Integer> entry : requestedFlights.entrySet()) {
                    payload = String.join(",", "QUERY_FLIGHT",
                            String.valueOf(entry.getKey()));
                    if (
                            Integer.parseInt(
                                    middleware.flightRedirectionWithoutSynchro(payload))
                                    < entry.getValue()) {
                        System.err.println("Flight " + entry.getKey() + " " +
                                "does not have enough available seats");
                        return false;
                    }
                }

                synchronized (middleware.getCarStreams()) {
                    synchronized (middleware.getRoomStreams()) {
                        payload = String.join(",", "QUERY_CARS", location);
                        if (car && Integer.parseInt(middleware.carRedirectionWithoutSynchro(payload)) < 1) return false;
                        payload = String.join(",", "QUERY_ROOMS", location);
                        if (room && Integer.parseInt(middleware.roomRedirectionWithoutSynchro(payload)) < 1) return false;

                        for (String flight : flightNumbers) {
                            if (!this.reserveFlight(customerID, Integer.parseInt(flight)))
                                return false;
                        }
                        if (car && !this.reserveCar(customerID, location)) return false;
                        if (room && !this.reserveRoom(customerID, location))
                            return false;
                        }
                }
            }
        }
        return true;
    }
}
