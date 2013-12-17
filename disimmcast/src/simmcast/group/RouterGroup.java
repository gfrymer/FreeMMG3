/*
 * Simmcast - a network simulation framework
 * RouterGroup.java
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

import simmcast.distribution.interfaces.NodeInterface;
import simmcast.node.HostNode;
import simmcast.node.Node;

/**
 * A RouterGroup is an abstract entity, which can be understood as
 * the membership activity requests sent by the upper application
 * layers. The router nodes should use these notifications to keep
 * their own routing tables.
 */
public class RouterGroup extends Group {

   /**
    * Adds a node to this group, if it is not already a member.
    * A call to this method does not automatically incur in exchange
    * of packets to inform an update of the group state, since the
    * Group is an "abstract" representation of the "ideal" group
    * state. However, the router responsible for the node added
    * to the group is immediately notified, which may lead to package
    * exchange, depending on the implementation of the routing
    * protocol.
    *
    * @param node_ A node reference.
    */
   public void join(NodeInterface node_) {
      super.join(node_);
      ((HostNode)node_).getRouter().notifyJoin(node_);
   }

   /**
    * Removes a node from this group, if it is a member.
    * A call to this method does not automatically incur in exchange
    * of packets to inform an update of the group state, since the
    * Group is an "abstract" representation of the "ideal" group
    * state. However, the router responsible for the node removed
    * from the group is immediately notified (only when the "leave"
    * operation is successful), which may lead to package exchange,
    * depending on the implementation of the routing protocol.
    *
    * @param nodeId_ A handle to the node that was removed.
    * @return true if the removal was successful.
    */
   public boolean leave(NodeInterface node_) {
      boolean left = super.leave(node_);
      if (left)
         ((HostNode)node_).getRouter().notifyLeave(node_);
      return left;
   }

}
