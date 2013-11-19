/*
 * Simmcast Engine - A Free Discrete-Event Process-Based Simulator
 * Process.java
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

/**
 * Processes are the basic entities in the simulation.
 * They are built on top of Java threads and provide a Java-like
 * API to the user. A process can be understod as a cooperative
 * thread: once it is executing user code, a process can only
 * be executed when the user code calls a predefined method
 * from this class.
 *
 * @author Hisham H. Muhammad
 */
abstract public class Process extends Thread implements ProcessInterface {

	// **************************************************
	// ATTRIBUTES
	// **************************************************

	/**
	 * A handle to the simulation scheduler, the core of the engine.
	 */
	protected SchedulerInterface simulationScheduler;

	/**
	 * A flag to indicate whether the simulation has started. That is,
	 * whether startProcess() has been issued.
	 */
	boolean started = false;

	/**
	 * A flag to indicate whether the process is running, or if its
	 * execution is currently blocked.
	 */
	boolean isProcRunning = false;

	/**
	 * The semaphore counter.
	 */
	int counter = 1;

	/**
	 * The object that serves a  s the object mutex.
	 */
	Object mutex = new Object();

	/**
	 * A list of other processes that are on hold waiting for this
	 * thread to finish. In other words, the list of processes that
	 * issued joinProcess() on this thread.
	 */
	ArrayList<Process> joinList = new ArrayList<Process>();

	/**
	 * PID generator for assigning PIDs to the processes
	 */
//	static int pidGen = 0;	MOVED TO PROCESS PROXY

	/**
	 * PID of the process in the simulation
	 */
	int pid; // = pidGen++;	MOVED TO PROCESS PROXY
	
	/**
	 * Simulation time when the process was started execution
	 * last. This is supposed to be used ONLY by the TimeWheel
	 * for its internal control.
	 */
	double lastSchedule = 0.0;

	// **************************************************
	// GETTERS/SETTERS
	// **************************************************

	/**
	 * Initialize the scheduler handle.
	 */
	public void setScheduler(SchedulerInterface scheduler_) {
		simulationScheduler = scheduler_;
	}

	// **************************************************
	// THREAD API (INTERNAL USE)
	// **************************************************

	/**
	 * Reserved for internal use. This method contains the
	 * execution routine of the actual Java thread corresponding
	 * to this process.
	 */
	public void run() {
		runProcess();
		isProcRunning = false;
		Iterator<Process> iter = joinList.iterator();
		while ( iter.hasNext() ) {
			Process item = (Process)iter.next();
			// TODO: use the low-level API instead?
			item.wakeUpProcess();
		}
		joinList = new ArrayList<Process>();
		// TODO: why is this commented? Is this correct? Check.
		// simulationScheduler.removeFromThreadPool(this);
		simulationScheduler.processBlockedOrFinished(getPid());
		/*Process next = simulationScheduler.nextToResume();
		if (next != null) {
			next.resumeProcess();
		} else {
			simulationScheduler.terminate();
		}*/
	}

	// **************************************************
	// INTERNAL CONTROL
	// **************************************************

	/**
	 * Reserved for internal use. Resumes the execution of the
	 * Java thread corresponding to this process.
	 */
	public void resumeProcess() {
/* ppio QUE HACEMOS CON ESTO ************************************////////////////
		//assert simulationScheduler.running.contains(this) : "resumed thread is not the one marked as running.";
/* fin QUE HACEMOS CON ESTO ************************************////////////////
		assert this != Thread.currentThread(): "resumed thread is already running";

		if (started) {
			synchronized (mutex) {
				counter++;
				// FIXME Is this correct? Check.
				if (counter > 0) // was: counter >= 0 
					mutex.notify();
			}
		} else {
			started = true;
			start();
			isProcRunning = true;
		}
	}

	// **************************************************
	// USER API
	// **************************************************

	/**
	 * This is the method that should be implemented in order to add
	 * the user logic to the process.
	 */
	abstract public void runProcess();

	/**
	 * Launch a process. Once this method is issued, this process will
	 * be scheduled for immediate execution.
	 */
	public void startProcess() {
		simulationScheduler.addToThreadPool(this);
		simulationScheduler.activateNow(this);
		/*if (simulationScheduler.started && simulationScheduler.running == null) {
			simulationScheduler.nextToResume().resumeProcess();
		}*/
	}

	/**
	 * Holds the execution of a process indefinitely.
	 */
	protected void sleepProcess() throws TerminatedException {
/* ppio QUE HACEMOS CON ESTO ************************************////////////////
		//assert simulationScheduler.running.contains(Thread.currentThread()): "'running' is out of sync";
/* fin QUE HACEMOS CON ESTO ************************************////////////////
		assert isProcRunning == true: "thread is flagged as not-running";
		//Process next = simulationScheduler.nextToResume();

		/*if (next == null) {
			throw new TerminatedException();
		}
		if (next != this) {
			next.resumeProcess();*/
		simulationScheduler.processBlockedOrFinished(getPid());
			synchronized (mutex) {
				try {
					counter--;
					isProcRunning = false;

					if (counter == 0)
						mutex.wait();

					isProcRunning = true;
				} catch (InterruptedException e) {
					throw new TerminatedException();
				}
			}
		//}
	}

	/**
	 * Holds the execution of a process for a given amount of time.
	 *
	 * @param relativeTime_ The amount of time the process should 
	 * sleep, starting from the current simulation time.
	 */
	protected void sleepProcess(double relativeTime_) throws TerminatedException  {
/* ppio QUE HACEMOS CON ESTO ************************************////////////////
		//assert simulationScheduler.running.contains(this): "Soft error: A thread cannot put another to sleep!";
/* fin QUE HACEMOS CON ESTO ************************************////////////////
		assert this == Thread.currentThread(): "HARD error: A thread cannot put another to sleep!";

		simulationScheduler.activateAt(relativeTime_, this);
		sleepProcess();
	}

	/**
	 * Makes the currently running process sleep until this
	 * process ceases execution.
	 */
	protected void joinProcess() throws TerminatedException { // IMPOSIBLE SABER A CUAL DE TODOS LOS PROCESOS QUIERE JOINEARSE
		//assert this != simulationScheduler.running: "Soft error: A thread cannot join itself!";
		assert this != Thread.currentThread(): "HARD error: A thread cannot join itself!";
		assert Thread.currentThread() instanceof Process: "HARD error: Current thread is not a simulation process!";

/* ppio QUE HACEMOS CON ESTO ************************************////////////////
/*		Process current = (Process)simulationScheduler.running.get(0);
		joinList.add(current);
		current.sleepProcess();*/
/* fin QUE HACEMOS CON ESTO ************************************////////////////
	}

	/**
	 * Resume execution of a process that is sleeping. The process will
	 * be scheduled for immediate execution.
	 */
	public void wakeUpProcess() { // ALGUNO DE LOS PROCESOS CORRIENDO PODRIA WAKEAR A OTRO QUE TAMBIEN ESTE CORRIENDO
		//assert this != simulationScheduler.running: "Soft error: A thread cannot wake itself!";
		assert this != Thread.currentThread(): "HARD error: A thread cannot wake itself!";

		simulationScheduler.activateNow(this);
	}

	// **************************************************
	// INCOMPATIBILITY CONTROL
	// **************************************************

	/**
	 * A method to avoid execution of sleep(int). It must never be called.
	 * The implementation of this method kills the simulation, in order
	 * to make this point very clear: it must <i>never</i> be called.
	 */
	public void sleep(int time_) {
		System.err.println();
		System.err.println("FATAL ERROR:");
		System.err.println("Use of sleep(int) is forbidden!");
		System.exit(1);
	}

	public double getLastSchedule() {
		return lastSchedule;
	}

	public void setLastSchedule(double schedule) {
		lastSchedule = schedule;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int mPid) {
		pid = mPid;
	}

	public boolean isRunning() {
		return isProcRunning;
	}

}
