package simmcast.distribution;
import simmcast.network.Network;
import simmcast.network.Packet;
import simmcast.network.PathAccountQueue;
import simmcast.node.EventSchedulerInterface;
import simmcast.node.Path;

public class EventSchedulerProxy implements EventSchedulerInterface {

	private Network network;

	public EventSchedulerProxy(Network mNetwork)
	{
		network = mNetwork;
	}

	public void schedulePacketArrival(double relativeTime_, Packet packet_,
			PathAccountQueue pathAccount_) {
		
	}

	public void schedulePacketDeparture(double relativeTime_, Path path_) {
		
	}

}
