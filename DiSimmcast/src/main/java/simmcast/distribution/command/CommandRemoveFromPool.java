package simmcast.distribution.command;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import simmcast.distribution.interfaces.ProcessInterface;
import simmcast.network.Network;

public class CommandRemoveFromPool extends CommandProtocol {

	public final static String NETWORK_ID = "netId";

	private int networkId;

	public CommandRemoveFromPool(int mWorkerId, int mCmdId, byte mAction, String mParameter)
	{
		super(mWorkerId, mCmdId, mAction, mParameter);
		JsonObject jo = getJsonParameters();
		if (jo!=null)
		{
			networkId = jo.get(NETWORK_ID).getAsInt();
		}
	}

	public CommandRemoveFromPool(int mNetworkId)
	{
		super(0, CommandProtocol.getNextCmdId(), ACTION_REMOVE_FROM_POOL, "");
		JsonObject gson = new JsonObject();
		networkId = mNetworkId;
		gson.addProperty(NETWORK_ID, networkId);
		parameters = gson.toString();
	}

	public String run(Network network)
	{
		ProcessInterface pi = network.getSimulationScheduler().getFromThreadPool(networkId);
		if (pi!=null)
		{
			network.getSimulationScheduler().removeFromThreadPool(pi);
			return OK_PREFIX;
		}
		else
		{
			return "No such process to remove: " + networkId;
		}
	}
}
