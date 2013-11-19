/*
 * Simmcast - a network simulation framework
 * DefaultRouterNode.java
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

package simmcast.route;

import simmcast.network.EmptyQueueException;
import simmcast.network.Network;
import simmcast.network.NetworkPacket;
import simmcast.network.Packet;
import simmcast.network.PacketQueue;
import simmcast.node.RouterNode;
import simmcast.node.TerminationException;

/**
 * This class implements the default Router architecture.
 * This architecture is based on two threads: a receiver thread,
 * which is responsible for the forwarding logic, having two
 * algorithms attached to it (one for unicast, one for mulitcast),
 * and a sender thread, which passively waits for packets directed
 * from the receiver thread and sends them.
 */
public class DefaultRouterNode extends RouterNode implements PacketOutlet {

   // *****************************************************
   // ATTRIBUTES
   // *****************************************************

   /**
    * A handle to the thread that performs packet reception
    * and prepares packets to be forwarded.
    */
   protected DefaultRouterNodeReceiver receiver;

   /**
    * A handle to the thread that sends packets that were
    * prepared to be forwarded.
    */
   protected DefaultRouterNodeSender sender;

   /**
    * A queue that holds packets that were prepared to be
    * forwarded. This is an internal, infinite queue. It
    * does not appear on the traces, it is just a communication
    * mechanism between receiver (the producer thread) and
    * sender (the consumer thread).
    */
   PacketQueue queue;

   /**
    * This is a temporary holder to the multicast algorithm object
    * that will be defined from the script file and later handed to
    * the receiver thread.
    */
   protected RoutingAlgorithmStrategy multicastAlgorithm;

   /**
    * This is a temporary holder to the unicast algorithm object
    * that will be defined from the script file and later handed to
    * the receiver thread.
    */
   protected RoutingAlgorithmStrategy unicastAlgorithm;

   // *****************************************************
   // CONSTRUCTORS
   // *****************************************************

   /**
    * Construct the node. Not much is done here, since the real
    * initialization is performed by the initialize method.
    */
   public DefaultRouterNode() {
   }

   // *****************************************************
   // CUSTOMIZATION
   // *****************************************************

   /**
    * Select the routing algorithm to be used for multicast transmissions.
    * This method is meant to be called from the script file.
    */
   public void setMulticastAlgorithm(RoutingAlgorithmStrategy algorithm_) {
      multicastAlgorithm = algorithm_;
   }

   /**
    * Select the routing algorithm to be used for unicast transmissions.
    * This method is meant to be called from the script file.
    */
   public void setUnicastAlgorithm(RoutingAlgorithmStrategy algorithm_) {
      unicastAlgorithm = algorithm_;
   }

   // *****************************************************
   // INIT ROUTINES
   // *****************************************************

   public void initialize(simmcast.engine.Scheduler scheduler_, Network network_, String name_) {
      super.initialize(scheduler_, network_, name_);
      queue = new PacketQueue("");
      receiver = new DefaultRouterNodeReceiver(this);
      sender = new DefaultRouterNodeSender(this);
      clientThread = receiver;
   }

   /**
    * Launch receiver and sender threads.
    * This is called automatically by the network object.
    */
   public void begin() {
      multicastAlgorithm.setPacketOutlet(this);
      unicastAlgorithm.setPacketOutlet(this);
      receiver.setMulticastAlgorithm(multicastAlgorithm);
      receiver.setUnicastAlgorithm(unicastAlgorithm);
      receiver.launch(true);
      sender.launch(true);
   }

   // *****************************************************
   // THREAD COMMUNICATION
   // *****************************************************

   /**
    * Feed a packet to the sender thread. This method is
    * called by the receiver thread.
    */
   public void deliverPacket(Packet packet_) {
      queue.enqueue(packet_);
      sender.wakeUp();
   }
   
   /**
    * Transmit a packet, passing through the receiver thread
    * so forwarding is executed.
    */
   public void forwardPacket(NetworkPacket p_) {
      receiver.forwardPackets((Packet)(p_.getData()), p_);
   }

   /**
    * Consumes a packet from the forwarding queue.
    * If there is no packet available, puts the sender
    * to sleep until there is. This method is
    * called by the sender thread.
    */
   public Packet getNextPacket() throws TerminationException {
      Packet packet = null;
      while (packet == null) {
         try {
            packet = queue.dequeue();
         } catch (EmptyQueueException e) {
            sender.sleep();
         }
      }
      return packet;
   }

}
