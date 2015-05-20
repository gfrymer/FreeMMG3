package simmcast.distribution.command;

import simmcast.network.Network;

public class CommandStopSimulation extends CommandProtocol {

	public CommandStopSimulation(int mWorkerId, int mCmdId, byte mAction, String mParameter)
	{
		super(mWorkerId, mCmdId, mAction, mParameter);
	}

	public CommandStopSimulation()
	{
		super(0, CommandProtocol.getNextCmdId(), ACTION_STOP_SIMULATION, "");
	}

	public String run(Network network)
	{
		network.terminateSimulation();
		return null;
	}
}
