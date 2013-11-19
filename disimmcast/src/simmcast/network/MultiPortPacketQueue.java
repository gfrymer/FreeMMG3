/*
 * Simmcast - a network simulation framework
 * MultiPortPacketQueue.java
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

import simmcast.node.Node;

/**
 * This implementation of a multi-queue consists of
 * multiple inner queues identified by the packet's
 * port field.
 * See the class description of PacketMultiQueue for
 * more details.
 *
 * @see PacketMultiQueue
 *
 * @author Hisham H. Muhammad
 */
public class MultiPortPacketQueue extends PacketMultiQueue {

   // *************************************************
   // CONSTANTS
   // *************************************************

   /**
    * Identifier for the anonymous RQ, used when non-transport
    * packets are used.
    */
   public static final int ANONYMOUS = -1;

   // *************************************************
   // CONSTRUCTORS
   // *************************************************

   /** 
    * Constructs a multiqueue indexed by the port field. 
    * See the class description for details. 
    * 
    * @param name_ A name for this queue, to identify it in the 
    * traces. 
    * 
    * @see simmcast.network.PacketMultiQueue 
    */
   public MultiPortPacketQueue(String name_) {
      super(name_);
      PacketQueue queue = new PacketQueue("RQ", Node.UNLIMITED);
      addQueue(queue, ANONYMOUS);
   }

   // *************************************************
   // QUEUE OPERATIONS
   // *************************************************
   
   /** 
    * Adds a packet to the end of a queue, anonymously. 
    * If there is a queue for the specific port ("port" field) 
    * of this packet, use that queue. Else, add it to the 
    * anonymous queue. 
    * 
    * @param packet_ The packet to be queued. 
    */
   public void enqueue(Packet packet_) throws FullQueueException {
      try {
         if (packet_.data instanceof TransportPacket) {
            TransportPacket tp = (TransportPacket)packet_.data;
            PacketQueue dummy = queue(tp.port);
            enqueue(packet_, tp.port);
         } else {
            enqueue(packet_, ANONYMOUS);
         }
      } catch (InvalidIdentifierException e1) {
         try {
            enqueue(packet_, -1);
         } catch (InvalidIdentifierException e2) {}
      }
   }

}
