/*
 * Simmcast - a network simulation framework
 * LimitedNormalStream.java
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

package simmcast.stream;

import java.io.IOException;

import simmcast.distribution.CloneOnWorker;

import arjuna.JavaSim.Distributions.NormalStream;

/**
 * @author Guilherme B. Bedin
 */
public class LimitedNormalStream extends NormalStream implements CloneOnWorker {

   protected double      max;
   protected double      min;
   protected boolean     limit;
   private String cloneOnWorker;

   public void setLimits(double max_, double min_) {
     max = max_;
     min = min_;
     limit = true;
   }

   /**
    *
    */
   public double getNumber () throws IOException, ArithmeticException {
    double num = super.getNumber();
    if (limit){
      if ( max < num) return max;
      if ( min > num) return min;

    }
    return  num;
   }

   /**
    *
    */
   public LimitedNormalStream (double m_, double sd_) {
     super(m_, sd_);
     limit = false;
     cloneOnWorker = "" + m_ + "," + sd_;
   }

   public LimitedNormalStream (double m_, double sd_, int StreamSelect, long MGSeed, long LCGSeed) {
     super(m_, sd_, StreamSelect, MGSeed, LCGSeed);
     limit = false;
     cloneOnWorker = "" + m_ + "," + sd_ + "," + StreamSelect+ "," + MGSeed+ "," + LCGSeed;
   }

	public String getConstructorParameters() {
		return cloneOnWorker;
	}

}
