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

public class TCPResourceManager extends ResourceManager
{
	private static int port = 1027;

	public TCPResourceManager(String name, int port) {
		super(name);
		TCPResourceManager.port = port;
	}

	public static void main(String args[]) throws IOException
	{
		if (args.length != 2) {
			System.err.println("Invalid inputs given");
			System.exit(1);
		}

		TCPResourceManager tcpResourceManager =
				new TCPResourceManager(args[0], Integer.parseInt(args[1]));

		ServerSocket serverSocket = new ServerSocket(port);

		System.out.println("Server running on port " + port + ", waiting for connection");

		while (true) {
			try {
				Socket middlewareSocket = serverSocket.accept();

				new Thread(new ServerRunner(tcpResourceManager,
						middlewareSocket)).start();
			} catch (IOException e) {
				System.err.println("Could not get connection");
			}
		}
	}
}