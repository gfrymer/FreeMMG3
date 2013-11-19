package simmcast.distribution;

public class CommandActivateAt extends CommandProtocol {

	private double time;
	private int pid;

	public CommandActivateAt(int mClientId, int mCmdId, byte mAction, String mParameter)
	{
		super(mClientId, mCmdId, mAction, mParameter);
		String[] sp = getSplittedParameters();
		time = Double.parseDouble(sp[0]);
		pid = Integer.parseInt(sp[1]);
	}

	public CommandActivateAt(double time, int pid)
	{
		super(0, CommandProtocol.getNextCmdId(), ACTION_ACTIVATE_AT, "");
		this.time = time;
		this.pid = pid;
		parameters = time + CommandProtocol.PARAMETER_SEPARATOR + pid;
	}

	public double getTime()
	{
		return time;
	}

	public int getPid()
	{
		return pid;
	}
}
