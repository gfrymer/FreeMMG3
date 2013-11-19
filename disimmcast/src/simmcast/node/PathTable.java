/*
 * Simmcast - a network simulation framework
 * PathTable.java
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

import java.util.Hashtable;

/**
 * This is a specialization of the Hashtable class,
 * in order to avoid explicit type-casting and allow
 * for cleaner code. This hash is indexed by the
 * network id of the nodes to which the inserted paths
 * point to (ie, the ids of the nodes at the "arriving
 * end" of the path). 
 *
 * @author Hisham H. Muhammad
 */
public class PathTable extends Hashtable {

   // *************************************************
   // TABLE OPERATIONS
   // *************************************************

   /**
    * Insert a path into the table.
    * No special checks are made: the path object is assumed
    * to be non-null, and is indexed by its destination (ie,
    * the host to which it connects to). Therefore, there
    * can not be two paths connecting the same pair of objects
    * in the same orientation.
    *
    * @param path_ The path to be inserted.
    */
   public void addPath(Path path_) {
      // assert (path_ != null);
      put(new Integer(path_.destinationId), path_);
   }

   /**
    * Verifies whether there is a path connecting to a given
    * node, and returns it. If there is no path, a
    * PathNotFoundException is thrown.
    *
    * @param destinationId_ The network id of the node to which
    * our requested path points to.
    * 
    * @return A handle to the requested path.
    *
    * @see simmcast.node.PathNotFoundException
    */
   public Path findPathTo(int destinationId_) throws PathNotFoundException {
      Path path = (Path)(get(new Integer(destinationId_)));
      if (path != null)
         return path;
      else
         throw new PathNotFoundException();
   }

}
