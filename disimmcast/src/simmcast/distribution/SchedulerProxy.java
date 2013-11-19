package simmcast.distribution;

import java.util.TreeMap;
import java.util.Vector;

import simmcast.engine.Process;
import simmcast.engine.ProcessInterface;
import simmcast.engine.SchedulerInterface;
import simmcast.network.Network;

public class SchedulerProxy implements SchedulerInterface {

	/**
	 * The current simulation time.
	 */
	double now = 0.0;

	private Network network;

	boolean started;

	/**
	 * A handle to the currently running process.
	 */
	Vector<ProcessInterface> running = new Vector<ProcessInterface>();

	/**
	 * The list of all processes in this simulation,
	 * also known as the thread pool.
	 */
	public TreeMap<Integer, ProcessInterface> threadPool = new TreeMap<Integer, ProcessInterface>();

	public SchedulerProxy(Network network)
	{
		this.network = network;
		started = false;
	}

	/**
	 * Returns the current simulation time. This is the logic
	 * clock of the simulator scheduler.
	 *
	 * @return The current simulation time, in simulated time
	 * units.
	 */
	public double currentTime() {
		return now;
	}

	/**
	 * Time informed by the server.
	 *
	 */
	public void setTime(double newTime) {
		now = newTime;
	}

	public ProcessInterface resumeProcess(int pid)
	{
		ProcessInterface p = (ProcessInterface) getFromThreadPool(pid);
		if (p!=null)
		{
			synchronized (running) {
				running.add(p);
			}
			p.resumeProcess();
		}
		return p;
	}

	public void processBlockedOrFinished(int pid) {
		if (!started)
			return;
		synchronized (running) {
			for (int i=0;i<running.size();i++)
			{
				if (running.get(i).getPid()==pid)
				{
					running.remove(i);
				}
			}
		}
		network.getClient().processBlockedOrFinished(pid);
	}

	public void activateAt(double relativeTime_, ProcessInterface process_) {
		network.getClient().activateAt(now + relativeTime_, process_);
	}

	public void activateNow(ProcessInterface process_) {
		activateAt(0,process_);
	}

	public void addToThreadPool(ProcessInterface process_) {
		if (network.getClient().addToThreadPool(process_))
		{
			threadPool.put(process_.getPid(), process_);
		}
	}

	public void start() {
		started = true;
	}

	public Vector<ProcessInterface> currentProcesses() {
		return running;
	}

	public ProcessInterface getFromThreadPool(int pid)
	{
		return threadPool.get(pid);
	}
}
