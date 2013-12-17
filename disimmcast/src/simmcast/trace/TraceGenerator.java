/*
 * Simmcast - a network simulation framework
 * TraceGenerator.java
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

package simmcast.trace;

import simmcast.distribution.interfaces.NodeInterface;
import simmcast.network.AbstractQueue;
import simmcast.network.Network;
import simmcast.network.Packet;
import simmcast.node.Node;
import simmcast.node.Path;

/**
 * The template for a Simmcast trace generator.
 * This allows the user to modify the output of Simmcast,
 * or to associate any other arbitrary code to its
 * events, which could be used, for example, to add
 * custom accounting or to connect its live output
 * to an application (eg, a visualizer).
 *
 * @author Hisham H. Muhammad
 */
abstract public class TraceGenerator {

   // *****************************************************
   // ATTRIBUTES
   // *****************************************************

   /**
    * A flag to enable/disable tracing.
    */
   boolean enabled;
   
   /**
    * A flag to launch the start() routine when
    * the tracer is enabled for the first time.
    */
   // TODO: this is a hack (see enable())   
   boolean started;

   Network network;

   // *****************************************************
   // CONSTRUCTORS
   // *****************************************************

   /**
    * Constructs an initially disabled trace generator.
    */
   public TraceGenerator() {
      enabled = false;
      started = false;
   }

   // *****************************************************
   // GETTERS / SETTERS
   // *****************************************************

   public void setNetwork(Network network_) {
      network = network_;
   }

   // *****************************************************
   // TRACE CONTROL
   // *****************************************************

   /**
    * Default initialization routine expected by the
    * simulator, may be defined by the subclass.
    * This method is empty by default.
    */
   public void start() {}

   /**
    * Default termination routine expected by the
    * simulator, may be defined by the subclass.
    * This method is empty by default.
    */
   public void finish() {}

   /**
    * Enables tracing: event handlers will be called
    * when trace events are triggered. This must be
    * controlled by the subclass verifiying the
    * "enabled" attribute.
    */
   public void enable() {
      enabled = true;
      // TODO: This is a hack because nodes and paths
      // are being traced too early in the process.
      // This will probably be changed at some point.
      if (!started) {
         started = true;
         start();
      }
   }

   /**
    * Disables tracing: triggering trace events will
    * not call the event handlers. This must be
    * controlled by the subclass verifiying the
    * "enabled" attribute.
    */
   public void disable() {
      enabled = false;
   }

   // *****************************************************
   // EVENTS
   // *****************************************************

   /**
    * Report an error message containing the simulation time
    * on standard error.
    *
    * @param text_ A textual description of the error.
    */
   public void error(String text_) {
      System.err.println("ERROR: ["+simulationTime()+"] "+text_);
   }

   /**
    * Report a message on the trace.
    * This method is empty by default.
    *
    * @param text_ Any message you intend to display.
    */
   public void message(String text_) {
   }

   /**
    * Report a message on the trace, containing also
    * a node handle.
    * This method is empty by default.
    *
    * @param node_ A reference to a node.
    * @param data_ Any message you intend to display.
    */
   public void nodeMessage(Node n_, String data_) {
   }

   /**
    * Report an error message on the trace, containing also the
    * simulation time and a node handle, to standard error.
    *
    * @param n_ The node where the error occurred.
    * @param text_ A textual description of the error.
    */
   public void nodeError(Node n_, String message_) {
      System.err.println("ERROR: ["+simulationTime()+"] "+n_.getName()+": "+message_);
   }

   /**
    * Transfer of a packet between queues.
    * This method is empty by default.
    *
    * @param packet_ A reference to the packet that moved. 
    * @param source_ The queue the packet left. 
    * @param destination_ The queue the packet moved to.
    */
   public void move(Packet packet_, AbstractQueue source_, AbstractQueue destination_) {
   }

   /**
    * Called when a loss happens interfering with the
    * transfer of a packet between queues. The reason
    * for the loss is passed textually.
    * This method is empty by default.
    *
    * @param packet_ A reference to the packet that was lost. 
    * @param source_ The queue the lost packet left. 
    * @param destination_ The queue the lost packet was supposed to reach. 
    * @param text_ A textual description of the loss situation. 
    */
   public void loss(Packet packet_, AbstractQueue source_, AbstractQueue destination_, String text_) {
   }

   /**
    * Creation of a node.
    * This method is empty by default.
    *
    * @param node_ The node that was created.
    */
   public void node(NodeInterface node_) {
   }

   /**
    * Creation of a path.
    * This method is empty by default.
    *
    * @param path_ The path that was created.
    */
   public void path(Path path_) {
   }

   // *****************************************************
   // UTILITIES
   // *****************************************************

   /**
    * This method returns the current simulation time,
    * in simulated time units.
    *
    * @return The current simulation time
    */
   public double simulationTime() {
      return network.simulationTime();
   }

}