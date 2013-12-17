/*
 * Simmcast Engine - A Free Discrete-Event Process-Based Simulator
 * TimeWheel.java
 * Copyright (C) 2003 Hisham H. Muhammad
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

package simmcast.engine;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.TreeMap;

import simmcast.distribution.interfaces.ProcessInterface;

/**
 * The time wheel is the data structure that describes the flow of events
 * in the simulation. It is a list of tuples describing events. Each tuple
 * contains a timestamp and a process handle. As the schedules advances the
 * simulation time, events are consumed in order from the time wheel.
 * Events can be added (scheduled for execution) at any future time.
 * Timings in the time wheel are absolute.
 *
 * @author Hisham H. Muhammad
 */
public class TimeWheel {

	/**
	 * The actual structure holding the wheel data.
	 */
	TreeMap<Double, ArrayList<Integer>> wheel;

	/**
	 * Build and initialize an empty time wheel.
	 */
	public TimeWheel() {
		wheel = new TreeMap<Double, ArrayList<Integer>>();
	}

	//TODO: Review the semantic of this function
	public long getSize() {
		return wheel.size();
	}

	/**
	 * Finds a process on the Time Wheel. If it's found, it 
	 * will be removed, for the insertion of the same process
	 * in another time. This method is called by "insertAt" 
	 * method.
	 * 
	 * @param time_ The time scheduled for execution of the process to be removed.
	 * @param pid_ The identifier of the process to be removed.
	 */
	private void searchEvent(double time_, int pid_) {
		synchronized(wheel) {
			if (wheel.containsKey(time_)) {
				if (wheel.get(time_).contains(pid_)) {
					if (wheel.get(time_).size() > 1) {
						wheel.get(time_).remove((Object) pid_);
					}
					else {
						wheel.remove(time_);
					}
				}
			}
		}
	}

	/**
	 * WARNING: a given process must exist in *only* one event at the 
	 * Time Wheel. In this case, if this process already exists, it's 
	 * erased and a new event will be inserted with the same process, 
	 * but with the new time specified in the parameter.    
	 *
	 * @param time_ Absolute time for execution.
	 * @param process_ The process to be scheduled at this time.
	 */
	public void insertAt(double time_, ProcessInterface proc_) {
		searchEvent(proc_.getLastSchedule(), proc_.getPid());
		synchronized(wheel) {
			if (!wheel.containsKey(time_))
				wheel.put(time_, new ArrayList<Integer>());
			wheel.get(time_).add(proc_.getPid());
			proc_.setLastSchedule(time_);
		}
	}

	/**
	 * Consume the next element from the time wheel. This will always
	 * be the first element of the wheel.
	 *
	 * @return The event tuple containing a handle to the next
	 * process to be scheduled.
	 */   
	public Event removeFirst() {
		Event resultEv = null;
		synchronized(wheel) {
			if (wheel.size() > 0) {
				double auxTime = wheel.firstKey();
				int auxPid = wheel.get(auxTime).remove(0);
				if (wheel.get(auxTime).size() <= 0)
					wheel.remove(auxTime);
				resultEv = new Event(auxTime, auxPid);
			}
		}
		return resultEv;
	}

	/**
	 * Return a handle to the next element that would be consumed by
	 * removeFirst(). The event is not actually removed.
	 *
	 * @return The event tuple containing a handle to the next
	 * process to be scheduled.
	 */
	public Event peekFirst() {
		Event resultEv = null;
		synchronized(wheel) {
			if (wheel.size() > 0) {
				double auxTime = wheel.firstKey();
				int auxPid = wheel.get(auxTime).get(0);
				resultEv = new Event(auxTime, auxPid);
			}
		}
		return resultEv;
	}

	/**
	 * Returns the string representation of the time wheel.
	 *
	 * @return A multiline string describing the current status of
	 * the time wheel.
	 */
	public String toString() {
		String out = "";
		for (Iterator<ArrayList<Integer>> iter = wheel.values().iterator(); iter.hasNext();) {
			ArrayList<Integer> walk = iter.next();
			out = out + "\n" + walk;
		}
		return out;
	}
}
