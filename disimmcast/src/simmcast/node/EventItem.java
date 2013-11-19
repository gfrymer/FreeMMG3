/*
 * Simmcast - a network simulation framework
 * EventItem.java
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


/**
 * This is the generic node for elements
 * of the EventQueue.
 *
 * @author Hisham H. Muhammad
 */
public abstract class EventItem {

   // *****************************************************
   // ATTRIBUTES
   // *****************************************************

   /**
    * Absolute simulation time when the event will
    * be triggered. This is set upon object construction
    * as (relative time + simulation time).
    */
   private double absoluteTime;

   /**
    * Amount of time the event will stay on hold between
    * its creation and its triggering.
    */
   private double relativeTime;

   /**
    * Link for queue structure. 
    */
   EventItem link;

   // *****************************************************
   // GETTERS/SETTERS
   // *****************************************************

   /**
    * Returns the absolute simulation time when the event will
    * be triggered.
    *
    * @return The absolute triggering time, in simulated time
    * units.
    */
   double getAbsoluteTime() {
      return absoluteTime;
   }

   /**
    * Returns the relative time that was passed to the
    * object constructor indicating the amount of time
    * the event is to hold before its triggering.
    *
    * @return The relative triggering time, counting the
    * moment the object was created as zero.
    */
   double getRelativeTime() {
      return relativeTime;
   }

   // *****************************************************
   // CONSTRUCTORS
   // *****************************************************

   /**
    * Creates the event, calculating proper timings.
    * Note that the event must be created and enqueued
    * in the same simulation time.
    *
    * @param relativeTime_ When the event should be triggered:
    * the amount of simulated time units, counting the moment
    * the object was created as zero.
    */
   EventItem(double absoluteTime_, double relativeTime_) {
      absoluteTime = absoluteTime_;
      relativeTime = relativeTime_;
      link = null;
   }

}
