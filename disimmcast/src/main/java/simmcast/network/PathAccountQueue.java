/*
 * Simmcast - a network simulation framework
 * PathAccountQueue.java
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

package simmcast.network;

/**
 * This is not the path queue, it is only intended to hold
 * status information (such as size) synchronized
 * to the path operations, since paths are not actually
 * implemented as proper queues, but as events in each of the nodes'
 * schedulers.
 *
 * @author Hisham H. Muhammad
 */
public class PathAccountQueue extends AbstractQueue {

   // *****************************************************
   // CONSTRUCTORS
   // *****************************************************

   /**
    * Initialize the unlimited path, using the default
    * name.
    */
   public PathAccountQueue() {
      super("PQ");
   }

   /**
    * Initialize the unlimited path, with a specific name.
    * This is used only in the Network class for the
    * UPPER_LAYER object.
    *
    * @param name_ The given name.
    */
   PathAccountQueue(String name_) {
      super(name_);
   }

   // *****************************************************
   // QUEUE OPERATIONS
   // *****************************************************
   
   /** 
    * Add a packet to the path count.
    * Since this is only a accounting queue, no packet is
    * actually added, therefore, there is no parameter.
    */
   public void enqueue() {
      accountEnqueue();
   }

   /** 
    * Subtract a packet from the path count.
    * Since this is only a accounting queue, no packets are
    * actually stored, therefore, there is no return value.
    */
   public void dequeue() {
      accountDequeue();
   }

}
