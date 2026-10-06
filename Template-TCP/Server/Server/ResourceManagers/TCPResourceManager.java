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

		ServerSocket serverSocket = new ServerSocket(port);

		resourceManager = new ResourceManager(p_name);

		System.out.println("Server running on port " + port + ", waiting for " +
				"connection");
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
