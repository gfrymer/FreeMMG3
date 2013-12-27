package simmcast.distribution.command;

import com.google.gson.JsonObject;

import simmcast.network.Network;

public class CommandBlockedOrFinished extends CommandProtocol {

	public final static String NETWORK_ID = "netId";

	private int networkId;

	public CommandBlockedOrFinished(int mClientId, int mCmdId, byte mAction, String mParameter)
	{
		super(mClientId, mCmdId, mAction, mParameter);
		JsonObject jo = getJsonParameters();
		if (jo!=null)
		{
			networkId = jo.get(NETWORK_ID).getAsInt();
		}
	}

	public CommandBlockedOrFinished(int mNetworkId)
	{
		super(0, CommandProtocol.getNextCmdId(), ACTION_BLOCKED_FINISHED, "");
		networkId = mNetworkId;
		JsonObject gson = new JsonObject();
		gson.addProperty(NETWORK_ID, networkId);
		parameters = gson.toString();
	}

	public int getNetworkId()
	{
		return networkId;
	}

	public String run(Network network)
	{
		network.getSimulationScheduler().processBlockedOrFinished(getNetworkId());
		return null;
	}
}
