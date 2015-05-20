/*
 * Simmcast - a network simulation framework
 * MacroPreprocessor.java
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

import java.util.Vector;

/**
 * The macro preprocessor used by the parser of the configuration
 * file.
 *
 * @author Hisham H. Muhammad
 */
public class MacroPreprocessor {

   // **********************************************
   // ATTRIBUTES
   // **********************************************

   /**
    * The list of macro names.
    */
   private Vector names = null;

   /**
    * The list of text strings the macros correspond to.
    */
   private Vector texts = null;

   /**
    * Creates a new table of macros.
    */
   public MacroPreprocessor() {
      names = new Vector();
      texts = new Vector();
   }

   /**
    * Adds an entry to the table of macros.
    * If a macro with the specified name already exists,
    * replace it.
    *
    * @param name_ The macro name
    * @param text_ The text it corresponds to
    */
   public void defineMacro(String name_, String text_) {
      int p = names.indexOf(name_);
      if (p == -1) {
         names.addElement(name_);
         texts.addElement(text_);
      } else {
         texts.set(p, text_);
      }
   }

   /**
    * Return the input string with all macros replaced.
    * The replacement is not recursive (ie, macros within
    * macros are not replaced). This is optimized due
    * to the fact that in Simmcast macros start with
    * MACRO_SYMBOL.
    *
    * @param in_ The input string.
    *
    * @return The string, with all macros replaced.
    */
   public String preprocessLine(String in_) {

      // Simmcast-optimized preprocessing:

      int inLength = in_.length();
      if (inLength > 1) {
         int skip = in_.indexOf(ScriptParser.MACRO_SYMBOL);
         if (skip != -1 && skip < inLength)
            return in_.substring(0,skip) + simmcastRecursivePreprocess(in_.substring(skip));
      }
      return in_;

   }

   /**
    * Return the input string with all macros replaced.
    * The replacement is not recursive (ie, macros within
    * macros are not replaced).
    *
    * @param in_ The input string.
    *
    * @return The string, with all macros replaced.
    */
   public String genericPreprocessLine (String in_) {

      // Generic preprocessing:

      return genericRecursivePreprocess(in_);

   }

   /**
    * Generic recursive algorithm for non-recursive
    * macro substitution (macros within macros are not
    * replaced, so there is no chance of deadlock).
    *
    * @param in_ The input string.
    *
    * @return The string, with all macros replaced.
    */
   public String genericRecursivePreprocess(String in_) {
      int nameCount = names.size();
      int remain;
      String before = "";
      String after = "";

      for (int m = 0; m < nameCount; m++) {
         String name;
         int l;

         name = (String)(names.elementAt(m));
         l = name.length();
         if ( in_.regionMatches(0, name, 0, l) ) {
            if (l < in_.length())
               after = genericRecursivePreprocess(in_.substring(l));
            return texts.elementAt(m) + after;
         }
      }

      if (in_.length() > 1) {
         after = genericRecursivePreprocess(in_.substring(1));
         return in_.substring(0,1) + after;
      } else
         return in_;
   }

   /**
    * An adaptation of the genericRecursivePreprocess
    * algorithm, taking advantage that in Simmcast, all
    * macros start with the MACRO_SYMBOL character.
    *
    * @param in_ The input string.
    *
    * @return The string, with all macros replaced.
    */
   public String simmcastRecursivePreprocess(String in_) {
      int nameCount = names.size();
      String before = "";
      String after = "";
      int inLength = in_.length();

      for (int m = 0; m < nameCount; m++) {
         String name;
         int l;

         name = (String)(names.elementAt(m));
         l = name.length();
         if ( in_.regionMatches(0, name, 0, l) ) {
            if (l < inLength)
               after = simmcastRecursivePreprocess(in_.substring(l));
            return texts.elementAt(m) + after;
         }
      }

       if (inLength > 1) {
         int searchFrom = 0;

         if (in_.charAt(0) == ScriptParser.MACRO_CHAR)
            searchFrom = 1;

         int skip = in_.indexOf(ScriptParser.MACRO_SYMBOL, searchFrom);
         if (skip != -1 && skip < inLength) {
            String remain = in_.substring(skip);
            after = simmcastRecursivePreprocess(remain);
            return in_.substring(0,skip) + after;
         }
      }
      return in_;
   }

}
