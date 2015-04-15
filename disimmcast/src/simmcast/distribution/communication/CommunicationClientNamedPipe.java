package simmcast.distribution.communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;

import simmcast.distribution.command.CommandProtocol;

public class CommunicationClientNamedPipe implements CommunicationClient, CommunicationOutputStream {

	private Connection connection;
	private String thisAddress;
	private String serverAddress;
	private int connNumber;

	@Override
	public boolean create() {
		return true;
	}

	@Override
	public Connection connect(String server) {
		try {
			serverAddress = server;
			thisAddress = CommunicationServerSocket.getFirstAddress().getHostAddress();

			DataOutputStream os = new DataOutputStream(new FileOutputStream(server));
			os.write(CommunicationServerNamedPipe.CONNECT_BYTE);
			os.write(thisAddress.charAt(0));
			os.flush();
			os.close();

			DataInputStream is = new DataInputStream(new FileInputStream(server + CommunicationServerNamedPipe.CLIENT_SUFFIX));
			connNumber = is.readByte();
			is.close();

			DataOutputStream ous = new DataOutputStream(new FileOutputStream(server + "_" + connNumber + CommunicationServerNamedPipe.IN_SUFFIX));
			DataInputStream ins = new DataInputStream(new FileInputStream(server + "_" + connNumber + CommunicationServerNamedPipe.OUT_SUFFIX));
			System.out.println("Connected to " + server);

			DataInputStream udpis = new DataInputStream(new UDPInputStream(thisAddress,CommunicationServerSocket.SERVER_PORT));
			connection = new Connection(-1, thisAddress, ins, ous, new java.util.concurrent.LinkedBlockingQueue<CommandProtocol>(), udpis, this);
			connection.start();
			return connection;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean disconnect() {
		connection.disconnect();

		try {
			Runtime.getRuntime().exec(CommunicationServerNamedPipe.REMOVE_PIPE_COMMAND + serverAddress + "_" + connNumber + CommunicationServerNamedPipe.IN_SUFFIX);
			Runtime.getRuntime().exec(CommunicationServerNamedPipe.REMOVE_PIPE_COMMAND + serverAddress + "_" + connNumber + CommunicationServerNamedPipe.OUT_SUFFIX);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

	@Override
	public String getDescription(boolean full) {
		return thisAddress + ((full) ? connNumber : "");
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
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
