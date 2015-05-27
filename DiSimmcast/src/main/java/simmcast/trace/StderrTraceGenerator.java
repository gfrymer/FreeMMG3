/*
 * Simmcast - a network simulation framework
 * SimmcastTraceGenerator.java
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

import simmcast.network.AbstractQueue;
import simmcast.network.Packet;
import simmcast.node.Node;
import simmcast.node.Path;

/**
 * This trace generator produces a file
 * listing the events occurred in the simulation,
 * using a native Simmcast format.
 */
public class StderrTraceGenerator extends TraceGenerator {

   // *****************************************************
   // CONSTRUCTORS
   // *****************************************************

   /**
    * Construct a trace generator.
    */
   public StderrTraceGenerator() {
      super();
   }

   // *****************************************************
   // TRACE CONTROL
   // *****************************************************

   /**
    * Initialization routine for the tracer, writes a header.
    */
   public void start() {
      System.err.println("Trace started.");
   }

   // *****************************************************
   // EVENTS
   // *****************************************************

   /**
    * Report an error message on the trace, containing the simulation time.
    * The message is echoed to standard error.
    * 
    * @param text_ A textual description of the error. 
    */
   public void error(String text_) {
      if (enabled)
         System.err.println("ERROR "+simulationTime()+text_);
   }

   /**
    * Report a message on the trace, containing the simulation time.
    * 
    * @param text_ Any message you intend to display. 
    */
   public void message(String text_) {
      if (enabled)
         System.err.println("MESSAGE "+simulationTime()+" "+text_);
   }

   /**
    * Report a message on the trace, containing also the simulation
    * time and a node handle.
    * 
    * @param node_ A reference to a node. 
    * @param data_ Any message you intend to display. 
    */
   public void nodeMessage(Node n_, String data_) {
      if (enabled)
         System.err.println("MESSAGE "+simulationTime()+" "+n_.getName()+" "+data_);
   }

   /**
    * Report an error message on the trace, containing also the
    * simulation time and a node handle. The message is echoed
    * to standard error.
    * 
    * @param n_ The node where the error occurred. 
    * @param text_ A textual description of the error. 
    */
   public void nodeError(Node n_, String message_) {
      if (enabled)
         System.err.println("ERROR "+simulationTime()+" "+n_.getName()+" "+message_);
   }

   /**
    * Transfer of a packet between queues.
    * 
    * @param packet_ A reference to the packet that moved.  
    * @param source_ The queue the packet left.  
    * @param destination_ The queue the packet moved to. 
    */
   public void move(Packet packet_, AbstractQueue source_, AbstractQueue destination_) {
      if (enabled) {
         StringBuffer t = new StringBuffer(19);
         t.append(network.simulationTime());
         while (t.length() < 19)
            t.append(' ');
         System.err.print(t+" ");
         System.err.print(" move "+source_.getName()+" "+source_.getSize());
         System.err.print(" => "+destination_.getName()+" "+destination_.getSize());
         System.err.println("\t"+packet_);
      }
   }

   /**
    * Called when a loss happens interfering with the
    * transfer of a packet between queues. The reason
    * for the loss is passed textually.
    *
    * @param packet_ A reference to the packet that was lost.  
    * @param source_ The queue the lost packet left.  
    * @param destination_ The queue the lost packet was supposed to reach.  
    * @param text_ A textual description of the loss situation.  
    */
   public void loss(Packet packet_, AbstractQueue source_, AbstractQueue destination_, String text_) {
      if (enabled) {
         StringBuffer t = new StringBuffer(19);
         t.append(network.simulationTime());
         while (t.length() < 19)
            t.append(' ');
         System.err.print(t+" ");
         System.err.print(" LOSS "+source_.getName()+" "+source_.getSize());
         System.err.print(" => "+destination_.getName()+" "+destination_.getSize());
         System.err.print("\t"+packet_);
         System.err.println(" ### "+text_);
      }
   }

   /**
    * Creation of a node.
    *
    * @param node_ The node that was created.
    */
   public void node(Node node_) {
      if (enabled)
         System.err.println("NODE "+node_.getNetworkId()+" "+node_.getName());
   }

   /**
    * Creation of a path.
    * This method is empty by default.
    *
    * @param path_ The path that was created.
    */
   public void path(Path path_) {
      if (enabled)
         System.err.println("LINK "+path_.getSource().getName()+" "+path_.getDestination().getName());
   }

}
