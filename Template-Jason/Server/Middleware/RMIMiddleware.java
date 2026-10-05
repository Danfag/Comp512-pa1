package Middleware;

import Server.Interface.IResourceManager;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.server.UnicastRemoteObject;


public class RMIMiddleware extends Middleware {
    private static String m_middlewareName = "Middleware";
    private static String m_rmiPrefix = "group_27_";

    private static String s_serverHost = "localhost";
    private static int s_serverPort = 1099;
    private static String s_serverName = "Server";
    private static String s_rmiPrefixFlight = "group_27_flight";
    private static String s_rmiPrefixCar = "group_27_car";
    private static String s_rmiPrefixRoom = "group_27_room";
    private static String s_rmiPrefixCustomer = "group_27_customer";

    private static String m_name = "";

    public static void main(String[] args)
    {
        if (args.length > 0)
        {
            s_serverHost = args[0];
        }
        if (args.length > 1)
        {
            s_serverName = args[1];
        }
        if (args.length > 2)
        {
            m_name = args[2];
        }
        if (args.length > 3)
        {
            System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUsage: java client.RMIClient [server_hostname [server_rmiobject]]");
            System.exit(1);
        }

        // Get a reference to the RMIRegister
        try {
            RMIMiddleware middleware = new RMIMiddleware(m_name);

            // Connect to server
            middleware.connectServer();

            // Make available for client to connect to
            IResourceManager middlewareResource =
                    (IResourceManager) UnicastRemoteObject.exportObject(middleware, 0);

            Registry m_registry;
            try {
                m_registry = LocateRegistry.createRegistry(1099);
            } catch (RemoteException e) {
                m_registry = LocateRegistry.getRegistry(1099);
            }
            final Registry registry = m_registry;
            registry.rebind(m_rmiPrefix + m_middlewareName, middlewareResource);

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        registry.unbind(m_rmiPrefix + m_middlewareName);
                        System.out.println("'" + m_middlewareName + "' " +
                                "resource manager unbound");
                    }
                    catch(Exception e) {
                        System.err.println((char)27 + "[31;1mMiddleware " +
                                "exception: " + (char)27 + "[0mUncaught exception");
                        e.printStackTrace();
                    }
                }
            });
            System.out.println("'" + m_middlewareName + "' middleware " +
                    "ready and bound to '" + m_rmiPrefix + m_middlewareName + "'");


        }
        catch (Exception e) {
            System.err.println((char)27 + "[31;1mClient exception: " + (char)27 + "[0mUncaught exception");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public RMIMiddleware(String p_name) {
        super(p_name);
    }

    public void connectServer()
    {
        connectServer(s_serverHost, s_serverPort, s_serverName);
    }

    public void connectServer(String server, int port, String name)
    {
        try {
            boolean first = true;
            String[] rmiPrefixes = {
                     s_rmiPrefixFlight,
                     s_rmiPrefixCar,
                     s_rmiPrefixRoom,
                     s_rmiPrefixCustomer};

            for (int i=0;i<rmiPrefixes.length;i++) {
                while (true) {
                    try {
                        Registry registry = LocateRegistry.getRegistry(server, port);
                        if (i == 0) {
                            flightResourceManager =
                                    (IResourceManager) registry.lookup(rmiPrefixes[i] + name);
                            System.out.println("Connected to '" + name + "' " +
                                    "server [" + server + ":" + port + "/" + rmiPrefixes[i] + name + "]");
                        }
                        if (i == 1) {
                            carResourceManager =
                                    (IResourceManager) registry.lookup(rmiPrefixes[i] + name);
                            System.out.println("Connected to '" + name + "' " +
                                    "server [" + server + ":" + port + "/" + rmiPrefixes[i] + name + "]");
                        }
                        if (i == 2) {
                            roomResourceManager =
                                    (IResourceManager) registry.lookup(rmiPrefixes[i] + name);
                            System.out.println("Connected to '" + name + "' " +
                                    "server [" + server + ":" + port + "/" + rmiPrefixes[i] + name + "]");
                        }
                        if (i == 3) {
                            customerResourceManager =
                                    (IResourceManager) registry.lookup(rmiPrefixes[i] + name);
                            System.out.println("Connected to '" + name + "' " +
                                    "server [" + server + ":" + port + "/" + rmiPrefixes[i] + name + "]");
                        }
                        break;
                    } catch (NotBoundException | RemoteException e) {
                        if (first) {
                            System.out.println("Waiting for '" + name + "' server" +
                                    " [" + server + ":" + port + "/" + rmiPrefixes[i] + name + "]");
                            first = false;
                        }
                    }
                    Thread.sleep(500);
                }
            }
        }
        catch (Exception e) {
            System.err.println((char)27 + "[31;1mServer exception: " + (char)27 + "[0mUncaught exception");
            e.printStackTrace();
            System.exit(1);
        }
    }





}