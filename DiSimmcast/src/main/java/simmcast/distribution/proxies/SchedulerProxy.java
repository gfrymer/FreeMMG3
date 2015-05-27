package simmcast.distribution.proxies;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

import simmcast.distribution.interfaces.ProcessInterface;
import simmcast.distribution.interfaces.SchedulerInterface;
import simmcast.engine.Process;
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
		network.getWorker().processBlockedOrFinished(pid);
	}

	public void activateAt(double relativeTime_, ProcessInterface process_) {
		network.getWorker().activateAt(relativeTime_, process_);
	}

	public void activateNow(ProcessInterface process_) {
		network.getWorker().activateAt(0, process_);
	}

	public void addToThreadPool(ProcessInterface process_) {
		if (network.getWorker().addToThreadPool(process_))
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

	public void removeFromThreadPool(ProcessInterface process_) {
		if (!started)
			return;
		if (network.getWorker().removeFromPool(process_.getPid()))
		{
			threadPool.remove(process_.getPid());
		}
	}

	@Override
	public void interrupt()
	{
		started = false;
		for (Iterator<ProcessInterface> iter = threadPool.values().iterator(); iter.hasNext();) {
			ProcessInterface walk = iter.next();
			walk.interrupt();
		}
		threadPool.clear();
	}
}
