package simmcast.distribution.communication;

import simmcast.distribution.command.CommandProtocol;

public interface CommunicationServer {

	public boolean create();
	public Connection listen(int connNumber, java.util.concurrent.LinkedBlockingQueue<CommandProtocol> inqueue);
	public String getDescription();
	public boolean disconnect();
}
