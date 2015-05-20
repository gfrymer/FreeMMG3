/*
 * Simmcast - a network simulation framework
 * QueueHead.java
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
 * Objects of this class are queue-heads. They exist to
 * be, permanently, the first element of a doubly-linked
 * queue. The first real element of a queue headed by
 * a queue-head is the object that the queue-head points to.
 * Having a queue-head object with a reference of the
 * queue it heads allows the queue reference to be reachable
 * from any object belonging to the queue.
 *
 * @author Hisham H. Muhammad
 */
class QueueHead extends Queueable {

   // *****************************************************
   // ATTRIBUTES
   // *****************************************************

   /**
    * A handle to the queue this object is a head of.
    */
   AbstractQueue queue;

   // *****************************************************
   // CONSTRUCTORS
   // *****************************************************

   /**
    * Initialize the queue-head.
    *
    * @param queue_ A handle to the queue this head is responsible
    * for.
    */
   QueueHead(AbstractQueue queue_) {
      queue = queue_;
      front = null;
      back = null;
      mainFront = null;
      mainBack = null;
   }

}
