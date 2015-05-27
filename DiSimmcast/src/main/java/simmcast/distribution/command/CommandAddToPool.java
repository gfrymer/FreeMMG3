package simmcast.distribution.command;

import simmcast.distribution.proxies.ProcessProxy;
import simmcast.network.Network;

public class CommandAddToPool extends CommandProtocol {

	public CommandAddToPool(int mWorkerId, int mCmdId, byte mAction, String mParameter)
	{
		super(mWorkerId, mCmdId, mAction, mParameter);
	}

	public CommandAddToPool()
	{
		super(0, CommandProtocol.getNextCmdId(), ACTION_ADD_TO_POOL, "");
	}

	public String run(Network network)
	{
		ProcessProxy pp = new ProcessProxy(network, getWorkerId());
		network.getSimulationScheduler().addToThreadPool(pp);
		return OK_PREFIX + pp.getPid();
	}
}
