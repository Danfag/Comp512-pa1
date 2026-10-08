package Middleware;

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
            throws RemoteException {
        synchronized (Middleware.getCustomerRM()) {
            synchronized (Middleware.getFlightRM()) {
                // Check if customer exists
                if (Middleware.getCustomerRM().queryCustomerInfo(customerID).isEmpty()) {
                    return false;
                }
                // Check if flight is available
                if (Middleware.getFlightRM().queryFlight(flightNumber) <= 0) {
                    return false;
                }

                int flightPrice =
                        Middleware.getFlightRM().queryFlightPrice(flightNumber);

                // Reserve for customer
                Middleware.getCustomerRM().reserveFlightForCustomer(customerID,
                        flightNumber, flightPrice);

                // Reserve for flight
                Middleware.getFlightRM().reserveFlight(customerID,
                        flightNumber);
            }
        }

        return true;
    }

    public boolean reserveCar(int customerID, String location)
            throws RemoteException {
        synchronized (Middleware.getCustomerRM()) {
            synchronized (Middleware.getCarRM()) {
                // Check if customer exists
                if (Middleware.getCustomerRM().queryCustomerInfo(customerID).isEmpty()) {
                    return false;
                }
                // Check if car is available
                if (Middleware.getCarRM().queryCars(location) <= 0) {
                    return false;
                }

                int carPrice =
                        Middleware.getCarRM().queryCarsPrice(location);

                // Reserve for customer
                Middleware.getCustomerRM().reserveCarForCustomer(customerID,
                        location,
                        carPrice);

                // Reserve for car
                Middleware.getCarRM().reserveCar(customerID, location);
            }
        }
        return true;
    }

    public boolean reserveRoom(int customerID, String location)
            throws RemoteException {
        synchronized (Middleware.getCustomerRM()) {
            synchronized (Middleware.getRoomRM()) {
                // Check if customer exists
                if (Middleware.getCustomerRM().queryCustomerInfo(customerID).isEmpty()) {
                    return false;
                }
                // Check if room is available
                if (Middleware.getRoomRM().queryRooms(location) <= 0) {
                    return false;
                }

                int roomPrice =
                        Middleware.getRoomRM().queryRoomsPrice(location);

                // Reserve for customer
                Middleware.getCustomerRM().reserveRoomForCustomer(customerID, location,
                        roomPrice);

                // Reserve for room
                Middleware.getRoomRM().reserveRoom(customerID, location);
            }
        }
        return true;
    }

    public boolean bundle(int customerID, Vector<String> flightNumbers,
                                       String location, boolean car, boolean room)
            throws RemoteException
    {
        if (flightNumbers == null || flightNumbers.isEmpty()) {
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

        synchronized (Middleware.getCustomerRM()) {
            synchronized (Middleware.getFlightRM()) {
                if (middleware.queryCustomerInfo(customerID).isEmpty()) {
                    return false;
                }

                // Validate the whole bundle before changing any RM state.
                for (Entry<Integer, Integer> entry : requestedFlights.entrySet()) {
                    if (middleware.queryFlight(entry.getKey()) < entry.getValue()) {
                        return false;
                    }
                }

                synchronized (Middleware.getCarRM()) {
                    synchronized (Middleware.getRoomRM()) {
                    if (car && middleware.queryCars(location) < 1) return false;
                    if (room && middleware.queryRooms(location) < 1) return false;

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
