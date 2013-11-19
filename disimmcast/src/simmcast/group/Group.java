/*
 * Simmcast - a network simulation framework
 * Group.java
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

import java.util.Vector;

import simmcast.node.Node;
import simmcast.node.NodeInterface;

/**
 * Objects of this class contain a series of network identifiers,
 * representing a multicast group. Groups are globally defined
 * in a network. "Groups" are abstract entities, and represent the
 * up-to-date representation in any given moment of the ideal
 * composition of a group. For more abstract protocols, this
 * information may be always available. For more realistic ones,
 * no node may have access to this information. This data can then
 * be used, for example, to estimate convergence time, ie, the
 * amount of time between a multicast group changes and a node (or
 * all nodes) becomes aware of the change.
 *
 * @author Hisham H. Muhammad
 */
public class Group implements GroupInterface {

   // *****************************************************
   // ATTRIBUTES
   // *****************************************************

   /**
    * The multicast identifier for the group. This is a unique integer
    * identifier, given by the simulation's Network object. Notice that
    * these network identifiers are chosen from a reserved range of the
    * address space of node network identifiers (like it happens with IP).
    */
   private int networkId;

   /**
    * A flag to ensure that the network identifier is set only once.
    * This attribute acts as a lock for "setNetworkId", to ensure that,
    * albeit public, it is only used once, by the Network object.
    */
   private boolean setNetworkIdOnce = false;

   /**
    * The list of network identifiers from the members of the group.
    * This is an array of node network ids.
    */
   int[] members;

   /**
    * The group name, as defined in the configuration file.
    */
   String name;

   // *****************************************************
   // CONSTRUCTORS
   // *****************************************************

   /**
    * Initialize a group object, containing no members.
    */
   public Group() {
      members = new int[0];
   }

   // *****************************************************
   // GETTERS/SETTERS
   // *****************************************************

   /**
    * Returns the group's multicast network identifier. Notice that
    * these network identifiers are chosen from a reserved range of the
    * address space of node network identifiers (like it happens with IP).
    * No other assumptions should be made by the client code about the
    * organization of network ids. This restriction is so that the address
    * space can be expanded/reorganized in the future if necessary.
    *
    * @return The group's network identifier.
    */
   public int getNetworkId() { return networkId; }

   /**
    * Sets the group's multicast network identifier. This method must
    * not be called by client code. It is called only once (and then
    * locked) by the simulation's Network object as a group is created
    * in the configuration file.
    *
    * @param networkId_ The network identifier given by the Network.
    */
   public void setNetworkId(int networkId_) {
      if (! setNetworkIdOnce) {
         networkId = networkId_;
         setNetworkIdOnce = true;
      } else {
         System.err.println("Group's networkId cannot be reset.");
         System.exit(1);
      }
   }

   /**
    * A shorthand to set the group's members list. This is
    * an optimization for ScriptParser and is not intended
    * to be used as a primary means of controlling a group.
    * Use join and leave instead.
    *
    * @param members_ A vector of members
    *
    * @deprecated Changes in the configuration file and the
    * internal structure of ScriptParser made this method
    * unnecessary. Use join() instead.
    */
   public void setMembers(Vector members_) {
      int count = members_.size();
      members = new int[count];
      for (int i = 0; i < count; i++) {
         members[i] = ((Integer)members_.get(i)).intValue();
      }
   }
   
   /**
    * Set the group name. This is set automatically when a
    * group is created from within the configuration file.
    *
    * @param name_ The group name.
    */
   public void setName(String name_) {
      name = name_;
   }

   /**
    * Obtain the group name, usually the name the group is
    * referred to inside the configuration file.
    *
    * @return The group name, or null if the name was never set.
    * Groups created through the configuration file always have
    * their names set.
    */
   public String getName() {
      return name;
   }

   // *****************************************************
   // UTILITY FUNCTIONS
   // *****************************************************

   /**
    * Returns the network identifier of the n-th element of the
    * group. This is a simple interface for members list access.
    * If you need to traverse, you may consider obtaining
    * a copy of the members array with getNetworkIds().
    * For efficiency, no check is made for parameter overflow.
    * You should obtain the size with size() and perform the
    * check yourself if needed.
    *
    * @return The network id of the n'th group member.
    * @see #getNetworkIds
    * @see #size
    */
   public int elementAt(int n) {
      return members[n];
   }

   /**
    * Search for a node identifier in the members list.
    * The semantics is similar to that of Java's Vector object.
    *
    * @param nodeId_ The network id of the searched node.
    *
    * @return The position of the identifier in the members
    * list, or -1 if the node is not a member of this group.
    */
   public int indexOf(int nodeId_) {
      for (int i = 0; i < members.length; i++) {
         if (members[i] == nodeId_) {
            return i;
         }
      }
      return -1;
   }

   /**
    * Returns the number of members in this group.
    *
    * @return The size of the group.
    */
   public int size() {
      return members.length;
   }

   /**
    * Adds a node to this group, if it is not already a member.
    * If the node is already a member, nothing happens.
    * A call to this method does not automatically incur in exchange
    * of packets to inform an update of the group state, since the
    * Group is an "abstract" representation of the "ideal" group
    * state. To more abstract simulations that ignore routing, this
    * may be sufficient. For more details, see the class description.
    *
    * @param node_ A node reference.
    */
   public void join(int nodeId) {
      // TODO: attempt to join a group the node is already in. Report in trace?
      if (indexOf(nodeId) == -1) {
         int l = members.length;
         int[] oldMembers = members;
         members = new int[l + 1];
         System.arraycopy(oldMembers, 0, members, 0, oldMembers.length);
         members[l] = nodeId;
      }
      // TODO: For orthogonality, join operations could return a boolean
      // on failure similarly to the leave() method.
   }

   public void join(NodeInterface node_) {
	      // TODO: attempt to join a group the node is already in. Report in trace?
      int nodeId = node_.getNetworkId();
      join(nodeId);
   }
   /**
    * Removes a node from this group, if it is a member.
    * A call to this method does not automatically incur in exchange
    * of packets to inform an update of the group state, since the
    * Group is an "abstract" representation of the "ideal" group
    * state. To more abstract simulations that ignore routing, this
    * may be sufficient. For more details, see the class description.
    *
    * @param nodeId_ A handle to the node that was removed.
    * @return Returns true if the removal was successful.
    */
   public boolean leave(int nodeId) {
      boolean removed = false;
      if (indexOf(nodeId) != -1) {
         int l = members.length;
         int[] oldMembers = members;
         members = new int[l - 1];
         int j = 0;
         for (int i = 0; i < l; i++) {
            if (oldMembers[i] != nodeId) {
               members[j] = oldMembers[i];
               j++;
            } else {
               removed = true;
            }
         }
      }
      return removed;
   }

   public boolean leave(NodeInterface node_) {
	  int nodeId = node_.getNetworkId();
	  return leave(nodeId);
   }

   /**
    * Returns an int array with all group members.
    * This array is a copy of the members list, and is provided
    * in case of extensive traversal of the list is required, as
    * direct access may be more time-efficient than elementAt().
    * For simple accesses, elementAt() may be more space-efficient.
    *
    * @return An array containing the network identifiers of all
    * group members.
    *
    * @see #elementAt
    */
   public int[] getNetworkIds() {
      int netIds[] = new int[members.length];
      System.arraycopy(members, 0, netIds, 0, netIds.length);
      return netIds;
   }

   /**
    * Returns a string representation of this group.
    * This is provided as a debugging aid only. Client simulation
    * code should not rely on the format of the returned information
    * as it may change/improve in future versions.
    *
    * @return A string representation of the group's members list.
    */
   public String toString() {
     return members.toString();
   }
}
