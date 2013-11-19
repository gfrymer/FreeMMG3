/*
 * Simmcast - a network simulation framework
 * Path.java
 * Copyright (C) 2001-2003 Hisham H. Muhammad
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package simmcast.node;

import java.util.Random;

import simmcast.network.FullQueueException;
import simmcast.network.Network;
import simmcast.network.Packet;
import simmcast.network.PacketQueue;
import simmcast.network.PathAccountQueue;
import arjuna.JavaSim.Distributions.RandomStream;

/**
 * Paths represent a packet flow between two nodes, and thus their meaning
 * in the model depends on what the nodes themselves are representing. Paths
 * are used to connect these nodes, and could represent a packet queue,
 * a physical link or else a logical path between two end-nodes. All paths
 * are unidirectional in essence, so that to model a bidirectional physical
 * link two Simmcast paths must be used.
 * The nodes which this path connects are called the "departing end", for
 * the node from which packets flow from, and the "arriving end", for the
 * node from which packets flow to.
 * Note that if the bandwidth is set to zero, then the packet is silently
 * dropped with no report in the traces: this emulates an infinite
 * propagation time.
 *
 * @author Hisham H. Muhammad
 */
public class Path {

	// *****************************************************
	// CONSTANTS
	// *****************************************************

	/**
	 * A constant to specify that a given resource
	 * (bandwidth, path capacity) is unlimited.
	 */
	static final public int UNLIMITED = -1;

	// *****************************************************
	// ATTRIBUTES
	// *****************************************************

	/**
	 * Color and label of a link (added by Ruthiano).
	 *
	 */
	protected String color;// = "black";
	protected String label;// = "";

	/**
	 * The node at the "departing end" of this path.
	 * See the class description for details.
	 */
	protected NodeInterface source;

	/**
	 * The node at the "arriving end" of this path.
	 * See the class description for details.
	 */
	protected NodeInterface destination;

	/**
	 * The network id of the destination node. This
	 * is merely a copy of the value returned by
	 * destination.getNetworkId() (which never changes
	 * during the simulation), provided as a convenience.
	 */
	int destinationId;

	/**
	 * A probablilistic stream which describes the
	 * distribution of propagation delays of this path.
	 * This is a parameter of the path, passed to the
	 * factory method addPath().
	 *
	 * @see simmcast.node.Node#addPath
	 */
	RandomStream propagationStream;

	/**
	 * The generator of numbers used by the draw 
	 * which decides on packet loss. This random generator
	 * is global to the entire simulation, and its seed
	 * initialized at the configuration file and implicitly
	 * passed to all objects.
	 */
	Random randomGenerator;

	/**
	 * The proportion to be applied on the draw when
	 * a packet loss decision is made. This loss rate
	 * is, therefore, a value between 0.0 and 1.0.
	 */
	double lossRate;

	/**
	 * The path bandwidth. 
	 */
	double bandwidth;

	/**
	 * A queue where packets remain stored during their
	 * "sending time" (ie, time that models the packet's 
	 * processing load on the sending node).
	 */
	PacketQueue senderQueue; 

	/**
	 * This abstract queue keeps track of the size of the path;
	 * that is, the number of packets currently within the path.
	 */
	PathAccountQueue pathAccount;

	// *****************************************************
	// CONSTRUCTORS
	// *****************************************************

	/**
	 * Constructs a path. This method should never be called by the
	 * user directly. Instead, the addPath() factory method of the
	 * Node class should be used instead, as it will perform other
	 * necessary internal initializations in addition to calling this
	 * constructor.
	 *
	 * @param source_ The source node, at the "departing end".
	 * @param destination_ The destination node, at the "arriving end".
	 * @param pathCapacity_ The capacity of the sender node's SQ. 
	 * @param bandwidth_ The path's bandwidth. Transmission time 
	 * of packets sent through this path are calculated as the 
	 * packet size divided by this bandwidth value. This generated 
	 * transmission time affects the packet departure time. 
	 * @param propagationStream_ This statistical stream will control 
	 * the generation of latency times. These generated times 
	 * affect the packet arrival time. 
	 * @param lossRate_ a rate between 0.0 and 1.0 indicated how 
	 * many packets (%) are randomically discarded. 
	 * @param randomGenerator_ a handle to the global random generator.
	 *
	 * @see simmcast.node.Node#addPath
	 */
	public Path(NodeInterface source_,
			NodeInterface destination_,
			int pathCapacity_,
			double bandwidth_,
			RandomStream propagationStream_,
			double lossRate_,
			Random randomGenerator_)
	{
		this(source_, destination_, pathCapacity_, bandwidth_,
				propagationStream_, lossRate_, randomGenerator_,
				"black", "");
	}

	//added by Ruthiano
	public Path(NodeInterface source_,
			NodeInterface destination_,
			int pathCapacity_,
			double bandwidth_,
			RandomStream propagationStream_,
			double lossRate_,
			Random randomGenerator_,
			String color_)
	{
		this(source_, destination_, pathCapacity_, bandwidth_,
				propagationStream_, lossRate_, randomGenerator_,
				color_, "");
	}

	//added by Ruthiano
	public Path(NodeInterface source_,
			NodeInterface destination_,
			int pathCapacity_,
			double bandwidth_,
			RandomStream propagationStream_,
			double lossRate_,
			Random randomGenerator_,
			String color_,
			String label_)
	{
		source = source_;
		destination = destination_;
		propagationStream = propagationStream_;
		lossRate = lossRate_;
		destinationId = destination_.getNetworkId();
		randomGenerator = randomGenerator_;
		bandwidth = bandwidth_;

		//setting color and label parameters:
		color = color_;
		label = label_;

		if (pathCapacity_ == UNLIMITED)
			senderQueue = new PacketQueue("SQ");
		else
			senderQueue = new PacketQueue("SQ", pathCapacity_);
		pathAccount = new PathAccountQueue();
	}

	// *****************************************************
	// GETTERS/SETTERS
	// *****************************************************

	/**
	 * Return the link color
	 * (added by Ruthiano)
	 * @return The color name.
	 */
	public String getColor() { return color; }

	/**
	 * Return the link label
	 * (added by Ruthiano)
	 * @return The label name.
	 */
	public String getLabel() { return label; }

	/** 
	 * A handle to the source at the "departing end" of this path.
	 *
	 * @return The source node.
	 */
	public NodeInterface getSource() { return source; }

	/**
	 * A handle to the source at the "departing end" of this path.
	 *
	 * @return The destination node.
	 */
	public NodeInterface getDestination() { return destination; }

	/**
	 * Obtain the value of the bandwidth property of this path.
	 * This is a ratio between packet size and simulated time
	 * units. 
	 *
	 * @return The path's bandwidth ratio.
	 */
	public double getBandwidth() { return bandwidth; }

	// *****************************************************
	// PACKET MANAGEMENT
	// *****************************************************

	/**
	 * Put a packet in the the sender's "sq" that is associated
	 * to this path, applying the "sq" delay.
	 * This is the public interface to the path, as used by the nodes.
	 *
	 * @param packet_ The packet added to this path.
	 */
	public void addPacket(Packet packet_) {
		try {

			if (senderQueue.isEmpty()) {
				senderQueue.enqueue(packet_);
				schedulePacketDeparture(packet_);
//				source.network.tracer.move(packet_, Network.UPPER_LAYER, senderQueue);
			} else
				senderQueue.enqueue(packet_);

		} catch (FullQueueException e) {
//			source.network.tracer.loss(packet_, Network.UPPER_LAYER, senderQueue, "drop: SQ is full");
		}
	}

	/**
	 * Apply the "pq" delay and loss probability on a packet.
	 * This is the public interface to the path.
	 */
	void sendPacket() {
		try {

			Packet packet = senderQueue.dequeue();
			double r = randomGenerator.nextDouble();
			if (r <= lossRate) {
//				source.network.tracer.loss(packet, senderQueue, pathAccount, "random loss");
			} else {
				double propagationTime = propagationStream.getNumber();
				if (propagationTime < 0) {
					// TODO: better check
					// System.err.println("Invalid propagation time");
					propagationTime = 0;
				}
				
				destination.getScheduler().schedulePacketArrival(propagationTime, packet, pathAccount);
				pathAccount.enqueue();
//				source.network.tracer.move(packet, senderQueue, pathAccount);
			}
			if (! senderQueue.isEmpty())
				schedulePacketDeparture( senderQueue.peek() );

		} catch (Exception e) {
			System.err.println(e);
		}
	}

	/**
	 * This is an auxiliary function for event management.
	 * It calculates the propagation time for a packet and
	 * schedules its delivery. Note that if the bandwidth
	 * is set to zero, then the packet is silently dropped
	 * with no report in the traces: this emulates an
	 * infinite propagation time.
	 *
	 * @param packet_ The packet flowing through this path.
	 */
	protected void schedulePacketDeparture(Packet packet_) {
		double time;
		if (bandwidth == 0)
			return;
		if (bandwidth != UNLIMITED)
			time = packet_.getSize() / bandwidth;
		else
			time = 0;
		source.getScheduler().schedulePacketDeparture(time, this);
	}

}
