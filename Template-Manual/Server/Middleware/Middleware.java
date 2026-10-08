package Middleware;

import Server.Interface.*;

import java.rmi.RemoteException;
import java.util.Vector;

public abstract class Middleware implements IResourceManager {
    protected String m_name;

    // Assigned by super class
    protected static IResourceManager flightRM = null;
    protected static IResourceManager carRM = null;
    protected static IResourceManager roomRM = null;
    protected static IResourceManager customerRM = null;
    protected static Orchestrator orchestratorRM = null;

    protected Middleware(String m_name) {
        this.m_name = m_name;
    }

    protected void setOrchestratorRM(Orchestrator orchestratorRM) {
        Middleware.orchestratorRM = orchestratorRM;
    }

    protected static IResourceManager getFlightRM() {
        if (flightRM == null) {
            throw new IllegalStateException("The middleware is not connected " +
                    "to the flights server. Please call connectServer() to " +
                    "connect to the servers");
        }
        return Middleware.flightRM;
    }

    protected static IResourceManager getCarRM() {
        if (carRM == null) {
            throw new IllegalStateException("The middleware is not connected " +
                    "to the cars server. Please call connectServer() to " +
                    "connect to the servers");
        }
        return Middleware.carRM;
    }

    protected static IResourceManager getRoomRM() {
        if (roomRM == null) {
            throw new IllegalStateException("The middleware is not connected " +
                    "to the rooms server. Please call connectServer() to " +
                    "connect to the servers");
        }
        return Middleware.roomRM;
    }

    protected static IResourceManager getCustomerRM() {
        if (customerRM == null) {
            throw new IllegalStateException("The middleware is not connected " +
                    "to the customers server. Please call connectServer() to " +
                    "connect to the servers");
        }
        return Middleware.customerRM;
    }

    private static Orchestrator getOrchestratorRM() {
        if (orchestratorRM == null) {
            throw new IllegalStateException("The middleware was not properly " +
                    "built. The orchestrator RM must be " +
                    "defined after " +
                    "instantiation");
        }
        return Middleware.orchestratorRM;
    }

    // Flight RM methods
    public boolean addFlight(int flightNum, int flightSeats, int flightPrice) throws RemoteException {
        synchronized (flightRM) {
            return getFlightRM().addFlight(flightNum, flightSeats,
                    flightPrice);
        }
    }

    public boolean deleteFlight(int flightNum)
            throws RemoteException {
        synchronized (flightRM) {
            return getFlightRM().deleteFlight(flightNum);
        }
    }

    public int queryFlight(int flightNumber)
            throws RemoteException {
        return getFlightRM().queryFlight(flightNumber);
    }

    public int queryFlightPrice(int flightNumber)
            throws RemoteException {
        return getFlightRM().queryFlightPrice(flightNumber);
    }

    // Car RM methods
    public boolean addCars(String location, int numCars, int price)
            throws RemoteException {
        synchronized (carRM) {
            return getCarRM().addCars(location, numCars, price);
        }
    }

    public boolean deleteCars(String location)
            throws RemoteException {
        synchronized (carRM) {
            return getCarRM().deleteCars(location);
        }
    }

    public int queryCars(String location)
            throws RemoteException {
        return getCarRM().queryCars(location);
    }
    public int queryCarsPrice(String location)
            throws RemoteException {
        return getCarRM().queryCarsPrice(location);
    }

    // Room RM methods
    public boolean addRooms(String location, int numRooms, int price)
            throws RemoteException {
        synchronized (roomRM) {
            return getRoomRM().addRooms(location, numRooms, price);
        }
    }

    public boolean deleteRooms(String location)
            throws RemoteException {
        synchronized (roomRM) {
            return getRoomRM().deleteRooms(location);
        }
    };

    public int queryRooms(String location)
            throws RemoteException {
        return  getRoomRM().queryRooms(location);
    }
    public int queryRoomsPrice(String location)
            throws RemoteException {
        return  getRoomRM().queryRoomsPrice(location);
    }

    // Customer RM methods
    public int newCustomer()
            throws RemoteException {
        synchronized (customerRM) {
            return getCustomerRM().newCustomer();
        }
    }

    public boolean newCustomer(int cid)
            throws RemoteException {
        synchronized (customerRM) {
            return getCustomerRM().newCustomer(cid);
        }
    }

    public boolean deleteCustomer(int customerID)
            throws RemoteException {
        return getOrchestratorRM().deleteCustomer(customerID);
    }

    public String queryCustomerInfo(int customerID)
            throws RemoteException {
        return getCustomerRM().queryCustomerInfo(customerID);
    };

    @Override
    public String getCustomerReservations(int customerID) throws RemoteException {
        return getCustomerRM().getCustomerReservations(customerID);
    }

    @Override
    public boolean releaseFlight(int flightNumber, int count) throws RemoteException {
        throw new RemoteException("This method should not be called by client");
    }

    @Override
    public boolean releaseCar(String location, int count) throws RemoteException {
        throw new RemoteException("This method should not be called by client");
    }

    @Override
    public boolean releaseRoom(String location, int count) throws RemoteException {
        throw new RemoteException("This method should not be called by client");
    }

    // Orchestrator RM methods
    @Override
    public boolean reserveFlight(int customerID, int flightNumber) throws RemoteException {
        return getOrchestratorRM().reserveFlight(customerID, flightNumber);
    }

    @Override
    public boolean reserveCar(int customerID, String location) throws RemoteException {
        return getOrchestratorRM().reserveCar(customerID, location);
    }

    @Override
    public boolean reserveRoom(int customerID, String location) throws RemoteException {
        return getOrchestratorRM().reserveRoom(customerID, location);
    }

    @Override
    public boolean bundle(int customerID, Vector<String> flightNumbers, String location, boolean car, boolean room) throws RemoteException {
        return getOrchestratorRM().bundle(customerID, flightNumbers, location,
                car
                , room);
    }

    @Override
    public boolean reserveRoomForCustomer(int customerID, String location, int price) throws RemoteException {
        throw new RemoteException("This method should not be called by client");
    }

    @Override
    public boolean reserveCarForCustomer(int customerID, String location, int price) throws RemoteException {
        throw new RemoteException("This method should not be called by client");
    }

    @Override
    public boolean reserveFlightForCustomer(int customerID, int flightNumber, int flightPrice) throws RemoteException {
        throw new RemoteException("This method should not be called by client");
    }

    public String getName() {
        return this.m_name;
    }
}