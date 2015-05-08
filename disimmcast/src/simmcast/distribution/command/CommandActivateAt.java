package simmcast.distribution.command;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import simmcast.network.Network;

public class CommandActivateAt extends CommandProtocol {

	public final static String PID = "pid";
	public final static String TIME = "time";

	private double time;
	private int pid;

	public CommandActivateAt(int mWorkerId, int mCmdId, byte mAction, String mParameter)
	{
		super(mWorkerId, mCmdId, mAction, mParameter);
		JsonObject jo = getJsonParameters();
		if (jo!=null)
		{
			time = jo.get(TIME).getAsDouble();
			pid = jo.get(PID).getAsInt();
		}
	}

	public CommandActivateAt(double time, int pid)
	{
		super(0, CommandProtocol.getNextCmdId(), ACTION_ACTIVATE_AT, "");
		this.time = time;
		this.pid = pid;
/*		JsonObject gson = new JsonObject();
		gson.addProperty(TIME, time);
		gson.addProperty(PID, pid);*/
		parameters = "{time:" + time + ",pid:" + pid + "}";//gson.toString();
	}

	public double getTime()
	{
		return time;
	}

	public int getPid()
	{
		return pid;
	}

	public String run(Network network)
	{
		network.getSimulationScheduler().activateAt(getTime(), network.getSimulationScheduler().getFromThreadPool(getPid()));
		return null;
	}
}
