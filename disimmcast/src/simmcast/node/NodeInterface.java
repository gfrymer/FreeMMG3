package simmcast.node;

import simmcast.network.Network;
import simmcast.network.PacketQueue;
import arjuna.JavaSim.Distributions.RandomStream;

public interface NodeInterface {

	public int getNetworkId();
	public String getName();
	public void begin();
	public void end();
	public int[] getNeighbors();
	public EventSchedulerInterface getScheduler();
	public Network getNetwork();
	public void addPath(NodeInterface destination_,
            int pathCapacity_,
            double bandwidth_,
            RandomStream propagationStream_,
            double lossRate_,
            int rqLimit_);
	public void addReceiverQueue(PacketQueue queue_, int senderNetworkId_);
}
