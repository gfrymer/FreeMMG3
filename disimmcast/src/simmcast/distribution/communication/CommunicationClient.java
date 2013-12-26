package simmcast.distribution.communication;

import java.io.DataOutputStream;


public interface CommunicationClient {

	public boolean create();
	public Connection connect(String server);	
	public String getDescription(boolean full);
	public boolean disconnect();
}
