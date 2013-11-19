/*
 * Simmcast - a network simulation framework
 * QueueException.java
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
 * A specialized Exception for queue objects. Exceptions
 * of this hierarchy do not need to be explicitly catched.
 * They are defined to aid logging and debugging.
 *
 * @author Hisham H. Muhammad
 */
public class QueueException extends RuntimeException {

   // ****************************************************
   // CONSTRUCTORS
   // ****************************************************

   /**
    * Implements the default constructor.
    */
   public QueueException() {
      super();
   }

   /**
    * Implements the default constructor, with a message.
    * 
    * @param m_ The error message.
    */
   public QueueException(String m_) {
      super(m_);
   }
}
