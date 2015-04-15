package simmcast.distribution.proxies;

import simmcast.distribution.interfaces.ProcessInterface;
import simmcast.network.Network;

public class ProcessProxy implements ProcessInterface {

	/**
	 * PID generator for assigning PIDs to the processes
	 */
	private static int pidGen = 0;

	private int pid = pidGen++;

	private Network network;
	private int clientId;
	double lastSchedule = 0.0;

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
		return lastSchedule;
	}

	public void setLastSchedule(double schedule) {
		lastSchedule = schedule;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int newpid) {
		pid = newpid;
	}

	public int getClientId() {
		return clientId;
	}

	public void interrupt() {
		network.getServer().terminateProcess(clientId, pid);
	}

	public boolean isRunning() {
		return false;
	}
}
