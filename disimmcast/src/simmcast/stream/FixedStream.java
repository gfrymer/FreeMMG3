/*
 * Simmcast - a network simulation framework
 * FixedStream.java
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

package simmcast.stream;

import java.io.IOException;

import simmcast.distribution.CloneOnWorker;

import arjuna.JavaSim.Distributions.RandomStream;

/**
 * A "dummy" distribution, that returns always the same number.
 * This is meant to be used when performing a simplified run of
 * a simulation experiment, abstracting a random distribution down
 * to a fixed value.
 *
 * @author Hisham H. Muhammad
 */
public class FixedStream extends RandomStream implements CloneOnWorker {

   double number;

   /**
    * Always returns the same number, defined in the
    * constructor.
    */
   public double getNumber () throws IOException, ArithmeticException {
      return number;
   }

   /**
    * Initializes a fixed stream (a stream that will always
    * return the same number).
    */
   public FixedStream (double number_) {
      number = number_;
   }

	public String getConstructorParameters() {
		return "" + number;
	}

}
