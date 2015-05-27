/*
 * Simmcast - a network simulation framework
 * PacketQueue.java
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
 * This implements a linked queue of packets, following a
 * FIFO policy. This queue may or may not have a limited size.
 *
 * @author Hisham H. Muhammad
 */
public class PacketQueue extends AbstractQueue {

   // *****************************************************
   // ATTRIBUTES
   // *****************************************************

   /**
    * A handle to the queue's first element, a queue-head.
    */
   private QueueHead head;

   /**
    * A handle to the queue's last element.
    */
   private Queueable tail;

   /**
    * A numeric identifier for this PacketQueue.
    * In the cases when it is relevant, it usually represents
    * the networkId of the sender of the packets stored
    * in this queue.
    */
   int id;

   // *****************************************************
   // CONSTRUCTORS
   // *****************************************************

   /**
    * Construct an unlimited packet queue.
    *
    * @param name_ The queue name, thorugh which it is identified in
    * the traces.
    */
   public PacketQueue(String name_) {
      super(name_);
      initialize();
   }

   /**
    * Construct a packet queue, limited by the specified
    * number of packets.
    *
    * @param name_ The queue name, through which it is identified in
    * the traces.
    * @param sizeLimit_ The maximum size to which the queue is
    * allowed to grow.
    */
   public PacketQueue(String name_, int sizeLimit_) {
      super(name_, sizeLimit_);
      initialize();
   }

   /**
    * Setup the queue. This is the part of the object
    * that is common to all constructors.
    */
   private void initialize() {
      head = new QueueHead(this);
      tail = head;
   }

   // *****************************************************
   // QUEUE OPERATIONS
   // *****************************************************

   /**
    * Adds a packet to the end of the queue.
    *
    * @param packet_ The packet to be added.
    */
   public void enqueue(Packet packet_) throws FullQueueException {
      if (packet_.front != null)
         throw new FatalQueueError("Attempt to enqueue an already queued packet.");

      accountEnqueue();

      tail.back = packet_;
      packet_.front = tail;
      packet_.back = null;
      tail = packet_;
   }

   /**
    * Removes the element in the head of the queue, and
    * returns it.
    *
    * @return The removed packet.
    */
   public Packet dequeue() throws EmptyQueueException {
      accountDequeue();

      Packet dequeued = (Packet)head.back;
      head.back = dequeued.back;
      if (head.back == null)
         tail = head;
      else
         head.back.front = head;
      dequeued.front = null;
      dequeued.back = null;
      return dequeued;
   }

   /**
    * Returns a handle to the queue's head element, but does
    * not remove it from the queue.
    *
    * @return A handle to the packet that is the current head
    * of the queue.
    */
   public Packet peek() throws EmptyQueueException {
      if (isEmpty())
         throw new EmptyQueueException();
      else
         return (Packet)head.back;
   }

   // *****************************************************
   // QUEUE IDENTITY
   // *****************************************************

   /**
    * A hash value so the queue can be uniquely identified.
    * This is primarily used by the multiqueue. The use made by
    * multiqueue tends to ensure that the hash code returned
    * here will be unique (the queue id). This hash code won't
    * be unique, however, for all queues in the simulation.
    *
    * @return The hash code (actually implemented to return
    * the queue id).
    */
   public int hashCode() {
      return id;
   }

   /**
    * Simplistic implementation for a test of equality.
    * This test serves the basic purposes of the multiqueue,
    * providing a correct comparison for equality only between
    * queues that are part of the same multiqueue.
    *
    * @param o_ The object to be tested for equality.
    *
    * @return Returns true if it is a packet queue with the
    * same id, false otherwise.
    */
   public boolean equals(Object o_) {
      if (!(o_ instanceof PacketQueue))
         return false;
      return (id == ((PacketQueue)o_).id);
   }

}
