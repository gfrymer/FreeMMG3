/*
 * Simmcast - a network simulation framework
 * DefaultRouterNodeReceiver.java
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
import simmcast.node.TerminationException;

/**
 * This thread is a basic stub of a receiver thread for
 * a router. It allows unicast and multicast algorithms
 * to be plugged in modularly.
 */

public class DefaultRouterNodeReceiver extends RouterNodeThread {

   // *****************************************************
   // ATTRIBUTES
   // *****************************************************

   /**
    * A handle to the router this thread is a part of.
    */

   DefaultRouterNode router;

   /**
    * A flag to indicate termination of the thread's event loop.
    */

   private boolean running;

   /**
    * A multicast routing algorithm.
    */

   RoutingAlgorithmStrategy multicastAlgorithm;

   /**
    * A unicast routing algorithm.
    */

   RoutingAlgorithmStrategy unicastAlgorithm;

   // *****************************************************
   // CONSTRUCTORS
   // *****************************************************

   /**
    * Feed Simmcast's constructor and save a handle of the node.
    */

   public DefaultRouterNodeReceiver(DefaultRouterNode router_) {
      super(router_);
      router = (DefaultRouterNode)router_;
      running = true;
   }

   // *****************************************************
   // GETTERS/SETTERS
   // *****************************************************

   public void setMulticastAlgorithm(RoutingAlgorithmStrategy multicastAlgorithm_) {
      multicastAlgorithm = multicastAlgorithm_;
   }

   public void setUnicastAlgorithm(RoutingAlgorithmStrategy unicastAlgorithm_) {
      unicastAlgorithm = unicastAlgorithm_;
   }

   // *****************************************************
   // SERVICES FOR STRATEGIES
   // *****************************************************

   public void begin() {
      unicastAlgorithm.begin();
      multicastAlgorithm.begin();
   }

   public void end() {
      unicastAlgorithm.end();
      multicastAlgorithm.end();
   }

   // *****************************************************
   // THREAD CUSTOMIZATION
   // *****************************************************

   /**
    * The user can program a policy for packet reception in
    * this thread. By default it performs simply a receive().
    */

   public Packet receptionOperation() throws TerminationException {
      return receive();
   }

   // *****************************************************
   // FORWARDING
   // *****************************************************

   /**
    * This method queries the routing algorithm and
    * performs the actual packet replication and forwarding.
    */
   void forwardPackets(Packet dataPacket_, NetworkPacket previousPacket_) {
      int source = dataPacket_.getSource();
      int destination = dataPacket_.getDestination();
      NetworkPacket routerPacket = previousPacket_;
      int[] nextHops;
      if (router.isMulticast(destination)) {
         nextHops = multicastAlgorithm.getNextHops(routerPacket);
      } else {
         nextHops = unicastAlgorithm.getNextHops(routerPacket);
      }
      if (nextHops.length > 1) {
         // TODO: it may be necessary to break the determinism
         // of this traversal. If it is necessary, this determinism
         // may cause serious problems.
         for (int i = 0; i < nextHops.length; i++) {
            Packet copy = dataPacket_.replicate();
            // TODO: Sending control packets using multicast will fail.
            NetworkPacket routerCopy = router.encapsulate(copy);
            routerCopy.setSource(router.getNetworkId());
            routerCopy.setDestination(nextHops[i]);
            routerCopy.setTTL(routerPacket.getTTL());
            router.deliverPacket(routerCopy);
         }
      } else if (nextHops.length == 1) {
         routerPacket.setSource(router.getNetworkId());
         routerPacket.setDestination(nextHops[0]);
         router.deliverPacket(routerPacket);
      } else {
         // TODO: trace drop
      }
   }

   /**
    * This is the basic execution loop. While the "running"
    * flag is on, it will keep receiving packets and directing
    * them to the proper algorithm strategy.
    */

   public void execute() throws TerminationException {
      NetworkPacket routerPacket;
      Packet hostPacket;

      unicastAlgorithm.onExecute();
      multicastAlgorithm.onExecute();
      while (running) {
         routerPacket = null;
         hostPacket = null;
         Packet receivedPacket = receptionOperation();
         if (receivedPacket instanceof NetworkPacket) {
            routerPacket = (NetworkPacket)receivedPacket;
            int ttl = routerPacket.getTTL();
            if (ttl == 0) {
               // TODO: discard silently or report back to the host (in unicast)?
               continue;
            } else {
               routerPacket.setTTL(ttl - 1);
            }
            hostPacket = (Packet)(receivedPacket.getData());
         } else {
            hostPacket = receivedPacket;
         }
         if (hostPacket.getDestination() == router.getNetworkId()) {
            if (hostPacket instanceof MulticastControlPacket) {
               multicastAlgorithm.handleControlPacket(hostPacket);
               continue;
            } else if (hostPacket instanceof UnicastControlPacket) {
               unicastAlgorithm.handleControlPacket(hostPacket);
               continue;
            }
         }
         if (router.isNeighbor(hostPacket.getDestination())) {
            routerPacket.setDestination(hostPacket.getDestination());
            router.deliverPacket(routerPacket);
         } else {
            forwardPackets(hostPacket, routerPacket);
         }
      }
   }

}
