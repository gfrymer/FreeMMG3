/*
 * Simmcast - a network simulation framework
 * AbstractQueue.java
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
 * This abstract class defines a set of operations
 * for a queue. The contract between this class and
 * its subclasses is as follows: the subclass is
 * responsible for providing an interface and
 * implementation for the actual queueing operations;
 * this class will take care of the accounting (empty,
 * full, size) as long as the accountEnqueue and
 * accountDequeue functions are properly called by
 * the subclass' implementation.
 *
 * @author Hisham H. Muhammad
 */
abstract public class AbstractQueue {

   // *****************************************************
   // CONSTANTS
   // *****************************************************

   /**
    * If the queue's sizeLimit attribute equals this, then
    * there is no size limit at all (the queue may grow
    * indefinitely as more elements are added).
    */
   static final private int UNLIMITED = -1;

   // *****************************************************
   // ATTRIBUTES
   // *****************************************************

   /**
    * Contains true if there are no elements in the queue.
    */
   private boolean empty;
   
   /**
    * Contains the number of elements in the queue.
    */
   private int size;

   /**
    * Contains the maximum number of elements in the queue,
    * or UNLIMITED if the queue is potentially infinite.
    */
   private int sizeLimit;

   /**
    * Contains the queue textual name.
    */
   private String name;

   // *****************************************************
   // CONSTRUCTORS
   // *****************************************************

   /**
    * Construct an unlimited queue.
    *
    * @param name_ A name through which this queue will be identified
    * in the traces.
    */
   public AbstractQueue(String name_) {
      name = name_;
      empty = true;
      size = 0;
      sizeLimit = UNLIMITED;
   }

   /**
    * Construct a queue, limited by the specified size.
    *
    * @param name_ A name through which this queue will be identified
    * in the traces.
    * @param sizeLimit_ The maximum size that this queue is allowed
    * to reach. Attempts to store at a given time more than this number
    * of objects to the queue will fail.
    *
    * @see simmcast.network.FullQueueException
    */
   public AbstractQueue(String name_, int sizeLimit_) {
      name = name_;
      empty = true;
      size = 0;
      sizeLimit = sizeLimit_;
   }

   // *****************************************************
   // GETTERS/SETTERS
   // *****************************************************

   /**
    * Returns the queue's textual name.
    *
    * @return The queue's name.
    */
   public String getName() { return name; }

   /**
    * Check if the queue is empty.
    *
    * @return Returns true if there are no elements in the queue.
    */
   public boolean isEmpty() { return empty; }

   /**
    * Check the current number of elements in the queue.
    *
    * @return The current number of elements in the queue.
    */
   public int getSize() { return size; }

   /**
    * Define the size limit. Only allow it to be changed if
    * the queue is empty.
    *
    * @param sizeLimit_ The new maximum size.
    */
   // TODO: It was the best solution we could find by now.
   // Something better has to be arranged.
   public void setSizeLimit(int sizeLimit_) {
      // assert?
      if (size == 0)
         sizeLimit = sizeLimit_;
   }

   /**
    * Check if the queue is full.
    *
    * @return Returns true if a limited queue has reached the maximum
    * number of elements.
    */
   public boolean isFull() {
      if (sizeLimit == UNLIMITED) return false;
      else return (size == sizeLimit);
   }

   // *****************************************************
   // ACCOUNTING CONTROL
   // *****************************************************

   /**
    * Updates flags and counters validating an enqueueing.
    * This must be called by the subclass' custom
    * enqueue operation.
    */
   void accountEnqueue() throws FullQueueException {
      if (!empty && size == sizeLimit)
         throw new FullQueueException();
      empty = false;
      size++;
   }

   /**
    * Updates flags and counters validating an dequeueing.
    * This must be called by the subclass' custom
    * enqueue operation.
    */
   void accountDequeue() throws EmptyQueueException {
      if (empty)
         throw new EmptyQueueException();
      size--;
      if (size == 0)
         empty = true;
   }

}
