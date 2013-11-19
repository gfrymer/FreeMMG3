/*
 * Simmcast - a network simulation framework
 * ImplosionTraceGenerator.java
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

package simmcast.trace;

import simmcast.network.AbstractQueue;
import simmcast.network.Packet;

/**
 * This is an example on how to use the trace subsystem for
 * accounting. Using this simple generator, you can monitor
 * the number of losses caused by buffer overflow at a given
 * node.
 * 
 * @author Hisham H. Muhammad
 */
public class ImplosionTraceGenerator extends TraceGenerator {

   // ************************************************
   // ATTRIBUTES
   // ************************************************

   /**
    * The implosion counter.
    */
   long implosions;

   /**
    * The node which we will be monitoring for implosions.
    */
   int  monitored;

   // ***********************************************
   // GETTERS/SETTERS
   // ***********************************************

   /**
    * Specify which node we will be monitoring.
    */
   public void monitor(int who) { monitored = who; }

   /**
    * Return total of occurred implosions.
    */
   public long getImplosions() { return implosions; }

   // ************************************************
   // TRACE EVENTS
   // ************************************************

   /**
    * The loss callback, specialized to increment a counter on a specific
    * situation: losses at the RQ of a specified node.
    *
    * @param packet_ A reference to the packet that was lost.
    * @param source_ The queue the lost packet left.
    * @param destination_ The queue the lost packet was supposed to reach.
    * @param text_ A textual description of the loss situation.
    */
   public void loss(Packet packet_, AbstractQueue source_, AbstractQueue destination_, String text_) {
      if (destination_.getName().equals("RQ") && packet_.getDestination() == monitored )
         implosions++;
   }
 
}
