/*
 * Simmcast Engine - A Free Discrete-Event Process-Based Simulator
 * Scheduler.java
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

import simmcast.distribution.interfaces.ProcessInterface;
import simmcast.distribution.interfaces.SchedulerInterface;

/**
 * This object is the core of the simulation engine.
 * It is internally used by the simulator. 
 */
public class Scheduler extends Thread implements SchedulerInterface {

	// **************************************************
	// ATTRIBUTES
	// **************************************************

	/**
	 * The current simulation time.
	 */
	double now = 0.0;

	/**
	 * Handle to the data structure that holds the scheduled
	 * processes.
	 */
	TimeWheel timeWheel = new TimeWheel();

	/**
	 * A handle to the currently running process.
	 */
	Vector<ProcessInterface> running = new Vector<ProcessInterface>();

	/**
	 * A flag to indicate that the simulation has started.
	 * Once this is set, it is never reset.
	 */
	boolean started = false;

	/**
	 * The list of all processes in this simulation,
	 * also known as the thread pool.
	 */
	public TreeMap<Integer, ProcessInterface> threadPool = new TreeMap<Integer, ProcessInterface>();

	// **************************************************
	// GETTERS/SETTERS
	// **************************************************

	/**
	 * Returns the current simulation time. This is the logic
	 * clock of the simulator scheduler.
	 *
	 * @return The current simulation time, in simulated time
	 * units.
	 */
	public double currentTime() {
		return now;
	}

	public void setTime(double newTime) {
		
	}

	/**
	 * Returns the handle to the process that is currently
	 * scheduled as current.
	 */
	public Vector<ProcessInterface> currentProcesses() {
		return running;
	} 

	// **************************************************
	// SIMULATION CONTROL
	// **************************************************

	/**
	 * Initiate the simulation. This is part of the public simulation
	 * control interface. The first process scheduled for immediate
	 * execution is started.
	 */
/*	public void start() {
		started = true;
		nextToResume().resumeProcess();
	}*/

	public Scheduler() {
		setName("["+getClass()+"]");
	}
	/**
	 * The object that serves a  s the object mutex.
	 */
	java.util.concurrent.LinkedBlockingQueue<Integer> mutex = new java.util.concurrent.LinkedBlockingQueue<Integer>();

	/* Thread principal del scheduler */
	public void run() {
		started = true;

		while (started)
		{
			if (resumeAllProcessesOnTime()) //(resumeNextProcess())
			{
				while (running.size()>0)
				{
					try {
						Integer pid = mutex.take();
						synchronized (running) {
							for (int i=0;i<running.size();i++)
							{
								if (running.get(i).getPid()==pid)
								{
									running.remove(i);
								}
							}
						}
					} catch (InterruptedException e) {
						started = false;
						terminate();
						return;
					}
				}
			}
			else
			{
				started = false;
			}
		}
		terminate();
	}

	public void processBlockedOrFinished(int pid)
	{
		if (!started)
			return;
//		ProcessInterface p = ((ProcessInterface) Thread.currentThread());
		try {
			mutex.put(pid); //p.getPid());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Terminate the simulation. This is part of the public simulation
	 * control interface. All processes are asked to interrupt, throwing
	 * TerminationException exceptions.
	 */
	public void terminate() {
		for (Iterator<ProcessInterface> iter = threadPool.values().iterator(); iter.hasNext();) {
			ProcessInterface walk = iter.next();
			walk.interrupt();
		}
		threadPool.clear();
	}

	// **************************************************
	// TIME WHEEL MANAGEMENT
	// **************************************************

	/**
	 * Returns a handle to the next process that is ready to execute.
	 * This called when a thread is about to resume: it removes it
	 * from the timewheel.
	 *
	 * @return A handle to the next process, or null if there are no
	 * events remaining in the timewheel.
	 */   
	private boolean /*Process*/ resumeNextProcess() {
		Event event;
		event = timeWheel.removeFirst();
		if (event != null) {
			now = event.time;
			ProcessInterface next = threadPool.get(event.pid);
			if (running.get(0) == next) // HACE FALTA DISTINGUIR ESTO??
			{
				running.get(0).resumeProcess();
			}
			else
			{
				running.set(0, next);
				running.get(0).resumeProcess();
			}
			return true;
		}
		else
			return false;
	}

	public ProcessInterface resumeProcess(int pid)
	{
		return threadPool.get(pid);
	}

	/**
	 * Returns a handle to the next process that is ready to execute.
	 * This called when a thread is about to resume: it removes it
	 * from the timewheel.
	 *
	 * @return A handle to the next process, or null if there are no
	 * events remaining in the timewheel.
	 */   
	private boolean /*Process*/ resumeAllProcessesOnTime() {
		ArrayList<ProcessInterface> willResume = new ArrayList<ProcessInterface>();
		Event event;
		synchronized (timeWheel) {
			event = timeWheel.removeFirst();
			if (event != null) {
				now = event.time;
				boolean sameTime = true;
				while (sameTime)
				{
					ProcessInterface next = threadPool.get(event.pid);
					if (next!=null)
					{
						willResume.add(next);
					}
					event = timeWheel.peekFirst();
					if (event!=null)
					{
						sameTime = (event.time == now);
						if (sameTime)
						{
							event = timeWheel.removeFirst();
						}
					}
					else
					{
						sameTime = false;
					}
				}
			}
		}
		if (willResume.size()>0)
		{
			System.out.println("Resuming time " + now + " processes: " + willResume.size());
			Iterator<ProcessInterface> pi = willResume.iterator();
			while (pi.hasNext())
			{
				ProcessInterface next = pi.next();
//				System.out.print(next.getPid() + " ");
				synchronized (running) {
					running.add(next);
				}
				next.resumeProcess();
			}
//			System.out.println();
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Schedule an event in the time wheel. 
	 *
	 * @param relativeTime_ How long from now, in simulated time units,
	 * should the process wait until it is scheduled for execution.
	 * @param process_ The process to be executed.
	 */
	public void activateAt(double relativeTime_, ProcessInterface process_) {
		synchronized (timeWheel)
		{
			timeWheel.insertAt(now + relativeTime_, process_);
		}
	}

	/**
	 * Schedule an event for immediate execution.
	 *
	 * @param process_ The process to be executed immediately.
	 */
	public void activateNow(ProcessInterface process_) {
		synchronized (timeWheel)
		{
			timeWheel.insertAt(now, process_);
		}
	}

	// **************************************************
	// THREAD POOL MANAGEMENT
	// **************************************************

	/**
	 * Register a process to be managed by this scheduler.
	 *
	 * @param process_ The process to be scheduled.
	 */
	public void addToThreadPool(ProcessInterface process_) {
		threadPool.put(process_.getPid(), process_);
	}

	/**
	 * Makes a process cease to be managed by this scheduler.
	 *
	 * @param process_ The process to be removed.
	 */
	public void removeFromThreadPool(ProcessInterface process_) {
		if (!started)
			return;
		threadPool.remove(process_.getPid());
	}

	public ProcessInterface getFromThreadPool(int pid) {
		return threadPool.get(pid);
	}

	// **************************************************
	// UTILITY FUNCTIONS
	// **************************************************

	/**
	 * Returns the string representation of the scheduler.
	 * It is presented as a table listing the timewheel and the
	 * thread pool.
	 *
	 * @return A multiline string presenting the current status
	 * of the scheduler.
	 */
	public String toString() {
		String pool = "";
		for (Iterator<ProcessInterface> iter = threadPool.values().iterator(); iter.hasNext();) {
			ProcessInterface walk = iter.next();
			pool = pool + "\n" + (walk.isRunning()? "* "+walk :". "+walk );
		}

		return "\n" +
		"========================================\n" +
		"now: " + now + "\n" +
		"running: " + running + "\n" +
		"----------------------------------------\n" +
		"timeWheel: " + timeWheel + "\n" +
		"----------------------------------------\n" +
		"threadPool: \n" + pool + "\n" +
		"========================================\n" +
		"\n";
	}

}
