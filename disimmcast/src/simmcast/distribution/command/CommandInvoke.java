package simmcast.distribution.command;

import java.lang.reflect.Method;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import simmcast.distribution.interfaces.GroupInterface;
import simmcast.distribution.interfaces.GroupTableInterface;
import simmcast.network.Network;
import simmcast.script.ScriptParser;

public class CommandInvoke extends CommandProtocol {

	public final static String NETWORK_ID = "netId";
	public final static String NAME = "name";
	public final static String FUNCTION = "fnct";
	public final static String ARGUMENTS = "args";

	private int networkId;
	private String name;
	private String function;
	private String[] arguments;

	public CommandInvoke(int mWorkerId, int mCmdId, byte mAction, String mParameter)
	{
		super(mWorkerId, mCmdId, mAction, mParameter);
		init(mCmdId, mAction, mParameter);
	}

	public CommandInvoke(int mCmdId, byte mAction, String mParameter)
	{
		super(0, mCmdId, mAction, mParameter);
		init(mCmdId, mAction, mParameter);
	}

	private void init(int mCmdId, byte mAction, String mParameter)
	{
		JsonObject jo = getJsonParameters();
		if (jo!=null)
		{
			if (jo.get(NETWORK_ID)!=null)
			{
				networkId = jo.get(NETWORK_ID).getAsInt();
				name = null;
			}
			else
			{
				name = jo.get(NAME).getAsString();
				networkId = -1;
			}
			function = jo.get(FUNCTION).getAsString();
			JsonArray jsonArgs = jo.get(ARGUMENTS).getAsJsonArray();
			arguments = new String[jsonArgs.size()];
			for (int i=0;i<jsonArgs.size(); i++)
			{
				arguments[i] = jsonArgs.get(i).getAsString();
			}
		}
	}

	public CommandInvoke(String mName, String mFunction, String[] mParameters)
	{
		super(0, CommandProtocol.getNextCmdId(), ACTION_INVOKE, "");
		this.networkId = -1;
		this.name = mName;
		this.function = mFunction;
		this.arguments = mParameters;
		JsonObject gson = new JsonObject();
		gson.addProperty(NAME, mName);
		gson.addProperty(FUNCTION, function);
		JsonArray args = new JsonArray();
    	for (int i=0; i<arguments.length; i++)
    	{
    		args.add(new JsonPrimitive(arguments[i]));
    	}
		gson.add(ARGUMENTS, args);
		parameters = gson.toString();
	}

	public CommandInvoke(int mNetworkId, String mFunction, String[] mParameters)
	{
		super(0, CommandProtocol.getNextCmdId(), ACTION_INVOKE, "");
		this.networkId = mNetworkId;
		this.name = null;
		this.function = mFunction;
		this.arguments = mParameters;
		JsonObject gson = new JsonObject();
		gson.addProperty(NETWORK_ID, networkId);
		gson.addProperty(FUNCTION, function);
		JsonArray args = new JsonArray();
    	for (int i=0; i<arguments.length; i++)
    	{
    		args.add(new JsonPrimitive(arguments[i]));
    	}
		gson.add(ARGUMENTS, args);
		parameters = gson.toString();
	}

	public int getNetworkId()
	{
		return networkId;
	}

	public String getName()
	{
		return name;
	}

	public String getFunction()
	{
		return function;
	}

	public String[] getArguments()
	{
		return arguments;
	}

	public String run(Network network)
	{
		Object obj = null;
		String fnctn = null;
		boolean isGroupTable = false;
		if ((Network.isMulticast(getNetworkId())) && (name==null))
		{
			isGroupTable = getFunction().startsWith(GroupTableInterface.GP_FNCTN_PREFIX);
			if (isGroupTable)
			{
				fnctn = getFunction().substring(GroupTableInterface.GP_FNCTN_PREFIX.length());
				obj = network.getGroups();
			}
			else
			{
				fnctn = getFunction();
				obj = network.getGroups().getGroupById(getNetworkId());
			}
		}
		else
		{
			fnctn = getFunction();
		}
		if (name!=null)
		{
			if (name.equals("network"))
			{
				obj = network;
			}
		}
		Method method;
		String err = null;
		try {
			method = ScriptParser.findMethod(obj, fnctn, getArguments().length);
			Object[] arguments = generateArguments(method.getParameterTypes(), getArguments());
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
				GroupInterface gi = network.getGroups().getGroupById(getNetworkId());
				ret = gi.getNetworkId() + "," + gi.getName();
				for (int j=0;j<gi.getNetworkIds().length;j++)
				{
					ret = ret + "," + gi.getNetworkIds()[j];
				}
			}
			return CommandProtocol.OK_PREFIX + ret.toString();
		} catch (Exception e) {
			return e.toString();
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
