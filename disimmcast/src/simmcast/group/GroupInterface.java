package simmcast.group;

public interface GroupInterface {
	public int getNetworkId();
	public String getName();
	public int elementAt(int n);
	public int indexOf(int nodeId_);
	public int size();
	public void join(int nodeId_);
	public boolean leave(int nodeId_);
	public int[] getNetworkIds();
}
