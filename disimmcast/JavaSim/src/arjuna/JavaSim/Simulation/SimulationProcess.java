/*
 * Copyright (C) 1996, 1997, 1998,
 *
 * Department of Computing Science,
 * The University,
 * Newcastle upon Tyne,
 * UK.
 * This file has bugfixes added by a patch
 * available at http://inf.unisinos.br/ released under the
 * GNU General Public License.
 *
 * $Id: SimulationProcess.java,v 1.3 1998/12/07 08:28:11 nmcl Exp $
 */

/*
   Changelog -
   2001.05.17 - HHM - Comment out deprecated methods
*/

package arjuna.JavaSim.Simulation;

import java.util.NoSuchElementException;

public class SimulationProcess extends Thread
{

public void finalize ()
    {
	if (!Terminated)
	{
	    Terminated = true;
	    Passivated = true;
	    wakeuptime = SimulationProcess.Never;

	    if (!idle())
		Scheduler.unschedule(this); // remove from scheduler queue

	    if (this == SimulationProcess.Current)
	    {
		try
		{
		    Scheduler.schedule();
		}
		catch (SimulationException e)
		{
		}
	    }
	    
	    SimulationProcess.allProcesses.Remove(this);
	}
    }

    /**
      Return the current simulation time.
      */
    
public final double Time ()
    {
	return SimulationProcess.CurrentTime();
    }

    /**
      Return the next simulation process which will run.
      */
    
public synchronized SimulationProcess next_ev () throws SimulationException, NoSuchElementException
    {
	if (!idle())
	    return Scheduler.ReadyQueue.getNext(this);
	else
	    throw(new SimulationException("SimulationProcess not on run queue."));
    }

    /**
      Return the simulation time at which this process will run.
      */
    
public final double evtime ()
    {
	return wakeuptime;
    }

    /**
      Activate this process before process 'p'. This process must not be
      running, or on the scheduler queue.
      */
    
public void ActivateBefore (SimulationProcess p) throws SimulationException, RestartException
    {
	if (Terminated || !idle()) return;

	Passivated = false;
    
	if (Scheduler.ReadyQueue.InsertBefore(this, p))
	    wakeuptime = p.wakeuptime;
	else
	    throw new SimulationException("'before' process is not scheduled.");
    }

    /**
      Activate this process after process 'p'. This process must not be
      running, or on the scheduler queue.
      */
    
public void ActivateAfter (SimulationProcess p) throws SimulationException, RestartException
    {
	if (Terminated || !idle()) return;

	Passivated = false;
    
	if (Scheduler.ReadyQueue.InsertAfter(this, p))
	    wakeuptime = p.wakeuptime;
	else
	    throw new SimulationException("'after' process is not scheduled.");
    }

    /**
      Activate this process at the specified simulation time. This process
      must not be running, or on the scheduler queue. 'AtTime' must be greater
      than, or equal to, the current simulation time. If 'prior' is true then
      this process will appear in the simulation queue before any other process
      with the same simulation time.
      */
    
public void ActivateAt (double AtTime, boolean prior) throws SimulationException, RestartException
    {
	if (Terminated || !idle()) return;

	if (AtTime < SimulationProcess.CurrentTime())
	    throw new SimulationException("Invalid time "+AtTime);

	Passivated = false;
	wakeuptime = AtTime;
	Scheduler.ReadyQueue.Insert(this, prior);
    }

    /**
      Activate this process at the specified simulation time. This process
      must not be running, or on the scheduler queue. 'AtTime' must be greater
      than, or equal to, the current simulation time.
      */
    
public void ActivateAt (double AtTime) throws SimulationException, RestartException
    {
	ActivateAt(AtTime, false);
    }

    /**
      This process will be activated after 'Delay' units of simulation time.
      This process must not be running, or on the scheduler queue. 'Delay' must
      be greater than, or equal to, zero. If 'prior' is true then this
      process will appear in the simulation queue before any other process
      with the same simulation time.
      */
    
public void ActivateDelay (double Delay, boolean prior) throws SimulationException, RestartException
    {
	if (Terminated || !idle()) return;

	if (!checkTime(Delay))
	    throw new SimulationException("Invalid delay time "+Delay);
	
	Passivated = false;
	wakeuptime = Scheduler.SimulatedTime + Delay;
	Scheduler.ReadyQueue.Insert(this, prior);
    }

    /**
      This process will be activated after 'Delay' units of simulation time.
      This process must not be running, or on the scheduler queue. 'Delay' must
      be greater than, or equal to, zero.
      */
    
public void ActivateDelay (double Delay) throws SimulationException, RestartException
    {
	ActivateDelay(Delay, false);
    }

    /**
      Activate this process at the current simulation time. This process
      must not be running, or on the scheduler queue.
      */
    
public void Activate () throws SimulationException, RestartException
    {
	if (Terminated || !idle()) return;

	Passivated = false;
	wakeuptime = CurrentTime();
	Scheduler.ReadyQueue.Insert(this, true);
    }

    /**
      Reactivate this process before process 'p'.
      */
    
public void ReActivateBefore (SimulationProcess p) throws SimulationException, RestartException
    {
	if (!idle())    
	    Scheduler.unschedule(this);
	
	ActivateBefore(p);
	if (SimulationProcess.Current == this)
	    Suspend();
    }

    /**
      Reactivate this process after process 'p'.
      */
    
public void ReActivateAfter  (SimulationProcess p) throws SimulationException, RestartException
    {
	if (!idle())
	    Scheduler.unschedule(this);
	
	ActivateAfter(p);
	if (SimulationProcess.Current == this)
	    Suspend();
    }

    /**
      Reactivate this process at the specified simulation time. 'AtTime' must
      be valid. If 'prior' is true then this process will appear in the
      simulation queue before any other process with the same simulation time.
      */
      
public void ReActivateAt (double AtTime, boolean prior) throws SimulationException, RestartException
    {
	if (!idle())
	    Scheduler.unschedule(this);

	ActivateAt(AtTime, prior);
	
	if (SimulationProcess.Current == this)
	{
	    Suspend();
	}
    }

    /**
      Reactivate this process at the specified simulation time. 'AtTime' must
      be valid.
      */
    
public void ReActivateAt (double AtTime) throws SimulationException, RestartException
    {
	ReActivateAt(AtTime, false);
    }

    /**
      Reactivate this process after 'Delay' units of simulation time. If
      'prior' is true then this process will appear in the simulation queue
      before any other process with the same simulation time.
      */
      
public void ReActivateDelay (double Delay, boolean prior) throws SimulationException, RestartException
    {
	if (!idle())
	    Scheduler.unschedule(this);
	
	ActivateDelay(Delay, prior);
	if (SimulationProcess.Current == this)
	    Suspend();
    }

    /**
      Reactivate this process after 'Delay' units of simulation time.
      */
    
public void ReActivateDelay (double Delay) throws SimulationException, RestartException
    {
	ReActivateDelay(Delay, false);
    }

    /**
      Reactivate this process at the current simulation time.
      */
    
public void ReActivate () throws SimulationException, RestartException
    {
	if (!idle())
	    Scheduler.unschedule(this);
	
	Activate();
	if (SimulationProcess.Current == this)
	    Suspend();
    }

    /**
      Cancels next burst of activity, process becomes idle.
      */
    
public void Cancel () throws RestartException
    {
	/*
	 * We must suspend this process either by removing it from
	 * the scheduler queue (if it is already suspended) or by
	 * calling suspend directly.
	 */

	if (!idle())  // process is running or on queue to be run
	{
	    // currently active, so simply suspend
	    
	    if (this == SimulationProcess.Current)
	    {
		wakeuptime = SimulationProcess.Never;
		Passivated = true;
		Suspend();
	    }
	    else
	    {
		Scheduler.unschedule(this); // remove from queue
	    }
	}
    }

    /**
      Terminate this process: no going back!
      */
    
public void terminate ()
    {
	if (!Terminated)
	{
	    Terminated = Passivated = true;
	    wakeuptime = SimulationProcess.Never;

	    if ((this != SimulationProcess.Current) && (!idle()))
		Scheduler.unschedule(this);

	    try
	    {
		Scheduler.schedule();
	    }
	    catch (SimulationException e)
	    {
	    }

	    SimulationProcess.allProcesses.Remove(this);
	    
	    /* HHM
	    stop();
            */
	}
    }

    /**
      Is the process idle?
      */
    
public synchronized boolean idle ()
    {
	if (wakeuptime >= SimulationProcess.CurrentTime())
	    return false;
	else
	    return true;
    }

    /**
      Has the process been passivated?
      */
    
public boolean passivated ()
    {
	return Passivated;
    }

    /**
      Has the process been terminated?
      */
    
public boolean terminated ()
    {
	return Terminated;
    }

    /**
      Return the currently active simulation process.
      */
    
public static SimulationProcess current () throws SimulationException
    {
	if (SimulationProcess.Current == null)
	    throw new SimulationException("Current not set.");
	
	return SimulationProcess.Current;
    }

    /**
      Return the current simulation time.
      */
    
public static double CurrentTime ()
    {
	return Scheduler.SimulatedTime;
    }

    /**
      Suspend the main thread.
      */
    
/* HHM
public static void mainSuspend ()
    {
	SimulationProcess.mainThread = Thread.currentThread();
	SimulationProcess.mainThread.suspend();
    }
*/    
    
    /**
      Resume the main thread.
      */
    

/* HHM
public static void mainResume () throws SimulationException
    {
	if (SimulationProcess.mainThread == null)
	    throw new SimulationException("No main thread");

	SimulationProcess.mainThread.resume();
    }
*/

protected SimulationProcess ()
    {
	wakeuptime = SimulationProcess.Never;
	Terminated = false;
	Passivated = true;
	started = false;

	SimulationProcess.allProcesses.Insert(this);
    }
    
protected void set_evtime (double time) throws SimulationException
    {
	if (!idle())
	{
	    if (time >= SimulationProcess.CurrentTime())
		wakeuptime = time;
	    else
		throw new SimulationException("Time "+time+" invalid.");
	}
	else
	    throw new SimulationException("SimulationProcess is not idle.");
    }

    /**
      Hold the current process for the specified amount of simulation time.
      */
    
protected void Hold (double t) throws SimulationException, RestartException
    {
	if ((this == SimulationProcess.Current) || (SimulationProcess.Current == null))
	{
	    wakeuptime = SimulationProcess.Never;
	    ActivateDelay(t, false);
	    Suspend();
	}
	else
	    throw new SimulationException("Hold applied to inactive object.");
    }
    
protected void Passivate () throws RestartException
    {
	if (!Passivated && (this == SimulationProcess.Current))
	    Cancel();
    }

    /**
      Suspend the process. If it is not running, then this routine should not
      be called.
      */
    
protected void Suspend () throws RestartException
    {
	try
	{
	    if (Scheduler.schedule())
	    {
		synchronized (mutex)
	        {
		    count--;
		    
		    if (count == 0)
		    {
			try
			{
			    mutex.wait();
			}
			catch (Exception e)
			{
			}
		    }

		}
	    }
	}
	catch (SimulationException e)
	{
	}

	if (Scheduler.simulationReset())
	    throw new RestartException();
    }

    /**
      Resume the specified process. This can only be called on a process
      which has previously been Suspend-ed or has just been created, i.e.,
      the currently active process will never have Resume called on it.
      */
    
protected void Resume ()
    {
	/*
	 * To compensate for the initial call to Resume by the
	 * application.
	 */
	
	if (SimulationProcess.Current == null)
	{
	    SimulationProcess.Current = this;
	    wakeuptime = SimulationProcess.CurrentTime();
	}
	
	if (!Terminated)
	{
	    if (!started)
	    {
		started = true;
		start();
	    }
	    else
	    {
		synchronized (mutex)
	        {
		    count++;

		    if (count >= 0)
			mutex.notify();
		}
	    }
	}
    }
    
private boolean checkTime (double time)
    {
	if (time >= 0)
	    return true;
	else
	    return false;
    }

void passivate ()
    {
	Passivated = true;
	wakeuptime = SimulationProcess.Never;
    }
    
public static final int Never = -1;

protected static SimulationProcessList allProcesses = new SimulationProcessList();
    
private double wakeuptime;
private boolean Terminated;
private boolean Passivated;
private boolean started;
private Object mutex = new Object();
private int count = 1;
    
private static Thread mainThread = null;

static SimulationProcess Current = null;
    
};
