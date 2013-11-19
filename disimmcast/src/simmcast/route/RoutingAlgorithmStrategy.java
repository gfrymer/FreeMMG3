/*
 * Simmcast - a network simulation framework
 * RoutingAlgorithmStrategy.java
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

import simmcast.network.NetworkPacket;
import simmcast.network.Packet;
import simmcast.node.RouterNode;

/**
 * Pluggable routing protocol. This class holds a
 * routing table and interfaces its queries, and manages
 * control packets to update this table.
 * Each node should have its own instance of this class.
 */

public abstract class RoutingAlgorithmStrategy {

   // *****************************************************
   // ATTRIBUTES
   // *****************************************************

   /**
    * A handle to the routing table, the object that holds
    * the inferred information about packet forwarding rules.
    */

   RoutingTable table = new RoutingTable();

   /**
    * Standard outlet for outgoing packets. The router's
    * outgoing packet queue should be plugged here, so
    * this class can output control packets.
    */

   public PacketOutlet packetOutlet;

   /**
    * The handle to the client thread that will manage the timers
    * from this strategy
    */

   RouterNodeThread thread;
   
   /**
    * Handle to the node to which this strategy is associated.
    * The client thread will be a thread from this node.
    */

   RouterNode node;

   // *****************************************************
   // ATTRIBUTES
   // *****************************************************

   /**
    * Constructs a basic strategy, attached to a node thread.
    * This node thread is responsible for managing the timers requested
    * by this strategy.
    */

   public RoutingAlgorithmStrategy(RouterNode node_) {
      node = node_;
      thread = node_.getClientThread();
   }

   // *****************************************************
   // GETTERS/SETTERS
   // *****************************************************

   /**
    * Sets the standard outlet for outgoing packets.
    * This is called by the router node, before the
    * strategy gets actually used.
    */

   public void setPacketOutlet(PacketOutlet outlet_) {
      packetOutlet = outlet_;
   }

   // *****************************************************
   // PACKET SENDING UTILITIES
   // *****************************************************

   /**
    * This method allows a packet to be sent from the
    * router to any other node of the simulation, be it
    * router or host. Since the packet will flow through
    * the forwarder, there must be some other routing
    * algorithm available that is able to direct the
    * packet to its proper destination. The common case
    * will be to use this message to send unicast packets
    * from a multicast routing algorithm; in this case,
    * the unicast strategy takes care of this packet.
    * Notice that if the packet ends up making use of
    * the same routing algorithm that sent it, a bug in
    * your protocol may generate send() commands
    * indefinitely.
    *
    * @param p_ The packet to be sent.
    * @param ttl_ The packet's "time-to-live": the maximum number of hops
    * this packet is allowed to pass through.
    */
   public void send(Packet p_, int ttl_) {
      NetworkPacket np = new NetworkPacket(p_.getSource(), packetOutlet.getNetworkId(), p_.getSize(), p_, ttl_);
      packetOutlet.forwardPacket(np);
   }

   /**
    * This method is a simplification of the more general
    * send() command, allowing to send packets only to directly
    * connected nodes. This is the common case for most 
    * control packet activity, since it makes it independent
    * of any other protocol. Sending to farther routers using
    * this method is possible, but the handleControlPacket()
    * method will have to perform the hop-by-hop forwarding
    * (which is quite realistic).
    *
    * @param p_ The backet to be sent.
    */
   public void sendToNeighbor(Packet p_) {
      packetOutlet.deliverPacket(p_);
   }

   // *****************************************************
   // TIMER CONTROL
   // *****************************************************

   public void setTimer(double relativeTime_, Object message_) {
      TimerMessageWrapper wrapper = new TimerMessageWrapper(message_, this);
      thread.setTimer(relativeTime_, wrapper);
   }

   public void onTimer(Object message_) {
   }

   // *****************************************************
   // PROTOCOL LOGIC
   // *****************************************************

   /**
    * Code added by the user here will be executed before the
    * simulation starts. It is called by the thread object that
    * hosts this algorithm object.
    * By default, this method does nothing.
    */
   public void begin() {
   }

   public void end() {
   }

   /**
    * Code added by the user here will be executed on the
    * right after the thread that hosts this object starts.
    * That is, unlike begin(), the simulation is running when
    * this method is called. Notice, however, that the thread
    * that controls this algorithm is the one who is responsible
    * for calling this method.
    * By default, this method does nothing.
    */
   public void onExecute() {
   }

   /**
    * Consumption of control packets.
    */

   public abstract void handleControlPacket(Packet p);

   /**
    * This is the interface to perform queries to the
    * routing table. This method determines to which
    * neighbors a packet should be sent to, given that
    * it is currently at the node this algorithm is
    * plugged into and has the indicated "from" and "to" fields.
    * In multicast protocols the array will
    * have zero, one or many elements, while in unicast protocols,
    * the array will have either zero or one element.
    * This method must never return null.
    */

   public abstract int[] getNextHops(Packet p_);

}
