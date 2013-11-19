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

import simmcast.engine.ProcessInterface;
import simmcast.group.Group;
import simmcast.group.GroupInterface;
import simmcast.group.GroupTableInterface;
import simmcast.network.Network;
import simmcast.node.Node;
import simmcast.node.NodeInterface;
import simmcast.script.ScriptParser;

public class Server implements Runnable {

	private static int SERVER_PORT = 12345;
    private ServerSocket server;
    private Vector<Socket> socketconnections;
    private int actualClient;
    private Network network;
    private Connection[] connections;

    private java.util.concurrent.LinkedBlockingQueue<CommandProtocol> in;

    private boolean connected;
    private Thread listenThread;

    public Server(Network network)
    {
    	this.network = network;
    	socketconnections = new Vector<Socket>();
		actualClient = 0;
    	try {
			server = new ServerSocket();
			server.setReuseAddress(true);
			SocketAddress sa = new InetSocketAddress(SERVER_PORT);
			server.bind(sa);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public void listenForConnections()
    {
    	System.out.println("Server listening on " + server.getInetAddress().getHostAddress() + ":" + SERVER_PORT);
    	new Thread(new Runnable() {
			public void run() {
		    	try {
		    		while (true)
		    		{
						Socket client = server.accept();
						socketconnections.add(client);
						System.out.println("New connection from " + client.getInetAddress().getHostAddress());
						System.out.println("Total connections: " + socketconnections.size());
		    		}
				} catch (IOException e) {
					if (!(e instanceof SocketException))
					{
						e.printStackTrace();
					}
				}
			}
		}).start();
    }

    public void stopListening()
    {
    	try {
			server.close();
			if (socketconnections.size()>0)
			{
				in = new java.util.concurrent.LinkedBlockingQueue<CommandProtocol>();
				connected = true;
				connections = new Connection[socketconnections.size()];
				for (int i=0; i<connections.length; i++)
				{
					DataInputStream is = new DataInputStream(socketconnections.get(i).getInputStream());
			    	DataOutputStream os = new DataOutputStream(socketconnections.get(i).getOutputStream());

			    	connections[i] = new Connection(i,socketconnections.get(i).getLocalAddress().getHostAddress(),in, is, os);
					connections[i].start();
				}
				listenThread = new Thread(this);
				listenThread.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public int createNode(int addressId, String label, String className, String[] arguments)
    {
    	int usedClient = actualClient;
    	CommandCreate cc = new CommandCreate(addressId, label, className, arguments);
    	String err = connections[usedClient].sendCmd(cc);
    	if (err==null)
    	{
    		actualClient = (actualClient + 1) % connections.length;
    		return usedClient;
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
    	String err = connections[clientId].sendCmd(ci);
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
    	String err = connections[clientId].sendCmd(crp);
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
    	for (int i=0;i<connections.length;i++)
    	{
    		String err = connections[i].sendCmd(cs);
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

    public void analyzeCmd(CommandProtocol cmd)
    {
//    	cmd.run(); HACER ESTO ASI
		switch (cmd.getAction())
		{
			case CommandProtocol.ACTION_ADD_TO_POOL:
				CommandAddToPool cap = (CommandAddToPool) cmd;
				ProcessProxy pp = new ProcessProxy(network, cap.getClientId());
				network.getSimulationScheduler().addToThreadPool(pp);
				connections[cap.getClientId()].sendOk(cap.getCmdId(),""+pp.getPid());
				break;
			case CommandProtocol.ACTION_ACTIVATE_AT:
				CommandActivateAt cat = (CommandActivateAt) cmd;
				network.getSimulationScheduler().activateAt(cat.getTime(), network.getSimulationScheduler().getFromThreadPool(cat.getPid()));
				connections[cat.getClientId()].sendOk(cat.getCmdId());
				break;
			case CommandProtocol.ACTION_BLOCKED_FINISHED:
				CommandBlockedOrFinished cbf = (CommandBlockedOrFinished) cmd;
				network.getSimulationScheduler().processBlockedOrFinished(cbf.getNetworkId());
				connections[cbf.getClientId()].sendOk(cbf.getCmdId());
				break;
			case CommandProtocol.ACTION_INVOKE:
				CommandInvoke ci = (CommandInvoke) cmd;
				Object obj = null;
				String fnctn = null;
				boolean isGroupTable = false;
				if (Network.isMulticast(ci.getNetworkId()))
				{
					isGroupTable = ci.getFunction().startsWith(GroupTableInterface.GP_FNCTN_PREFIX);
					if (isGroupTable)
					{
						fnctn = ci.getFunction().substring(GroupTableInterface.GP_FNCTN_PREFIX.length());
						obj = network.getGroups();
					}
					else
					{
						fnctn = ci.getFunction();
						obj = network.getGroups().getGroupById(ci.getNetworkId());
					}
				}
				Method method;
				String err = null;
				try {
					method = ScriptParser.findMethod(obj, fnctn, ci.getArguments().length);
					Object[] arguments = generateArguments(method.getParameterTypes(), ci.getArguments());
					Object ret = "";
					if (method.getReturnType()!=void.class)
					{
						ret = method.invoke(obj, arguments);
					}
					else
					{
						method.invoke(obj, arguments);
					}
					if (isGroupTable)
					{
						GroupInterface gi = network.getGroups().getGroupById(ci.getNetworkId());
						ret = gi.getNetworkId() + "," + gi.getName();
						for (int j=0;j<gi.getNetworkIds().length;j++)
						{
							ret = ret + "," + gi.getNetworkIds()[j];
						}
					}
					connections[ci.getClientId()].sendOk(ci.getCmdId(),CommandProtocol.OK_PREFIX + ret.toString());
					return;
				} catch (Exception e) {
					err = e.toString();
				}
				connections[ci.getClientId()].sendError(ci.getCmdId(),err);
				break;
		}
    }

    public String getClientAddress(int clientId)
    {
    	return connections[clientId].getAddress();
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

	   private Object[] generateArguments(Class[] classTypes_, String[] passedArguments_) throws Exception {
		      String argument = null;
		      try {
		         if (passedArguments_.length != classTypes_.length)
		            throw new Exception("Invalid number of arguments");
		         Object[] arguments = new Object[classTypes_.length];
		         int index = 0;
		         for (int i = 0; i < passedArguments_.length; i++) {
		            argument = passedArguments_[i];
		            Class classType = classTypes_[index];

		            // String support
		            if (classType.equals(String.class))
		               arguments[index] = argument;
		            // Handle Java's eight primitive types
		            else if (classType.equals(Boolean.TYPE))
		               arguments[index] = new Boolean(argument);
		            else if (classType.equals(Byte.TYPE))
		               arguments[index] = new Byte(argument);
		            else if (classType.equals(Character.TYPE))
		               arguments[index] = new Character(argument.charAt(0));
		            else if (classType.equals(Double.TYPE))
		            	// teste
		               arguments[index] = new Double(ScriptParser.unitConverter(argument));
		            	//arguments[index] = new Double(argument);
		            else if (classType.equals(Float.TYPE))
		               // teste
		               arguments[index] = new Float(ScriptParser.unitConverter(argument).floatValue());
		               //arguments[index] = new Float(argument);
		            else if (classType.equals(Integer.TYPE)) {
		               try {
		            	  // teste
		            	  arguments[index] = new Integer(ScriptParser.unitConverter(argument).intValue());
		                  //arguments[index] = new Integer(argument);
		               } catch (NumberFormatException ne) {
	                     throw new Exception("Parameter "+argument+" is not a number or a node/group id.");
		               }
		            } else if (classType.equals(Long.TYPE))
		               // teste
		               arguments[index] = new Long(ScriptParser.unitConverter(argument).longValue());
		               //arguments[index] = new Long(argument);
		               
		            else if (classType.equals(Short.TYPE))
		               // teste
		               arguments[index] = new Short(ScriptParser.unitConverter(argument).shortValue());
		               //arguments[index] = new Short(argument);
		            else
		            // Handle an object reference
		            	throw new Exception("Parameter "+argument+" is not of "+classType);
		            index++;
		         }
		         return arguments;

		      // Exception handlers
		      } catch (IndexOutOfBoundsException e) {
		         throw new Exception("Exceeding argument: "+argument);
		      }
		   }
}