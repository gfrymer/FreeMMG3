package simmcast.distribution;

import simmcast.group.GroupInterface;
import simmcast.group.GroupTableInterface;
import simmcast.network.Network;

public class GroupTableProxy implements GroupTableInterface {

	private Network network;

	public GroupTableProxy(Network mNetwork)
	{
		network = mNetwork;
	}

	public GroupInterface removeGroup(int i_) {
		String[] params = new String[1];
		params[0] = "" + i_;
		String paramsRet = network.getClient().invokeCommand(-1, i_, GroupTableInterface.GP_FNCTN_PREFIX + "removeGroup", params);
		if (paramsRet!=null)
		{
			return GroupProxy.createGroupProxy(network,paramsRet);
		}
		return null;
	}

	public GroupInterface getGroupById(int i_) {
		String[] params = new String[1];
		params[0] = "" + i_;
		String paramsRet = network.getClient().invokeCommand(-1, i_, GroupTableInterface.GP_FNCTN_PREFIX + "getGroupById", params);
		if (paramsRet!=null)
		{
			return GroupProxy.createGroupProxy(network,paramsRet);
		}
		return null;
	}

	public int[] getMembersById(int i_) {
		String[] params = new String[1];
		params[0] = "" + i_;
		String paramsRet = network.getClient().invokeCommand(-1, i_, GroupTableInterface.GP_FNCTN_PREFIX + "getGroupById", params);
		if (paramsRet!=null)
		{
			return GroupProxy.createGroupProxy(network,paramsRet).getNetworkIds();
		}
		return null;
	}
}
