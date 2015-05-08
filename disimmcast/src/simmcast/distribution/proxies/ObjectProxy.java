package simmcast.distribution.proxies;

import arjuna.JavaSim.Distributions.RandomStream;
import simmcast.distribution.interfaces.EventSchedulerInterface;
import simmcast.distribution.interfaces.NodeInterface;
import simmcast.network.Network;
import simmcast.network.PacketQueue;
import simmcast.node.EventScheduler;

public class ObjectProxy implements Proxyable {

	private Network network;
	private int workerId;
	protected Class classType;
	private String workerDescription;
	private String label;

	public ObjectProxy(int mWorkerId, Network mNetwork, String mLabel, String className, String[] arguments) throws ClassNotFoundException
	{
    	classType = Class.forName(className);
		network = mNetwork;
    	label = mLabel;
    	workerId = network.getManager().createObject(mWorkerId,label,className,arguments);
    	workerDescription = network.getManager().getWorkerDescription(workerId);
	}

	public ObjectProxy(Network mNetwork, String mLabel, int mWorkerId, String mWorkerDescription) throws ClassNotFoundException
	{
		network = mNetwork;
		label = mLabel;
		workerId = mWorkerId;
		workerDescription = mWorkerDescription;
	}

	public int getWorkerId() {
		return workerId;
	}

	public String getName() {
		return label;
	}

	public String getWorkerDescription() {
		return workerDescription;
	}
	public Class getClassType()
	{
		return classType;
	}

	public boolean invoke(String function, String[] arguments)
	{
		network.getManager().invokeCommand(workerId, getName(), function, arguments);
		return false;
	}
}
