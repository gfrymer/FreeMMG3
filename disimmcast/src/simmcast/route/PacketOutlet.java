/*
 * Simmcast - a network simulation framework
 * PacketOutlet.java
 * Copyright (C) 2002-2003 Hisham H. Muhammad
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

package simmcast.route;

import simmcast.network.NetworkPacket;
import simmcast.network.Packet;

/**
 * An object that implements this interface can serve as an outlet
 * of packets to a routing algorithm. This means that it knows how
 * to process packets the routing algorithm intends to send.
 */
public interface PacketOutlet {

   /**
    * Returns the network id of the router the algorithm is
    * working for.
    */
   public int getNetworkId();

   void deliverPacket(Packet p_);

   void forwardPacket(NetworkPacket p_);

}
