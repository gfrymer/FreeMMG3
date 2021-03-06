package simmcast.distribution.proxies;

import simmcast.distribution.interfaces.GroupInterface;
import simmcast.distribution.interfaces.GroupTableInterface;
import simmcast.distribution.interfaces.NodeInterface;
import simmcast.network.Network;

public class GroupProxy implements GroupInterface {

	private Network network;
	private int netId;
	private String name;
	private int[] netIds;

	public static GroupProxy createGroupProxy(Network mNetwork, String params)
	{
		String[] sp = params.split(",");
		int[] netIds = new int[sp.length-2];
		for (int j=2;j<sp.length;j++)
		{
			netIds[j-2] = Integer.parseInt(sp[j]);
		}
		return new GroupProxy(mNetwork, Integer.parseInt(sp[0]),sp[1],netIds);
	}

	public GroupProxy (Network mNetwork, int mNetId, String mName, int[] mNetIds)
	{
		network = mNetwork;
		netId = mNetId;
		name = mName;
		netIds = mNetIds;
	}

	public int getNetworkId() {
		return netId;
	}

	public String getName() {
		return name;
	}

	public int elementAt(int n) {
		return netIds[n];
	}

	public int indexOf(int nodeId_) {
      for (int i = 0; i < netIds.length; i++) {
          if (netIds[i] == nodeId_) {
             return i;
          }
       }
       return -1;
	}

	public int size() {
		return netIds.length;
	}

	public void join(int nodeId) {
		String[] params = new String[1];
		params[0] = "" + nodeId;
		network.getWorker().invokeCommand(netId, "join", params);
	}

	public boolean leave(int nodeId) {
		String[] params = new String[1];
		params[0] = "" + nodeId;
		return network.getWorker().invokeCommand(netId, "leave", params).equals("1");
	}

	public int[] getNetworkIds() {
		return netIds;
	}

}
