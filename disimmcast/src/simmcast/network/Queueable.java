/*
 * Simmcast - a network simulation framework
 * Queueable.java
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

package simmcast.network;

/**
 * Queue linking handles are stored directly in the
 * queued object. This design enforces that an object will
 * be in at two queues (one "specific" and one "main" queue)
 * at a time.
 *
 * @author Hisham H. Muhammad
 */
public class Queueable {

   // *****************************************************
   // QUEUE LINKING
   // *****************************************************

   /**
    * Handle to the object that comes before this one in the specific queue.
    */
   Queueable front = null;

   /**
    * Handle to the object that comes after this one in the specific queue.
    */
   Queueable back = null;

   /**
    * Handle to the object that comes before this one in the main queue.
    */
   Queueable mainFront = null;

   /**
    * Handle to the object that comes after this one in the main queue.
    */
   Queueable mainBack = null;

}
