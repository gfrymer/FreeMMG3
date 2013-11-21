package simmcast.distribution;

import java.lang.reflect.Method;

import simmcast.group.GroupInterface;
import simmcast.group.GroupTableInterface;
import simmcast.network.Network;
import simmcast.script.ScriptParser;

public class CommandInvoke extends CommandProtocol {

	private int networkId;
	private String function;
	private String[] arguments;

	public CommandInvoke(int mCmdId, byte mAction, String mParameter)
	{
		super(0, mCmdId, mAction, mParameter);
		String[] sp = getSplittedParameters();
		networkId = Integer.parseInt(sp[0]);
		function = sp[1];
		arguments = new String[sp.length-2];
		for (int i=2;i<sp.length; i++)
		{
			arguments[i-2] = sp[i];
		}
	}

	public CommandInvoke(int mNetworkId, String mFunction, String[] mParameters)
	{
		super(0, CommandProtocol.getNextCmdId(), ACTION_INVOKE, "");
		this.networkId = mNetworkId;
		this.function = mFunction;
		this.arguments = mParameters;
		parameters = networkId + CommandProtocol.PARAMETER_SEPARATOR + function + CommandProtocol.PARAMETER_SEPARATOR;
    	for (int i=0; i<arguments.length; i++)
    	{
    		parameters += arguments[i];
    		if (i<arguments.length-1)
    		{
    			parameters += CommandProtocol.PARAMETER_SEPARATOR;
    		}
    	}	
	}

	public int getNetworkId()
	{
		return networkId;
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
		if (Network.isMulticast(getNetworkId()))
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
