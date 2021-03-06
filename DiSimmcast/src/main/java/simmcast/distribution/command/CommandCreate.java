package simmcast.distribution.command;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import simmcast.network.Network;
import simmcast.node.Node;
import simmcast.script.ScriptParser;

public class CommandCreate extends CommandProtocol {

	public final static String ADDRESS_ID = "addrId";
	public final static String LABEL = "label";
	public final static String CLASS_NAME = "class";
	public final static String ARGUMENTS = "args";

	private int addressId;
	private String label;
	private String className;
	private String[] arguments;

	public CommandCreate(int mCmdId, byte mAction, String mParameter)
	{
		super(0, mCmdId, mAction, mParameter);
		JsonObject jo = getJsonParameters();
		if (jo!=null)
		{
			addressId = jo.get(ADDRESS_ID).getAsInt();
			label = jo.get(LABEL).getAsString();
			className = jo.get(CLASS_NAME).getAsString();
			JsonArray jsonArgs = jo.get(ARGUMENTS).getAsJsonArray();
			arguments = new String[jsonArgs.size()];
			for (int i=0;i<jsonArgs.size(); i++)
			{
				arguments[i] = jsonArgs.get(i).getAsString();
			}
		}
	}

	public CommandCreate(int addressId, String label, String className, String[] arguments)
	{
		super(0, CommandProtocol.getNextCmdId(), ACTION_CREATE, "");
		this.addressId = addressId;
		this.label = label;
		this.className = className;
		this.arguments = arguments;
		JsonObject gson = new JsonObject();
		gson.addProperty(ADDRESS_ID, addressId);
		gson.addProperty(LABEL, label);
		gson.addProperty(CLASS_NAME, className);
		JsonArray args = new JsonArray();
    	for (int i=0; i<arguments.length; i++)
    	{
    		args.add(new JsonPrimitive(arguments[i]));
    	}
		gson.add(ARGUMENTS, args);
		parameters = gson.toString();
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
