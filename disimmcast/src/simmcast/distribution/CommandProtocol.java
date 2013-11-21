package simmcast.distribution;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import simmcast.network.Network;
import simmcast.script.InvalidFileException;
import simmcast.script.ScriptParser;

public class CommandProtocol {

	public static final byte ACTION_CREATE = 1;
	public static final byte ACTION_INVOKE = 2;
	public static final byte ACTION_START_SIMULATION = 3;
	public static final byte ACTION_STOP_SIMULATION = 4;
	public static final byte ACTION_ADD_TO_POOL = 5;
	public static final byte ACTION_ACTIVATE_AT = 6;
	public static final byte ACTION_RESUME_PROCESS = 7;
	public static final byte ACTION_BLOCKED_FINISHED = 8;

	public static final byte ACTION_OK = 100;
	public static final byte ACTION_ERROR = 101;

	public static final String[] ACTIONS_STRINGS = {"CREATE","INVOKE","START_SIMULATION","STOP_SIMULATION","ADD_TO_POOL","ACTIVATE_AT","RESUME_PROCESS","BLOCKED_FINISHED"};
	public static final Class[] ACTIONS_CLASSES = {CommandCreate.class,CommandInvoke.class,CommandStartSimulation.class,CommandStopSimulation.class,CommandAddToPool.class,CommandActivateAt.class,CommandResumeProcess.class,CommandBlockedOrFinished.class};
	public static final int PARAMETER_SIZE = 32;
	public static final String PARAMETER_SEPARATOR = ";"; 

	public static final Class[] CONSTRUCTOR_FULL = {int.class,int.class,byte.class,String.class};
	public static final Class[] CONSTRUCTOR_SIMPLE = {int.class,byte.class,String.class};

	public static final String OK_PREFIX = "OK_";

	protected int clientId;
	protected int cmdId;
	protected byte action;
	protected String parameters;

	private static int nextCmdId = 1;

	public static int getNextCmdId()
	{
		return nextCmdId++;
	}

	public CommandProtocol(int mClientId, int mCmdId, byte mAction, String mParameter)
	{
		clientId = mClientId;
		cmdId = mCmdId;
		action = mAction;
		parameters = mParameter;
	}

	public int getClientId()
	{
		return clientId;
	}

	public int getCmdId()
	{
		return cmdId;
	}

	public byte getAction()
	{
		return action;
	}

	public String getParameters()
	{
		return parameters;
	}

	public String[] getSplittedParameters()
	{
		return parameters.split(";");
	}

	public String toString()
	{
		return ACTIONS_STRINGS[action - 1] + " - " + parameters;
	}

	public static CommandProtocol createFromAction(int clientid, int cmdid, byte action, String parameters)
	{
		CommandProtocol cp = null;

		if (action<=ACTIONS_CLASSES.length)
		{
				Class classType = ACTIONS_CLASSES[action-1];
		        Object[] args;
		        Constructor constructor = null;
		        try 
		        {
		        	constructor = ScriptParser.findConstructor(classType,CONSTRUCTOR_FULL);
				} catch (InvalidFileException e) {
				}
		        if (constructor!=null)
		        {
			        args = new Object[4];
		        	args[0] = clientid;
		        	args[1] = cmdid;
		        	args[2] = action;
		        	args[3] = parameters;
		        }
		        else
		        {
			        try 
			        {
			        	constructor = ScriptParser.findConstructor(classType,CONSTRUCTOR_SIMPLE);
					} catch (InvalidFileException e) {
						e.printStackTrace();
						return null;
					}
			        args = new Object[3];
		        	args[0] = cmdid;
		        	args[1] = action;
		        	args[2] = parameters;
		        }
			try {
		        cp = (CommandProtocol) constructor.newInstance(args);
		        return cp;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		if ((action==ACTION_OK) || (action==ACTION_ERROR))
		{
			return new CommandProtocol(clientid, cmdid, action, parameters);
		}
		return null;
	}

	public String run(Network network)
	{
		return null;
	}
}
