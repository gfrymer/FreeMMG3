package simmcast.distribution.communication;

import java.io.DataOutputStream;


public interface CommunicationClient {

	public boolean create();
	public Connection connect(String server);	
	public String getDescription();
	public boolean disconnect();
}
