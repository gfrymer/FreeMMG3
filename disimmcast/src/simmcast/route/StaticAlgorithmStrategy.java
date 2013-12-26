/*
 * Simmcast - a network simulation framework
 * StaticAlgorithmStrategy.java
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

import java.util.Vector;

import simmcast.distribution.interfaces.NodeInterface;
import simmcast.distribution.interfaces.RouterNodeInterface;
import simmcast.network.Network;
import simmcast.network.Packet;
import simmcast.node.Node;
import simmcast.node.RouterNode;

/**
 * The static algorithm strategy handles both unicast and multicast
 * protocols. It provides support for aribitrary topologies without
 * using a proper distributed routing algorithm. This is achieved
 * giving this strategy an artificial, always up-to-date knowledge about
 * the state of the entire network.
 *
 * This way, this algorithm fills routing tables by computing a path
 * algorithm on an automatically updated graph of the simulated network.
 * There are no packet exchanges, and the convergence time is zero. 
 */
public class StaticAlgorithmStrategy extends RoutingAlgorithmStrategy {

   /**
    * A handle to the network object. This algorithm
    * gathers information from it to make centralized decisions
    * about the routing table.
    */
   Network network;

   GraphTraversalAlgorithm gta;

   // *****************************************************
   // CONSTRUCTORS
   // *****************************************************

   /**
    * Construct a static algorithm strategy, with knowledge about
    * the entire network.
    */

   public StaticAlgorithmStrategy(RouterNodeInterface node_, Network network_) {
      super(node_);
      network = network_;
   }

/*
   public StaticAlgorithmStrategy(Node node_) {
      node = node_;
      gta = new ShortestPathTree(network.getNodeCount());
   }
   public void setNetwork(Network network_) {
      network = network_;
   }
*/
   // *****************************************************
   // GRAPH CONTROL
   // *****************************************************

   /**
    * Infer connection information from the nodes, and
    * build a graph from them.
    */
   public void buildGraph() {
      int size = network.getNodeCount();
      gta = new ShortestPathTree(size+1);
      // TODO: Relies on the false assumption that nodes
      // are numbered from 1 to size. This is true if,
      // for example, nodes don't disappear. Currently,
      // this never happens, but may occur in the future.
      // Also, it is never stated in the API that the first
      // node is 1. Using this undocumented information
      // to our own good is WRONG.
//System.err.println("Size: "+size);
      for (int i=1; i <= size; i++) {
         NodeInterface node = network.getNodeById(i);
//System.err.print("Edge: "+node.getNetworkId()+" [ " );
//int[] a = node.getNeighbors();
//for (int j = 0; j < a.length; j++) {
//   System.err.print(""+a[j]+" ");
//}
//System.err.println("]");
         gta.addEdge(node.getNetworkId(), node.getNeighbors());
      }
   }

   public void updateTable(int from_, int to_) {
      Vector neighbors;
      if (gta == null) {
         buildGraph();
      }
      Integer here = new Integer(node.getNetworkId());
      if (Network.isMulticast(to_)) {
         neighbors = new Vector();
         int[] members = network.getGroupMembersById(to_);
         for (int i=0; i < members.length; i++) {
//System.err.println("");
//System.err.println("From: "+from_);
//System.err.println("To: "+to_);
            Vector path = gta.traverse(from_, members[i]);
//System.err.println(path);
            int place = path.indexOf(here);
	    Integer nextHopCandidate = (Integer)(path.get(place+1));
            if (place != -1 && place < path.size() - 1 && neighbors.indexOf(nextHopCandidate) == -1) {
               neighbors.add(nextHopCandidate);
            }
         }
//System.err.println("put "+neighbors+" in table ("+from_+","+to_+")");
//System.err.println("");
         int nlength = neighbors.size();
         if (nlength > 0) {
            int[] neighborsArray = new int[nlength];
            for (int i = 0; i < nlength; i++) {
               neighborsArray[i] = ((Integer)neighbors.get(i)).intValue();
            }
            table.putNeighbors(from_, to_, neighborsArray);
         }
      } else {
         Vector path = gta.traverse(from_, to_);
         int place = path.indexOf(here);
         int next;
         if (place != -1 && place < path.size() - 1) {
            next = ((Integer)path.get(place+1)).intValue();
            int[] next_convertedToArray = new int[1];
            next_convertedToArray[0] = next;
            table.putNeighbors(from_, to_, next_convertedToArray);
         }
      }
   }

   // *****************************************************
   // ALGORITHM INTERFACE
   // *****************************************************

   /**
    * Control packets are never generated, therefore this
    * method is empty.
    */

   public void handleControlPacket(Packet p) {
   }

   /**
    * This is the interface to perform queries to the
    * routing table. It handles multicast and unicast
    * the same way.
    * Returns an array with length = 0 if there are no
    * neighbors to which the packet should be sent.
    */

   public int[] getNextHops(Packet p_) {
      Packet data = (Packet)p_.getData();
      int from = data.getSource();
      int to = data.getDestination();
      int[] neighbors = table.getNeighbors(from, to);
      if (neighbors == null) {
//System.err.println("Will update table");
         updateTable(from, to);
         neighbors = table.getNeighbors(from, to);
//System.err.println(neighbors);
         if (neighbors == null) {
            return new int[0];
         } else {
            return neighbors;
         }
      } // else // commented to avoid compiler error
         // assert (neighbors != null);
         return neighbors;
   }

}
