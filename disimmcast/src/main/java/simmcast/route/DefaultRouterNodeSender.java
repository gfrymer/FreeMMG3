/*
 * Simmcast - a network simulation framework
 * DefaultRouterNodeSender.java
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
import simmcast.node.NodeThread;
import simmcast.node.TerminationException;

/**
 * This threads sends packets prepared by the receiver
 * thread.
 */

public class DefaultRouterNodeSender extends NodeThread {

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

   // *****************************************************
   // CONSTRUCTORS
   // *****************************************************

   /**
    * Feed Simmcast's constructor and save a handle of the node
    */

   public DefaultRouterNodeSender(DefaultRouterNode router_) {
      super(router_);
      router = (DefaultRouterNode)router_;
      running = true;
   }

   // *****************************************************
   // THREAD CUSTOMIZATION
   // *****************************************************

   public void execute() throws TerminationException {
      Packet packet;
      while (running) {
         packet = router.getNextPacket();
         ((NetworkPacket)packet).setSource(router.getNetworkId());
         send(packet);
      }
   }
}
