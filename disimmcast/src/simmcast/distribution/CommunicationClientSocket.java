package simmcast.distribution;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Vector;

public class CommunicationClientSocket implements CommunicationClient {

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
			connection = new Connection(-1, socketconnection.getLocalAddress() + ":" + socketconnection.getLocalPort(), is, os);
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
}
