package simmcast.node;

import simmcast.network.Packet;
import simmcast.network.PathAccountQueue;

public interface EventSchedulerInterface {
	public void schedulePacketArrival(double relativeTime_, Packet packet_, PathAccountQueue pathAccount_);
	public void schedulePacketDeparture(double relativeTime_, Path path_);
}
