/*
 * Simmcast - a network simulation framework
 * ArrivalEventItem.java
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

import simmcast.network.Packet;
import simmcast.network.PathAccountQueue;

/**
 * This event symbolizes the arrival of a packet at the
 * node. The time the packet spends waiting for this
 * event represents the period it is in the
 * LQ. At the specified time, the packet held by
 * this object is available for the node's queues.
 *
 * @author Hisham H. Muhammad
 */
public class ArrivalEventItem extends EventItem {

   // *****************************************************
   // ATTRIBUTES
   // *****************************************************

   /**
    * A handle to the packet that is arriving.
    */
   Packet packet;
   
   /**
    * A handle to the accountant of the path
    * this packet came from.
    */
   PathAccountQueue pathAccount;

   // *****************************************************
   // CONSTRUCTORS
   // *****************************************************

   /**
    * Creates the event, calculating proper timings.
    * Note that the event must be created and enqueued
    * in the same simulation time.
    *
    * @param relativeTime_ The amount of simulation time from
    * now (ie, counting "now" as zero) when the event should
    * be triggered.
    * @param packet_ The packet this arrival event refers to.
    * @param pathAccount_ The queue resposible for accounting
    * this arrival.
    */
   public ArrivalEventItem(double absoluteTime_, double relativeTime_, Packet packet_, PathAccountQueue pathAccount_) {
      super(absoluteTime_, relativeTime_);
      packet = packet_;
      pathAccount = pathAccount_;
   }

}
