/*
 * Simmcast - a network simulation framework
 * UserEventItem.java
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
 * This type of event implements the
 * support for user-defined asynchronous events.
 * Calling this object's run() method performs a 
 * callback for the passed thread's onTimer()
 * method, using an user-specified object as
 * a message identifier. Note that this
 * object is should uniquely identify the
 * event (eg, the event is located performing
 * a search using equals() on this
 * message identifier).
 *
 * @author Hisham H. Muhammad
 */
public class UserEventItem extends EventItem {

   // *****************************************************
   // ATTRIBUTES
   // *****************************************************

   /**
    * A handle to the object whose thread onTimer() is
    * to be called.
    */
   private NodeThread threadToCall;
   
   /**
    * The object to pass as a parameter when onTimer() is
    * called.
    */
   Object message;

   // *****************************************************
   // CONSTRUCTORS
   // *****************************************************

   /**
    * Creates the event, calculating proper timings,
    * and setting up the proper data for the callback.
    * Note that the event must be created and enqueued
    * in the same simulation time.
    *
    * @param relativeTime_ The amount of simulated time (counting "now" as zero) when
    * the method should be called.
    * @param threadToCall_ The object that holds the onTimer() method that is to be called.
    * @param message_ The parameter to pass to onTimer() when it is called.
    */
   public UserEventItem(double absoluteTime_, double relativeTime_, NodeThread threadToCall_, Object message_) {
      super(absoluteTime_, relativeTime_);
      threadToCall = threadToCall_;
      message = message_;
   }

   // *****************************************************
   // EVENT MANAGEMENT
   // *****************************************************

   /**
    * The callback interface. This passes the message
    * object to the thread's callback method.
    */
   public void run() {
      threadToCall.onTimer(message);
   }

}
