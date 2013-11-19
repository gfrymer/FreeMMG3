/*
 * Simmcast - a network simulation framework
 * NodeThreadQueue.java
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

import java.util.Vector;

/**
 * This object implements an infinite queue of NodeThread objects.
 * 
 * @author Hisham H. Muhammad
 */
public class NodeThreadQueue {

   // *****************************************************
   // ATTRIBUTES
   // *****************************************************

   /** 
    * The container that is responsible for the queue
    * elements.
    */
   Vector queue;

   // *****************************************************
   // CONSTRUCTORS
   // *****************************************************

   /** 
    * Initializes a new, empty queue.
    */
   public NodeThreadQueue() {
      queue = new Vector();
   }

   // *****************************************************
   // QUEUE OPERATIONS
   // *****************************************************

   /**
    * Adds a thread to the end of the queue.
    *
    * @param thread_ The thread to be added.
    */
   public void push(NodeThread thread_) {
      queue.add(thread_);
   }

   /**
    * Removes the thread from the head of the queue,
    * returning a handle to it.
    *
    * @return A handle to the thread at the end of the
    * queue, or null if the thread is empty.
    */
   public NodeThread pop() {
      if (! queue.isEmpty())
         return ((NodeThread) queue.remove(0));
      else
         return null;
   }

   /**
    * Returns a handle to the element in the head of the queue,
    * but does not remove it.
    *
    * @return A handle to the thread at the end of the
    * queue, or null if the thread is empty.
    */
   public NodeThread peek() {
      if (! queue.isEmpty())
         return ((NodeThread) queue.firstElement() );
      else
         return null;
   }

}
