/*
 * Simmcast - a network simulation framework
 * EventScheduler.java
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

import java.util.HashSet;
import java.util.Iterator;

import simmcast.distribution.interfaces.EventSchedulerInterface;
import simmcast.engine.TerminatedException;
import simmcast.network.FullQueueException;
import simmcast.network.Packet;
import simmcast.network.PathAccountQueue;

/**
 * This is an implicit thread created for
 * each node, supposed to be only used internally by the simulator.
 * This thread is responsible for the transfer of packets between
 * queues and handling of asynchronous events.
 *
 * @author Hisham H. Muhammad
 */
public final class EventScheduler extends simmcast.engine.Process implements EventSchedulerInterface {

   // *****************************************************
   // ATTRIBUTES
   // *****************************************************

   /**
    * This scheduler's event queue.
    */
   private EventQueue eventQueue;

   /**
    * A handle to the node this scheduler is responsible for.
    */
   private Node node;

   /**
    * List of threads currently waiting to receive a packet.
    */
   private HashSet waitingList;

   /**
    * A flag to internally control thread termination.
    */
   private boolean running;

   // *****************************************************
   // CONSTRUCTORS
   // *****************************************************

   /**
    * Initializes the node's scheduler.
    *
    * @param node_ The node this schedule is responsible for.
    */
   public EventScheduler(Node node_, /*simmcast.engine.Scheduler*/simmcast.distribution.interfaces.SchedulerInterface scheduler_) {
      simulationScheduler = scheduler_;
      node = node_;
      eventQueue = new EventQueue(this);
      waitingList = new HashSet();
      setName(node.name+"[class EventScheduler]");
   }

   // *****************************************************
   // THREAD LIFETIME
   // *****************************************************

   /**
    * Thread execution loop.
    */
   public void runProcess() {
      try {
         while (running || !eventQueue.isEmpty()) {
            if (eventQueue.isEmpty()) {
               block();
            } else {
               manageEvent();
            }
         }
      } catch (TerminatedException e) {
      }
   }

   // *****************************************************
   // SIMULATOR-LEVEL THREAD CONTROL
   // *****************************************************

   /**
    * Instructs the thread to start execution.
    */
   public void launch() {
//      try {

         running = true;
//       this.Activate();
         startProcess();

//      } catch (SimulationException e) { System.out.println(e); }
//        catch (RestartException e)    { System.out.println(e); }
   }

   /**
    * Instructs the thread to terminate execution.
    */
   public void abort() {
      running = false;
      unblock();
   }

   /**
    * Cancels the scheduler undefinitely, until
    * it is explicitly unblocked.
    */
   void block() throws TerminatedException {
      sleepProcess();
   }

   /**
    * Cancels the scheduler's execution for
    * an specified amount of time.
    *
    * @param The absolute amount of simulated time units the thread
    * should block.
    */
   void block(double time_) throws TerminatedException {
      sleepProcess(time_);
   }

   /**
    * Resumes the scheduler's execution.
    */
   void unblock() {
//     try {

//         this.ReActivate();
      wakeUpProcess();

//    } catch (SimulationException e) { System.out.println(e); }
//        catch (RestartException e)    { System.out.println(e); }
   }

   // *****************************************************
   // EVENT MANAGEMENT
   // *****************************************************

   /**
    * The event controller/dispatcher.
    */
   private void manageEvent() throws TerminatedException {

      double time;

      eventQueue.storeHeadState();
      time = eventQueue.getHeadAbsoluteTime() - simulationScheduler.currentTime();

      if (time > 0) {
         block(time);
         return;
      } else if (time < 0) {
         node.network.tracer.nodeError(node, "Processing expired event: time: "+time);
      }

      if (eventQueue.headDidNotChange()) {
         EventItem event = eventQueue.dequeueHead();

         if (event instanceof DepartureEventItem) {
            ((DepartureEventItem)event).path.sendPacket();

         } else if (event instanceof ArrivalEventItem) {
            ArrivalEventItem arrival = ((ArrivalEventItem)event);
            try {
               node.receiverQueue.enqueue(arrival.packet);
               arrival.pathAccount.dequeue();
               node.network.tracer.move(arrival.packet, arrival.pathAccount, node.receiverQueue);
            } catch (FullQueueException e) {
               node.network.tracer.loss(arrival.packet, arrival.pathAccount, node.receiverQueue, "drop: RQ is full");
            }
            Iterator iter = waitingList.iterator();
            while (iter.hasNext())
               ((NodeThread)iter.next()).unblock();
            waitingList.clear();

         } else if (event instanceof UserEventItem) {
            ((UserEventItem)event).run();

         }
      }
      // else: head was changed. Let next run of the
      // "while" loop in the run() method deal with this.

   }

   // *****************************************************
   // INTERFACE FOR PATHS
   // *****************************************************

   /**
    * Registers a packet departure event in the
    * scheduler's event queue. At the specified time,
    * a packet should be removed from the path
    * held by this item and an arrival event
    * should be created (if the packet was not dropped).
    * This method provides scheduler support for
    * the Path class.
    *
    * @param relativeTime_ The amount of simulated time counting
    * from now on (ie, now being zero) when the packet departure
    * should occur.
    * @param path_ The path from which the departing packet should
    * be removed. Notice that packets are added to the path object
    * always in order, since the SQ delay is fixed.
    */
   public void schedulePacketDeparture(double relativeTime_, Path path_) {
      eventQueue.enqueue(new DepartureEventItem(simulationScheduler.currentTime()+relativeTime_, relativeTime_, path_));
   }

   /**
    * Registers a packet arrival event in the
    * scheduler's event queue.
    * At the specified time, the packet held by
    * this event is available for the node's RQ.
    * This method provides scheduler support for
    * the Path class.
    *
    * @param relativeTime_ The amount of simulated time counting
    * from now on (ie, now being zero) when the packet arrival
    * should occur.
    * @param packet_ The packet that will arrive.
    * @param pathAccount_ An abstract accounting queue that
    * should be updated in order to maintain PQ statistics.
    */
   public void schedulePacketArrival(double relativeTime_, Packet packet_, PathAccountQueue pathAccount_) {
      eventQueue.enqueue(new ArrivalEventItem(simulationScheduler.currentTime()+relativeTime_, relativeTime_, packet_, pathAccount_));
   }

   // *****************************************************
   // INTERFACE FOR BLOCKING RECEIVE
   // *****************************************************

   /**
    * Registers a thread to be awaken by the scheduler
    * upon the arrival of a packet. The thread is added
    * to a "waiting list". When a packet is received,
    * all threads that are waiting are awaken; one of
    * them will obtain the packet, the others will 
    * return null. Higher level code then ensures that
    * blocking receive operations will function properly,
    * looping until a reception is successful: this way,
    * this method provides the scheduler support for
    * the node's receive() primitives.
    *
    * @param thread_ The thread that wants to be awaken
    * when a packet arrives.
    */
   void wakeOnReceive(NodeThread thread_) {
      waitingList.add(thread_);
   }

   /**
    * Removes a thread from the packet arrival
    * waiting list. This method provides the
    * scheduler support for the node's receiving
    * timeout.
    *
    * @param thread_ The thread that should stop waiting
    * for a reception.
    */
   void giveUpReceiving(NodeThread thread_) {
      waitingList.remove(thread_);
   }

   // *****************************************************
   // INTERFACE FOR USER EVENTS
   // *****************************************************

   /**
    * Adds an user-defined event to the queue. This method is
    * interfaced through a wrapper in the NodeThread class.
    * A user event represents an invocation of an onEvent()
    * method, passing an arbitrary parameter.
    *
    * @param relativeTime_ The amount of simulated time counting
    * from now on (ie, now being zero) when the event should
    * be triggered.
    * @param thread_ The thread object that contains the onEvent()
    * method that should be triggered in the specified time
    * @param message_ This arbitrary object serves two purposes:
    * it acts as a unique event identifier (to allow, for example,
    * for events to be canceled), and it is passed as a parameter
    * to the onEvent() method that occurss when this event is
    * triggered.
    */
   void scheduleEvent(double relativeTime_, NodeThread thread_, Object message_) {
      eventQueue.enqueue(new UserEventItem(simulationScheduler.currentTime()+relativeTime_, relativeTime_, thread_, message_));
   }

   /**
    * Removes an user-defined event to the queue.
    * This method is interfaced through a
    * wrapper in the NodeThread class.
    *
    * @param message_ An arbitrary object that serves as a
    * unique message identifier.
    */
   boolean cancelEvent(Object message_) {
      return eventQueue.cancelUserEvent(message_);
   }

}
