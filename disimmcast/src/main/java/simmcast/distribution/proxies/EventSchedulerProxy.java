package simmcast.distribution.proxies;
import simmcast.distribution.interfaces.EventSchedulerInterface;
import simmcast.distribution.interfaces.NodeInterface;
import simmcast.network.Network;
import simmcast.network.Packet;
import simmcast.network.PathAccountQueue;
import simmcast.node.Path;

public class EventSchedulerProxy implements EventSchedulerInterface {

	private NodeInterface node;
	private Network network;

	public EventSchedulerProxy(NodeInterface mNode, Network mNetwork)
	{
		node = mNode;
		network = mNetwork;
	}

	public void schedulePacketArrival(double relativeTime_, Packet packet_,
			PathAccountQueue pathAccount_) {
		// crear el CommandPacketArrival
		// darselo a Worker y que el CommandPacketArrival
		// se encargue de hacer lo que corresponda
		network.getWorker().sendPacket(((NodeProxy) node).getWorkerDescription(), relativeTime_, packet_);
	}

	public void schedulePacketDeparture(double relativeTime_, Path path_) {
		
	}

}
