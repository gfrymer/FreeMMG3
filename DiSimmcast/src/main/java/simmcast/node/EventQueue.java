/*
 * Simmcast - a network simulation framework
 * EventQueue.java
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
 * An object of this class holds the queue of events used
 * by the node's event scheduler. Insertions to this queue
 * maintain events temporally ordered.
 * The head state is especially important because it reports
 * on the next event to be processed.
 *
 * @author Hisham H. Muhammad
 */
public class EventQueue {

   // *************************************************
   // ATTRIBUTES
   // *************************************************

   /**
    * Handle of the first element of the queue.
    */
   EventItem head;

   /**
    * A temporary handle where the head state is stored.
    * Used by methods that check whether the state of the
    * queue head has changed or not.
    */
   EventItem backingStore;

   /**
    * A handle to the event scheduler this object works for.
    */
   private EventScheduler scheduler;

   // **************************************************
   // CONSTRUCTORS
   // **************************************************

   /**
    * Initializes an empty queue.
    *
    * @param scheduler_ The scheduler this queue works for.
    */
   public EventQueue(EventScheduler scheduler_) {
      head = null;
      backingStore = null;
      scheduler = scheduler_;
   }

   // **************************************************
   // GETTERS/SETTERS
   // **************************************************

   /**
    * Obtain the time the event at the head should
    * be triggered, in absolute simualted time units.
    *
    * @return The absoute triggering time of the head event.
    */
   double getHeadAbsoluteTime() {
      return head.getAbsoluteTime();
   }

   /**
    * Obtain the time the event at the head should
    * be triggered, in relative simualted time units,
    * counting the moment the event was created as zero.
    * Notice that's not counting from the moment
    * this method was called.
    *
    * @return The relative triggering time of the head event
    * upon event creation.
    */
   double getHeadRelativeTime() {
      return head.getRelativeTime();
   }
   /**
    * Check for emptiness.
    *
    * @return Returns true if there is no element in this
    * queue, false otherwise.
    */
   boolean isEmpty() {
      return (head == null);
   }

   // **************************************************
   // HEAD STATE CONTROL
   // **************************************************

   /**
    * Save a handle of the head into a temporary
    * variable, in order to compare it later.
    * The queue is not modified.
    */
   void storeHeadState() {
      backingStore = head;
   }

   /**
    * Compare the current head with the event that
    * was temporarily stored by storeHeadState().
    * The queue is not modified.
    *
    * @return Returns true if the head is the same event
    * as it was the last time storeHeadState() was called,
    * false otherwise.
    */
   boolean headDidNotChange() {
      return (backingStore == head);
   }

   // **************************************************
   // HIGH-LEVEL QUEUE OPERATIONS
   // **************************************************

   /**
    * This is the generic method for queue insertion.
    * As events are inserted, the queue is maintained ordered
    * by absolute triggering time.
    * If the head is modified, the scheduler will be informed
    * of this change.
    *
    * @param event_ The event to be inserted.
    */
   public void enqueue(EventItem event_) {
      if ( (head != null) && (event_.getAbsoluteTime() >= head.getAbsoluteTime()) )
         insertItem(event_);
      else
         insertHead(event_);
   }

   /**
    * Cancels an enqueued user-defined (onEvent()-type) event.
    *
    * @param message_ The object that serves as a message identifier for the
    * search event.
    *
    * @return Returns true if the event was found and cancelled,
    * false otherwise (ie, the event was not found).
    */
   public boolean cancelUserEvent(Object message_) {
      if (head == null)
         return false;

      // A sketch of the algorithm:
      // (1) If the head is the user event we're looking for,
      // (2) Then unlink the head, report the change to the scheduler and exit successfully.
      // (3) Else...
      // (4)    Loop through the queue of events until we find the user event we're looking for,
      //        or reach the end of the queue.
      // (5)    If we reached the end,
      // (6)    Then exit with failure.
      // (7)    Else unlink the event and exit successfully.

      if ( (head instanceof UserEventItem)
            && ((UserEventItem)head).message.equals(message_) ) // (1)
      {
         head = head.link;
         scheduler.unblock();
         return true; // (2)
      } else { // (3)
         EventItem prev = head;
         EventItem walk = head.link;
         while ( (walk != null)
            && ( (!(walk instanceof UserEventItem))
                 || (! ((UserEventItem)walk).message.equals(message_)) ) // (4)
         ) {
            prev = walk;
            walk = walk.link;
         }
         if (walk == null) { // (5)
            return false; // (6)
         } else {
            prev.link = walk.link;
            return true; // (7)
         }
      }
   }

   /**
    * Removes the element at the head of the queue.
    * No checks are made to ensure the queue is not empty.
    *
    * @return The element that was the head of the queue.
    */
   EventItem dequeueHead() {
      // assert (head != null);
      EventItem prev = head;
      head = head.link;

      prev.link = null;

      return prev;
   }

   // *************************************************
   // LOW-LEVEL QUEUE OPERATIONS
   // *************************************************

   /**
    * Inserts an item in the queue (not in the head).
    * As events are inserted, the queue is maintained ordered
    * by absolute triggering time. For efficiency, there is
    * a separate method, insertHead(), for the cases where
    * the item to be inserted should be the first item.
    * Notice that inserting an element that should be the new
    * head with this method will not work. If you are unsure
    * whether your element should be the head or not, use
    * enqueue().
    *
    * @param event_ The event to be inserted.
    */
   private void insertItem(EventItem event_) {
      double time = event_.getAbsoluteTime();
      EventItem prev = head;
      EventItem walk = head.link;
      while ((walk != null) && (time > walk.getAbsoluteTime())) {
         prev = walk;
         walk = walk.link;
      }
      prev.link  = event_;
      event_.link = walk;
   }

   /**
    * Inserts an item as the queue's head (ie, as the new first
    * item of the queue). The scheduler is informed of this
    * addition.
    *
    * @param event_ The event to be inserted before the head of
    * the queue
    */
   private void insertHead(EventItem event_) {
      event_.link = head;
      head = event_;
      // TODO: Ugly but it works. Or does it?
      if (Thread.currentThread() != scheduler)
         scheduler.unblock();
   }

}
