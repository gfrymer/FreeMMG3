/*
 * Simmcast Engine - A Free Discrete-Event Process-Based Simulator
 * Event.java
 * Copyright (C) 2003 Hisham H. Muhammad
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

package simmcast.engine;

/**
 * An event object is a tuple that is composed of
 * a simulation time and a process. This corresponds
 * to a time when a given process should be awaken.
 * 
 * @author Hisham H. Muhammad
 */
class Event {

   /**
    * The time when the process should be awaken.
    */
   public double time;
   
   /**
    * The process that is to be awaken at the event's specified time.
    */
   public int pid;
   
   /**
    * Create an event entry, relating the given time and process.
    *
    * @param time_ The time when the process should be awaken.
    * @param process_ The process that is to be awaken at the event's specified time.
    */
   public Event(double time_, int pid_) {
      time = time_;
      pid = pid_;
   }

   /**
    * Returns the string representation of this event, as a tuple.
    *
    * @return The string representation of this event.
    */
   public String toString() {
      return "<" + time + ", " + pid + ">";
   }

}
