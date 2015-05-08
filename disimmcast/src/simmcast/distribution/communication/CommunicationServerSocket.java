package simmcast.distribution.communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;
import java.util.regex.Pattern;

import simmcast.distribution.command.CommandProtocol;

public class CommunicationServerSocket implements CommunicationServer {

	public static int MANAGER_PORT = 12345;
    private ServerSocket server;
    private Vector<Socket> socketconnections;

    private static final String IPV4_BASIC_PATTERN_STRING =
            "(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}" + // initial 3 fields, 0-255 followed by .
             "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])"; // final field, 0-255

    private static final Pattern IPV4_PATTERN =
        Pattern.compile("^" + IPV4_BASIC_PATTERN_STRING + "$");

    public static InetAddress getFirstAddress()
    {
    	return getFirstAddress(null);
    }

    public static InetAddress getFirstAddress(String checkAddr)
    {
    	Enumeration<NetworkInterface> nets;
		try {
			nets = NetworkInterface.getNetworkInterfaces();
	        for (NetworkInterface netint : Collections.list(nets))
	        {
				Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
		        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
		        	if (inetAddress.isLoopbackAddress() || inetAddress.isMulticastAddress() || !IPV4_PATTERN.matcher(inetAddress.getHostAddress()).matches())
		        	{
		        		continue;
		        	}
		        	if (checkAddr!=null)
		        	{
		        		if (!checkAddr.equals(inetAddress.getHostAddress()))
		        		{
		        			continue;
		        		}
		        	}
		            return inetAddress;	            	
		        }
	        }
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }

	@Override
	public boolean create() {
		return create(null);
	}

	@Override
	public boolean create(String inetAddr) {
    	socketconnections = new Vector<Socket>();
    	try {
			server = new ServerSocket();
			server.setReuseAddress(true);
			SocketAddress sa = new InetSocketAddress(getFirstAddress(inetAddr),MANAGER_PORT);
			server.bind(sa);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public Connection listen(int connNumber, java.util.concurrent.LinkedBlockingQueue<CommandProtocol> inqueue)
	{
    	try {
			Socket client = server.accept();

			DataInputStream is = new DataInputStream(client.getInputStream());
	    	DataOutputStream os = new DataOutputStream(client.getOutputStream());

	    	Connection cn = new Connection(connNumber, ((InetSocketAddress) client.getRemoteSocketAddress()).getAddress().getHostAddress(), is, os, inqueue);
			cn.start();

			return cn;
		} catch (IOException e) {
			if (!(e instanceof SocketException))
			{
				e.printStackTrace();
			}
			return null;
		}
	}

	@Override
	public String getDescription() {
		return server.getInetAddress().getHostAddress() + ":" + MANAGER_PORT;
	}

	@Override
	public boolean disconnect() {
		try {
			server.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

}
