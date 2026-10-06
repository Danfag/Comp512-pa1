package Middleware;

import Server.Common.ResourceManager;
import Server.Interface.*;

import java.rmi.RemoteException;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Vector;


public abstract class Middleware implements IResourceManager {

    protected String m_name = "";

    IResourceManager flightResourceManager = null;
    IResourceManager carResourceManager = null;
    IResourceManager roomResourceManager = null;
    IResourceManager customerResourceManager = null;


    public Middleware(String p_name) {
        this.m_name = p_name;
    }

    public boolean addFlight(int flightNum, int flightSeats, int flightPrice) throws RemoteException {
        return flightResourceManager.addFlight(flightNum, flightSeats,
                flightPrice);

    }

    public boolean addCars(String location, int numCars, int price)
            throws RemoteException {
        return carResourceManager.addCars(location, numCars, price);
    }

    public boolean addRooms(String location, int numRooms, int price)
            throws RemoteException {
        return roomResourceManager.addRooms(location, numRooms, price);
    }

    public int newCustomer()
            throws RemoteException {
        return customerResourceManager.newCustomer();
    }

    public boolean newCustomer(int cid)
            throws RemoteException {
        return customerResourceManager.newCustomer(cid);
    }

    public boolean deleteFlight(int flightNum)
            throws RemoteException {
        return flightResourceManager.deleteFlight(flightNum);
    }

    public boolean deleteCars(String location)
            throws RemoteException {
        return carResourceManager.deleteCars(location);
    };

    public boolean deleteRooms(String location)
            throws RemoteException {
        return roomResourceManager.deleteRooms(location);
    };

    public boolean deleteCustomer(int customerID)
            throws RemoteException {
        return customerResourceManager.deleteCustomer(customerID);
    }

    public int queryFlight(int flightNumber)
            throws RemoteException {
        return flightResourceManager.queryFlight(flightNumber);
    }

    public int queryCars(String location)
            throws RemoteException {
        return carResourceManager.queryCars(location);
    }

    public int queryRooms(String location)
            throws RemoteException {
        return roomResourceManager.queryRooms(location);
    }

    public String queryCustomerInfo(int customerID)
            throws RemoteException {
        return customerResourceManager.queryCustomerInfo(customerID);
    };

    public int queryFlightPrice(int flightNumber)
            throws RemoteException {
        return flightResourceManager.queryFlightPrice(flightNumber);
    }

    public int queryCarsPrice(String location)
            throws RemoteException {
        return carResourceManager.queryCarsPrice(location);
    }

    public int queryRoomsPrice(String location)
            throws RemoteException {
        return roomResourceManager.queryRoomsPrice(location);
    }

    public boolean reserveFlight(int customerID, int flightNumber)
            throws RemoteException {
        return flightResourceManager.reserveFlight(customerID, flightNumber);
    }

    public boolean reserveCar(int customerID, String location)
            throws RemoteException {
        return carResourceManager.reserveCar(customerID, location);
    }

    public boolean reserveRoom(int customerID, String location)
            throws RemoteException {
        return roomResourceManager.reserveRoom(customerID, location);
    }

    public boolean bundle(int customerID, Vector<String> flightNumbers,
                          String location, boolean car, boolean room)
            throws RemoteException {

        if (car && this.queryCars(location) <= 0) {
            return false;
        }
        if (room && this.queryRooms(location) <= 0) {
            return false;
        }
        for (String flightNumber : flightNumbers) {
            if (this.queryFlight(Integer.parseInt(flightNumber)) <= 0) {
                return false;
            }
        }

        // All is available: reserve
        if (car) {
            if (!this.reserveCar(customerID, location)) return false;
        }
        if (room) {
            if (!this.reserveRoom(customerID, location)) return false;
        }

        for (String flightNumber : flightNumbers) {
            if (!this.reserveFlight(customerID,
                    Integer.parseInt(flightNumber))) return false;
        }
        return true;
    };

    public String getName() {
        return this.m_name;
    }

    public abstract void connectServer();
}