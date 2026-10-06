// -------------------------------
// adapted from Kevin T. Manley
// CSE 593
// -------------------------------

package Server.ResourceManagers;

import Server.Common.ResourceManager;
import Server.Interface.IResourceManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPResourceManager
{
	private static String p_name = "";
	private static int port = 1027;
	private static IResourceManager resourceManager;

	public static void main(String args[]) throws IOException
	{
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		}
		if (args.length > 1) {
			p_name = args[1];
		}

		// Switch statement to instantiate the right subclass based on p_name
		switch (p_name.toLowerCase()) {
			case "flight":
				resourceManager = new FlightResourceManager();
				break;
			case "car":
				resourceManager = new CarResourceManager();
				break;
			case "room":
				resourceManager = new RoomResourceManager();
				break;
			case "customer":
				resourceManager = new CustomerResourceManager();
				break;
			default:
				System.out.println("Unknown type '" + p_name + "', defaulting to base ResourceManager.");
				resourceManager = new ResourceManager(p_name);
				break;
		}

		ServerSocket serverSocket = new ServerSocket(port);

		System.out.println("Server running on port " + port + ", waiting for connection");

		while (true) {
			try {
				Socket middlewareSocket = serverSocket.accept();

				new Thread(new ServerRunner(resourceManager,
						middlewareSocket)).start();
			} catch (IOException e) {
				System.err.println("Could not get connection");
			}
		}
	}
}