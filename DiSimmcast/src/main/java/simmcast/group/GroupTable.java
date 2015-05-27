/*
 * Simmcast - a network simulation framework
 * GroupTable.java
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

package simmcast.group;

import java.util.Hashtable;

import simmcast.distribution.interfaces.GroupInterface;
import simmcast.distribution.interfaces.GroupTableInterface;

/**
 * A specialization of Hashtable to handle Group objects.
 * This is a helper object used by the simulator's kernel.
 * It may also be useful for client code, as routers may
 * want to store tables of their own group objects. The
 * semantics are the same of those from the Hashtable object,
 * but helper functions are provided to use integers as
 * keys and avoid explicit type-casting.
 *
 * @author Hisham H. Muhammad
 */
public class GroupTable extends Hashtable implements GroupTableInterface {

   /**
    * Returns the group bearing this identifier and
    * removes it from the group list, or returns null
    * if no such group was found.
    *
    * @param i_ The group network identifier.
    *
    * @return The removed group object.
    */
   public GroupInterface removeGroup(int i_) {
      return (GroupInterface)remove(new Integer(i_));
   }

   /**
    * Given a group's multicast identifier, it returns
    * the handle for the corresponding group object, or
    * null if no group bearing this identifier was
    * registered.
    *
    * @param i_ the group network identifier.
    *
    * @return A reference to the group object, or null, if not found.
    */
   public GroupInterface getGroupById(int i_) {
      return (GroupInterface)get(new Integer(i_));
   }

   /**
    * Given a group's multicast identifier, it returns
    * a vector of network identifiers representing the
    * members of that group, or null if no group bearing
    * this identifier was registered.
    *
    * @param i_ the group network identifier.
    *
    * @return A copy of the members list of the given group,
    * or null, if not found. Notice that changes in the returned
    * member list do not change the actual composition of the group.
    */
   public int[] getMembersById(int i_) {
      GroupInterface group = (GroupInterface)get(new Integer(i_));
      if (group != null) {
         return group.getNetworkIds();
      } else
         return null;
   }

}
