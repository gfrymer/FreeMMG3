/*
 * Simmcast - a network simulation framework
 * DepartureEventItem.java
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


/**
 * This event symbolizes the departure of a packet from
 * a node. The time the packet spends waiting for this
 * event represents the period it is in the sender's SQ.
 * At the specified time, a packet should be removed
 * from the path held by this item, path processing 
 * should happen on it (delay calculation and perhaps
 * packet drop) and an arrival event should be created
 * (if the packet was not dropped).
 * This packet holds only a handle to the Path object,
 * and not to the packet itself because at this point
 * (processing delay at the sender), no reordering happens,
 * so the event always refers to the packet that is at
 * the head of the queue.
 *
 * @author Hisham H. Muhammad
 */
public class DepartureEventItem extends EventItem {

   // *****************************************************
   // ATTRIBUTES
   // *****************************************************

   /**
    * The path through which the packet should flow.
    */
   Path path;

   // *****************************************************
   // CONSTRUCTORS
   // *****************************************************

   /**
    * Creates the event, calculating proper timings.
    * Note that the event must be created and enqueued
    * in the same simulation time.
    */
   public DepartureEventItem(double absoluteTime_, double relativeTime_, Path path_) {
      super(absoluteTime_, relativeTime_);
      path = path_;
   }

}
