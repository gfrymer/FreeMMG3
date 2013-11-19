package simmcast.distribution;

public class CommandBlockedOrFinished extends CommandProtocol {

	private int networkId;

	public CommandBlockedOrFinished(int mCmdId, byte mAction, String mParameter)
	{
		super(0, mCmdId, mAction, mParameter);
		String[] sp = getSplittedParameters();
		networkId = Integer.parseInt(sp[0]);
	}

	public CommandBlockedOrFinished(int mNetworkId)
	{
		super(0, CommandProtocol.getNextCmdId(), ACTION_BLOCKED_FINISHED, "");
		networkId = mNetworkId;
		parameters = "" + mNetworkId;
	}

	public int getNetworkId()
	{
		return networkId;
	}
}
