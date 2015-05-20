/*
 * Simmcast - a network simulation framework
 * TopologyGenerator.java
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

package simmcast.topology;

import simmcast.node.HostNode;
import simmcast.node.RouterNode;

/**
 * Simmcast's topology generation architecture.
 * The basic functioning is this:
 * Create an object of a subclass of this in your configuration file,
 * pass it some hosts and routers and allow it to generate the
 * paths connecting them.
 *
 * @author Hisham H. Muhammad
 */
abstract public class TopologyGenerator {

   // *****************************************************
   // ATTRIBUTES
   // *****************************************************

   /**
    * A lock to allow it to generate paths only after it is 
    * completely configured.
    */
   protected boolean configured = false;

   /**
    * The list of routers fed to the generator.
    */
   protected RouterNode[] routers;

   /**
    * The total number of routers as informed by the user.
    */
   protected int declaredRouterCount = 0;

   /**
    * The actual number of routers entered. The generation will
    * be possible only if this and the declared count match.
    */
   protected int routerCount = 0;

   /**
    * The list of hosts fed to the generator.
    */
   protected HostNode[] hosts;

   /**
    * The total number of hosts as informed by the user.
    */
   protected int declaredHostCount = 0;

   /**
    * The actual number of hosts entered. The generation will
    * be possible only if this and the declared count match.
    */
   protected int hostCount = 0;

   // *****************************************************
   // GETTERS/SETTERS
   // *****************************************************

   /**
    * Configures the topology generator, specifying the
    * number of nodes that will be included. This must be
    * set before any router is inserted.
    *
    * @param routers_ The number of routers that will be added.
    * @param hosts_ The number of hosts that will be added.
    */
   public void setSize(int routers_, int hosts_) throws TopologyConfigurationException {

      if (configured) {
         throw new TopologyConfigurationException("Topology was already configured.");
      }

      routers = new RouterNode[routers_];
      hosts = new HostNode[hosts_];
      declaredRouterCount = routers_;
      declaredHostCount = hosts_;
      configured = true;
   }

   // *****************************************************
   // CONFIGURATION OF NODES
   // *****************************************************

   /**
    * Add a router to the list of routers to be part of the topology.
    *
    * @param node_ The added router.
    */
   public void addRouter(RouterNode node_) throws TopologyConfigurationException {
      assert configured == true;
      if (!configured) {
         throw new TopologyConfigurationException("Topology was not yet configured.");
      }
      routers[routerCount] = node_;
      routerCount++;
   }

   /**
    * Add a host to the list of routers to be part of the topology.
    *
    * @param node_ The added host.
    */
   public void addHost(HostNode node_) throws TopologyConfigurationException {
      assert configured == true;
      if (!configured) {
         throw new TopologyConfigurationException("Topology was not yet configured.");
      }

      hosts[hostCount] = node_;
      hostCount++;
   }

   // *****************************************************
   // GENERATION
   // *****************************************************

   /**
    * The generation interface. This method checks for consistency of
    * the configured router and host lists and calls the internal generator
    * function.
    * Once the size of the lists is set and the proper number of elements is
    * add to each of them, call this method.
    */
   public void generate() throws TopologyConfigurationException, TopologyGenerationException {
      if (routerCount != declaredRouterCount) {
         throw new TopologyConfigurationException("Not all declared router nodes were inserted.");
      }
      if(hostCount != declaredHostCount) {
         throw new TopologyConfigurationException("Not all declared host nodes were inserted.");
      }
      generatorFunction();
   }

   /**
    * The internal generation function. Code that implements this
    * function in subclasses can assume all the required data is
    * valid, and take care of the topology generation exclusively.
    */
   abstract protected void generatorFunction() throws TopologyGenerationException;

}
