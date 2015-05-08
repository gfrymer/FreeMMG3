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
	private int workerId;
	double lastSchedule = 0.0;

	public ProcessProxy(Network mNetwork, int mWorkerId)
	{
		network = mNetwork;
		workerId = mWorkerId;
	}

	public void resumeProcess()
	{
		network.getManager().resumeProcess(workerId, pid);
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

	public int getWorkerId() {
		return workerId;
	}

	public void interrupt() {
		network.getManager().terminateProcess(workerId, pid);
	}

	public boolean isRunning() {
		return false;
	}
}
