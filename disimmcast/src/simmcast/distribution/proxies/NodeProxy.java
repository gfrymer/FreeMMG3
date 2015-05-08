package simmcast.distribution.proxies;

import arjuna.JavaSim.Distributions.RandomStream;
import simmcast.distribution.interfaces.EventSchedulerInterface;
import simmcast.distribution.interfaces.NodeInterface;
import simmcast.network.Network;
import simmcast.network.PacketQueue;
import simmcast.node.EventScheduler;

public class NodeProxy implements NodeInterface {

	private int networkId;
	private Network network;
	private int workerId;
	private Class classType;
	private String workerDescription;
	private String label;
	private EventSchedulerProxy eventScheduler;

	public NodeProxy(Network mNetwork, String mLabel, String className, String[] arguments) throws ClassNotFoundException
	{
    	classType = Class.forName(className);
		network = mNetwork;
    	networkId = network.obtainUnicastAddress();
    	label = mLabel;
    	workerId = network.getManager().createNode(networkId,label,className,arguments);
    	workerDescription = network.getManager().getWorkerDescription(workerId);
	}

	public NodeProxy(Network mNetwork, String mLabel, int mNetworkId, int mWorkerId, String mWorkerDescription) throws ClassNotFoundException
	{
		network = mNetwork;
		networkId = mNetworkId;
		label = mLabel;
		workerId = mWorkerId;
		workerDescription = mWorkerDescription;
	}

	public int getNetworkId() {
		return networkId;
	}

	public int getWorkerId() {
		return workerId;
	}

	public void begin() {
	}

	public void end() {	
	}

	public String getName() {
		return label;
	}

	public String getWorkerDescription() {
		return workerDescription;
	}

	public int[] getNeighbors() {
		// TODO Auto-generated method stub
		return null;
	}

	public Class getClassType()
	{
		return classType;
	}

	public boolean invoke(String function, String[] arguments)
	{
		network.getManager().invokeCommand(workerId, networkId, function, arguments);
		return false;
	}

	public void addPath(NodeInterface destination_,
            int pathCapacity_,
            double bandwidth_,
            RandomStream propagationStream_,
            double lossRate_,
            int rqLimit_)
	{
		
	}

	public EventSchedulerInterface getScheduler() {
		if (eventScheduler == null)
		{
			eventScheduler = new EventSchedulerProxy(this, network);
		}
		return eventScheduler;
	}

	public Network getNetwork() {
		return network;
	}

	public void addReceiverQueue(PacketQueue queue_, int senderNetworkId_) {
	}
}
