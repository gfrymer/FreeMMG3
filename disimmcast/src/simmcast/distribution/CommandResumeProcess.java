package simmcast.distribution;

import simmcast.engine.ProcessInterface;
import simmcast.network.Network;

public class CommandResumeProcess extends CommandProtocol {

	private double newTime;
	private int networkId;

	public CommandResumeProcess(int mCmdId, byte mAction, String mParameter)
	{
		super(0, mCmdId, mAction, mParameter);
		String[] sp = getSplittedParameters();
		networkId = Integer.parseInt(sp[0]);
		newTime = Double.parseDouble(sp[1]);
	}

	public CommandResumeProcess(int mNetworkId, double mNewTime)
	{
		super(0, CommandProtocol.getNextCmdId(), ACTION_RESUME_PROCESS, "");
		networkId = mNetworkId;
		newTime = mNewTime;
		parameters = "" + mNetworkId + CommandProtocol.PARAMETER_SEPARATOR + mNewTime;
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
