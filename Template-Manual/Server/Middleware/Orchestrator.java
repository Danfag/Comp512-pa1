package Middleware;

import Server.Interface.IResourceManager;

import java.rmi.RemoteException;
import java.util.Vector;

// instead of extending maybe just have a refernce to one?
// that's kind of what we want anyways
public class Orchestrator {
    private static Middleware middleware;

    private static final String p_name = "Orchestrator";

    public Orchestrator(Middleware middleware) {
        Orchestrator.middleware = middleware;
    }

    // There is this reserveItem primitive which uses
    // the other read,write primtimes. the orchestrator which implements
    // IResourceManager should re-implement this, but instead of checking the
    // map it uses the RMI, and hopefully can use other primitive wrappers
    // (like instead of of read_data(customer), just do getCustomer)
    public boolean reserveFlight(int customerID, int flightNumber)
            throws RemoteException {
        return false;
    }

    public boolean reserveCar(int customerID, String location)
            throws RemoteException {
        return false;
    }

    public boolean reserveRoom(int customerID, String location)
            throws RemoteException {
        return false;
    }

    public synchronized boolean bundle(int customerID, Vector<String> flightNumbers,
                                       String location, boolean car, boolean room)
            throws RemoteException {
        // Customer existence can be checked via the customer resource manager.
        if (middleware.queryCustomerInfo(customerID).isEmpty()) {
            return false;
        }

        if (flightNumbers == null || flightNumbers.isEmpty()) {
            return false;
        }

        // Count duplicate flight numbers because a bundle may request the same
        // flight more than once.
        java.util.Map<Integer, Integer> requestedFlights = new java.util.HashMap<>();
        try {
            for (String flight : flightNumbers) {
                int flightNum = Integer.parseInt(flight);
                requestedFlights.put(flightNum, requestedFlights.getOrDefault(flightNum, 0) + 1);
            }
        } catch (NumberFormatException e) {
            return false;
        }

        // Validate the whole bundle before changing any RM state.
        for (java.util.Map.Entry<Integer, Integer> entry : requestedFlights.entrySet()) {
            if (middleware.queryFlight(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }
        if (car && middleware.queryCars(location) < 1) return false;
        if (room &&  middleware.queryRooms(location) < 1) return false;

        // With bundle synchronized at the middleware, another client cannot
        // interleave a second bundle through this middleware between validation
        // and reservation.
        for (String flight : flightNumbers) {
            if (!this.reserveFlight(customerID, Integer.parseInt(flight))) return false;
        }
        if (car && !this.reserveCar(customerID, location)) return false;
        if (room && ! this.reserveRoom(customerID, location)) return false;
        return true;
    }
}
