package Server.RMI;

import Server.Interface.IResourceManager;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Main {
    public static void main(String args[])
    {
        if (args.length != 2)
        {
            System.err.println("Invalid inputs given");
            System.exit(1);
        }

        // Create the RMI server entry
        try {
            // Create a new Server object
            RMIResourceManager server = new RMIResourceManager(args[0],
                    Integer.parseInt(args[1]));

            // Dynamically generate the stub (client proxy)
            IResourceManager resourceManager =
                    (IResourceManager) UnicastRemoteObject.exportObject(server, RMIResourceManager.serverPort);

            // Bind the remote object's stub in the registry; adjust port if appropriate
            Registry l_registry;
            try {
                l_registry = LocateRegistry.createRegistry(RMIResourceManager.serverPort);
            } catch (RemoteException e) {
                l_registry =
                        LocateRegistry.getRegistry(RMIResourceManager.serverPort);
            }
            final Registry registry = l_registry;
            registry.rebind(RMIResourceManager.rmiPrefix + RMIResourceManager.serverName,
                    resourceManager);

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        registry.unbind(RMIResourceManager.rmiPrefix + RMIResourceManager.serverName);
                        System.out.println("'" + RMIResourceManager.serverName + "' " +
                                "resource manager " +
                                "unbound");
                    }
                    catch(Exception e) {
                        System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
                        e.printStackTrace();
                    }
                }
            });
            System.out.println("'" + RMIResourceManager.serverName + "' resource " +
                    "manager " +
                    "server ready and bound to '" + RMIResourceManager.rmiPrefix + RMIResourceManager.serverName + "'");
        }
        catch (Exception e) {
            System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
