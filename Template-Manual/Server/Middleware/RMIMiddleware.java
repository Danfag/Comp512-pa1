package Middleware;

import Server.Interface.IResourceManager;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;


public class RMIMiddleware extends Middleware {
    // For client to connect to middleware
    public static String middlewareName = "RMIMiddleware";
    public int middlewarePort;

    public static class ServerInfo {
        IResourceManager resourceManager;
        String name;
        String host;
        int port;
        public ServerInfo(IResourceManager resourceManager,
                                  String name,
                             String host,
                             int port) {
            this.resourceManager = resourceManager;
            this.name = name;
            this.host = host;
            this.port = port;
        }
    }

    // For middleware to connect to server(s)
    public static ServerInfo flightServer = new ServerInfo
            (flightRM, "Flights", "localhost", 1027);
    public static ServerInfo carServer = new ServerInfo
            (carRM, "Cars", "localhost", 1028);
    public static ServerInfo roomServer = new ServerInfo
            (roomRM, "Rooms", "localhost", 1029);
    public static ServerInfo customerServer = new ServerInfo
            (customerRM, "Customers", "localhost", 1030);

    public static final String serversRmiPrefix = "group_27_";

    private RMIMiddleware(String m_middlewareName) {
        super(m_middlewareName);
    }

    public static RMIMiddleware create() {
        RMIMiddleware rmiMiddleware = new RMIMiddleware(middlewareName);

        Orchestrator orchestratorRM = new Orchestrator(rmiMiddleware);

        rmiMiddleware.setOrchestratorRM(orchestratorRM);

        return rmiMiddleware;
    }

    public static void connectServer() throws Exception
    {
        // Connect to flights RM
        ServerInfo[] servers = {flightServer, carServer,
                roomServer, customerServer};

        while (flightRM == null || carRM == null || roomRM == null || customerRM == null) {
            for (ServerInfo server : servers) {
                if (server.resourceManager == null) {
                    System.out.println("Connecting to " + server.name + " server" +
                            "...");
                    Registry registry = LocateRegistry.getRegistry(server.host,
                            server.port);
                    try {
                        server.resourceManager =
                                (IResourceManager) registry.lookup(serversRmiPrefix + server.name);

                        switch (server.name) {
                            case "Flights": {
                                flightRM = server.resourceManager;
                                continue;
                            }
                            case "Cars": {
                                carRM = server.resourceManager;
                                continue;
                            }
                            case "Rooms": {
                                roomRM = server.resourceManager;
                                continue;
                            }
                            case "Customers": {
                                customerRM = server.resourceManager;
                                continue;
                            }
                            default:
                                System.out.println("Something wrong");
                        }
                    } catch (NotBoundException e) {
                        System.out.println("Could not connect to " + server.name + " " +
                                "server: " + e.getMessage() + "\n");
                    }
                }
            }
            Thread.sleep(500);
        }
        System.out.println("Connections complete!");
    }
}