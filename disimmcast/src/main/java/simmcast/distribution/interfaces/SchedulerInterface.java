package simmcast.distribution.interfaces;

import java.util.Vector;

public interface SchedulerInterface {
	public double currentTime();
	public void setTime(double newTime);
	public void processBlockedOrFinished(int pid);
	public void activateAt(double relativeTime_, ProcessInterface process_);
	public void activateNow(ProcessInterface process_);
	public void addToThreadPool(ProcessInterface process_);
	public void removeFromThreadPool(ProcessInterface process_);
	public void start();
	public void interrupt();
	public ProcessInterface resumeProcess(int pid);
	public ProcessInterface getFromThreadPool(int pid);
	public Vector<ProcessInterface> currentProcesses();
}
