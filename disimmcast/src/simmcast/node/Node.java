/*
 * Simmcast - a network simulation framework
 * Node.java
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

import simmcast.engine.SchedulerInterface;
import simmcast.network.EmptyQueueException;
import simmcast.network.InvalidIdentifierException;
import simmcast.network.MultiSenderPacketQueue;
import simmcast.network.Network;
import simmcast.network.Packet;
import simmcast.network.PacketMultiQueue;
import simmcast.network.PacketQueue;
import arjuna.JavaSim.Distributions.RandomStream;

/**
 * A node. Nodes are the fundamental interacting entities,
 * and are uniquely identified by an integer. Their correspondence
 * in the model is not dictated by the simulator: depending on the
 * desired level of abstraction, nodes can represent a protocol agent
 * in a host, a router, or one of many interacting entities in a
 * host/router. A node will contain one or more threads of execution.
 *
 * @author Hisham H. Muhammad
 */
public abstract class Node implements NodeInterface {

   // *****************************************************
   // CONSTANTS
   // *****************************************************

   /**
    * A special value meaning "unlimited". In this context,
    * it is used to specify the size of the RQ.
    */
   static final public int UNLIMITED = -1;

   // *****************************************************
   // ATTRIBUTES
   // *****************************************************
   
   /**
    * Color, shape and label nodes (added by Ruthiano)
    */
   protected String color = "black";
   protected String shape = "circle";
   protected String label = "";

   /**
    * The object that models the RQ.
    */
   protected PacketQueue receiverQueue;

   /**
    * A representation of the RQ as a combination of
    * multiple queues.
    */
   // TODO: Haven't decided yet if this will be split
   // into two classes (SimpleNode/BasicNode and Node;
   // or Node and MultiNode?) A node that doesn't need
   // a PacketMultiQueue could benefit with performance
   // gains stemming from a simpler queue.
   protected PacketMultiQueue receiverMultiQueue;

   /**
    * A table of paths connecting this node to other
    * nodes.
    */
   protected PathTable paths = null;

   /**
    * The event scheduler. This is an internal thread to each
    * node, controlling all simulation events, such as packet
    * arrival and departure, and asynchronous events.
    */
   protected EventScheduler scheduler;

   /**
    * This is a handle to the Network object, which controls the
    * simulation as a whole. Having a reference of this objects
    * gives access to some global structures/methods. 
    */
   public Network network;

   /**
    * The node's unique identifier. This is set by the Network
    * object during node creation (at the ScriptParser), and
    * must never be changed.
    */
   protected int networkId;
   
   /**
    * The node's name: a textual identification to allow a correspondence
    * to be made between the data entered at the configuration file
    * and the numeric identifiers used by Simmcast.
    */
   protected String name;
   
   /**
    * An internal thread counter. This is incremented as threads are
    * created and decremented as they finish. The objective is to
    * control the end of the simulation.
    */
   int threadCounter = 0;

   /**
    * The time a thread blocks when it issues a "send packet" operation.
    * This is a property of the node to model processing times.
    */
   // TODO: check these attributes (access methods and uses)
   protected double sendTime;

   /**
    */
   protected double receiveTime;

   /**
    * An array of network ids of neighboring nodes. This is useful
    * to model the usual knowledge a node has about its vicinity.
    */
   // TODO: is this the best way to implement it?
   // Should it be up here at Node or at a subclass?
   protected int[] neighbors = new int[0];

   /*simmcast.engine.Scheduler*/ simmcast.engine.SchedulerInterface simulationScheduler;

   // *****************************************************
   // GETTERS/SETTERS
   // *****************************************************
   
   /**
    * Set node label (added by Ruthiano).
    *
    * @param label_ The label name of a node.
    */
   public void setLabel(String label_) { label = label_; }

   /**
    * Set node color (added by Ruthiano).
    *
    * @param color_ The color name of a node.
    */
   public void setColor(String color_) { color = color_; }

   /**
    * Set node shape (added by Ruthiano).
    *
    * @param shape_ The shape name of a node.
    */
   public void setShape(String shape_) { shape = shape_; }

   /**
    * Returns the node's label as defined in the configuration file.
    * (added by Ruthiano)
    *
    * @return The node's label name.
    */
   public String getLabel() { return label; }

   /**
    * Returns the node's color as defined in the configuration file.
    * (added by Ruthiano)
    *
    * @return The node's color name.
    */
   public String getColor() { return color; }

   /**
    * Returns the node's shape as defined in the configuration file.
    * (added by Ruthiano)
    *
    * @return The node's shape name.
    */
   public String getShape() { return shape; }

   /**
    * Returns the node's unique numeric identifier (also known as
    * network id).
    *
    * @return The node's unique numeric identifier.
    */
   public int getNetworkId()                { return networkId; }

   /**
    * Sets the node's unique numeric identifier. This is set by
    * the Network object during node creation (at the ScriptParser),
    * and must never be changed.
    *
    * @param networkId_ The network id as given by the Network object.
    */
   public void setNetworkId(int networkId_) { networkId = networkId_; }

   /**
    * Returns the node's name as defined in the configuration file.
    * This is a textual identification to allow a correspondence
    * to be made between the data entered at the configuration file
    * and the numeric identifiers used by Simmcast.
    *
    * @return The node's name.
    */
   public String getName()                  { return name; }

   /**
    * Returns the configured time a thread blocks when it issues
    * a "receive packet" operation. This is a property of the node
    * to model processing times.
    */
   public double getReceiveTime()           { return receiveTime; }

   // *****************************************************
   // SCRIPT INIT ROUTINES
   // *****************************************************

   /**
    * Prepare data structures for execution.
    *
    * @param network_ The network object responsible to the
    * simulation this node is a part of.
    * @param name_ The node's name as given by the user in
    * the configuration file.
    */
   public void initialize(/*simmcast.engine.Scheduler*/simmcast.engine.SchedulerInterface scheduler_, Network network_, String name_) {
      simulationScheduler = scheduler_;
      network = network_;
      name = name_;
      receiverQueue = new MultiSenderPacketQueue("RQ");
      receiverMultiQueue = (PacketMultiQueue)receiverQueue;
      paths = new PathTable();
      scheduler = new EventScheduler(this, scheduler_);
      scheduler.launch();
   }

   /**
    * Sets the RQ global limit (combined capacity of all RQs
    * in this node). If the "multi-RQ", when viewed as one, holds
    * a number of packets equal to this limit, then a received
    * packet will be discarded, even if a specific RQ destined to
    * the packet's sender still has available space.
    *
    * @param limit_ The combined capacity of all RQs in this node.
    */
   // TODO: This is a very ugly workaround and must be fixed ASAP
   // Problem - how to fix. Ideally this should be set at the
   // constructor, but we can't force the user to add it to his/her
   // own constructor, can we?
   public void setRQLimit(int limit_) {
      receiverQueue.setSizeLimit(limit_);
   }

   /**
    * Set sending time: delay incurred on this thread on each send()
    * operation. This parameter models the node's processing load
    * on transmission.
    *
    * @param sendTime_ The amount of simulated time units a thread
    * will block when it issues a "send operation".
    */
   public void setSendTime(double sendTime_) {
      sendTime = sendTime_;
   }

   /**
    * Set receive time: delay incurred on this thread on each
    * successful receive() operation. This parameter models the
    * node's processing load on reception.
    *
    * @param receiveTime_ The amount of simualted time units a thread
    * will block when it issues a successful "receive operation"
    * (receive(), tryReceive() when it does not return null...) 
    */
   public void setReceiveTime(double receiveTime_) {
      receiveTime = receiveTime_;
   }

   /**
    * Create a path between this and another node,
    * with unlimited RQ in the other side. Provided
    * for compatibility with abstract simulations,
    * where only the global RQ limit is relevant.
    * If you intend to extend "addPath" in your own
    * subclass, don't use this method. Instead, use
    * the other "addPath". This is a wrapper, your
    * additions will be reflected on calls of this
    * method as well.
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
    */
   public void addPath(NodeInterface destination_,
                       int pathCapacity_,
                       double bandwidth_,
                       RandomStream propagationStream_,
                       double lossRate_)
   {
      addPath(destination_, pathCapacity_, bandwidth_,
              propagationStream_, lossRate_, UNLIMITED);
   }

      //added by Ruthiano... ( --> + color_ )
   public void addPath(NodeInterface destination_,
                       int pathCapacity_,
                       double bandwidth_,
                       RandomStream propagationStream_,
                       double lossRate_,
		       String color_)
   {
      addPath(destination_, pathCapacity_, bandwidth_,
              propagationStream_, lossRate_, UNLIMITED, color_);
   }

   //added by Ruthiano... ( --> + color_, label_ )
   public void addPath(NodeInterface destination_,
                       int pathCapacity_,
                       double bandwidth_,
                       RandomStream propagationStream_,
                       double lossRate_,
		       String color_, 
		       String label_)
   {
      addPath(destination_, pathCapacity_, bandwidth_,
              propagationStream_, lossRate_, UNLIMITED, color_, label_);
   }
   
   /**
    * Create a bidirectional path between this and another node.
    */
   public void addBidirectionalPath(NodeInterface destination_,
                       int pathCapacity_,
                       double bandwidth_,
                       RandomStream propagationStream_,
                       double lossRate_)
   {
      addPath(destination_, pathCapacity_, bandwidth_,
              propagationStream_, lossRate_, UNLIMITED);
      destination_.addPath(this, pathCapacity_, bandwidth_,
	      propagationStream_, lossRate_, UNLIMITED);
   }

   /**
    * Create a path between this and another node,
    * with the given properties: total path capacity, in
    * number of packets; path bandwidth (packet size /
    * simulation time units); a random stream to model
    * propagation delays, a loss rate, and the size in
    * packets of the receiver queue at the other side.
    * If a subclass redefines addPath and does not call
    * this (super) implementation, it must register a
    * remote queue with registerQueueAt.
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
    * @param lossRate_ A rate between 0.0 and 1.0 indicated how
    * many packets (%) are randomically discarded.
    * @param rqLimit_ The maximum size for the specific RQ of the
    * node at the arriving end of the path.
    */
   public void addPath(NodeInterface destination_,
                       int pathCapacity_,
                       double bandwidth_,
                       RandomStream propagationStream_,
                       double lossRate_,
                       int rqLimit_)
   {
      Path path = new Path(this,
                           destination_,
                           pathCapacity_,
                           bandwidth_,
                           propagationStream_,
                           lossRate_,
                           network.randomGenerator);
      registerQueueAt(destination_, rqLimit_);
      paths.addPath(path);
      network.tracer.path(path);
   }
   
   
   //added by Ruthiano...
   public void addPath(NodeInterface destination_,
                       int pathCapacity_,
                       double bandwidth_,
                       RandomStream propagationStream_,
                       double lossRate_,
                       int rqLimit_,
		       String color_)
   {
      Path path = new Path(this,
                           destination_,
                           pathCapacity_,
                           bandwidth_,
                           propagationStream_,
                           lossRate_,
                           network.randomGenerator,
			   color_);

      registerQueueAt(destination_, rqLimit_);
      paths.addPath(path);
      network.tracer.path(path);
   }

   //added by Ruthiano...
   public void addPath(NodeInterface destination_,
                       int pathCapacity_,
                       double bandwidth_,
                       RandomStream propagationStream_,
                       double lossRate_,
                       int rqLimit_,
		       String color_,
		       String label_)
   {
      Path path = new Path(this,
                           destination_,
                           pathCapacity_,
                           bandwidth_,
                           propagationStream_,
                           lossRate_,
                           network.randomGenerator,
			   color_, 
			   label_);

      registerQueueAt(destination_, rqLimit_);
      paths.addPath(path);
      network.tracer.path(path);
   }
   
   // TCP methods added by Lucas A.S.
   public void addTCPPath(Node destination_,
		   double bandwidth_,
		   RandomStream propagationStream_,
		   double offset_)
   {
	   addTCPPath(destination_, bandwidth_, propagationStream_,
			   offset_, UNLIMITED);
   }

   public void addTCPPath(Node destination_,
		   double bandwidth_,
		   RandomStream propagationStream_,
		   double offset_,
		   String color_)
   {
	   addTCPPath(destination_, bandwidth_, propagationStream_,
			   offset_, UNLIMITED, color_);
   }

   public void addTCPPath(Node destination_,
		   double bandwidth_,
		   RandomStream propagationStream_,
		   double offset_,
		   String color_, 
		   String label_)
   {
	   addTCPPath(destination_, bandwidth_, propagationStream_, offset_,
			   UNLIMITED, color_, label_);
   }

   /**
    * Create a bidirectional TCP path between this and another node.
    */
   public void addBidirectionalTCPPath(Node destination_,
		   double bandwidth_,
		   RandomStream propagationStream_,
		   double offset_)
   {
	   addTCPPath(destination_, bandwidth_, propagationStream_,
			   offset_, UNLIMITED);
	   destination_.addTCPPath(this, bandwidth_, propagationStream_,
			   offset_, UNLIMITED);
   }

   /**
    * Same as addPath, but ensures correct packet arrival order.
    * Path capacity is unlimited and loss rate is set to 0.
    * 
    * 
    *
    * @param destination_ The destination node. The created path
    * will connect from this node to the node specified in this
    * parameter.
    * @param bandwidth_ The path's bandwidth. Transmission time
    * of packets sent through this path are calculated as the
    * packet size divided by this bandwidth value. This generated
    * transmission time affects the packet departure time.
    * @param propagationStream_ This statistical stream will control
    * the generation of latency times. These generated times
    * affect the packet arrival time.
    * @param offset_ time between almost simultaneous packets.
    * @param rqLimit_ The maximum size for the specific RQ of the
    * node at the arriving end of the path.
    */
   public void addTCPPath(Node destination_,
		   double bandwidth_,
		   RandomStream propagationStream_,
		   double offset_,
		   int rqLimit_)
   {
	   if (offset_ <= 0) offset_ = 0.001;
			   
	   Path path = new TCPPath(this,
			   destination_,
			   bandwidth_,
			   propagationStream_,
			   offset_,
			   network.randomGenerator);
	   registerQueueAt(destination_, rqLimit_);
	   paths.addPath(path);
	   network.tracer.path(path);
   }

   public void addTCPPath(Node destination_,
		   double bandwidth_,
		   RandomStream propagationStream_,
		   double offset_,
		   int rqLimit_,
		   String color_)
   {
	   if (offset_ <= 0) offset_ = 0.001;
	   
	   Path path = new TCPPath(this,
			   destination_,
			   bandwidth_,
			   propagationStream_,
			   offset_,
			   network.randomGenerator,
			   color_);

	   registerQueueAt(destination_, rqLimit_);
	   paths.addPath(path);
	   network.tracer.path(path);
   }

   public void addTCPPath(Node destination_,
		   double bandwidth_,
		   RandomStream propagationStream_,
		   double offset_,
		   int rqLimit_,
		   String color_,
		   String label_)
   {
	   if (offset_ <= 0) offset_ = 0.001;
	   
	   Path path = new TCPPath(this,
			   destination_,
			   bandwidth_,
			   propagationStream_,
			   offset_,
			   network.randomGenerator,
			   color_, 
			   label_);

	   registerQueueAt(destination_, rqLimit_);
	   paths.addPath(path);
	   network.tracer.path(path);
   }

   /**
    * Register a new receiver queue at some other node.
    * This is one half of the two-part process to create the
    * receiver queues on each node multi-RQ.
    * This method creates a receiver queue and hands it to
    * the node at the "receiving end" of a path. Using
    * addReceiverQueue(), this node inserts the RQ in its
    * own multi-RQ.
    *
    * @param destination_ The node at "receiving end" of a path.
    * This method is called, therefore, at the "sending end".
    * @param rqLimit_ A maximum size for the RQ to be created for
    * this path. It may be the constant UNLIMITED.
    */
   protected void registerQueueAt(NodeInterface destination_, int rqLimit_) {
      PacketQueue queue;
      if (rqLimit_ == UNLIMITED)
         queue = new PacketQueue("RQ");
      else
         queue = new PacketQueue("RQ", rqLimit_);
      destination_.addReceiverQueue(queue, networkId);

      neighbors = appendToArray(neighbors, destination_.getNetworkId());
   }

   /** 
    * Increase an integer array size by one and add an item in the
    * (new) last element.
    * This is a utility function to simulate dynamic insertion into
    * an array. This technique is efficient for arrays that are read
    * very often and written to rarely.
    *
    * @param array_ The array to be extended.
    * @param item_ The integer to add in the end of the array.
    */
   protected int[] appendToArray(int[] array_, int item_) {
      int size = array_.length;
      int[] newArray = new int[size+1];
      System.arraycopy(array_, 0, newArray, 0, size);
      newArray[size] = item_;
      return newArray;
   }

   /**
    * Registers a new receiver queue, to be used with packets
    * originated from a given sender. This method is called
    * by some other node, which creates a queue and passes it
    * along. This is the "other" half of the two-part process
    * to create the receiver queues on each node multi-RQ.
    *
    * @param queue_ The queue to be added to this node's multi-RQ.
    * @param senderNetworkId_ Identification of the node that
    * sent this packet queue.
    */
   public void addReceiverQueue(PacketQueue queue_, int senderNetworkId_) {
      if (receiverMultiQueue != null) {
         receiverMultiQueue.addQueue(queue_, senderNetworkId_);
      }
   }

   // *****************************************************
   // PACKET TX/RX PRIMITIVES
   // *****************************************************

   /**
    * Basic primitive for sending packets.
    * 
    * @param packet_ The packet to be sent. 
    * @param thread_ The thread that requested this operation, 
    * which will block for sendTime time. 
    */
   protected void send(Packet packet_, NodeThread thread_) throws TerminationException {
      try {
         Path receiverPath = null;
         int destination = packet_.getDestination();

         if (sendTime > 0)
            thread_.block(sendTime);
         if (Network.isMulticast(destination)) {
            Packet[] packets = packet_.expandPacket(network);
            for (int i = 0; i < packets.length; i++) {
               destination = packets[i].getDestination();
               receiverPath = paths.findPathTo(destination);
               receiverPath.addPacket(packets[i]);
            }
         } else {
            receiverPath = paths.findPathTo(destination);
            receiverPath.addPacket(packet_);
         }

      } catch (PathNotFoundException e) {
         network.tracer.nodeError(this, "Path not found.");
      }
   }

   /**
    * Attempts to receive a packet from any sender.
    * Returns null if the receiverQueue is empty.
    *
    * @return A received packet, or null if there is no packet available. 
    */
   protected Packet tryReceive() {
      if (receiverQueue.isEmpty())
         return null;
      else {
         Packet packet = null;
            try {
               packet = receiverQueue.dequeue();
            } catch (EmptyQueueException e) { System.err.println(e); }
            network.tracer.move(packet, receiverQueue, Network.UPPER_LAYER);
         return packet;
      }
   }

   /**
    * Attempts to receive a packet from a given sender.
    * Returns null if the sender's receiverQueue is empty.
    *
    * @param sender_ The sender from which a reception should
    * be attempted.
    *
    * @return Returns a packet if there is any packet sent by
    * the requested sender available for consumption, or null
    * if there isn't.
    */
   protected Packet tryReceive(int sender_) throws InvalidIdentifierException {
      try {

         if (receiverMultiQueue.isEmpty(sender_))
            return null;
         else {
            Packet packet = null;
            try {
               packet = receiverMultiQueue.dequeue(sender_);
            } catch (EmptyQueueException e) { System.err.println(e); }
            network.tracer.move(packet, receiverQueue, Network.UPPER_LAYER);
            return packet;
         } 
      
      } catch (InvalidIdentifierException e) {
         // TODO: trace?
         return null;
      }
   }

   // *****************************************************
   // USER CUSTOM CODE
   // *****************************************************

   /**
    * Code for node's initialization, before
    * the simulation is started. This is
    * empty by default, and intended to be
    * augmented by the user.
    */
   public void begin() {
      // to be augmented by the user.
   }

   /**
    * Code for node's finalization, after the
    * simulation has ended. This is
    * empty by default, and intended to be
    * augmented by the user.
    */
   public void end() {
      // to be augmented by the user.
   }

   // *****************************************************
   // THREAD LIFETIME CONTROL
   // *****************************************************

   /**
    * Increments the node's internal thread counter, so
    * that the node can manage its EventScheduler and
    * the Network controller can handle its thread.
    * (see decrementThreadCounter).
    */
   void incrementThreadCounter() {
      threadCounter++;
      network.incrementThreadCounter();
   }

   /**
    * Control the node's internal thread counter, so that
    * its EventScheduler is shut down after the last
    * user-defined thread is cancelled. Information is
    * also passed to the main Network controller, so it
    * can control when the simulation should end.
    */
   void decrementThreadCounter() {
      threadCounter--;
      if (threadCounter == 0) {
         scheduler.abort();
      }
      network.decrementThreadCounter();
   }

   // *****************************************************
   // USER UTILITIES
   // *****************************************************

   /**
    * Returns an array of network ids of neighboring nodes. This
    * represents the usual knowledge a node has about its vicinity.
    * Notice that, for efficiency, a copy of the actual neighbors
    * list is passed, so do not modify it!
    *
    * @return A reference to the list of network ids of all
    * neighbors to this node.
    */
   public int[] getNeighbors() {
      return neighbors;
   }

   /**
    * The node's name. This utility is provided for convenience
    * only. No code should depend on the format returned by
    * this method.
    *
    * @return A textual representation of this node.
    */
   public String toString() {
      return name;
   }

   public EventSchedulerInterface getScheduler()
   {
	   return scheduler;
   }

   public Network getNetwork()
   {
	   return network;
   }
};
