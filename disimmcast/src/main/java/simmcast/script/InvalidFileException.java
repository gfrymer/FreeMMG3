/*
 * Simmcast - a network simulation framework
 * InvalidFileException.java
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

package simmcast.script;


public class InvalidFileException extends Exception {

   public int lineNumber;
   public Exception original = null;

   InvalidFileException() {
      super();
   }

   InvalidFileException(String S) {
      super(S);
   }

   InvalidFileException(String S, Exception e) {
      super(S);
      original = e;
   }

   public String toString() {
      return getMessage();
   }

   public void printOriginalStackTrace() {
      if (original != null)
         original.printStackTrace();
   }

}
