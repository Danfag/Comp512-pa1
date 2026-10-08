// -------------------------------
// adapted from Kevin T. Manley
// CSE 593
// -------------------------------

package Server.RMI;

import Server.Common.*;

public class RMIResourceManager extends ResourceManager
{
	public static String serverName = "Server";
	public static String rmiPrefix = "group_27_";
	public static int serverPort = 1027;


	public RMIResourceManager(String name, int port)
	{
		super(name);
		serverName = name;
		serverPort = port;
	}
}
