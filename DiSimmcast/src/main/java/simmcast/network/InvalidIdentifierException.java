/*
 * Simmcast - a network simulation framework
 * InvalidIdentifierException.java
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
 * Issued when an invalid identifier is passed to a
 * queue operation. This can be caused, for example,
 * by a "tryReceive" with an invalid id.
 *
 * @author Hisham H. Muhammad
 */
public class InvalidIdentifierException extends RuntimeException {

   /**
    * Implements the default constructor.
    */
   public InvalidIdentifierException() {
      super();
   }
   
   /**
    * Implements the default constructor with a message.
    *
    * @param m_ The error message.
    */

   public InvalidIdentifierException(String m_) {
      super(m_);
   }
}
