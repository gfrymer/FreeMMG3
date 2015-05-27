/*
 * Simmcast - a network simulation framework
 * TopologyGenerationException.java
 * Copyright (C) 2001-2003 Guilherme B. Bedin
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

/**
 * A specialized Exception for topology generation. Exceptions
 * of this hierarchy must be explictly catched.
 *
 * @author Guilherme B. Bedin
 */
public class TopologyGenerationException extends Exception {

   /**
    * Implements the default constructor.
    */
   public TopologyGenerationException() {
      super();
   }

   /**
    * Implements the default constructor, with a message.
    *
    * @param msg_ The error message.
    */
   public TopologyGenerationException(String msg_) {
      super(msg_);
   }

}
