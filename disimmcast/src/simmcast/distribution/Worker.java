package simmcast.distribution;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Hashtable;

import simmcast.distribution.command.CommandActivateAt;
import simmcast.distribution.command.CommandAddToPool;
import simmcast.distribution.command.CommandBlockedOrFinished;
import simmcast.distribution.command.CommandCreate;
import simmcast.distribution.command.CommandCreateObject;
import simmcast.distribution.command.CommandInvoke;
import simmcast.distribution.command.CommandPacketArrival;
import simmcast.distribution.command.CommandProtocol;
import simmcast.distribution.command.CommandRemoveFromPool;
import simmcast.distribution.command.CommandStopSimulation;
import simmcast.distribution.communication.CommunicationClient;
import simmcast.distribution.communication.CommunicationClientNamedPipe;
import simmcast.distribution.communication.CommunicationClientSocket;
import simmcast.distribution.communication.Connection;
import simmcast.distribution.interfaces.NodeInterface;
import simmcast.distribution.interfaces.ProcessInterface;
import simmcast.distribution.interfaces.RouterNodeInterface;
import simmcast.distribution.proxies.NodeProxy;
import simmcast.distribution.proxies.ObjectProxy;
import simmcast.distribution.proxies.Proxyable;
import simmcast.distribution.proxies.RouterNodeProxy;
import simmcast.engine.Process;
import simmcast.group.Group;
import simmcast.network.Network;
import simmcast.network.Packet;
import simmcast.node.Node;
import simmcast.node.NodeVector;
import simmcast.script.ScriptParser;

public class Worker implements Runnable {

	private Connection connection;
	private CommunicationClient commClient;
	private boolean started;
	private Thread thread;

	private static final String PROXY_SUFFIX = "Proxy";

   /**
    * A simple "symbol table", where objects created during
    * the parsing of the configuration file are stored.
    */
   Hashtable symbols;

	/** This is a handle to the reflected Class object of the
    * Node class. This is provided for efficiency, since lookup
    * for this class is frequent, as part of the type checking
    * mechanism of this configuration file parser.
    */
   private Class nodeClass;

   /**
    * This is a handle to the reflected Class object of the
    * Group class. This is provided for efficiency, since lookup
    * for this class is frequent, as part of the type checking
    * mechanism of this configuration file parser.
    */
   private Class groupClass;

   /**
    * This is a handle to the reflected Class object of the
    * String class. This is provided for efficiency, since lookup
    * for this class is frequent, as part of the type checking
    * mechanism of this configuration file parser.
    */
   private Class stringClass;

   /**
    * This is a handle to the reflected Class object of the
    * Proxyable interface. This is provided for efficiency, since lookup
    * for this class is frequent, as part of the type checking
    * mechanism of this configuration file parser.
    */
   private Class proxyableInterface;

   /**
    * This is a handle to the reflected Class object of the
    * CloneOnWorker interface. This is provided for efficiency, since lookup
    * for this class is frequent, as part of the type checking
    * mechanism of this configuration file parser.
    */
   private Class cloneOnWorkerInterface;

   private Network network;
   
   private int cmdIdOk;

   private java.util.concurrent.LinkedBlockingQueue<CommandProtocol> in;

    public Worker(Network network)
	{
    	this.network = network;
    	commClient = (Manager.USE_SOCKETS) ? new CommunicationClientSocket() : new CommunicationClientNamedPipe();
    	commClient.create();
		symbols = new Hashtable();
		symbols.put("network", network);
		started = false;
	}

	public boolean connect(String host)
	{
		connection = commClient.connect(host);
		if (connection!=null)
		{
			in = connection.getInQueue();
			return true;
		}
		return false;
	}

    public boolean processBlockedOrFinished(int pid)
    {
    	CommandBlockedOrFinished cbf= new CommandBlockedOrFinished(pid);
    	return send(cbf)==null;
    }

    public boolean removeFromPool(int pid)
    {
    	CommandRemoveFromPool crp = new CommandRemoveFromPool(pid);
    	return send(crp)==null;
    }

    public boolean activateAt(double relativeTime_, ProcessInterface process_)
    {
    	CommandActivateAt cat = new CommandActivateAt(relativeTime_, process_.getPid());
    	return send(cat)==null;
    }

    public String sendPacket(String where, double relativeTime_, Packet p_)
    {
    	if (where.equals(commClient.getDescription(false)))
    	{
    		CommandPacketArrival cpa = new CommandPacketArrival(where, relativeTime_, p_);
    		try {
				in.put(cpa);
	    		return null;
			} catch (InterruptedException e) {
	    		return e.toString();
			}
    	}
    	else
    	{
    		return connection.sendPacket(where, relativeTime_, p_);
    	}
    }

    public boolean addToThreadPool(ProcessInterface process_) 
    {
    	CommandAddToPool cap = new CommandAddToPool();
    	String ok = send(cap,true);
    	if (ok!=null)
    	{
    		if (ok.startsWith(CommandProtocol.OK_PREFIX))
    		{
	    		try
	    		{
	    			int pid = Integer.parseInt(ok.substring(CommandProtocol.OK_PREFIX.length()));
	    			process_.setPid(pid);
	    			return true;
	    		} catch (NumberFormatException ex)
	    		{
	    			System.out.println("Error addToThreadPool " + ex.toString());
	    			return false;
	    		}
    		}
    		else
    		{
    			System.out.println("Error addToThreadPool " + ok);
    			return false;
    		}
    	}
    	else
    	{
    		return false;
    	}
    }

    private String send(CommandProtocol cp)
    {
    	return send(cp, false);
    }

    private String send(CommandProtocol cp, boolean hasParam)
    {
    	String error = connection.sendCmd(cp);
    	if (error==null)
    	{
    		return null;
    	}
    	else
    	{
    		if (hasParam)
    		{
    			return error;
    		}
    		else
    		{
    			System.out.println("Error sending " + cp.toString());
    			return error;
    		}
    	}
   }

    public String invokeCommand(int addressId, String function, String[] arguments)
    {
    	CommandInvoke ci = new CommandInvoke(addressId, function, arguments);
    	String errData = connection.sendCmd(ci);
    	if (errData==null)
    		return errData;
    	if (errData.startsWith(CommandProtocol.OK_PREFIX))
    	{
    		return errData.substring(CommandProtocol.OK_PREFIX.length());
    	}
    	else
    	{
    		System.out.println(errData);
    		return null;
    	}
    }

    public String invokeCommand(String name, String function, String[] arguments)
    {
    	CommandInvoke ci = new CommandInvoke(name, function, arguments);
    	String errData = connection.sendCmd(ci);
    	if (errData==null)
    		return errData;
    	if (errData.startsWith(CommandProtocol.OK_PREFIX))
    	{
    		return errData.substring(CommandProtocol.OK_PREFIX.length());
    	}
    	else
    	{
    		System.out.println(errData);
    		return null;
    	}
    }

    public boolean createNodesAndObjects() throws Exception
	{
    	network.nodes  = new NodeVector();
        nodeClass = Class.forName("simmcast.node.Node");
        groupClass = Class.forName("simmcast.group.Group");
        //streamClass = Class.forName("arjuna.JavaSim.Distributions.RandomStream");
        stringClass = Class.forName("java.lang.String");
        proxyableInterface = Proxyable.class;
        cloneOnWorkerInterface = CloneOnWorker.class;

		CommandProtocol cmd = in.take();
		while (cmd.getAction()!=CommandProtocol.ACTION_START_SIMULATION)
		{
			switch (cmd.getAction())
			{
				case CommandProtocol.ACTION_CREATE:
					CommandCreate cc = (CommandCreate) cmd;
			        Class classType = Class.forName(cc.getClassName());
			        if ( Modifier.isAbstract(classType.getModifiers()) ) {
			           System.err.println("Error: Class is abstract.");
			           throw new Exception();
			        }

			        if (!nodeClass.isAssignableFrom(classType)) {
			        	connection.sendError(cc.getCmdId(), "Create on Worker only for node objects, use Create Object instead");
			        	break;
			        }

			        Constructor constructor = ScriptParser.findConstructor(classType, cc.getArguments().length);
		            Object[] generated = generateArguments(constructor.getParameterTypes(), cc.getArguments());
		            Object newObject = constructor.newInstance(generated);

		            System.out.println("Create " + cc.getLabel());
		            Node node = (Node)newObject;
		            network.initializeNode(node, cc.getLabel());
		            node.setNetworkId(cc.getAddress());
		            network.nodes.add(node);
		            network.tracer.node(node);

		            symbols.put(cc.getLabel(), node);
		            connection.sendOk(cc.getCmdId());
					break;

				case CommandProtocol.ACTION_CREATE_OBJECT:
					CommandCreateObject co = (CommandCreateObject) cmd;
			        Class classTypeObj = Class.forName(co.getClassName());
			        if ( Modifier.isAbstract(classTypeObj.getModifiers()) ) {
			           System.err.println("Error: Class is abstract.");
			           throw new Exception();
			        }

			        Constructor constructorobj = ScriptParser.findConstructor(classTypeObj, co.getArguments().length);
		            Object[] generatedobj = generateArguments(constructorobj.getParameterTypes(), co.getArguments());
		            Object newObjectobj = constructorobj.newInstance(generatedobj);

		        	symbols.put(co.getLabel(),newObjectobj);

		            connection.sendOk(co.getCmdId());
					break;

				case CommandProtocol.ACTION_INVOKE:
					CommandInvoke ci = (CommandInvoke) cmd;
					Object o;
					if (ci.getNetworkId()>-1)
					{
						o = network.nodes.getNodeById(ci.getNetworkId());
					}
					else
					{
						o = symbols.get(ci.getName());
					}
					if (o!=null)
					{
						Method method = ScriptParser.findMethod(o, ci.getFunction(), ci.getArguments().length);
						Object[] arguments = generateArguments(method.getParameterTypes(), ci.getArguments());
						method.invoke(o, arguments);

						connection.sendOk(ci.getCmdId());
					}
					else
					{
						connection.sendError(ci.getCmdId(), "Cannot find " + ((ci.getNetworkId()>-1) ? " Node " + ci.getNetworkId() : " Object " + ci.getName()));
					}
					break;
			}
			cmd = in.take();
		}
		cmdIdOk = cmd.getCmdId(); //connection.sendOk(cmd.getCmdId());
		return true;
	}

    public void startSimulation()
    {
    	connection.sendOk(cmdIdOk);
    	started = true;
    	thread = new Thread(this);
    	thread.start();
    }

    public boolean stopSimulation()
    {
    	CommandStopSimulation css = new CommandStopSimulation();
    	return send(css)==null;
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
	            if (classType.equals(stringClass))
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
	                  Object object = symbols.get(argument);
	                  do {
	                     if (object != null) {
	                        // An example of poor OO design.
	                        if (nodeClass.isAssignableFrom(object.getClass())) {
	                           arguments[index] = new Integer( ((Node)object).getNetworkId() );
	                           break;
	                        } else if (groupClass.isAssignableFrom(object.getClass())) {
	                           arguments[index] = new Integer( ((Group)object).getNetworkId() );
	                           break;
	                        }
	                     }
	                     throw new Exception("Parameter "+argument+" is not a number or a node/group id.");
	                  } while (false);
	               }
	            } else if (classType.equals(Long.TYPE))
	               // teste
	               arguments[index] = new Long(ScriptParser.unitConverter(argument).longValue());
	               //arguments[index] = new Long(argument);
	               
	            else if (classType.equals(Short.TYPE))
	               // teste
	               arguments[index] = new Short(ScriptParser.unitConverter(argument).shortValue());
	               //arguments[index] = new Short(argument);
	            // Handle an object reference
	            else if ((classType.isArray()) && (argument.startsWith("["))) {
	            	String[] args = argument.substring(1,argument.length()-1).split(",");
	            	Object newArr = Array.newInstance(classType.getComponentType(), args.length);
            		Class[] ctype = new Class[1];
            		ctype[0] = classType.getComponentType();
            		String[] parg = new String[1];
	            	for (int h=0;h<args.length;h++)
	            	{
	            		parg[0] = args[h];
	            		Object[] val = generateArguments(ctype, parg);
	            		Array.set(newArr, h, val[0]);
	            	}
	            	arguments[index] = newArr;
	            }
	            else {
	               Object object = symbols.get(argument);
	               if (object==null)
	               {
	            	   if (argument.startsWith("("))//cloneOnWorkerInterface.isAssignableFrom(classType))
	            	   {
	            		   String args = argument.substring(1,argument.length()-1);
	            		   String[] params = args.split(",");
	            		   object = symbols.get(params[0]);
	            		   if (object==null)
	            		   {
	            			   String[] paramsClone = new String[params.length-2];
	            			   System.arraycopy(params, 2, paramsClone, 0, params.length-2);
	            			   classType = Class.forName(params[1]);
	            			   Constructor constructor = ScriptParser.findConstructor(classType, paramsClone.length);
	            			   Object[] generated = generateArguments(constructor.getParameterTypes(), paramsClone);
	            			   object = constructor.newInstance(generated);
	            		   }
	            		   argument = params[0];
	            	   }
	            	   else if (proxyableInterface.isAssignableFrom(classType))
	            	   {
	            		   String classProxyName = classType.getCanonicalName();
	            		   Class classImplement = Class.forName(classProxyName.substring(0,classProxyName.length() - PROXY_SUFFIX.length()));
	            		   Constructor constructor = ScriptParser.findConstructor(classImplement, 0);
	            		   object = constructor.newInstance();
	            	   }
	            	   else if (classType.isAssignableFrom(RouterNodeInterface.class))
	            	   {
	            		   String args = argument.substring(1,argument.length()-1);
	            		   String[] params = args.split(",");
	            		   object = symbols.get(params[0]);
	            		   if (object==null)
	            		   {
		            		   object = new RouterNodeProxy(network, params[0], Integer.parseInt(params[1]), Integer.parseInt(params[2]), params[3]);
	            		   }
	            		   argument = params[0];
	            	   }
	            	   else if (classType.isAssignableFrom(NodeInterface.class))
	            	   {
	            		   String args = argument.substring(1,argument.length()-1);
	            		   String[] params = args.split(",");
	            		   object = symbols.get(params[0]);
	            		   if (object==null)
	            		   {
		            		   object = new NodeProxy(network, params[0], Integer.parseInt(params[1]), Integer.parseInt(params[2]), params[3]);	            		   
	            		   }
	            		   argument = params[0];
	            	   }
	            	   else if (argument.startsWith("{"))
	            	   {
	            		   String args = argument.substring(1,argument.length()-1);
	            		   String[] params = args.split(",");
	            		   object = symbols.get(params[0]);
	            		   if (object==null)
	            		   {
		            		   object = new ObjectProxy(network, params[0], Integer.parseInt(params[1]), params[2]);	            		   
	            		   }
	            		   argument = params[0];
	            	   }
	            	   else
	            		   throw new Exception("Parameter "+argument+" is not Proxyable");
	            	   symbols.put(argument, object);
	               }
            	   if (classType.isAssignableFrom(object.getClass()))
            	   {
 	                  arguments[index] = object;
            	   }
            	   else
 	                  throw new Exception("Parameter "+argument+" is not of "+classType);
	            }
	            index++;
	         }
	         return arguments;

	      // Exception handlers
	      } catch (IndexOutOfBoundsException e) {
	         throw new Exception("Exceeding argument: "+argument);
	      }
	   }

   	public void run() {
		CommandProtocol cmd;
		try {
			cmd = new CommandProtocol(0, 0, (byte)0, "");
			while ((cmd.getAction()!=CommandProtocol.ACTION_STOP_SIMULATION) && (started))
			{
				cmd = in.take();
				String ret = cmd.run(network);
				if (ret!=null)
				{
					if (ret.startsWith(CommandProtocol.OK_PREFIX))
					{
						connection.sendOk(cmd.getCmdId(),ret.substring(CommandProtocol.OK_PREFIX.length()));
					}
					else
					{
						connection.sendError(cmd.getCmdId(),ret);
					}
				}
				else
				{
					if (cmd.getAction()==CommandProtocol.ACTION_PACKET_ARRIVAL)
					{
						connection.sendPacketOk(((CommandPacketArrival) cmd).getFromWorker(),cmd.getCmdId());
					}
					else
					{
						connection.sendOk(cmd.getCmdId());
					}
				}
			}
		} catch (InterruptedException e) {
		}
		connection.disconnect();
		commClient.disconnect();
		network.terminateSimulation();
   	}
}
