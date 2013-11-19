package simmcast.distribution;

import simmcast.engine.ProcessInterface;
import simmcast.network.Network;

public class ProcessProxy implements ProcessInterface {

	/**
	 * PID generator for assigning PIDs to the processes
	 */
	private static int pidGen = 0;

	private int pid = pidGen++;

	private Network network;
	private int clientId;

	public ProcessProxy(Network mNetwork, int mClientId)
	{
		network = mNetwork;
		clientId = mClientId;
	}

	public void resumeProcess()
	{
		network.getServer().resumeProcess(clientId, pid);
	}

	public double getLastSchedule() {
		return 0;
	}

	public void setLastSchedule(double schedule) {
		
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int newpid) {

	}

	public int getClientId() {
		return clientId;
	}

	public void interrupt() {
	}

	public boolean isRunning() {
		return false;
	}
}
