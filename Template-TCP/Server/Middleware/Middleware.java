package Middleware;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

// Probably dont need to implement all, just need to reroute
public abstract class Middleware {
    protected String m_name;

    protected SocketStreams flightStreams;
    protected SocketStreams carStreams;
    protected SocketStreams roomStreams;
    protected SocketStreams customerStreams;
    protected Orchestrator orchestratorRM = null;

    protected Middleware(String m_name) {
        this.m_name = m_name;
    }

    protected void setOrchestratorRM(Orchestrator orchestratorRM) {
        this.orchestratorRM = orchestratorRM;
    }

    public static class SocketStreams {
        public BufferedReader from_resource;
        public PrintWriter to_resource;

        public SocketStreams (BufferedReader in,
                            PrintWriter out) {
            this.from_resource = in;
            this.to_resource = out;
        }
    }

    protected SocketStreams getFlightStreams() {
        if (flightStreams == null) {
            throw new IllegalStateException("The middleware is not connected " +
                    "to the flights server. Please call connectServer() to " +
                    "connect to the servers");
        }
        return flightStreams;
    }

    protected SocketStreams getCarStreams() {
        if (carStreams == null) {
            throw new IllegalStateException("The middleware is not connected " +
                    "to the cars server. Please call connectServer() to " +
                    "connect to the servers");
        }
        return carStreams;
    }

    protected SocketStreams getRoomStreams() {
        if (roomStreams == null) {
            throw new IllegalStateException("The middleware is not connected " +
                    "to the rooms server. Please call connectServer() to " +
                    "connect to the servers");
        }
        return roomStreams;
    }

    protected SocketStreams getCustomerStreams() {
        if (customerStreams == null) {
            throw new IllegalStateException("The middleware is not connected " +
                    "to the customers server. Please call connectServer() to " +
                    "connect to the servers");
        }
        return customerStreams;
    }

    private Orchestrator getOrchestratorRM() {
        if (orchestratorRM == null) {
            throw new IllegalStateException("The middleware was not properly " +
                    "built. The orchestrator RM must be " +
                    "defined after " +
                    "instantiation");
        }
        return orchestratorRM;
    }

    // Flight redirection
    public String flightRedirectionWithoutSynchro(String request) throws IOException
    {
        getFlightStreams().to_resource.println(request);
        return getFlightStreams().from_resource.readLine();
    }

    public String flightRedirectionWithSynchro(String request) throws IOException
    {
        synchronized(flightStreams) {
            return flightRedirectionWithoutSynchro(request);
        }
    }

    // Car redirection
    public String carRedirectionWithoutSynchro(String request) throws IOException
    {
        getCarStreams().to_resource.println(request);
        return getCarStreams().from_resource.readLine();
    }

    public String carRedirectionWithSynchro(String request) throws IOException
    {
        synchronized(carStreams) {
            return carRedirectionWithoutSynchro(request);
        }
    }


    // Room redirection
    public String roomRedirectionWithoutSynchro(String request) throws IOException
    {
        getRoomStreams().to_resource.println(request);
        return getRoomStreams().from_resource.readLine();
    }

    public String roomRedirectionWithSynchro(String request) throws IOException
    {
        synchronized(roomStreams) {
            return roomRedirectionWithoutSynchro(request);
        }
    }

    // Customer redirection
    public String customerRedirectionWithoutSynchro(String request) throws IOException
    {
        getCustomerStreams().to_resource.println(request);
        if (request.split(",", 2)[0].trim().equalsIgnoreCase("QUERY_CUSTOMER")) {
            return readFramedResponse(getCustomerStreams().from_resource);
        }
        return getCustomerStreams().from_resource.readLine();
    }

    private String readFramedResponse(BufferedReader reader) throws IOException
    {
        String lengthLine = reader.readLine();
        if (lengthLine == null) {
            throw new IOException("Connection closed before response length");
        }

        int length;
        try {
            length = Integer.parseInt(lengthLine);
        } catch (NumberFormatException e) {
            throw new IOException("Invalid response length: " + lengthLine, e);
        }
        if (length < 0) {
            throw new IOException("Invalid negative response length: " + length);
        }

        char[] response = new char[length];
        int offset = 0;
        while (offset < length) {
            int count = reader.read(response, offset, length - offset);
            if (count == -1) {
                throw new IOException("Connection closed before response completed");
            }
            offset += count;
        }
        return new String(response);
    }

    public String customerRedirectionWithSynchro(String request) throws IOException
    {
        synchronized(customerStreams) {
            return customerRedirectionWithoutSynchro(request);
        }
    }

    // Orchestrator RM methods
    public boolean reserveFlight(int customerID, int flightNumber) throws IOException {
        return getOrchestratorRM().reserveFlight(customerID, flightNumber);
    }

    public boolean reserveCar(int customerID, String location) throws IOException {
        return getOrchestratorRM().reserveCar(customerID, location);
    }

    public boolean reserveRoom(int customerID, String location) throws IOException {
        return getOrchestratorRM().reserveRoom(customerID, location);
    }

    public boolean bundle(int customerID, String[] flightNumbers,
                          String location, boolean car, boolean room) throws IOException {
        return getOrchestratorRM().bundle(customerID, flightNumbers, location,
                car
                , room);
    }

    public String getName() {
        return this.m_name;
    }
}