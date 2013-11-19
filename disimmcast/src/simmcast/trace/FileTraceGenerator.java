/*
 * Simmcast - a network simulation framework
 * FileTraceGenerator.java
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

package simmcast.trace;

import java.io.FileOutputStream;
import java.io.PrintWriter;

/**
 * Trace generators of this family produce a file
 * listing the events occurred in the simulation.
 * This class abstracts some file handling utilities
 * to ease the creation of trace generators.
 * 
 * @author Hisham H. Muhammad
 */
abstract public class FileTraceGenerator extends TraceGenerator {

   // *****************************************************
   // ATTRIBUTES
   // *****************************************************

   /**
    * The name of the trace file.
    */
   private String traceFileName;

   /**
    * The file handle used for trace generation.
    */
   private PrintWriter traceFile;

   // *****************************************************
   // CONSTRUCTORS
   // *****************************************************

   /**
    * Constructs a file trace generator.
    */
   public FileTraceGenerator() {
      super();
   }

   // *****************************************************
   // TRACE CONTROL
   // *****************************************************

   /**
    * Defines which trace file should be used, enabling
    * tracing.
    *
    * @param file_ The filename.
    */
   public void setFile(String file_) {
      try {

         if (traceFile != null)
            traceFile.close();
         traceFile = new PrintWriter(new FileOutputStream(file_));
         enable();

      } catch (Exception e) {
         System.err.println("Could not create trace file.");
         traceFile = null;
         disable();
      }
   }

   /**
    * Initialization routine for the tracer.
    * Does nothing by default. A subclass will
    * usually output a header here.
    */
   public void start() {
   }

   /**
    * Termination routine for the tracer, closes the file.
    */
   public void finish() {
      if (traceFile != null)
         traceFile.close();
   }

   /**
    * Enables tracing from this point on, appending output
    * to the same file that was previously set.
    */
   public void enable() {
      if (traceFile != null) {
         super.enable();
      } else {
         System.err.println("Trace file was not previously set.");
         traceFile = null;
         disable();
      }
   }

   // *****************************************************
   // UTILITIES
   // *****************************************************

   /**
    * Outputs a line with a carriage return.
    *
    * @param s_ The string to be output.
    */
   public void println(String s_) {
      traceFile.println(s_);
   }

   /**
    * Outputs a line with no carriage return.
    *
    * @param s_ The string to be output.
    */
   public void print(String s_) {
      traceFile.print(s_);
   }

}
