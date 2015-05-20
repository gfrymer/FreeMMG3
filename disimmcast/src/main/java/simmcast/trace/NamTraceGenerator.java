/*
 * Simmcast - a network simulation framework
 * NamTraceGenerator.java
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

import java.util.Vector;

import simmcast.network.AbstractQueue;
import simmcast.network.Packet;
import simmcast.node.Node;
import simmcast.node.Path;

//temporary... (added by Ruthiano)
//(display label links in the future...)
class Elements
{
	public String label;
	public int source, destination;
};

/**
 * This trace generator produces a file
 * listing the events occurred in the simulation,
 * using the NAM format.
 *
 * @author Hisham H. Muhammad
 */
public class NamTraceGenerator extends FileTraceGenerator {

   // *****************************************************
   // ATTRIBUTES
   // *****************************************************

   float linkDelay;
   Elements e;
   Vector v = new Vector();

   // *****************************************************
   // CONSTRUCTORS
   // *****************************************************

   public NamTraceGenerator() {
      super();
   }

   // *****************************************************
   // GETTERS/SETTERS
   // *****************************************************

   public void setLinkDelay(float delay_) {
      linkDelay = delay_;
   }

   // *****************************************************
   // TRACE CONTROL
   // *****************************************************

   /**
    * Initialization routine for the tracer, writes a header.
    */
   public void start() {
      super.start();
      if (enabled) {
         println("V -t * -v 1.0a5 -a 0");
         println("A -t * -n 1 -p 0 -o 0xffffffff -c 31 -a 1");
         println("A -t * -h 1 -m 2147483647 -s 0");
      }
   }
   
   public void finish() {
      super.finish();
   }

   // *****************************************************
   // EVENTS
   // *****************************************************

   /**
    * Transfer of a packet between queues.
    */
   public void move(Packet packet_, AbstractQueue source_, AbstractQueue destination_) {
      if (enabled) {

         double time = simulationTime();
         int source = packet_.getSource();
         int destination = packet_.getDestination();
         long size = packet_.getSize();
         long seq = packet_.getSeq();
         
         String type = ""+source_.getName()+destination_.getName();

         if (type.equals("SQPQ")) {
            println("h -t "+time+" -e "+size+" -s "+source+" -d "+destination+" -c "+packet_.getSeq() );
         } else if (type.equals("PQRQ")) {
            println("r -t "+time+" -e "+size+" -s "+source+" -d "+destination+" -c "+packet_.getSeq() );
         } else
            return; // other events are ignored.
      }
   }
   
   /**
    * NAM does its own processing for losses, so it is not
    * needed to report it explicitly.
    */
   public void loss(Packet packet_, AbstractQueue source_, AbstractQueue destination_, String text_) {
      if (enabled) {

         double time = simulationTime();
         int source = packet_.getSource();
         int destination = packet_.getDestination();
         long size = packet_.getSize();
         long seq = packet_.getSeq();
         
         String type = ""+source_.getName()+destination_.getName();

         println("#LOSS -t "+time+" -e "+size+" -s "+source+" -d "+destination+" -c "+packet_.getSeq() );
      }
   }

   /**
    * Creation of a node.
    */
   public void node(Node node_) {
      if (enabled) {
         print("n -t * -a "+node_.getNetworkId()+" -s "+node_.getNetworkId());
         //print(" -S UP -v circle -c black -i black");
         print(" -S UP -v " + node_.getShape() + " -c " + node_.getColor());
		 print(" -i " + node_.getColor());
		 
		 //modifying label state:
         println(" -S DLABEL -l " + node_.getLabel());
      }
   }

   /**
    * Creation of a path.
    */
   public void path(Path path_) {
      if (enabled) {
		e = new Elements();
		e.source = path_.getSource().getNetworkId();
		e.destination = path_.getDestination().getNetworkId();
		e.label = path_.getLabel();
		v.add(e);
         
		 println("l -t * -s "+path_.getSource().getNetworkId()+
                       " -d "+path_.getDestination().getNetworkId()+
                 " -S UP -r "+path_.getBandwidth()+
                       " -D "+linkDelay+
                       " -c black");
      
	  
	  }
   }

   /**
    * Write label links (added by Ruthiano).
    */
   public void WriteLabelLinks() {
      if (enabled) {
         for(int i=0; i < v.size(); i++)
	 {
		e = (Elements) v.elementAt(i);
		if (e.label != "") {
		   println("l -t 0 -s "  + e.source + " -d " +
		           e.destination + " -S DLABEL -l " +
			   e.label);
                }
	 }
      }
   } //method


}
