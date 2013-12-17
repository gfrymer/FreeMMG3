package simmcast.distribution.command;

public class CommandStopSimulation extends CommandProtocol {

	public CommandStopSimulation(int mCmdId, byte mAction, String mParameter)
	{
		super(0, mCmdId, mAction, mParameter);
	}

	public CommandStopSimulation()
	{
		super(0, CommandProtocol.getNextCmdId(), ACTION_STOP_SIMULATION, "");
	}

	public boolean run()
	{
		return false;
	}
}
