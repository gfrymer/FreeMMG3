/*
 * Simmcast - a network simulation framework
 * RouterNode.java
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
import simmcast.network.Network;
import simmcast.network.NetworkPacket;
import simmcast.network.Packet;
import simmcast.route.RouterNodeThread;

/**
 * A RouterNode is responsible for packet forwarding and
 * distribution. It may be connected to an arbitrary number
 * of hosts and routers.
 *
 * Most routing protocols in Simmcast assume bi-directional
 * links: in other words, two Path objects per link),
 * so you should create these in your configuration files.
 * Notice that neighborhood controls provided by this object
 * are based on paths departing from this object. Paths arriving
 * to this object are not computed, but if all links are
 * bi-directional, this will not make any difference.
 *
 * An implementation of a routing architecture in Simmcast
 * consists of two parts: a forwarding scheme and a routing
 * algorithm. The forwarding scheme contains the thread logic
 * for transmission and reception. The routing algorithm
 * provides services for the forwarded, such as deciding to
 * where the forward should send a received packet to.
 * As the routing algorithm may request timers, there is
 * an additional connection between the algorithm and the
 * forwarder: the forwarder should declare a "client thread"
 * and make it available for the node so that the algorithm may
 * attach timers to it.
 *
 * A practical implementation of a routing scheme should also 
 * contain at least one thread that subclasses RouterNodeThread
 * if you intend to make timers available for the routing algorithms.
 * It may also be necessary to add calls to the algorithms' 
 * onExecute() methods at the beginning of the threads' execute()
 * method, as some routing algorithms expects this to be called 
 * at the very beginning of the simulation.
 *
 * @author Hisham H. Muhammad
 */
// TODO: Most issues presented in the last paragraph are workarounds.
// As these are fixed, the documentation should be updated.
public abstract class RouterNode extends Node {

   // *****************************************************
   // ATTRIBUTES
   // *****************************************************

   /**
    * Size of the routing header. Router packet sizes are
    * computed as the sum of this routing header size and
    * the encapsulated packet size.
    */
   protected int headerSize = 0;

   /**
    * This is a list of network ids of the neighboring routers,
    * that is, the routers that this node has paths directly
    * connecting to.
    */
   int[] neighborRouters = new int[0];
   
   /**
    * The thread that will manage the timers that the
    * routing algorithms may need.
    * This must be set by the node at some point.
    * See the class description for details.
    */
   public RouterNodeThread clientThread;

   // *****************************************************
   // GETTERS/SETTERS
   // *****************************************************

   /**
    * Set the routing header size to be added to the
    * encapsulated packet size.
    *
    * @param headerSize_ The value to be added to the size of
    * the encapsulated packet, when generating the size of a
    * router packet at the encapsulate() function.
    */
   public void setHeaderSize(int headerSize_) {
      headerSize = headerSize_;
   }

   // *****************************************************
   // NEIGHBOR CONTROL
   // *****************************************************

   /**
    * This is extended for internal reasons: this guarantees that
    * the list of neighboring routers is up-to-date, as this method
    * is called whenever a path starting from this node is added.
    * See the superclass documentation for this method for more
    * details
    *
    * @param destination_ The node at the "receiving end" of the
    * newly created thread.
    * @param rqLimit_ The maximum size of the RQ at the destination
    * node responsible for receiving packets from this node.
    *
    * @see simmcast.node.Node
    */
   protected void registerQueueAt(Node destination_, int rqLimit_) {
      super.registerQueueAt(destination_, rqLimit_);
      if (destination_ instanceof RouterNode) {
         neighborRouters = appendToArray(neighborRouters, destination_.getNetworkId());
      }
   }

   // *****************************************************
   // UTILITY METHODS
   // *****************************************************

   /**
    * Create a packet containing the given packet.
    *
    * @param hostPacket_ The packet to be encapsulated.
    *
    * @return A new packet, when the packet that was
    * given as a parameter is stored in its data field.
    */
   public NetworkPacket encapsulate(Packet hostPacket_) {
      return new NetworkPacket(networkId,
                              headerSize + hostPacket_.getSize(),
                              hostPacket_);
   }

   /**
    * Find out if there is a path object connecting this
    * node and the node represented by the given id.
    *
    * @param id_ the network id of the node to be checked
    * for neighborhood. No checks are made to verify if
    * id_ is a valid network id for a node.
    *
    * @return Returns true if this node is a neighbor of id_,
    * false otherwhise.
    */
   public boolean isNeighbor(int id_) {
      for (int i = 0; i < neighbors.length; i++) {
         if (neighbors[i] == id_)
            return true;
      }
      return false;
   }

   /**
    * Find out if there is a path object connecting this
    * node and the router node represented by the given id.
    * No check is made to assure the given id_ is really a router.
    *
    * @param id_ the network id of the router to be checked
    * for neighborhood. No checks are made to verify if
    * id_ is really a valid network id of a router.
    *
    * @return Returns true if this node is a router and a
    * neighbor of id_, false otherwhise.
    */
   public boolean isNeighborRouter(int id_) {
      for (int i = 0; i < neighborRouters.length; i++) {
         if (neighborRouters[i] == id_)
            return true;
      }
      return false;
   }

   /**
    * Find out if there is a path object connecting this
    * node and the host node represented by the given id.
    * No check is made to assure if the node is really a host node.
    *
    * @param id_ the network id of the host to be checked
    * for neighborhood. No checks are made to verify if
    * id_ is really a valid network id of a host.
    *
    * @return Returns true if this node is a host and a
    * neighbor of id_, false otherwhise.
    */
   public boolean isNeighborHost(int id_) {
      // TODO: Store another array for hosts?
      return (!isNeighborRouter(id_) && isNeighbor(id_));
   }

   /**
    * Returns the number of routers connected to this
    * node (ie, how many routers are linked to this node
    * through a path object).
    *
    * @return The number of neighbor routers.
    */
   public int neighborRouterCount() {
      return neighborRouters.length;
   }

   /**
    * A list of the network ids of all routers connected to
    * this node (ie, all routers that are linked to this node
    * through a path object).
    *
    * @return A reference to the neighbor routers list. For
    * efficiency, a reference of the node's own list is given,
    * so do not modify it! If you need to do so, create your
    * own copy using System.arraycopy().
    */
   public int[] neighborRouterIds() {
      return neighborRouters;
   }

   /**
    * Same as Network.isMulticast. This proxy function is
    * intended to make the code more readable.
    *
    * @param id_ A network id.
    *
    * @return Returns true if the id_ lies in the address
    * space of multicast groups; returns false otherwise.
    *
    * @see Network#isMulticast
    */
   public boolean isMulticast(int id_) {
      return Network.isMulticast(id_);
   }

   /**
    * Returns the thread declared as the "client thread", to
    * which timers may be attached by the routing algorithm.
    * See the class description for details.
    *
    * @return A handle to the "client thread".
    */
   // TODO: A more elegant solution is seriously required
   // for the whole timer issue.
   public RouterNodeThread getClientThread() {
      return clientThread;
   }

   /**
    * Returns an array containing the network ids of all
    * hosts that are part of the specified multicast group.
    * 
    * @param groupId_ The identifier of the multicast group
    * to be checked for neighboring host members. No check
    * is made to assure that this parameter is a network id
    * of an existing multicast group (or even within the
    * valid multicast address space).
    *
    * @return List of hosts who are members of the group groupId_ 
    * and neighbors (ie, directly connected) to this router.
    */
   public int[] getNeighborHostsInGroup(int groupId_) {
      // TODO: Reduce complexity on this one.
      // It is currently O(n**2), could be 2*(O(n*log n))+O(n)
      int[] groupMembers = network.getGroupMembersById(groupId_);
      int[] neighborsInGroup = new int[neighbors.length];
      int c = 0;
      for (int a = 0; a < neighbors.length; a++) {
         for (int b = 0; b < groupMembers.length; b++) {
	    if (neighbors[a] == groupMembers[b]) {
	       neighborsInGroup[c] = neighbors[a];
	       c++;
	    }
	 }
      }
      int[] result = new int[c];
      System.arraycopy(neighborsInGroup, 0, result, 0, c);
      return result;
   }

   // *****************************************************
   // MEMBERSHIP MANAGEMENT
   // *****************************************************

   /**
    * This method is called whenever a node connected to
    * this router is added to a RouterGroup.  See the
    * RouterGroup class description for details.
    *
    * @param node_ The node that joined the group.
    */
   public void notifyJoin(NodeInterface node_) {
   }

   /**
    * This method is called whenever a node connected to
    * this router is removed from a RouterGroup. See the
    * RouterGroup class description for details.A RouterGroup
    * is an abstract entity, which can be understood as
    * the membership activity requests sent by the upper
    * application layers. The router nodes should use
    * these notifications to keep their own routing tables.
    *
    * @param node_ The node that joined the group.
    */
   public void notifyLeave(NodeInterface node_) {
   }

}
