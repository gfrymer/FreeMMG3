/*
 * Simmcast - a network simulation framework
 * NodeThread.java
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

import simmcast.engine.TerminatedException;
import simmcast.network.InvalidIdentifierException;
import simmcast.network.Packet;
import simmcast.network.PacketType;

/**
 * NodeThread is the basic unit of execution in Simmcast.
 * Objects of this class are "hosted" by Node objects.
 * Being an extension of JavaSim's SimulationProcess, which is,
 * by the way, an extension of Java's own Thread class,
 * NodeThread offers thread semantics to the programmer.
 * However, method names are changed to avoid clashing with
 * JavaSim's inner workings. The main method is execute(),
 * which acts as a thread's usual run() method.
 *
 * @author Hisham H. Muhammad
 */
abstract public class NodeThread extends simmcast.engine.Process {

   // *****************************************************
   // ATTRIBUTES
   // *****************************************************

   /**
    * A handle for this thread's parent node.
    */
   protected Node node;

   /**
    * A flag to indicate whether the thread is
    * in blocked state. Only the simulator can
    * "block" a thread, the user can only put
    * it to "sleep". The difference is that
    * blocking is prioritary, ie, the
    * user cannot control (sleep/wakeUp) the
    * thread when it is in blocked state.
    */
   private boolean blocked = false;

   /**
    * A flag to indicate whether this thread is "persistent"
    * (daemon thread) or "ephemeral" (regular thread).
    * The simulation will end when all regular threads
    * finish executing. Daemon threads are then killed.
    *
    */
   protected boolean daemon = false;

   /**
    * Indicates whether the thread has started and has not
    * finished yet.
    */
   protected boolean running = false;

   // *****************************************************
   // CONSTRUCTORS
   // *****************************************************

   /**
    * The default constructor is made private to
    * prevent it from being used.
    */
   private NodeThread() {
   }

   /**
    * Constructs a thread object. This has to be
    * augmented in the child class because of
    * restrictions in constructor inheritance in
    * the Java language.
	* (modified by Ruthiano)
    *
    * @param node_ The node this thread is a part of.
    */
   protected NodeThread(Node node_) {
   	  this(node_, "circle", "black", "");
   }

   protected NodeThread(Node node_, String label) {
      this(node_, "circle", "black", label);
   }

   protected NodeThread(Node node_, String shape, String color) {
      this(node_, shape, color, "");
   }

   protected NodeThread(Node node_, String shape, String color, String label) {
      super();
      node = node_;

      node.setColor(color);
      node.setShape(shape);
      node.setLabel(label);

      setName(node.name+"["+getClass()+"]");
   }

   // *****************************************************
   // GETTERS/SETTERS
   // *****************************************************

   /**
    * Indicates whether the thread has started and has not
    * finished yet. The running flag is set right before
    * execute() begins and is reset right after execute() ends.
    * If a thread is sleeping, it is still considered a
    * running thread.
    *
    * @return A flag to indicate whether the thread is in a
    * running state.
    */
   public boolean isRunning() {
      return running;
   }

   // *****************************************************
   // THREAD LIFETIME
   // *****************************************************

   /**
    * Instructs the thread to start execution.
    * This is the method that should be called from, say,
    * the Node object, so that the thread starts execution.
    */
   public void launch() {
      launch(daemon);
   }

   /**
    * Instructs the thread to start execution.
    * This is the method that should be called from, say,
    * the Node object, so that the thread starts execution.
    *
    * @param daemon_ If a thread is flagged as "daemon", then the
    * simulator will not wait for this thread to finish
    * before ending the simulation. Threads that perform "while
    * true" kind of tasks are typically daemon threads.
    */
   public void launch(boolean daemon_) {
      begin();
      if (! daemon_)
         node.incrementThreadCounter();
      setScheduler(node.simulationScheduler);
      daemon = daemon_;
      startProcess();
   }

   /**
    * Performs the actual execution of thread.
    * This is the implementation of the actual run() method
    * from Java's Thread class. It is called internally by
    * JavaSim and should be ignored by the user.
    * To add your execution code to the thread, use execute()
    * instead.
    */
   public void runProcess() {
      running = true;
      try {
         execute();
      } catch (TerminationException e) {
         // TODO: Report termination
      }
      running = false;
      abort();
   }

   /**
    * This is the user-defined code for the thread.
    * This has to be defined by the user.
    */
   abstract public void execute() throws TerminationException;

   /**
    * Instructs the thread to terminate execution.
    */
   public void abort() {
      node.decrementThreadCounter();
      end();
   }

   // *****************************************************
   // SIMULATOR-LEVEL THREAD CONTROL
   // *****************************************************

   /**
    * Instructs the thread to block execution
    * indefinitely (ie., until it is explicitly
    * unblocked).
    * This is a simulator-level method and must
    * not be called by the user.
    */
   void block() throws TerminationException {
      if ( !simulationScheduler.currentProcesses().contains(this) ) {
         System.out.println("Error: "+/*simulationScheduler.currentProcess()+*/" an outsider is trying to block "+this);
         System.out.println("A thread can only block itself.");
         NullPointerException e = new NullPointerException();
         e.printStackTrace();
         return;
      }
      assert blocked == false;
      blocked = true;
      try {
         sleepProcess();
      } catch (TerminatedException e) {
         throw new TerminationException();
      }
   }

   /**
    * Instructs the thread to block execution
    * during a defined amount of simulation time.
    * This is a simulator-level method and must
    * not be called by the user.
    *
    * @param time_ The amount of simulated time a thread should block.
    */
   void block(double time_) throws TerminationException {
      assert blocked == false;
      blocked = true;
      try {
         sleepProcess(time_);
      } catch (TerminatedException e) {
         throw new TerminationException();
      }
      blocked = false;
   }

   /**
    * Instructs the thread to resume execution,
    * returning to unblocked state.
    */
   void unblock() {
      assert blocked == true;
      blocked = false;
      wakeUpProcess();
   }

   // *****************************************************
   // PACKET TX/RX PRIMITIVES
   // *****************************************************

   /**
    * Basic primitive for sending packets.
    *
    * @param packet_ The packet that should be sent.
    */
   public void send(Packet packet_) throws TerminationException {
      node.send(packet_, this);
   }

   /**
    * Creates and sends a packet. This is a shorthand for
    * the send() method, when you are not using a subclassed
    * type of packet.
    *
    * @param to_ The destination field of the to-be-created packet.
    * @param type_ The packet type identifier, allowing this method
    * to be used with different kinds of packets when the distinction
    * is made using composition.
    * @param size_ The simulated packet size, a value to be used in
    * bandwidth consumption calculations.
    */
   public void send(int to_, PacketType type_, int size_) throws TerminationException {
      Packet packet = new Packet(node.networkId, to_, type_, size_);
      node.send(packet, this);
   }

   /**
    * Creates and sends a packet holding a generic object. This
    * is a shorthand for the send() method, when you are not using
    * a subclassed type of packet.
    *
    * @param to_ The destination field of the to-be-created packet.
    * @param type_ The packet type identifier, allowing this method
    * to be used with different kinds of packets when the distinction
    * is made using composition.
    * @param size_ The simulated packet size, a value to be used in
    * bandwidth consumption calculations.
    * @param data_ An arbitrary object, modelling the information 
    * that is carried within the packet.
    */
   public void send(int to_, PacketType type_, int size_, Object data_) throws TerminationException {
      Packet packet = new Packet(node.networkId, to_, type_, size_, data_);
      node.send(packet, this);
   }

   /**
    * Basic primitive for receiving packets arriving
    * from a specific sender. If there are no packets
    * in this sender's RQ in the receiverQueue,
    * blocks the thread's execution until a
    * packet arrives.
    *
    * @param sender_ The network id of the node this thread should
    * wait a packet from.
    *
    * @return The received packet that eventually arrived.
    * This method will never return null, as it blocks the
    * thread until there is effectively data to be received
    * from the specific sender.
    */
   public Packet receive(int sender_) throws InvalidIdentifierException, TerminationException {
      Packet received = node.tryReceive(sender_);
      while (received == null) {
         node.scheduler.wakeOnReceive(this);
         block();
         received = node.tryReceive(sender_);
      }
      if (node.receiveTime > 0)
         block(node.receiveTime);
      return received;
   }

   /**
    * Primitive for receiving packets arriving from
    * a specific sender. If there are no packets in
    * this sender's RQ in the receiverQueue,
    * waits until a packet arrives, or if the time
    * defined by _timeout expires, gives up returning
    * null.
    *
    * @param timeout_ A maximum time to wait for a packet
    * arrival, in simulated time units.
    * @param sender_ The network id of the node this thread should
    * wait a packet from.
    *
    * @return The received packet that eventually arrived,
    * or null if the timeout expired.
    */
   public Packet receive(double timeout_, int sender_) throws InvalidIdentifierException, TerminationException  {

      double waitTime = simulationScheduler.currentTime() + timeout_;
      Packet received = node.tryReceive(sender_);

      if ((received == null) && (simulationScheduler.currentTime() < waitTime))
         node.scheduler.wakeOnReceive(this);
      while ((received == null) && (simulationScheduler.currentTime() < waitTime)) {
         block(waitTime - simulationScheduler.currentTime());
         received = node.tryReceive(sender_);
      }
      if (received == null)
         node.scheduler.giveUpReceiving(this);
      else if (node.receiveTime > 0)
         block(node.receiveTime);

      return received;
   }

   /**
    * Attempts to receive a packet from a specific
    * sender's RQ. Returns null if the receiverQueue is empty.
    *
    * @param sender_ The network id of the node this thread should
    * attempt to receive a packet from.
    *
    * @return The received packet, or null if there was no
    * packet available at the time of this method call.
    */
   public Packet tryReceive(int sender_) throws InvalidIdentifierException, TerminationException {
      // TODO: manage delay (busy, processing time).
      Packet received = node.tryReceive(sender_);
      if (received != null)
         block(node.receiveTime);
      return received;
   }

   /**
    * Basic primitive for receiving packets from any sender.
    * If there are no packets in the receiverQueue,
    * blocks the thread's execution until a
    * packet arrives.
    *
    * @return The received packet that eventually arrived.
    * This method will never return null, as it blocks the
    * thread until there is effectively data to be received.
    */
   public Packet receive() throws TerminationException {
      Packet received = node.tryReceive();
      while (received == null) {
         node.scheduler.wakeOnReceive(this);
         block();
         received = node.tryReceive();
      }
      if (node.receiveTime > 0)
         block(node.receiveTime);
      return received;
   }

   /**
    * Primitive for receiving packets from any sender.
    * If there are no packets in the receiverQueue,
    * waits until a packet arrives, or if the time
    * defined by timeout_ expires, gives up returning
    * null.
    *
    * @param timeout_ A maximum time to wait for a packet
    * arrival, in simulated time units.
    *
    * @return The received packet that eventually arrived,
    * or null if the timeout expired.
    */
   public Packet receive(double timeout_) throws TerminationException {

      double waitTime = simulationScheduler.currentTime() + timeout_;
      Packet received = node.tryReceive();

      if ((received == null) && (simulationScheduler.currentTime() < waitTime))
         node.scheduler.wakeOnReceive(this);
      while ((received == null) && (simulationScheduler.currentTime() < waitTime)) {
         block(waitTime - simulationScheduler.currentTime());
         received = node.tryReceive();
      }
      if (received == null)
         node.scheduler.giveUpReceiving(this);
      else if (node.receiveTime > 0)
         block(node.receiveTime);

      return received;
   }

   /**
    * Attempts to receive a packet from any sender.
    * Returns null if the receiverQueue is empty as
    * a whole (no packets in any of the contained RQ's).
    *
    * @return The received packet, or null if there was no
    * packet available at the time of this method call.
    */
   public Packet tryReceive() throws TerminationException {
      // TODO: manage delay (busy, processing time).
      Packet received = node.tryReceive();
      if (received != null)
         block(node.receiveTime);
      return received;
   }

   // *****************************************************
   // USER CUSTOM CODE
   // *****************************************************

   /**
    * Custom initialization code, may be augmented.
    * Code inserted here will be executed
    * when the launch() method is called, but
    * before the thread actually starts to run.
    */
   public void begin() {
      // to be augmented by the user
   }

   /**
    * Custom termination code, may be augmented.
    * Code inserted here will be executed
    * when the thread is finished (either normally
    * or aborted), after the thread stops.
    */
   public void end() {
      // to be augmented by the user
   }

   // *****************************************************
   // USER-DEFINED EVENT MANAGEMENT
   // *****************************************************

   /**
    * Method to be triggered by events added to
    * the EventScheduler. This, by default,
    * does nothing, and is intended to be augmented.
    *
    * @param message_ An arbitrary object, to be used as
    * a message identifier or a callback parameter.
    * This is used as the message unique id for cancelTimer
    *
    * @see #cancelTimer
    */
   public void onTimer(Object message_) {
      // to be augmented by the user
   }

   /**
    * Schedule an event for posterior execution.
    * The object passed is used as
    * a message identifier. Note that this
    * object is should uniquely identify the
    * event (eg, the event is located performing
    * a search using equals() on this 
    * message identifier).
    *
    * @param relativeTime_ Amount of simulated time in the future from now
    * when the onTimer method should be called with message_ a parameter.
    * @param message_ The parameter to be passed to onTimer when it
    * is called, relativeTime_ simulated-time-units from now.
    */
   public void setTimer(double relativeTime_, Object message_) {
      node.scheduler.scheduleEvent(relativeTime_, this, message_);
   }

   /**
    * Remove a scheduled event.
    *
    * @param message_ The message identifier to look for in the
    * event queue.
    */
   public boolean cancelTimer(Object message_) {
      return node.scheduler.cancelEvent(message_);
   }

   // *****************************************************
   // USER-LEVEL THREAD CONTROL
   // *****************************************************

   /**
    * Instructs the thread to stop execution
    * indefinitely (ie., until it is awaken explicitly
    * by another method).
    */
   public void sleep() throws TerminationException {
      // TODO: if blocked, sleep after it unblocks?
      // OTOH, how can it request to sleep when it is blocked?
      // Is this case even possible?
      if ( !simulationScheduler.currentProcesses().contains(this) ) {
         System.err.println("Only the thread can put itself to sleep");
         return;
      }
      if (! blocked) {
         try {
            sleepProcess();
         } catch (TerminatedException e) {
            throw new TerminationException();
         }
      } else {
         // Must never happen
         assert false: "?SIMMCAST UNDEF'D CASE ERROR";
      }
   }

   /**
    * Instructs the thread to stop execution
    * during a defined amount of simulation time.
    *
    * @param time_ Interval in simulated time units the thread
    * should sleep.
    */
   public void sleep(double time_) throws TerminationException {
      // TODO: if blocked, sleep for the time difference after it unblocks
      // Is this case possible?
      if ( !simulationScheduler.currentProcesses().contains(this) ) {
         System.err.println("Only the thread can put itself to sleep");
         return;
      }
      if (! blocked) {
         try {
            sleepProcess(time_);
         } catch (TerminatedException e) {
            throw new TerminationException();
         }
      } else {
         // Must never happen
         assert false: "?SIMMCAST UNDEF'D CASE ERROR";
      }
   }

   /**
    * Instructs the thread to resume execution
    * if sleeping.
    */
   public void wakeUp() {
      // TODO: if blocked, just cancel eventual pending sleep
      if (! blocked) {
         wakeUpProcess();
      } else {
         // Must never happen
         assert false: "?SIMMCAST UNDEF'D CASE ERROR";
      }
   }

   /**
    * Sleep current thread until this thread ends.
    */
   public void joinThread() throws TerminatedException {
      joinProcess();
   }

   /**
    * Forces a premature abortion of the simulation.
    */
   public void terminateSimulation() {
      node.network.terminateSimulation();
   }

   /**
    * Returns a handle to the current running thread.
    * In JavaSim's simulation model only one thread
    * runs at a time.
    *
    * @return A handle to the NodeThread object which is 
    * currently running.
    */
   public static NodeThread currentNodeThread() {
      return (NodeThread)Thread.currentThread();
   }

}
