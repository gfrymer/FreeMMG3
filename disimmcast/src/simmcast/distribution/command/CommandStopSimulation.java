package simmcast.distribution.command;

import simmcast.network.Network;

public class CommandStopSimulation extends CommandProtocol {

	public CommandStopSimulation(int mClientId, int mCmdId, byte mAction, String mParameter)
	{
		super(mClientId, mCmdId, mAction, mParameter);
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
