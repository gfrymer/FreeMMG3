package simmcast.distribution.communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Vector;

import simmcast.distribution.command.CommandProtocol;

public class CommunicationClientSocket implements CommunicationClient, CommunicationOutputStream {

	private Socket socketconnection;
	private InetSocketAddress serverAddr;
	private Connection connection;

	@Override
	public boolean create() {
		socketconnection = new Socket();
		return true;
	}

	@Override
	public Connection connect(String server) {
		try {
			serverAddr = new InetSocketAddress(server, CommunicationServerSocket.SERVER_PORT);
			socketconnection.connect(serverAddr, 10000);
			DataInputStream is = new DataInputStream(socketconnection.getInputStream());
			DataOutputStream os = new DataOutputStream(socketconnection.getOutputStream());
			System.out.println("Connected to " + serverAddr.getHostName() + ":" + serverAddr.getPort());
			DataInputStream udpis = new DataInputStream(new UDPInputStream(socketconnection.getInetAddress().getHostName(),CommunicationServerSocket.SERVER_PORT));
			connection = new Connection(-1, /*socketconnection.getLocalAddress() + ":" + socketconnection.getLocalPort()*/ socketconnection.getLocalAddress().getHostName(), is, os, new java.util.concurrent.LinkedBlockingQueue<CommandProtocol>(), udpis, this);
			connection.start();
			return connection;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean disconnect() {
		try {
			connection.disconnect();
			socketconnection.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public String getDescription() {
		return socketconnection.getLocalAddress() + ":" + socketconnection.getLocalPort();
	}

	@Override
	public DataOutputStream getOutputStream(String client)
	{
		DataOutputStream udpos;
		try {
			udpos = new DataOutputStream(new UDPOutputStream(client,CommunicationServerSocket.SERVER_PORT));
			return udpos;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
