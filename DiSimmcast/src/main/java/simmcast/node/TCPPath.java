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

// Added by Lucas A.S.

package simmcast.node;

import java.util.Random;

import simmcast.distribution.interfaces.NodeInterface;
import simmcast.network.Packet;
import arjuna.JavaSim.Distributions.RandomStream;


/**
 * Extends the class Path to create a TCP version of Path,
 * where packet order is kept.
 * There is no limit to path's capacity and packets cannot
 * be lost. 
 */
public class TCPPath extends Path {
	
	// *****************************************************
	// ATTRIBUTES
	// *****************************************************
	
	protected double offset;
	protected double prevArrivalTime = 0;
	
	//*****************************************************
	// CONSTRUCTORS
	// *****************************************************
	
	public TCPPath(NodeInterface source_,
			NodeInterface destination_,
			double bandwidth_,
			RandomStream propagationStream_,
			double offset_,
			Random randomGenerator_) {
		
		super(source_, destination_, UNLIMITED, bandwidth_,
				propagationStream_, 0.0, randomGenerator_);
		
		offset = offset_;
	}
	
	public TCPPath(NodeInterface source_,
			NodeInterface destination_,
			double bandwidth_,
			RandomStream propagationStream_,
			double offset_,
			Random randomGenerator_,
			String color_) {
		
		super(source_, destination_, UNLIMITED, bandwidth_, propagationStream_,
				0.0, randomGenerator_, color_);
		
		offset = offset_;
	}
	
	public TCPPath(NodeInterface source_,
			NodeInterface destination_,
			double bandwidth_,
			RandomStream propagationStream_,
			double offset_,
			Random randomGenerator_,
			String color_,
			String label_) {
		
		super(source_, destination_, UNLIMITED, bandwidth_, propagationStream_,
				0.0, randomGenerator_, color_, label_);
		
		offset = offset_;
	}
	
	// *****************************************************
	// PACKET MANAGEMENT
	// *****************************************************
	
	/**
	 * Apply the "pq" delay and loss probability on a packet.
	 * This is the public interface to the path.
	 */
	void sendPacket() {
		try {

			Packet packet = senderQueue.dequeue();
			double r = randomGenerator.nextDouble();
			if (r <= lossRate) {
				source.getNetwork().tracer.loss(packet, senderQueue, pathAccount, "random loss");
			} else {
				double propagationTime = propagationStream.getNumber();
				
				if (propagationTime < 0) {
					// TODO: better check
					// System.err.println("Invalid propagation time");
					propagationTime = 0;
				}
				
				if (source.getNetwork().simulationTime() + propagationTime <= prevArrivalTime) {
					propagationTime = prevArrivalTime - source.getNetwork().simulationTime() + offset;
				}
				
				prevArrivalTime = source.getNetwork().simulationTime() + propagationTime;
				
				destination.getScheduler().schedulePacketArrival(propagationTime, packet, pathAccount);
				pathAccount.enqueue();
				source.getNetwork().tracer.move(packet, senderQueue, pathAccount);
			}
			if (! senderQueue.isEmpty())
				schedulePacketDeparture( senderQueue.peek() );

		} catch (Exception e) {
			System.err.println(e);
		}
	}

}
