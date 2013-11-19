package simmcast.distribution;

public class CommandInvoke extends CommandProtocol {

	private int networkId;
	private String function;
	private String[] arguments;

	public CommandInvoke(int mCmdId, byte mAction, String mParameter)
	{
		super(0, mCmdId, mAction, mParameter);
		String[] sp = getSplittedParameters();
		networkId = Integer.parseInt(sp[0]);
		function = sp[1];
		arguments = new String[sp.length-2];
		for (int i=2;i<sp.length; i++)
		{
			arguments[i-2] = sp[i];
		}
	}

	public CommandInvoke(int mNetworkId, String mFunction, String[] mParameters)
	{
		super(0, CommandProtocol.getNextCmdId(), ACTION_INVOKE, "");
		this.networkId = mNetworkId;
		this.function = mFunction;
		this.arguments = mParameters;
		parameters = networkId + CommandProtocol.PARAMETER_SEPARATOR + function + CommandProtocol.PARAMETER_SEPARATOR;
    	for (int i=0; i<arguments.length; i++)
    	{
    		parameters += arguments[i];
    		if (i<arguments.length-1)
    		{
    			parameters += CommandProtocol.PARAMETER_SEPARATOR;
    		}
    	}	
	}

	public int getNetworkId()
	{
		return networkId;
	}

	public String getFunction()
	{
		return function;
	}

	public String[] getArguments()
	{
		return arguments;
	}
}
