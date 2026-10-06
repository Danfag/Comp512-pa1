package Server.ResourceManagers;

import Server.Interface.IResourceManager;
import Server.Common.Trace;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * RMI middleware for the distributed travel reservation system.
 *
 * The client talks only to this object.  Resource-specific calls are routed to
 * the Flights, Cars, and Rooms resource managers.  Customers are replicated
 * at all three RMs so each RM can maintain the reservation records that belong
 * to the resources it owns.
 */
public class RMIMiddleware implements IResourceManager {
    private static final int RMI_PORT = 1099;
    private static final String RMI_PREFIX = "group_xx_";
    private static final String SERVER_NAME = "Middleware";

    private final IResourceManager flightsRM;
    private final IResourceManager carsRM;
    private final IResourceManager roomsRM;

    public RMIMiddleware(IResourceManager flightsRM, IResourceManager carsRM, IResourceManager roomsRM) {
        this.flightsRM = flightsRM;
        this.carsRM = carsRM;
        this.roomsRM = roomsRM;
    }

    private static IResourceManager connect(String host, String name) throws Exception {
        Registry registry = LocateRegistry.getRegistry(host, RMI_PORT);
        IResourceManager rm = (IResourceManager) registry.lookup(RMI_PREFIX + name);
        System.out.println("Connected to '" + name + "' RM [" + host + ":" + RMI_PORT + "/" + RMI_PREFIX + name + "]");
        return rm;
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java Server.RMI.RMIMiddleware <flights-host> <cars-host> <rooms-host>");
            System.exit(1);
        }

        try {
            IResourceManager flights = connect(args[0], "Flights");
            IResourceManager cars = connect(args[1], "Cars");
            IResourceManager rooms = connect(args[2], "Rooms");

            RMIMiddleware middleware = new RMIMiddleware(flights, cars, rooms);
            IResourceManager stub = (IResourceManager) UnicastRemoteObject.exportObject(middleware, 0);

            Registry registry;
            try {
                registry = LocateRegistry.createRegistry(RMI_PORT);
            } catch (RemoteException e) {
                registry = LocateRegistry.getRegistry(RMI_PORT);
            }
            final Registry finalRegistry = registry;
            registry.rebind(RMI_PREFIX + SERVER_NAME, stub);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    finalRegistry.unbind(RMI_PREFIX + SERVER_NAME);
                    System.out.println("'" + SERVER_NAME + "' middleware unbound");
                } catch (Exception e) {
                    System.err.println("Could not unbind middleware: " + e.getMessage());
                }
            }));

            System.out.println("'" + SERVER_NAME + "' middleware ready and bound to '" + RMI_PREFIX + SERVER_NAME + "'");
        } catch (NotBoundException e) {
            System.err.println("Middleware could not find a ResourceManager in an RMI registry: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Middleware exception: uncaught exception");
            e.printStackTrace();
            System.exit(1);
        }
    }

    // Resource-specific operations -------------------------------------------------

    @Override
    public synchronized boolean addFlight(int flightNum, int flightSeats, int flightPrice) throws RemoteException {
        return flightsRM.addFlight(flightNum, flightSeats, flightPrice);
    }

    @Override
    public synchronized boolean addCars(String location, int numCars, int price) throws RemoteException {
        return carsRM.addCars(location, numCars, price);
    }

    @Override
    public synchronized boolean addRooms(String location, int numRooms, int price) throws RemoteException {
        return roomsRM.addRooms(location, numRooms, price);
    }

    @Override
    public synchronized boolean deleteFlight(int flightNum) throws RemoteException {
        return flightsRM.deleteFlight(flightNum);
    }

    @Override
    public synchronized boolean deleteCars(String location) throws RemoteException {
        return carsRM.deleteCars(location);
    }

    @Override
    public synchronized boolean deleteRooms(String location) throws RemoteException {
        return roomsRM.deleteRooms(location);
    }

    @Override
    public int queryFlight(int flightNumber) throws RemoteException {
        return flightsRM.queryFlight(flightNumber);
    }

    @Override
    public int queryCars(String location) throws RemoteException {
        return carsRM.queryCars(location);
    }

    @Override
    public int queryRooms(String location) throws RemoteException {
        return roomsRM.queryRooms(location);
    }

    @Override
    public int queryFlightPrice(int flightNumber) throws RemoteException {
        return flightsRM.queryFlightPrice(flightNumber);
    }

    @Override
    public int queryCarsPrice(String location) throws RemoteException {
        return carsRM.queryCarsPrice(location);
    }

    @Override
    public int queryRoomsPrice(String location) throws RemoteException {
        return roomsRM.queryRoomsPrice(location);
    }

    @Override
    public synchronized boolean reserveFlight(int customerID, int flightNumber) throws RemoteException {
        return flightsRM.reserveFlight(customerID, flightNumber);
    }

    @Override
    public synchronized boolean reserveCar(int customerID, String location) throws RemoteException {
        return carsRM.reserveCar(customerID, location);
    }

    @Override
    public synchronized boolean reserveRoom(int customerID, String location) throws RemoteException {
        return roomsRM.reserveRoom(customerID, location);
    }

    // Customer operations ----------------------------------------------------------

    @Override
    public synchronized int newCustomer() throws RemoteException {
        // Let one RM generate the id, then replicate that exact customer id.
        int cid = flightsRM.newCustomer();
        boolean carsCreated = carsRM.newCustomer(cid);
        boolean roomsCreated = roomsRM.newCustomer(cid);

        if (!carsCreated || !roomsCreated) {
            // Extremely unlikely id collision. Undo the new copies we know about
            // and retry using the explicit-id operation with another generated id.
            flightsRM.deleteCustomer(cid);
            if (carsCreated) carsRM.deleteCustomer(cid);
            if (roomsCreated) roomsRM.deleteCustomer(cid);
            return newCustomer();
        }
        return cid;
    }

    @Override
    public synchronized boolean newCustomer(int cid) throws RemoteException {
        // Check existence first so a duplicate request cannot leave the replicas
        // in different states.
        if (!flightsRM.queryCustomerInfo(cid).isEmpty()
                || !carsRM.queryCustomerInfo(cid).isEmpty()
                || !roomsRM.queryCustomerInfo(cid).isEmpty()) {
            return false;
        }

        boolean flightCreated = flightsRM.newCustomer(cid);
        boolean carCreated = carsRM.newCustomer(cid);
        boolean roomCreated = roomsRM.newCustomer(cid);
        return flightCreated && carCreated && roomCreated;
    }

    @Override
    public synchronized boolean deleteCustomer(int customerID) throws RemoteException {
        // A valid replicated customer is present at every RM.  Calling all three
        // also causes each RM to restore the inventory reserved from that RM.
        boolean flightDeleted = flightsRM.deleteCustomer(customerID);
        boolean carDeleted = carsRM.deleteCustomer(customerID);
        boolean roomDeleted = roomsRM.deleteCustomer(customerID);
        return flightDeleted && carDeleted && roomDeleted;
    }

    @Override
    public String queryCustomerInfo(int customerID) throws RemoteException {
        String flightBill = flightsRM.queryCustomerInfo(customerID);
        String carBill = carsRM.queryCustomerInfo(customerID);
        String roomBill = roomsRM.queryCustomerInfo(customerID);

        if (flightBill.isEmpty() && carBill.isEmpty() && roomBill.isEmpty()) {
            return "";
        }

        String header = "Bill for customer " + customerID + "\n";
        StringBuilder result = new StringBuilder(header);
        appendBillLines(result, flightBill, header);
        appendBillLines(result, carBill, header);
        appendBillLines(result, roomBill, header);
        return result.toString();
    }

    private static void appendBillLines(StringBuilder result, String bill, String header) {
        if (bill == null || bill.isEmpty()) return;
        if (bill.startsWith(header)) {
            result.append(bill.substring(header.length()));
        } else {
            result.append(bill);
        }
    }

    // Bundle ----------------------------------------------------------------------

    @Override
    public synchronized boolean bundle(int customerID, Vector<String> flightNumbers,
                                       String location, boolean car, boolean room)
            throws RemoteException {
        // Customer existence can be checked on any replica.
        if (flightsRM.queryCustomerInfo(customerID).isEmpty()) {
            Trace.warn("Middleware::bundle failed--customer " + customerID + " does not exist");
            return false;
        }

        if (flightNumbers == null || flightNumbers.isEmpty()) {
            Trace.warn("Middleware::bundle failed--at least one flight is required");
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
            Trace.warn("Middleware::bundle failed--invalid flight number");
            return false;
        }

        // Validate the whole bundle before changing any RM state.
        for (Map.Entry<Integer, Integer> entry : requestedFlights.entrySet()) {
            if (flightsRM.queryFlight(entry.getKey()) < entry.getValue()) {
                return false;
            }
        }
        if (car && carsRM.queryCars(location) < 1) return false;
        if (room && roomsRM.queryRooms(location) < 1) return false;

        // With bundle synchronized at the middleware, another client cannot
        // interleave a second bundle through this middleware between validation
        // and reservation.
        for (String flight : flightNumbers) {
            if (!flightsRM.reserveFlight(customerID, Integer.parseInt(flight))) return false;
        }
        if (car && !carsRM.reserveCar(customerID, location)) return false;
        if (room && !roomsRM.reserveRoom(customerID, location)) return false;
        return true;
    }

    @Override
    public String getName() throws RemoteException {
        return SERVER_NAME;
    }
}
