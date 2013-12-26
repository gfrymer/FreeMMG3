package simmcast.distribution;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Vector;

import simmcast.distribution.command.CommandCreate;
import simmcast.distribution.command.CommandInvoke;
import simmcast.distribution.command.CommandProtocol;
import simmcast.distribution.command.CommandResumeProcess;
import simmcast.distribution.command.CommandStartSimulation;
import simmcast.distribution.command.CommandStopSimulation;
import simmcast.distribution.command.CommandTerminateProcess;
import simmcast.distribution.communication.CommunicationServer;
import simmcast.distribution.communication.CommunicationServerSocket;
import simmcast.distribution.communication.Connection;
import simmcast.distribution.interfaces.GroupInterface;
import simmcast.distribution.interfaces.GroupTableInterface;
import simmcast.distribution.interfaces.NodeInterface;
import simmcast.distribution.interfaces.ProcessInterface;
import simmcast.group.Group;
import simmcast.network.Network;
import simmcast.node.Node;
import simmcast.script.ScriptParser;

public class Manager implements Runnable {

    private Network network;
    private Vector<Connection> connections;
    private CommunicationServer commServer;
    private int actualWorker;
 
    private java.util.concurrent.LinkedBlockingQueue<CommandProtocol> in;

    private boolean connected;
    private Thread listenThread;

    public Manager(Network network)
    {
    	this.network = network;
    	actualWorker = 0;
    	commServer = new CommunicationServerSocket();
    	commServer.create();
    }

    public void listenForConnections()
    {
    	in = new java.util.concurrent.LinkedBlockingQueue<CommandProtocol>();
    	connections = new Vector<Connection>();
    	System.out.println("Manager listening on " + commServer.getDescription());
    	new Thread(new Runnable() {
			public void run() {
    			int totConnections = 0;
	    		while (true)
	    		{
	    			Connection clientConn = commServer.listen(totConnections, in);
	    			if (clientConn!=null)
	    			{
	    				totConnections++;
		    			connections.add(clientConn);
						System.out.println("New connection from " + clientConn.getDescription());
						System.out.println("Total connections: " + totConnections);
	    			}
	    			else
	    			{
	    				break;
	    			}
	    		}
			}
		}).start();
    }

    public void stopListening()
    {
		commServer.disconnect();
		connected = true;
		listenThread = new Thread(this);
		listenThread.start();
    }

    public int createNode(int addressId, String label, String className, String[] arguments)
    {
    	int usedWorker = actualWorker;
    	CommandCreate cc = new CommandCreate(addressId, label, className, arguments);
    	String err = connections.get(usedWorker).sendCmd(cc);
    	if (err==null)
    	{
    		actualWorker = (actualWorker + 1) % connections.size();
    		return usedWorker;
		}
    	else
    	{
    		System.err.println(err);
    		return -1;
    	}
    }

    public boolean invokeCommand(int clientId, int addressId, String function, String[] arguments)
    {
    	CommandInvoke ci = new CommandInvoke(addressId, function, arguments);
    	String err = connections.get(clientId).sendCmd(ci);
    	if (err==null)
    	{
    		return true;
		}
    	else
    	{
    		System.err.println(err);
    		return false;
    	}
    }

    public boolean resumeProcess(int clientId, int addressId)
    {
    	CommandResumeProcess crp = new CommandResumeProcess(addressId, network.getSimulationScheduler().currentTime());
    	String err = connections.get(clientId).sendCmd(crp);
    	if (err==null)
    	{
    		return true;
		}
    	else
    	{
    		System.err.println(err);
    		return false;
    	}
    }

    public boolean terminateProcess(int clientId, int addressId)
    {
    	CommandTerminateProcess ctp = new CommandTerminateProcess(addressId);
    	String err = connections.get(clientId).sendCmd(ctp);
    	if (err==null)
    	{
    		return true;
		}
    	else
    	{
    		System.err.println(err);
    		return false;
    	}
    }

    public boolean startSimulation()
    {
    	CommandStartSimulation cs = new CommandStartSimulation();
    	for (int i=0;i<connections.size();i++)
    	{
    		String err = connections.get(i).sendCmd(cs);
    		if (err==null)
    		{
    		}
    		else
    		{
    			System.err.println(err);
    			return false;
    		}
    	}
    	return true;
    }

    public boolean stopSimulation()
    {
    	connected = false;
    	CommandStopSimulation ss = new CommandStopSimulation();
    	for (int i=0;i<connections.size();i++)
    	{
    		String err = connections.get(i).sendCmd(ss);
    		if (err==null)
    		{
    		}
    		else
    		{
    			if (err.length()>0)
    			{
	    			System.err.println(err);
	    			return false;
    			}
    		}
    		connections.get(i).disconnect();
    	}
    	listenThread.interrupt();
    	return true;
    }

    public void analyzeCmd(CommandProtocol cmd)
    {
    	String ret = cmd.run(network);
    	if (ret==null)
    	{
    		connections.get(cmd.getClientId()).sendOk(cmd.getCmdId());
    	}
    	else
    	{
    		if (ret.startsWith(CommandProtocol.OK_PREFIX))
    		{
        		connections.get(cmd.getClientId()).sendOk(cmd.getCmdId(),ret.substring(CommandProtocol.OK_PREFIX.length()));
    		}
    		else
    		{
        		connections.get(cmd.getClientId()).sendError(cmd.getCmdId(),ret);
    		}
    	}
    }

    public String getClientDescription(int clientId)
    {
    	return connections.get(clientId).getDescription();
    }

	public void run()
	{
		while (connected)
		{
			try {
				CommandProtocol cp = in.take();
				analyzeCmd(cp);
			} catch (InterruptedException e) {
			}
		}
	}

}