/*
 * Simmcast - a network simulation framework
 * PacketMultiQueue.java
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

import java.util.Hashtable;

/**
 * <p>
 * This implements a set of packet queues, which can
 * also be viewed as a single queue (the "main queue").
 * All queues (including the main queue) follow a FIFO policy,
 * and they may or may not have a limited size.
 * </p><p>
 * When viewed as a single queue for dequeueing of objects,
 * the objects are removed from the "main queue": that is,
 * the object dequeued is the one that would be dequeued if
 * all insertions to all queues were performed in a single
 * queue. This is called "anonymous dequeueing".
 * </p><p>
 * When an object is inserted in the multiqueue without
 * using a queue id (that is, viewing the multiqueue
 * as a single queue for insertion), the queue to which the
 * object is inserted has to be decided (in this class, this
 * decision is left as an abstract method). If there is no match,
 * it will be inserted in an extra "anonymous queue", from
 * which it can only be removed with anonymous dequeueing.
 * This whole queueing process (which may or may not use the
 * anonymous queue) is called "anonymous queueing".
 * </p>
 *
 * @author Hisham H. Muhammad
 */
// TODO: Consistencies. Those could be assertions since
// this area is performance-critical (Java 1.4, anyone?).
// I just need to double-check later if there are no possible
// weird cases, such as receiving an "enqueue" with no
// queue added, or an indexed operation with an invalid index
// (it is not enough to catch invalid indexes here -- I have
// to make sure sent indexes are always valid: this is internal
// simulator code, not to be dependant on the user's good-will.
public abstract class PacketMultiQueue extends PacketQueue {

   // *****************************************************
   // CONSTANTS
   // *****************************************************

   /**
    * A special value to identify the queue as being unlimited.
    * An unlimited queue can hold a potentially infinite amount
    * of packets (limited, of course, by the computer's memory).
    */
   static final public int UNLIMITED = 0;

   // *****************************************************
   // ATTRIBUTES
   // *****************************************************

   /**
    * A collection of PacketQueue objects, which hold
    * the actual packets.
    */
   Hashtable queues;

   /**
    * Head of the main queue.
    */
   QueueHead mainHead;

   /**
    * Tail of the main queue.
    */
   Queueable mainTail;

   // *****************************************************
   // CONSTRUCTORS
   // *****************************************************

   /**
    * Construct a packet multiqueue without a global size limit.
    *
    * @param name_ The queue's name, as shown in the traces.
    */
   public PacketMultiQueue(String name_) {
      super(name_);
      initialize();
   }

   /**
    * Construct a packet multiqueue with a global size limit.
    * The global size limit is a counter that may be used
    * to limit the total number of packets stored in the
    * whole set of queues. This is different than the actual
    * queue size constraint, set to each queue separately.
    *
    * @param name_ The queue's name, as shown in the traces.
    * @param sizeLimit_ The maximum size the queue is allowed
    * to grow.
    */
   public PacketMultiQueue(String name_, int sizeLimit_) {
      super(name_, sizeLimit_);
      initialize();
   }

   /**
    * Setup the queue. This is the part of the object
    * initialization that is common to all constructors.
    */
   private void initialize() {
      queues = new Hashtable();
      mainHead = new QueueHead(this);
      mainHead.mainBack = null;
      mainTail = mainHead;
      // Anonymous queue.
      // TODO: Must it be called RQ?
      addQueue(new PacketQueue("RQ"), -1);
   }

   // *****************************************************
   // MEMBER MANAGEMENT
   // *****************************************************

   /**
    * Register a new queue member.
    *
    * @param queue_ A handle to a queue, that will be
    * be controlled by this multiqueue.
    * @param id_ The "queue id" through which this queue
    * will be identified from now on.
    */
   public void addQueue(PacketQueue queue_, int id_) {
      queues.put( new Integer(id_), queue_ );
   }

   /**
    * A shortcut to refer to a queue by its id, not by
    * the actual index in the container where it is stored.
    * Notice that since access to the returned queue override
    * the checks from the multiqueue, it can break the multiqueue's
    * consistency, therefore it should only be used for reading!
    *
    * @param id_ The queue id.
    * 
    * @return The handle to the queue, allowing it to be
    * accessed directly, instead of through the multiqueue.
    */
   PacketQueue queue(int id_) {
      PacketQueue q = (PacketQueue)queues.get( new Integer(id_) );
      if (q != null)
         return q;
      else
         throw new InvalidIdentifierException();
   }

   // *****************************************************
   // ANONYMOUS QUEUE OPERATIONS
   // *****************************************************

   /**
    * Adds a packet to the end of a queue, anonymously.
    * Subclasses will have to define their own criteria for 
    * definining how a queue id is extracted from the packet.
    * This is called "anonymous queueing", as explained in the
    * class description.
    *
    * @param packet_ The packet to be queued.
    */
   abstract public void enqueue(Packet packet_) throws FullQueueException;

   /**
    * Removes the element in the head of the "main queue"
    * (all member queues seen as a single queue), and
    * returns it.
    * This is called "anonymous dequeueing", as explained in
    * the class description.
    *
    * @return The dequeued packet.
    */
   public Packet dequeue() throws EmptyQueueException {
      accountDequeue();

      // First, remove it from the main queue,
      // using the "main" pointers.
      Packet dequeued = (Packet)mainHead.mainBack;
      mainHead.mainBack = dequeued.mainBack;
      if (mainHead.mainBack == null)
         mainTail = mainHead;
      else
         mainHead.mainBack.mainFront = mainHead;

      // An object dequeued off the main queue is always
      // the first of its specific queue. Therefore, we
      // can obtain the specific queue it belongs to and
      // ask it to dequeue it from there as well.
      PacketQueue q = (PacketQueue)(((QueueHead)dequeued.front).queue);
      q.dequeue();

      dequeued.mainFront = null;
      dequeued.mainBack = null;
      return dequeued;
   }

   /**
    * Returns a handle to the main queue's head element, but does
    * not remove it from the queue.
    * This is the "anonymous peek".
    * 
    * @return A handle to the peeked element.
    */
   public Packet peek() throws EmptyQueueException {
      if (isEmpty())
         throw new EmptyQueueException();
      else {
         return (Packet)mainHead.mainBack;
      }
   }

   // *****************************************************
   // SPECIFIC QUEUE OPERATIONS
   // *****************************************************

   /**
    * Adds a packet to the end of a specific queue.
    *
    * @param packet_ The packed to be queued.
    * @param id_ The queue id.
    */
   public void enqueue(Packet packet_, int id_) throws FullQueueException, InvalidIdentifierException {
      if (packet_.front != null)
         throw new FatalQueueError("Attempt to enqueue an already queued packet.");

      accountEnqueue();

      // Link in the specific queue.
      queue(id_).enqueue(packet_);

      // Perform the linking in the main queue manually
      mainTail.mainBack = packet_;
      packet_.mainFront = mainTail;
      packet_.mainBack = null;
      mainTail = packet_;
   }

   /**
    * Removes the element in the head of one of the
    * member queues, and returns it.
    *
    * @param id_ The queue id.
    *
    * @return The dequeued packet.
    */
   public Packet dequeue(int id_) throws EmptyQueueException, InvalidIdentifierException {
      accountDequeue();

      // Dequeue it from the specific queue.
      Packet dequeued = queue(id_).dequeue();

      // Fix the links from the main queue.
      Queueable front = dequeued.mainFront;
      Queueable back = dequeued.mainBack;
      front.mainBack = back;
      if (back != null)
         back.mainFront = front;
      else
         if (mainHead.mainBack == null)
            mainTail = mainHead;

      dequeued.mainFront = null;
      dequeued.mainBack = null;
      return dequeued;
   }

   /**
    * Returns a handle to the head element of a member queue,
    * but does not remove it from the queue.
    *
    * @param id_ The queue id.
    *
    * @return A handle to the peeked element.
    */
   public Packet peek(int id_) throws EmptyQueueException, InvalidIdentifierException {
      return queue(id_).peek();
   }

   /**
    * Queries whether a specific queue is empty.
    *
    * @param id_ The queue id.
    *
    * @return Returns true if the queue is empty, false otherwise.
    */
   public boolean isEmpty(int id_) throws InvalidIdentifierException {
      return queue(id_).isEmpty();
   }
}
