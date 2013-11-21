package simmcast.distribution;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import simmcast.network.Network;
import simmcast.node.Node;
import simmcast.script.ScriptParser;

public class CommandCreate extends CommandProtocol {

	private int addressId;
	private String label;
	private String className;
	private String[] arguments;

	public CommandCreate(int mCmdId, byte mAction, String mParameter)
	{
		super(0, mCmdId, mAction, mParameter);
		String[] sp = getSplittedParameters();
		addressId = Integer.parseInt(sp[0]);
		label = sp[1];
		className = sp[2];
		arguments = new String[sp.length-3];
		for (int i=3;i<sp.length; i++)
		{
			arguments[i-3] = sp[i];
		}
	}

	public CommandCreate(int addressId, String label, String className, String[] arguments)
	{
		super(0, CommandProtocol.getNextCmdId(), ACTION_CREATE, "");
		this.addressId = addressId;
		this.label = label;
		this.className = className;
		this.arguments = arguments;
		parameters = addressId + CommandProtocol.PARAMETER_SEPARATOR + label + CommandProtocol.PARAMETER_SEPARATOR + className + CommandProtocol.PARAMETER_SEPARATOR;
    	for (int i=0; i<arguments.length; i++)
    	{
    		parameters += arguments[i];
    		if (i<arguments.length-1)
    		{
    			parameters += CommandProtocol.PARAMETER_SEPARATOR;
    		}
    	}	
	}

	public int getAddress()
	{
		return addressId;
	}

	public String getLabel()
	{
		return label;
	}

	public String getClassName()
	{
		return className;
	}

	public String[] getArguments()
	{
		return arguments;
	}

	public String run(Network network)
	{
        return null;
	}
}
