package simmcast.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Random;

import simmcast.distribution.Worker;
import simmcast.distribution.Manager;
import simmcast.distribution.interfaces.GroupInterface;
import simmcast.distribution.interfaces.GroupTableInterface;
import simmcast.distribution.interfaces.NodeInterface;
import simmcast.distribution.interfaces.SchedulerInterface;
import simmcast.distribution.proxies.GroupTableProxy;
import simmcast.distribution.proxies.NodeProxy;
import simmcast.distribution.proxies.ProcessProxy;
import simmcast.engine.TerminatedException;
import simmcast.group.Group;
import simmcast.group.GroupTable;
import simmcast.node.Node;
import simmcast.node.NodeVector;
import simmcast.script.InvalidFileException;
import simmcast.script.ScriptParser;
import simmcast.trace.NullTraceGenerator;
import simmcast.trace.TraceGenerator;

// This the Network. It controls the simulation. We want it to work.

/**
 * An object of this class controls the entire simulation. There
 * must be a single instance of this class, for it controls, among
 * other things, the address space and the lifetime of the threads.
 *
 * @author Hisham H. Muhammad
 */
public class Network extends simmcast.engine.Process {

   // *****************************************************
   // CONSTANTS
   // *****************************************************

   /**
    * Addresses ranging from MULTICAST_ADDRESS are
    * processed by this class using information from
    * the GroupVector.
    */
   private static final int MULTICAST_ADDRESS = -1;

   // The queue's internals are never used. It could have
   // been any subclass of AbstractQueue; I just used
   // PathAccountQueue so I wouldn't have to create yet
   // another subclass.

   /**
    * A generic queue representing some abstract
    * "upper layer" to be reported in the trace files.
    */
   public static final PathAccountQueue UPPER_LAYER = new PathAccountQueue("Up");

   // *****************************************************
   // ATTRIBUTES
   // *****************************************************

   /**
    * Contains true during the simulation run.
    */
   private boolean running = false;

   /**
    * A counter to control the simulation end.
    */
   private int threadCounter = 0;

   /**
    * A generic random number generator for the simulation.
    */
   // TODO: should a Draw object be used instead?
   public Random randomGenerator;

   /**
    * The main repository of nodes for the simulation.
    */
   public NodeVector nodes;

   /**
    * The main repository of multicast groups for the simulation.
    */
   private GroupTableInterface groups;

   /**
    * A handle to the tracer used in the simulation.
    */
   public TraceGenerator tracer;

   /**
    * Controller of the unicast address space.
    */
   protected int nextUnicastAddress;

   /**
    * Controller of the multicast address space.
    */
   protected int nextMulticastAddress;

   /**
    * Command line arguments.
    */
   protected String[] arguments;

   private boolean isManager;
   private Manager manager;
   private Worker worker;
   
   protected HashMap<String, Object> cacheable_fields;

   /**
    * TODO
    */
   Object mtx = new Object();

   // *****************************************************
   // CONSTRUCTORS
   // *****************************************************

   /**
    * Constructs a network object, which is responsible
    * for the entire simulation run. There must be only
    * one object of this class per simulation.
    */
   public Network(boolean _isManager, String host) {
      setName("["+getClass()+"]");
      isManager = _isManager;
      tracer = new NullTraceGenerator();
      if (isManager)
      {
		manager = new Manager(this,host);
		manager.listenForConnections();
		System.out.println("Network on server mode and listening for client connections");
		System.out.println("Press ENTER to start simulation");
	    BufferedReader reader = new BufferedReader(new InputStreamReader(
	            System.in));
	    try {
			reader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    manager.stopListening();
	    setScheduler(new simmcast.engine.Scheduler());
	    nextUnicastAddress = 1;
	    nextMulticastAddress = MULTICAST_ADDRESS;
      }
      else
      {
  		System.out.println("Network on client mode and connecting to server on: " + host);
		worker = new Worker(this);
		if (worker.connect(host))
		{
	  		System.out.println("Client connected succesfully");
		}
	    setScheduler(new simmcast.distribution.proxies.SchedulerProxy(this));
      }
   }

   // *****************************************************
   // INITIALIZATION
   // *****************************************************
 
   public void initializeNode(Node node_, String label_) {
      node_.initialize(simulationScheduler, this, label_);
   }
 
   // *****************************************************
   // THREAD LIFETIME
   // *****************************************************

   /**
    * Returns the server object
    */
   public Manager getServer()
   {
	   return manager;
   }

   /**
    * Returns the network simulationScheduler
    */
   public SchedulerInterface getSimulationScheduler()
   {
	   return simulationScheduler;
   }

   /**
    * Returns the client object
    */
   public Worker getClient()
   {
	   return worker;
   }

   /**
    * Perform the simulation based on parameters from the
    * specified input file. This launches the network thread,
    * keeping the main thread on hold.
    *
    * @param inputFile_ The filename of the configuration file.
    */
   public synchronized void runSimulation(String inputFile_) {
       randomGenerator = new Random(System.currentTimeMillis());
	   if (isManager)
	   {
	      try {
	         ScriptParser parser = new ScriptParser(this, inputFile_, arguments);
	         groups = parser.getGroups();
	      } catch (InvalidFileException e) {
	         System.err.println("Error processing configuration file:");
	         System.err.println(inputFile_+": "+e.lineNumber+": "+e);
	         System.exit(1);
	      }
	   }
	   else
	   {
		   try {
			   worker.createNodesAndObjects();
			   groups = new GroupTableProxy(this);
		   } catch (Exception e) {
			   e.printStackTrace();
		   }
	   }
      running = true;
      tracer.setNetwork(this);

      if (isManager)
      {
    	  /* THIS IS TO SET NETWORK PROCESS REAL PID */
    	  ProcessProxy temp = new ProcessProxy(this, -1); 
    	  setPid(temp.getPid());
    	  temp = null;
    	  /* --------------------------------------- */
    	  startProcess();
	      manager.startSimulation();
      }
      else
      {
          //--------------------------------------------------------
          // Starting <b>all</b> threads.
          // (Including this network thread)
          for (int i=0; i<nodes.size(); i++)
          {
	         System.out.println("Begin " + nodes.nodeAt(i).getName());
             nodes.nodeAt(i).begin();
          }

          start();
          worker.startSimulation();
      }
      //--------------------------------------------------------

      simulationScheduler.start();

      // Holding execution of the <b> main</b> thread:
      try {
         wait();
         join();
      } catch (Exception e) { System.out.println("Error at join: "+e); }

      simulationScheduler.interrupt();
      if (isManager)
      {
    	  //manager.stopSimulation();
      }
      else
      {
	      for (int i=0; i<nodes.size(); i++)
		         nodes.nodeAt(i).end();
      }
      tracer.finish();
   }

   /**
    * Performs the actual execution of the simulation.
    */
   public void runProcess() {
      try {
         sleepProcess();
      } catch (TerminatedException e) {
      }
   }

   public void run() {
	   if (isManager)
	   {
		   super.run();
	   }
	   else
	   {
		   synchronized (mtx) {
			   try {
				   mtx.wait();
			   } catch (InterruptedException e) {
			   }
		   }
	   }
   }
   /**
    * Instructs the process to exit the simulation loop.
    * This can be forcibly called, but it is usually called
    * as the internal thread count is decremented in such
    * a way that the simulation can finish.
    */
   public void terminateSimulation() {
      if (running == true) {
         running = false;
      }
      else
      {
    	  return;
      }
      if (isManager)
      {
    	  manager.stopSimulation();
      }
      else
      {
    	  worker.stopSimulation();
      }
	  interrupt();
   }

   // *****************************************************
   // CONTROL OF OTHER THREADS
   // *****************************************************

   /**
    * Increments the thread counter, in order to control
    * internally the simulation end. This function must not
    * be called by the user's code.
    */
   public void incrementThreadCounter() {
      threadCounter++;
   }

   /**
    * Decrements the thread counter, in order to control
    * internally the simulation end. This function must not
    * be called by the user's code.
    */
   public void decrementThreadCounter() {
      threadCounter--;
      if (threadCounter == 0)
         terminateSimulation();
   }

   // *****************************************************
   // ADDRESS SPACE MANAGEMENT
   // *****************************************************

   /**
    * Returns true if the identifier refers to a multicast address.
    * This method must be the only way used by a simulation to
    * infer whether a network id is from a node or a group.
    * No other assumption should be made, as the address space
    * rules can be changed/improved in the future.
    *
    * @param id_ A network identifier, which may be from a node or
    * from a group.
    */
   static public boolean isMulticast(int id_) {
      return (id_ <= MULTICAST_ADDRESS);
   }

   /**
    * Returns the next free unicast address in the network's
    * address space, marking it as allocated.
    * This is usually called during processing of the configuration
    * file, as nodes are created. The client code shouldn't need
    * to use this normally.
    *
    * @return A new network id within the unicast range.
    */
   public int obtainUnicastAddress() {
      int result = nextUnicastAddress;
      nextUnicastAddress++;
      return result;
   }

   /**
    * Returns the next free multicast address in the network's
    * address space, marking it as allocated.
    * This is usually called during processing of the configuration
    * file, as groups are created. The client code shouldn't need
    * to use this normally.
    *
    * @return A new network id within the multicast range.
    */
   public int obtainMulticastAddress() {
      int result = nextMulticastAddress;
      nextMulticastAddress--;
      return result;
   }

   // *****************************************************
   // UTILITIES
   // *****************************************************

   /**
    * Configure the global random seed of the simulation.
    * This is used, for example, by all path objects for the draws to
    * determine packet losses.
    * As this seed is entered, the random generator is reset.
    * This method is meant to be called from the configuration file.
    *
    * @param seed_ The new seed.
    */
   public void setSeed(long seed_) {
      randomGenerator = new Random(seed_);   
   }

   /**
    * Pass "arguments" to the configuration file.
    * The list of String arguments passed to this method is
    * available to the configuration file, both as individual
    * String objects (named "arg1", "arg2", and so on) and as
    * Simmcast macros (named "!ARG1", "!ARG2", and so on).
    *
    * @param arguments_ A list of String arguments. This is usually
    * the array obtained as the parameter of the "main" function,
    * allowing the configuration file to be parameterized from the
    * command line.
    */
   public void setArguments(String arguments_[]) {
      arguments = arguments_;
   }

   /**
    * This method returns the current simulation time,
    * in simulated time units.
    *
    * @return The current simulation time.
    */
   public double simulationTime() {
      return simulationScheduler.currentTime();
   }

   /**
    * Defines a trace generator to be used from now on.
    * The previous tracer is finished. There is always a
    * trace generator connected to a Network object, to
    * allow direct calls to methods such as tracer.message().
    * When a tracer is not used (or not explicitly defined),
    * the NullTraceGenerator, which basically ignores all
    * trace commands, is used.
    *
    * @param tracer_ A new trace generator. This must never
    * be null (to use a dummy trace generator, use
    * NullTraceGenerator; however, you may consider using
    * the tracer's disable() method instead).
    *
    * @see simmcast.trace.TraceGenerator
    * @see simmcast.trace.NullTraceGenerator
    */
   public void setTracer(TraceGenerator tracer_) {
      // assert (tracer_ != null);
      tracer.finish();
      tracer = tracer_;
   }

   /**
    * Obtain a node handle by giving its network id.
    * If no such node is found, null is returned.
    * No checks are made about the validity of the
    * input data: if an invalid id is sent, null is
    * returned as well.
    *
    * @param id_ The node's network identifier.
    *
    * @return A reference to the node object corresponding
    * to the given id, or null.
    */
   public NodeInterface getNodeById(int id_) {
      return nodes.getNodeById(id_);
   }

   /**
    * Obtain the name of a node or group, given its id.
    * No checks are made about the validity of the
    * input data: if an invalid id is sent, null is
    * returned as well.
    *
    * @param id_ The node or group network identifier.
    *
    * @return The name of the node or group, or null.
    */
   public String getNameById(int id_) {
      if (isMulticast(id_)) {
         GroupInterface g = groups.getGroupById(id_);
         if (g != null) {
            return g.getName();
         }
      } else {
    	 NodeInterface n = nodes.getNodeById(id_);
         if (n != null) {
            return n.getName();
         }
      }
      return null;
   }

   /**
    * Inform how many nodes exist in this network.
    * Nodes belong to the network only when they are created in
    * the configuration file. Therefore, it is not correct to
    * assume that this is the same as the number of Node objects
    * created.
    *
    * @return The amount of nodes declared in the simulation.
    */
   public int getNodeCount() {
      return nodes.size();
   }

   /**
    * Returns an array listing the members of a given
    * multicast group. If no such group is found, null is returned.
    * No checks are made about the validity of the
    * input data: if an invalid id is sent, null is
    * returned as well.
    *
    * @param id_ The network id of the multicast group.
    *
    * @return An array listing the network ids of all members
    * of the specified multicast group. The returned array is
    * a copy: changes to it won't affect the multicast group in
    * question.
    */
   public int[] getGroupMembersById(int id_) {
      // assert (isMulticast(id_));
      return groups.getMembersById(id_);
   }

   public GroupTableInterface getGroups() {
	   return groups;
   }

   public Object getAsClass(String fieldName, Class classType)
   {
	   Object g = get(fieldName);
	   if (classType.isAssignableFrom(g.getClass()))
	   {
		   return g;
	   }
	   if (g instanceof String)
	   {
		   String[] args = ((String) g).split(",");
		   try {
			   Constructor c = ScriptParser.findConstructor(classType, args.length);
			   Object[] parsedArgs = new Object[args.length];
			   for (int i=0;i<args.length;i++)
			   {
				   if (c.getParameterTypes()[i].equals(Byte.class))
				   {
					   parsedArgs[i] = new Byte(args[i]);
				   }
				   if (c.getParameterTypes()[i].equals(Character.class))
				   {
					   parsedArgs[i] = new Character(args[i].charAt(0));
				   }
				   if (c.getParameterTypes()[i].equals(Long.class))
				   {
					   parsedArgs[i] = new Long(args[i]);
				   }
				   if (c.getParameterTypes()[i].equals(Integer.class))
				   {
					   parsedArgs[i] = new Integer(args[i]);
				   }
				   if (c.getParameterTypes()[i].equals(Short.class))
				   {
					   parsedArgs[i] = new Short(args[i]);
				   }
				   if (c.getParameterTypes()[i].equals(Double.class))
				   {
					   parsedArgs[i] = new Double(args[i]);
				   }
				   if (c.getParameterTypes()[i].equals(Float.class))
				   {
					   parsedArgs[i] = new Float(args[i]);
				   }
				   if (c.getParameterTypes()[i].equals(Boolean.class))
				   {
					   parsedArgs[i] = new Boolean(args[i]);
				   }
				   if (c.getParameterTypes()[i].equals(String.class))
				   {
					   parsedArgs[i] = args[i];
				   }
			   }
			   return c.newInstance(parsedArgs);
		   } catch (InvalidFileException e) {
			   e.printStackTrace();
		   } catch (IllegalArgumentException e) {
			   e.printStackTrace();
		   } catch (InstantiationException e) {
			   e.printStackTrace();
		   } catch (IllegalAccessException e) {
			   e.printStackTrace();
		   } catch (InvocationTargetException e) {
			e.printStackTrace();
		   }
		   return null;
	   }
	   return g;
   }

   public int getAsInt(String fieldName)
   {
	   Object g = get(fieldName);
	   if (g instanceof Integer)
	   {
		   return ((Integer) g).intValue();
	   }
	   if (g instanceof String)
	   {
		   return new Integer((String) g).intValue();
	   }
	   return Integer.MIN_VALUE;
   }

   public double getAsDouble(String fieldName)
   {
	   Object g = get(fieldName);
	   if (g instanceof Double)
	   {
		   return ((Double) g).doubleValue();
	   }
	   if (g instanceof String)
	   {
		   return new Double((String) g).doubleValue();
	   }
	   return Double.MIN_VALUE;
   }

   public char getAsChar(String fieldName)
   {
	   Object g = get(fieldName);
	   if (g instanceof Character)
	   {
		   return ((Character) g).charValue();
	   }
	   if (g instanceof String)
	   {
		   return ((String) g).charAt(0);
	   }
	   return 0;
   }

   public byte getAsByte(String fieldName)
   {
	   Object g = get(fieldName);
	   if (g instanceof Byte)
	   {
		   return ((Byte) g).byteValue();
	   }
	   if (g instanceof String)
	   {
		   return new Byte((String) g).byteValue();
	   }
	   return 0;
   }

   public float getAsFloat(String fieldName)
   {
	   Object g = get(fieldName);
	   if (g instanceof Float)
	   {
		   return ((Float) g).floatValue();
	   }
	   if (g instanceof String)
	   {
		   return new Float((String) g).floatValue();
	   }
	   return Float.MIN_VALUE;
   }

   public boolean getAsBoolean(String fieldName)
   {
	   Object g = get(fieldName);
	   if (g instanceof Boolean)
	   {
		   return ((Boolean) g).booleanValue();
	   }
	   if (g instanceof String)
	   {
		   return new Boolean((String) g).booleanValue();
	   }
	   return Boolean.FALSE;   
   }

   public short getAsShort(String fieldName)
   {
	   Object g = get(fieldName);
	   if (g instanceof Short)
	   {
		   return ((Short) g).shortValue();
	   }
	   if (g instanceof String)
	   {
		   return new Short((String) g).shortValue();
	   }
	   return Short.MIN_VALUE;	   
   }

   public long getAsLong(String fieldName)
   {
	   Object g = get(fieldName);
	   if (g instanceof Long)
	   {
		   return ((Long) g).longValue();
	   }
	   if (g instanceof String)
	   {
		   return new Long((String) g).longValue();
	   }
	   return Long.MIN_VALUE;	   
   }

   public Object get(String fieldName)
   {
	   try {
			Field field = getClass().getDeclaredField(fieldName);
			Class returnType = field.getClass();
			if (isManager)
			{
				field.setAccessible(true);
				return field.get(this);
			}
			else
			{
				boolean hasCacheField = false;
				if (cacheable_fields!=null)
				{
					if (cacheable_fields.containsKey(fieldName))
					{
						hasCacheField = true;
						Object o = cacheable_fields.get(fieldName);
						if (o!=null)
						{
							return o;
						}
					}
				}
				String[] arguments = new String[1];
				arguments[0] = fieldName;
				String retdata = worker.invokeCommand("network", "get", arguments);
				if (hasCacheField)
				{
					cacheable_fields.put(fieldName, retdata);
				}
				return retdata;
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   return null;
   }

};
