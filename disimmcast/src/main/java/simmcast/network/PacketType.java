/*
 * Simmcast - a network simulation framework
 * PacketType.java
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
 * Create static objects of this class to represent
 * different packet types in your simulation, if you
 * intend to categorize packets through composition.
 * You can also categorize them with inheritance, which
 * could be considered less scaleable (generating
 * too many subclasses) but more flexible (allowing
 * extra fields for specific packets).
 *
 * @author Hisham H. Muhammad
 */
public class PacketType {

   // *****************************************************
   // CONSTANTS
   // *****************************************************

   /**
    * Convenience type for simulations with a single
    * packet type.
    */
   public static final PacketType DEFAULT = new PacketType("DEFAULT");

   // *****************************************************
   // ATTRIBUTES
   // *****************************************************

   /**
    * A name to be displayed when a packet is converted to
    * a string.
    */
   String name;

   // *****************************************************
   // CONSTRUCTORS
   // *****************************************************

   /**
    * Constructs a type object, giving it a name.
    *
    * @param name_ The name.
    */
   public PacketType(String name_) {
      name = name_;
   }

   // *****************************************************
   // REPRESENTATION
   // *****************************************************
  
   /**
    * Returns the type name, to be used primarily when a packet
    * is converted to a string.
    *
    * @return The string representation of the type.
    */
   public String toString() {
      return name;
   }

   /**
    * Overrides the default method from Object.
    *
    * @param other_ The object to compare with this object.
    * @return true if both are the same type, false otherwise.
    */
   public boolean equals(Object other_) {
       if (!(other_ instanceof PacketType)) {
	   return false;
       } 
       return this.name.equals(((PacketType)other_).name);
   }
}	   
