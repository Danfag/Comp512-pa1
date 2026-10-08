package Middleware;

import Server.Interface.IResourceManager;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Main {
    public static void main(String[] args)
    {
        if (args.length != 9) {
            System.err.println("Invalid inputs given");
            System.exit(1);
        }

        // Set hosts and ports according to input
        RMIMiddleware.flightServer.host = args[0];
        RMIMiddleware.flightServer.port = Integer.parseInt(args[1]);
        RMIMiddleware.carServer.host = args[2];
        RMIMiddleware.carServer.port = Integer.parseInt(args[3]);
        RMIMiddleware.roomServer.host = args[4];
        RMIMiddleware.roomServer.port = Integer.parseInt(args[5]);
        RMIMiddleware.customerServer.host = args[6];
        RMIMiddleware.customerServer.port = Integer.parseInt(args[7]);

        // Connect to server
        System.out.println("Connecting middleware to server...");
        try {
            RMIMiddleware.connectServer();
        } catch (Exception e) {
            System.out.println(
                    "Middleware cannot connect to server: " + e.getMessage());
        }
        System.out.println("Connection complete.");

        RMIMiddleware middleware = RMIMiddleware.create();
        middleware.middlewarePort = Integer.parseInt(args[8]);

        // Open middleware to registry
        try {
            IResourceManager middlewareResource =
                    (IResourceManager) UnicastRemoteObject.exportObject(middleware, middleware.middlewarePort);

        Registry m_registry;
        try {
            m_registry = LocateRegistry.createRegistry(middleware.middlewarePort);
        } catch (RemoteException e) {
            m_registry = LocateRegistry.getRegistry(middleware.middlewarePort);
        }

        final Registry registry = m_registry;
        registry.rebind(RMIMiddleware.serversRmiPrefix + RMIMiddleware.middlewareName,
                middlewareResource);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    registry.unbind(RMIMiddleware.serversRmiPrefix + RMIMiddleware.middlewareName);
                    System.out.println("'" + RMIMiddleware.middlewareName + "' " +
                            "resource manager unbound");
                }
                catch(Exception e) {
                    System.err.println((char)27 + "[31;1mMiddleware " +
                            "exception: " + (char)27 + "[0mUncaught exception");
                    e.printStackTrace();
                }
            }
        });
        System.out.println("'" + RMIMiddleware.middlewareName + "' middleware " +
                "ready and bound to '" + RMIMiddleware.serversRmiPrefix + RMIMiddleware.middlewareName +
                "'");

        } catch (RemoteException e) {
            System.err.println("Error occurred while exposing middleware RMI " +
                    "resource. Aborting...");
            System.exit(1);
        }
    }
}
