package simmcast.distribution.proxies;

import simmcast.network.Network;

public class NetworkProxy extends ObjectProxy {

	public NetworkProxy(Network mNetwork, String mLabel, int mWorkerId,
			String mWorkerDescription) throws ClassNotFoundException {
		super(mNetwork, mLabel, mWorkerId, mWorkerDescription);
		classType = mNetwork.getClass();
	}
}
