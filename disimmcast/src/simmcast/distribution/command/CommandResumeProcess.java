package simmcast.distribution.command;

import com.google.gson.JsonObject;

import simmcast.distribution.interfaces.ProcessInterface;
import simmcast.network.Network;

public class CommandResumeProcess extends CommandProtocol {

	public final static String NEW_TIME = "ntime";
	public final static String NETWORK_ID = "netId";

	private double newTime;
	private int networkId;

	public CommandResumeProcess(int mCmdId, byte mAction, String mParameter)
	{
		super(0, mCmdId, mAction, mParameter);
		JsonObject jo = getJsonParameters();
		if (jo!=null)
		{
			networkId = jo.get(NETWORK_ID).getAsInt();
			newTime = jo.get(NEW_TIME).getAsDouble();
		}
	}

	public CommandResumeProcess(int mNetworkId, double mNewTime)
	{
		super(0, CommandProtocol.getNextCmdId(), ACTION_RESUME_PROCESS, "");
		networkId = mNetworkId;
		newTime = mNewTime;
		JsonObject gson = new JsonObject();
		gson.addProperty(NETWORK_ID, networkId);
		gson.addProperty(NEW_TIME, newTime);
		parameters = gson.toString();
	}

	public int getNetworkId()
	{
		return networkId;
	}

	public double getNewTime()
	{
		return newTime;
	}

	public String run(Network network)
	{
		network.getSimulationScheduler().setTime(getNewTime());
		ProcessInterface p = network.getSimulationScheduler().resumeProcess(getNetworkId());
		if (p!=null)
		{
			return null;
		}
		else
		{
			return "No such process: " + getNetworkId();
		}
	}
}
