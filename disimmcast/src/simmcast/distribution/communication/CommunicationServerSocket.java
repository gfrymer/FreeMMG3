package simmcast.distribution.communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Vector;

import simmcast.distribution.command.CommandProtocol;

public class CommunicationServerSocket implements CommunicationServer {

	public static int SERVER_PORT = 12345;
    private ServerSocket server;
    private Vector<Socket> socketconnections;

	@Override
	public boolean create() {
    	socketconnections = new Vector<Socket>();
    	try {
			server = new ServerSocket();
			server.setReuseAddress(true);
			SocketAddress sa = new InetSocketAddress(SERVER_PORT);
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

	    	Connection cn = new Connection(connNumber, client.getLocalAddress().getHostAddress(), is, os, inqueue);
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
		return server.getInetAddress().getHostAddress() + ":" + SERVER_PORT;
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
