package simmcast.distribution;

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

	public boolean run()
	{
/*		ProcessProxy pp = new ProcessProxy(network, cap.getClientId());
		network.getSimulationScheduler().addToThreadPool(pp);
		connections[cap.getClientId()].sendOk(cap.getCmdId(),""+pp.getPid());*/
		return true;
	}
}
