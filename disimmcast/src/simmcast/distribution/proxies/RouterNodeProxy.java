package simmcast.distribution.proxies;

import simmcast.distribution.interfaces.NodeInterface;
import simmcast.distribution.interfaces.RouterNodeInterface;
import simmcast.network.Network;
import simmcast.route.RouterNodeThread;

public class RouterNodeProxy extends NodeProxy implements RouterNodeInterface {

	public RouterNodeProxy(Network mNetwork, String mLabel, String className,
			String[] arguments) throws ClassNotFoundException {
		super(mNetwork, mLabel, className, arguments);
	}

	public RouterNodeProxy(Network mNetwork, String mLabel, int mNetworkId,
			int mClientId, String mClientDescription)
			throws ClassNotFoundException {
		super(mNetwork, mLabel, mNetworkId, mClientId, mClientDescription);
	}

	@Override
	public RouterNodeThread getClientThread() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void notifyJoin(NodeInterface node_) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyLeave(NodeInterface node_) {
		// TODO Auto-generated method stub
		
	}
}
