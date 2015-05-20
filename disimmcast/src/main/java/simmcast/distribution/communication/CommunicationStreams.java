package simmcast.distribution.communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public interface CommunicationStreams {

	public DataOutputStream getOutputStream(String worker);
	public DataInputStream getInputStream(String worker);
	public void listenWorkers();
	public boolean disconnect();
}
