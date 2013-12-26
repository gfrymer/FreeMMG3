package simmcast.distribution.interfaces;

import simmcast.route.RouterNodeThread;

public interface RouterNodeInterface extends NodeInterface {
	public RouterNodeThread getClientThread();
}
