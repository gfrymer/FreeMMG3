package simmcast.distribution.command;

import com.google.gson.JsonObject;

import simmcast.distribution.interfaces.ProcessInterface;
import simmcast.network.Network;

public class CommandTerminateProcess extends CommandProtocol {

	public final static String NETWORK_ID = "netId";

	private int networkId;

	public CommandTerminateProcess(int mCmdId, byte mAction, String mParameter)
	{
		super(0, mCmdId, mAction, mParameter);
		JsonObject jo = getJsonParameters();
		if (jo!=null)
		{
			networkId = jo.get(NETWORK_ID).getAsInt();
		}
	}

	public CommandTerminateProcess(int mNetworkId)
	{
		super(0, CommandProtocol.getNextCmdId(), ACTION_TERMINATE_PROCESS, "");
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
		ProcessInterface p = network.getSimulationScheduler().getFromThreadPool(networkId);
		if (p!=null)
		{
			p.interrupt();
			return null;
		}
		else
		{
			return "No such process: " + getNetworkId();
		}
	}
}
