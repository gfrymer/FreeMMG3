package simmcast.engine;

public interface ProcessInterface {

	public void resumeProcess();
	public double getLastSchedule();
	public void setLastSchedule(double schedule);
	public int getPid();
	public void setPid(int newpid);
	public void interrupt();
	public boolean isRunning();
}
