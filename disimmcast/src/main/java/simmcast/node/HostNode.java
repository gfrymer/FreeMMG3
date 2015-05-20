/*
 * Simmcast - a network simulation framework
 * HostNode.java
 * Copyright (C) 2002-2003 Hisham H. Muhammad
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

import simmcast.distribution.interfaces.NodeInterface;
import simmcast.distribution.interfaces.RouterNodeInterface;
import simmcast.network.InvalidIdentifierException;
import simmcast.network.MultiPortPacketQueue;
import simmcast.network.Network;
import simmcast.network.NetworkPacket;
import simmcast.network.Packet;
import simmcast.network.PacketMultiQueue;
import simmcast.network.PacketQueue;
import arjuna.JavaSim.Distributions.RandomStream;

/**
 * A HostNode contains the protocol or application logic.
 * A host has a single outgoing path (to a RouterNode).
 *
 * @author Hisham H. Muhammad
 */
public abstract class HostNode extends Node {

   // *****************************************************
   // CONSTANTS
   // *****************************************************

   /**
    * Maximum allowed value for a packet's "time-to-live".
    */
   public static final int MAX_TTL = 128;

   // *****************************************************
   // ATTRIBUTES
   // *****************************************************

   /**
    * The single path originating from this object is the
    * one to the router.
    */
   Path routerPath = null;

   /**
    * The router this host is connected to.
    */
   RouterNodeInterface router = null;

   /**
    * The network id of the router to which this host is connected to.
    */
   int routerAddress;

   /**
    * "Time to live". Number of hops a packet can travel through before
    * it is automatically discarded.
    */
   int ttl = MAX_TTL;

   // *****************************************************
   // GETTERS/SETTERS
   // *****************************************************

   /**
    * Every host node is connected to at most one node,
    * a router node. This methods returns a handle to this
    * router node.
    *
    * @return The router this host is connected to, or
    * null if the host is not connected to any router.
    */
   public RouterNodeInterface getRouter() {
      return router;
   }

   // *****************************************************
   // SCRIPT INIT ROUTINES
   // *****************************************************

   /**
    * Prepare data structures for execution.
    * This is overriden because the "paths" PathTable
    * is not used in HostNodes, and therefore, does not
    * need to be allocated.
    *
    * @param network_ The network object responsible to the 
    * simulation this node is a part of. 
    * @param name_ The node's name as given by the user in
    * the configuration file. 
    */
   public void initialize(simmcast.engine.Scheduler scheduler_, Network network_, String name_) {
      super.initialize(scheduler_, network_, name_);
      receiverQueue = new MultiPortPacketQueue("RQ");
      receiverMultiQueue = (PacketMultiQueue)receiverQueue;
   }

   /**
    * Create a path between the host and the router.
    * If there is a router already connected to the host,
    * the existing path will be destroyed and a new one
    * will be created.
    *
    * @param destination_ The destination node. The created path 
    * will connect from this node to the node specified in this 
    * parameter. 
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
    * @param rqLimit_ The maximum size for the specific RQ of the 
    * node at the arriving end of the path. 
    */
   public void addPath(NodeInterface destination_,
                       int pathCapacity_,
                       double bandwidth_,
                       RandomStream propagationStream_,
                       double lossRate_, int rqLimit_)
   {
      // TODO: Check the implications if the host's router is
      // redefined dinamically
      if (routerPath != null) {
         network.tracer.nodeError(this, "Attempted to redefine the host's router.");
      }
      routerPath = new Path(this,
                             destination_,
                             pathCapacity_,
                             bandwidth_,
                             propagationStream_,
                             lossRate_,
                             network.randomGenerator);
      registerQueueAt(destination_, rqLimit_);
      routerAddress = destination_.getNetworkId();
      network.tracer.path(routerPath);
      try {
         router = (RouterNodeInterface)destination_;
      } catch (ClassCastException e) {
         System.err.println("Error: Attempted to connect a host to a node which is not a router.");
         throw e;
      }
   }
   
   // Added by Lucas A.S.
   public void addTCPPath(NodeInterface destination_,
		   double bandwidth_,
		   RandomStream propagationStream_,
		   double offset_, int rqLimit_)
   {
	   //TODO: Check the implications if the host's router is
	   //redefined dinamically
	   if (routerPath != null) {
		   network.tracer.nodeError(this, "Attempted to redefine the host's router.");
	   }
	   
	   if (offset_ <= 0) offset_ = 0.001;
	   
	   routerPath = new TCPPath(this,
			   destination_,
			   bandwidth_,
			   propagationStream_,
			   offset_,
			   network.randomGenerator);
	   
	   registerQueueAt(destination_, rqLimit_);
	   routerAddress = destination_.getNetworkId();
	   network.tracer.path(routerPath);
	   
	   try {
		   router = (RouterNodeInterface)destination_;
	   } catch (ClassCastException e) {
		   System.err.println("Error: Attempted to connect a host to a node which is not a router.");
		   throw e;
	   }
   }

   /**
    * The host node's RQ is not controlled by the path structure,
    * so simply ignore the request.
    *
    * @param queue_ Dummy parameter for interface compatibility.
    * @param senderNetworkId_ Dummy parameter for interface compatibility.
    */
   public void addReceiverQueue(PacketQueue queue_, int senderNetworkId_) {
   }

   /**
    * Declares a port; ie, allows the given port identifier to be used in a
    * receive(p) request. An RQ is created to store data sent to this port.
    * Its size is defined to be unlimited.
    *
    * @param port_ The port to which packets will be sent to this node.
    */
   public void declarePort(int port_) {
      declarePort(port_, UNLIMITED);
   }

   /**
    * Declares a port; ie, allows the given port identifier to be used in a
    * receive(p) request. An RQ is created to store data sent to this port.
    *
    * @param port_ The port to which packets will be sent to this node.
    */
   public void declarePort(int port_, int rqLimit_) {
      if (receiverMultiQueue != null) {
         PacketQueue queue = new PacketQueue("RQ", rqLimit_);
         receiverMultiQueue.addQueue(queue, port_);
      }
   }

   // *****************************************************
   // PACKET TX/RX PRIMITIVES
   // *****************************************************

   /** 
    * Sets the "time to live" value for all packets sent from this node.
    *
    * @param ttl_ TTL parameter.
    */
   public void setTTL(int ttl_) {
      ttl = (ttl_ <= MAX_TTL ? ttl_ : MAX_TTL);
   }

   /**
    * Basic primitive for sending packets.
    *
    * @param packet_ The packet to be sent.
    * @param thread_ The thread that requested this operation,
    * which will block for sendTime time.
    */
   protected void send(Packet packet_, NodeThread thread_) throws TerminationException {
      if (sendTime > 0)
         thread_.block(sendTime);
      if (routerPath != null) {
         if (ttl > 0) {
            NetworkPacket rp = new NetworkPacket(packet_.getSource(), routerAddress, packet_.getSize(), packet_, ttl - 1);
            routerPath.addPacket(rp);
	 }
      } else {
         network.tracer.nodeError(this, "Can't send packets: no router defined.");
      }
   }

   /**
    * Basic primitive for receiving packets.
    * The network layer header is removed.
    *
    * @return A received packet, or null if there is no packet available.
    */
   protected Packet tryReceive() {
      Packet p = super.tryReceive();
      if (p != null) {
         NetworkPacket np = null;
         np = (NetworkPacket)p;
         return (Packet)np.getData();
      } else {
         return null;
      }
   }

   /**
    * Basic primitive for receiving packets, spefcifying
    * a port from which a packet should be received.
    *
    * @param port_ The port through which the packet should have
    * been received.
    *
    * @return A received packet, or null if there is no packet available.
    */
   protected Packet tryReceive(int port_) throws InvalidIdentifierException {
//System.err.println("INSTANCEOF RQ = "+receiverQueue.getClass().getName());
      Packet p = super.tryReceive(port_);
      if (p != null) {
         NetworkPacket np = (NetworkPacket)p;
         return (Packet)np.getData();
      } else {
         return null;
      }
   }

}
