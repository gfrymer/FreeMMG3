package simmcast.distribution.interfaces;

import simmcast.route.RouterNodeThread;

public interface RouterNodeInterface extends NodeInterface {
	public RouterNodeThread getClientThread();
	public void notifyJoin(NodeInterface node_);
	public void notifyLeave(NodeInterface node_);
}
