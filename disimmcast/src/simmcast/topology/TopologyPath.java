/*
 * Simmcast - a network simulation framework
 * TopologyPath.java
 * Copyright (C) 2001-2003 Guilherme B. Bedin
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

package simmcast.topology;

/**
 * A "path information object" to be used during the topology generation.
 * This is a temporary data scructure to be used by topology generators.
 *
 * @author Guilherme B. Bedin
 */
public class TopologyPath {

   // *************************************************
   // ATTRIBUTES
   // *************************************************

   /**
    * The node at the departing end of the path.
    */
   public int       from;

   /**
    * The node at the arriving end of the path
    */
   public int       to;

   /**
    * The path's latency.
    */
   public double    delay = 0.0000;

   /**
    * The path's bandwidth.
    */
   public double    bandwidth;

   /**
    * A textual description of the path's "queue type".
    */
   public String    queueType;

   /**
    * The path queue size (that is, its maximum capacity)
    */
   public int       queueSize;

   /**
    * The probability of a packet loss (0.0 >= p >= 1.0) in this path.
    */
   public double    lossRate = 0.0000;

   // *************************************************
   // CONSTRUCTORS
   // *************************************************

   /**
    * Creates a "path information object" to be used during the topology generation.
    */
   TopologyPath() {}

   /**
    * Creates a "path information object" to be used during the topology generation.
    *
    * @param from_ The node at the departing end of the path
    * @param to_ The node at the arriving end of the path
    */
   TopologyPath(int from_, int to_) {
      from = from_;
      to = to_;
   }

   /**
    * Creates a "path information object" to be used during the topology generation.
    *
    * @param from_ The node at the departing end of the path
    * @param to_ The node at the arriving end of the path
    * @param delay_ The path's latency.
    */
   TopologyPath(int from_, int to_, double delay_) {
      from = from_;
      to = to_;
      delay = delay_;
   }

   /**
    * Creates a "path information object" to be used during the topology generation.
    *
    * @param from_ The node at the departing end of the path
    * @param to_ The node at the arriving end of the path
    * @param delay_ The path's latency.
    * @param bandwidth_ The path's bandwidth.
    */
   TopologyPath(int from_, int to_, double delay_, double bandwidth_) {
      from = from_;
      to = to_;
      delay = delay_;
      bandwidth = bandwidth_;
   }

   /**
    * Creates a "path information object" to be used during the topology generation.
    *
    * @param from_ The node at the departing end of the path
    * @param to_ The node at the arriving end of the path
    * @param delay_ The path's latency.
    * @param bandwidth_ The path's bandwidth.
    * @param queueType_ A textual description of the path's "queue type".
    * @param queueSize_ The path queue size (that is, its maximum capacity)
    * @param lossRate_ The probability of a packet loss (0.0 >= p >= 1.0) in this path.
    */
   TopologyPath(int from_, int to_, double delay_, double bandwidth_, String queueType_, int queueSize_, double lossRate_) {
      from = from_;
      to = to_;
      delay = delay_;
      bandwidth = bandwidth_;
      queueType = queueType_;
      queueSize = queueSize_;
      lossRate = lossRate_;
   }

   // *************************************************
   // UTILITIES
   // *************************************************

   /**
    * A textual representation of this path information object.
    * No code should depend on the output format returned by this
    * object.
    */
   public String toString() {
      String st = "[";
      st += from+"->"+to;
      if(delay >= 0.0)
         st += " d:"+delay;
      if(bandwidth > 0.0)
         st += " band:"+bandwidth;
      if(queueType != null)
         st += " queueType:"+queueType;
      if(queueSize > 0)
         st += " queueSize:"+queueSize;
      if(lossRate >= 0.0)
         st += " lossRate:"+lossRate;
      st += "]";
      return st;
   }

}
