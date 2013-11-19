/*
 * Simmcast - a network simulation framework
 * ShortestPathTree.java
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

import java.util.Vector;

/**
 * Implements graph traversal following a Shortest Path
 * Tree approach. The vertices are not weighted (that is,
 * they all weigh the same). Consistency is expected
 * in the entered neighbor list so that all vertices are
 * bidirectional: when adding edges (2, {...,10,...}),
 * remember to add (10, {...,2,...}).
 */

public class ShortestPathTree extends GraphTraversalAlgorithm {

   public ShortestPathTree(int nodeCount_) {
      super(nodeCount_);
   }

   /**
    * Traverse the graph using a simplified Shortest Path Tree scheme,
    * where all edges weigh one.
    * <pre>
    * (1) List of shortest paths from any given node to a destination.
    * (2) List of nodes to be traversed next. Start from the destination.
    * (3) Start with the shortest path from dest to dest, which is,
    *     of course, empty.
    * (4) While there is someone in the "traverse next" list:
    *     (5) Pick next item to be traversed.
    *     (6) Check each of its neighbors...
    *         (7) If this neighbor was never checked, or has a
    *             "current shortest path" larger than the one reachable
    *             from this object...
    *             (8) ...set this path as the shortest one
    *             (9) and add it to the list of edges to check.
    * </pre>
    */

   public Vector traverse(int from_, int to_) {
      Vector[] paths = new Vector[graph.length]; // (1)
      Vector next = new Vector(); // (2)
      next.add(new Integer(to_));
      paths[to_] = new Vector();
      paths[to_].add(0, new Integer(to_)); // (3)
      while (next.size() > 0) { // (4)
         int current = ((Integer)next.remove(0)).intValue(); // (5)
         GraphEdge edge = graph[current];
         if (edge != null) {
            for (int i = 0; i < edge.neighbors.length; i++) { // (6)
               int neighbor = edge.neighbors[i];
               Vector p = paths[neighbor];
               if (p == null || p.size() > paths[current].size() + 1) { // (7)
                  Vector pc = paths[current]; // (8)
                  // assertion:
                  if (pc == null) { System.err.println("paths["+current+"] is null"); System.exit(1); }
                  p = new Vector(pc);
                  p.add(0, new Integer(neighbor));
                  paths[neighbor] = p;
                  next.add(new Integer(neighbor)); // (9)
               }
            }
         }
      }
      if (paths[from_] == null)
         paths[from_] = new Vector();
      return paths[from_];
   }

}
