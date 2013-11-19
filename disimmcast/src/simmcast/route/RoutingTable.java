/*
 * Simmcast - a network simulation framework
 * RoutingTable.java
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

import java.util.Hashtable;

/**
 * This is a routing table, to be used by RoutingAlgorithmStrategy objects.
 * This is an associative list, where entries have the form
 * (S,D)->(N), that is, associating a tuple containing a source S and a
 * destination D to a list of neighbor nodes N.
 */

public class RoutingTable extends Hashtable {

   /**
    * Retrieve an entry from the routing table.
    */

   public int[] getNeighbors(int from_, int to_) {
      Object ret = get(new RoutingTableTuple(from_, to_));
      return (int[])(ret);
   }

   /**
    * Add or update an entry from the routing table.
    */

   public void putNeighbors(int from_, int to_, int[] neighbors_) {
      RoutingTableTuple key = new RoutingTableTuple(from_, to_);
      if (get(key) != null) {
         remove(key);
      }
      put(key, neighbors_);
   }

}

/**
 * Tuples that act as keys for the hash table that implements
 * the routing table.
 */

class RoutingTableTuple {

   int from;
   int to;

   public RoutingTableTuple(int from_, int to_) {
      from = from_;
      to = to_;
   }

   public int hashCode() {
      return (to * 1000000 + from);
   }

   public boolean equals(Object o_) {
      if (! (o_ instanceof RoutingTableTuple))
         return false;
      RoutingTableTuple rtt = (RoutingTableTuple)o_;
      return (rtt.from == from) && (rtt.to == to);
   }

}
