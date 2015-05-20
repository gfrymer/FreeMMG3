package simmcast.distribution.communication;

public interface CommunicationClient {

	public boolean create();
	public Connection connect(String server);	
	public String getDescription(boolean full);
	public boolean disconnect();
}
