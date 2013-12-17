package simmcast.distribution.communication;

import java.io.DataOutputStream;

public interface CommunicationOutputStream {

	public DataOutputStream getOutputStream(String client);
}
