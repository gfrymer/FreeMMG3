/*
 * Simmcast - a network simulation framework
 * GraphTraversalAlgorithm.java
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
 * Base class for a family of graph traversal algorithms.
 *
 * @author Hisham H. Muhammad
 */
public abstract class GraphTraversalAlgorithm {

   /**
    * A graph is declared as a set of edges (each edge object contains the
    * set of neighboring edges, from which the vertex set can be derived).
    */
   protected GraphEdge[] graph;

   /**
    * The number of edges.
    */
   int edgeCount;

   /**
    * Declare a new graph, with a given number of yet unfilled edges.
    *
    * @param edgeCount_ The intended number of edges.
    */
   public GraphTraversalAlgorithm(int edgeCount_) {
      graph = new GraphEdge[edgeCount_];
      edgeCount = edgeCount_;
   }

   /**
    * Specify a given edge. The given identifier must be non-negative
    * and less than the number of edges specified in the constructor.
    * If an edge with the same id was previously defined, it is replaced.
    *
    * @param id_ The edge identifier.
    * @param neighbors_ The neighboring nodes.
    */
   public void addEdge(int id_, int[] neighbors_) {
      // assert (id_ >= 0 && id_ < edgeCount_);
      graph[id_] = new GraphEdge(id_, neighbors_);
   }

   /**
    * The implementation of the graph traversal algorithm.
    * This method executes the algorithm, returning a vector
    * enumerating the path of edges between the source and
    * destination edges.
    *
    * @param from_ The source edge.
    * @param to_ The destination edge.
    * @return A list of the identifiers (as Integer objects) that
    * describe, in order, the traversal between the source and
    * destination edges.
    */
   // TODO: Documentation fix: is the returned set inclusive?
   public abstract Vector traverse(int from_, int to_);

}
