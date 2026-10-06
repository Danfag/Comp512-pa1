package Client;

import Server.Interface.IResourceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.Vector;

public class TCPResourceManager implements IResourceManager  {
    Socket socket = null;
    PrintWriter out;
    BufferedReader in;

    public TCPResourceManager(Socket socket, PrintWriter outStream,
                              BufferedReader inStream) {
        this.socket = socket;
        this.out = outStream;
        this.in = inStream;
    }

    public boolean addFlight(int flightNum, int flightSeats, int flightPrice) throws RemoteException {
        String payload = String.join(",", "ADD_FLIGHT", String.valueOf(flightNum),
                String.valueOf(flightSeats),
                String.valueOf(flightPrice));
        out.println(payload);

        try {
            return Boolean.parseBoolean(in.readLine());
        } catch (IOException e) {
            System.err.println("Error occurred while reading response");
            return false;
        }
    }

    public boolean addCars(String location, int numCars, int price) throws RemoteException {
        String payload = String.join(",", "ADD_CARS", location,
                String.valueOf(numCars),
                String.valueOf(price));
        out.println(payload);

        try {
            return Boolean.parseBoolean(in.readLine());
        } catch (IOException e) {
            System.err.println("Error occurred while reading response");
            return false;
        }
    }

    public boolean addRooms(String location, int numRooms, int price) throws RemoteException {
        String payload = String.join(",", "ADD_ROOMS", location,
                String.valueOf(numRooms),
                String.valueOf(price));
        out.println(payload);

        try {
            return Boolean.parseBoolean(in.readLine());
        } catch (IOException e) {
            System.err.println("Error occurred while reading response");
            return false;
        }
    }

    public int newCustomer() throws RemoteException {
        out.println("ADD_CUSTOMER");

        try {
            return Integer.parseInt(in.readLine());
        } catch (IOException e) {
            System.err.println("Error occurred while reading response");
            return -1;
        }
    }

    public boolean newCustomer(int cid) throws RemoteException {
        String payload = String.join(",", "ADD_CUSTOMER_ID",
                String.valueOf(cid));
        out.println(payload);

        try {
            return Boolean.parseBoolean(in.readLine());
        } catch (IOException e) {
            System.err.println("Error occurred while reading response");
            return false;
        }
    }

    public boolean deleteFlight(int flightNum) throws RemoteException {
        String payload = String.join(",", "DELETE_FLIGHT", String.valueOf(flightNum));
        out.println(payload);

        try {
            return Boolean.parseBoolean(in.readLine());
        } catch (IOException e) {
            System.err.println("Error occurred while reading response");
            return false;
        }
    }

    public boolean deleteCars(String location) throws RemoteException {
        String payload = String.join(",", "DELETE_CARS", location);
        out.println(payload);

        try {
            return Boolean.parseBoolean(in.readLine());
        } catch (IOException e) {
            System.err.println("Error occurred while reading response");
            return false;
        }
    }

    public boolean deleteRooms(String location) throws RemoteException {
        String payload = String.join(",", "DELETE_ROOMS", location);
        out.println(payload);

        try {
            return Boolean.parseBoolean(in.readLine());
        } catch (IOException e) {
            System.err.println("Error occurred while reading response");
            return false;
        }
    }

    public boolean deleteCustomer(int customerID) throws RemoteException {
        String payload = String.join(",", "DELETE_CUSTOMER", String.valueOf(customerID));
        out.println(payload);

        try {
            return Boolean.parseBoolean(in.readLine());
        } catch (IOException e) {
            System.err.println("Error occurred while reading response");
            return false;
        }
    }

    public int queryFlight(int flightNumber) throws RemoteException {
        String payload = String.join(",", "QUERY_FLIGHT", String.valueOf(flightNumber));
        out.println(payload);

        try {
            return Integer.parseInt(in.readLine());
        } catch (IOException e) {
            System.err.println("Error occurred while reading response");
            return -1;
        }
    }

    public int queryCars(String location) throws RemoteException {
        String payload = String.join(",", "QUERY_CARS", location);
        out.println(payload);

        try {
            return Integer.parseInt(in.readLine());
        } catch (IOException e) {
            System.err.println("Error occurred while reading response");
            return -1;
        }
    }

    public int queryRooms(String location) throws RemoteException {
        String payload = String.join(",", "QUERY_ROOMS", location);
        out.println(payload);

        try {
            return Integer.parseInt(in.readLine());
        } catch (IOException e) {
            System.err.println("Error occurred while reading response");
            return -1;
        }
    }

    public String queryCustomerInfo(int customerID) throws RemoteException {
        String payload = String.join(",", "QUERY_CUSTOMER", String.valueOf(customerID));
        out.println(payload);

        try {
            return in.readLine();
        } catch (IOException e) {
            System.err.println("Error occurred while reading response");
            return "";
        }
    }

    public int queryFlightPrice(int flightNumber) throws RemoteException {
        String payload = String.join(",", "QUERY_FLIGHT_PRICE", String.valueOf(flightNumber));
        out.println(payload);

        try {
            return Integer.parseInt(in.readLine());
        } catch (IOException e) {
            System.err.println("Error occurred while reading response");
            return -1;
        }
    }

    public int queryCarsPrice(String location) throws RemoteException {
        String payload = String.join(",", "QUERY_CARS_PRICE", location);
        out.println(payload);

        try {
            return Integer.parseInt(in.readLine());
        } catch (IOException e) {
            System.err.println("Error occurred while reading response");
            return -1;
        }
    }

    public int queryRoomsPrice(String location) throws RemoteException {
        String payload = String.join(",", "QUERY_ROOMS_PRICE", location);
        out.println(payload);

        try {
            return Integer.parseInt(in.readLine());
        } catch (IOException e) {
            System.err.println("Error occurred while reading response");
            return -1;
        }
    }

    public boolean reserveFlight(int customerID, int flightNumber) throws RemoteException {
        String payload = String.join(",", "RESERVE_FLIGHT", String.valueOf(customerID), String.valueOf(flightNumber));
        out.println(payload);

        try {
            return Boolean.parseBoolean(in.readLine());
        } catch (IOException e) {
            System.err.println("Error occurred while reading response");
            return false;
        }
    }

    public boolean reserveCar(int customerID, String location) throws RemoteException {
        String payload = String.join(",", "RESERVE_CAR", String.valueOf(customerID), location);
        out.println(payload);

        try {
            return Boolean.parseBoolean(in.readLine());
        } catch (IOException e) {
            System.err.println("Error occurred while reading response");
            return false;
        }
    }

    public boolean reserveRoom(int customerID, String location) throws RemoteException {
        String payload = String.join(",", "RESERVE_ROOM", String.valueOf(customerID), location);
        out.println(payload);

        try {
            return Boolean.parseBoolean(in.readLine());
        } catch (IOException e) {
            System.err.println("Error occurred while reading response");
            return false;
        }
    }

    public boolean bundle(int customerID, Vector<String> flightNumbers, String location, boolean car, boolean room) throws RemoteException {
        String payload = String.join(",", "BUNDLE",
                String.valueOf(customerID), location, String.valueOf(car),
                String.valueOf(room),
                String.join(",", flightNumbers));
        out.println(payload);

        try {
            return Boolean.parseBoolean(in.readLine());
        } catch (IOException e) {
            System.err.println("Error occurred while reading response");
            return false;
        }
    }

    public String getName() throws RemoteException {
        String payload = "GET_NAME";
        out.println(payload);

        try {
            return in.readLine();
        } catch (IOException e) {
            throw new RemoteException("Error occurred while reading response");
        }
    }
}
