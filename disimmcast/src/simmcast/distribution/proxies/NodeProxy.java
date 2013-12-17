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
	private int clientId;
	private Class classType;
	private String clientDescription;
	private String label;
	private EventSchedulerProxy eventScheduler;

	public NodeProxy(Network mNetwork, String mLabel, String className, String[] arguments) throws ClassNotFoundException
	{
    	classType = Class.forName(className);
		network = mNetwork;
    	networkId = network.obtainUnicastAddress();
    	label = mLabel;
    	clientId = network.getServer().createNode(networkId,label,className,arguments);
    	clientDescription = network.getServer().getClientDescription(clientId);
	}

	public NodeProxy(Network mNetwork, String mLabel, int mNetworkId, int mClientId, String mClientDescription) throws ClassNotFoundException
	{
		network = mNetwork;
		networkId = mNetworkId;
		label = mLabel;
		clientId = mClientId;
		clientDescription = mClientDescription;
	}

	public int getNetworkId() {
		return networkId;
	}

	public int getClientId() {
		return clientId;
	}

	public void begin() {
	}

	public void end() {	
	}

	public String getName() {
		return label;
	}

	public String getClientDescription() {
		return clientDescription;
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
		network.getServer().invokeCommand(clientId, networkId, function, arguments);
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
