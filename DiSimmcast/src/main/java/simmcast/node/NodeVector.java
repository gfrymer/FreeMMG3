/*
 * Simmcast - a network simulation framework
 * NodeVector.java
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

import java.util.Iterator;
import java.util.Vector;

import simmcast.distribution.interfaces.NodeInterface;

/**
 * This is a specialization of the Vector class,
 * in order to reduce type-casting and allow for
 * cleaner code. It also aims to provides basic iteration
 * functions for nodes.
 *
 * @author Hisham H. Muhammad
 */
public class NodeVector extends Vector {

   // *************************************************
   // VECTOR OPERATIONS
   // *************************************************

   /**
    * The casted version of Vector.get().
    *
    * @return The i-th node of this Vector, or null
    * if there are less than i+1 elements.
    */
   public NodeInterface nodeAt(int i_) {
      return (NodeInterface)get(i_);
   }

   /** 
    * The casted version of Vector.add().
    *
    * @param node_ The node to be added to the vector.
    */
   public void addNode(NodeInterface node_) {
      add(node_);
   }

   // ************************************************
   // UTILITY METHODS
   // ************************************************

   /**
    * Obtain a node from this vector, given its network id.
    * If there is no such node in the vector, return null.
    *
    * @param id_ The network id to be searched.
    *
    * @return A reference to the node that corresponds to
    * this network identifier, or null if no node matches
    * this id.
    */
   public NodeInterface getNodeById(int id_) {
      Iterator iter = iterator();
      while (iter.hasNext()) {
         NodeInterface node = (NodeInterface)(iter.next());
         if (node.getNetworkId() == id_)
            return node;
      }
      return null;
   }

}
