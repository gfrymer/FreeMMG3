package simmcast.distribution;

import simmcast.network.Network;

public class CommandAddToPool extends CommandProtocol {

	public CommandAddToPool(int mClientId, int mCmdId, byte mAction, String mParameter)
	{
		super(mClientId, mCmdId, mAction, mParameter);
		String[] sp = getSplittedParameters();
	}

	public CommandAddToPool()
	{
		super(0, CommandProtocol.getNextCmdId(), ACTION_ADD_TO_POOL, "");
		parameters = "";
	}

	public String run(Network network)
	{
		ProcessProxy pp = new ProcessProxy(network, getClientId());
		network.getSimulationScheduler().addToThreadPool(pp);
		return OK_PREFIX + pp.getPid();
	}
}
