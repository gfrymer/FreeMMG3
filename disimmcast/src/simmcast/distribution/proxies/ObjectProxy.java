package simmcast.distribution.proxies;

import arjuna.JavaSim.Distributions.RandomStream;
import simmcast.distribution.interfaces.EventSchedulerInterface;
import simmcast.distribution.interfaces.NodeInterface;
import simmcast.network.Network;
import simmcast.network.PacketQueue;
import simmcast.node.EventScheduler;

public class ObjectProxy implements Proxyable {

	private Network network;
	private int clientId;
	private Class classType;
	private String clientDescription;
	private String label;

	public ObjectProxy(int mClientId, Network mNetwork, String mLabel, String className, String[] arguments) throws ClassNotFoundException
	{
    	classType = Class.forName(className);
		network = mNetwork;
    	label = mLabel;
    	clientId = network.getServer().createObject(mClientId,label,className,arguments);
    	clientDescription = network.getServer().getClientDescription(clientId);
	}

	public ObjectProxy(Network mNetwork, String mLabel, int mClientId, String mClientDescription) throws ClassNotFoundException
	{
		network = mNetwork;
		label = mLabel;
		clientId = mClientId;
		clientDescription = mClientDescription;
	}

	public int getClientId() {
		return clientId;
	}

	public String getName() {
		return label;
	}

	public String getClientDescription() {
		return clientDescription;
	}
	public Class getClassType()
	{
		return classType;
	}

	public boolean invoke(String function, String[] arguments)
	{
		network.getServer().invokeCommand(clientId, getName(), function, arguments);
		return false;
	}
}
