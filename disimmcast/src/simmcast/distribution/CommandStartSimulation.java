package simmcast.distribution;

public class CommandStartSimulation extends CommandProtocol {

	public CommandStartSimulation(int mCmdId, byte mAction, String mParameter)
	{
		super(0, mCmdId, mAction, mParameter);
	}

	public CommandStartSimulation()
	{
		super(0, CommandProtocol.getNextCmdId(), ACTION_START_SIMULATION, "");
	}

	public boolean run()
	{
		return false;
	}
}
