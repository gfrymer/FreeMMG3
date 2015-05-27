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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import simmcast.distribution.command.CommandProtocol;

public class CommunicationClientSocket implements CommunicationClient, CommunicationStreams {

	public static int WORKER_PORT = 12346;
	private Socket socketconnection;
	private InetSocketAddress managerAddr;
	private InetSocketAddress packetServerAddr;
	private ServerSocket packetServer;
	private Connection connection;
	private String thisAddress;
	private HashMap<String, Streams> packetStreams;
	private Map<String, Streams> synchPacketStreams;
	private Thread t2;
	private boolean connected;

	private class Streams {
		public DataInputStream is;
		public DataOutputStream os;
		public Socket sk;
		public Thread th;
	}

	@Override
	public boolean create() {
		socketconnection = new Socket();
		packetStreams = new HashMap<String, Streams>();
		synchPacketStreams = Collections.synchronizedMap(packetStreams);
		return true;
	}

	@Override
	public Connection connect(String manager) {
		try {
			managerAddr = new InetSocketAddress(manager, CommunicationServerSocket.MANAGER_PORT);
			socketconnection.connect(managerAddr, 10000);
			DataInputStream is = new DataInputStream(socketconnection.getInputStream());
			DataOutputStream os = new DataOutputStream(socketconnection.getOutputStream());
			System.out.println("Connected to " + managerAddr.getHostName() + ":" + managerAddr.getPort());

			thisAddress = CommunicationServerSocket.getFirstAddress().getHostAddress();
    		packetServer = new ServerSocket();
    		packetServer.setReuseAddress(true);
			packetServerAddr = new InetSocketAddress(thisAddress, WORKER_PORT);
			packetServer.bind(packetServerAddr);

			connected = true;
//			DataInputStream udpis = new DataInputStream(new UDPInputStream(thisAddress,CommunicationServerSocket.SERVER_PORT));
			connection = new Connection(-1, /*socketconnection.getLocalAddress() + ":" + socketconnection.getLocalPort()*/ socketconnection.getLocalAddress().getHostName(), is, os, new java.util.concurrent.LinkedBlockingQueue<CommandProtocol>(), this);
			connection.start();
			listenWorkers();
			return connection;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean disconnect() {
		if (!connected)
		{
			return false;
		}
		try {
			connected = false;
			connection.disconnect();
			socketconnection.close();
			if (t2!=null)
			{
				packetServer.close();
				t2.interrupt();
			}
			Set<String> ks = synchPacketStreams.keySet();
			synchronized (synchPacketStreams) {
				Iterator<String> it = ks.iterator();
				while (it.hasNext())
				{
					Streams ss = synchPacketStreams.get(it.next());
					if (ss!=null)
					{
						ss.is.close();
						ss.os.close();
						ss.sk.close();
						ss.th.interrupt();
					}
				}
			}
			synchPacketStreams.clear();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public String getDescription(boolean full) {
		return thisAddress + ((full) ? (":" + socketconnection.getLocalPort()) : "");
	}

	private Streams addStream(DataOutputStream os, DataInputStream is, Socket sk)
	{
		Streams ss = new Streams();
		ss.os = os;
		ss.is = is;
		ss.sk = sk;
		final DataInputStream dins = is;
		ss.th = new Thread(new Runnable() {
			@Override
			public void run() {
				while (connected)
				{
					connection.receive(dins);
				}
			}
		});
		ss.th.start();
		return ss;
	}

	private Streams connectWorker(String worker)
	{
		try {
			InetSocketAddress packetToAddr = new InetSocketAddress(worker, WORKER_PORT);
			Socket socketTo = new Socket();
			socketTo.connect(packetToAddr, 10000);
			DataOutputStream os = new DataOutputStream(socketTo.getOutputStream());
			DataInputStream is = new DataInputStream(socketTo.getInputStream());
			return addStream(os,is,socketTo);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	synchronized public DataOutputStream getOutputStream(String worker)
	{
		if (synchPacketStreams.containsKey(worker))
		{
			return synchPacketStreams.get(worker).os;
		}
		Streams ss = connectWorker(worker);
		if (ss!=null) {
			synchPacketStreams.put(worker, ss);
			return ss.os;
		}
		return null;

		/*DataOutputStream udpos;
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
		return null;*/
	}

	@Override
	synchronized public DataInputStream getInputStream(String worker) {
		if (synchPacketStreams.containsKey(worker))
		{
			return synchPacketStreams.get(worker).is;
		}
		Streams ss = connectWorker(worker);
		if (ss!=null) {
			synchPacketStreams.put(worker, ss);
			return ss.is;
		}
		return null;
	}

	@Override
	public void listenWorkers() {
		t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				while (connected)
				{
			    	try {
						Socket workerFrom = packetServer.accept();

						synchronized (synchPacketStreams) {
							Streams ss = addStream(new DataOutputStream(workerFrom.getOutputStream()), new DataInputStream(workerFrom.getInputStream()), workerFrom);
							synchPacketStreams.put(workerFrom.getInetAddress().getHostAddress(),ss);
						}
					} catch (IOException e) {
						if (!(e instanceof SocketException))
						{
							e.printStackTrace();
						}
					}
				}
			}
		});
		t2.start();
	}
}
