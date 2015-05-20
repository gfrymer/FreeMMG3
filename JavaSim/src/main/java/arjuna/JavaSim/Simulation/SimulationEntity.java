package arjuna.JavaSim.Simulation;


public class SimulationEntity extends SimulationProcess
{

public void finalize ()
    {
	super.finalize();
    }
    
    /**
      Interrupt the given process (which *must* be in Wait or WaitFor), and
      resume it. If immediate resumption is required then this process will
      be suspended (placed back on to the scheduler queue for "immediate"
      resumption when the interrupted process has finished).
      */
    
public void Interrupt (SimulationEntity toInterrupt, boolean immediate) throws SimulationException, RestartException
    {
	if (toInterrupt.terminated())
	    throw(new SimulationException("Entity already terminated."));

	if (!toInterrupt._waiting)
	    throw(new SimulationException("Entity not waiting."));
    
	toInterrupt._interrupted = true;

	// remove from queue for "immediate" activation

	Scheduler.unschedule(toInterrupt); // remove from queue and prepare to suspend

	// will take over when this process is suspended
	
	toInterrupt.ReActivateAt(SimulationProcess.CurrentTime(), true);

	/*
	 * Put "this" on to queue and suspend so that interrupted process
	 * can run.
	 */
	
	if (immediate)
	    ReActivateAt(SimulationProcess.CurrentTime());
    }
    
public final void trigger ()
    {
	_triggered = true;
    }

    /**
      Must wake up any waiting process before we "die".
      Currently only a single process can wait on this condition, but
      this may change to a list later.
      */
    
public void terminate ()
    {
	/*
	 * Resume waiting process before this one "dies".
	 */
	
	if (_isWaiting != null)
	{
	    // remove from queue for "immediate" activation
	    
	    try
	    {
		_isWaiting.Cancel();
		_isWaiting.ReActivateAt(SimulationProcess.CurrentTime(), true);
	    }
	    catch (RestartException e)
	    {
	    }
	    catch (SimulationException e)
	    {
	    }
	    
	    _isWaiting = null;
	}

	super.terminate();	
    }

protected SimulationEntity ()
    {
	super();
	
	_isWaiting = null;
	_interrupted = _triggered = _waiting = false;
    }

    /**
     Wait for specified period of time. If this process is interrupted
     then the InterruptedException is thrown.
     */
    
protected void Wait (double waitTime) throws SimulationException, RestartException, InterruptedException
    {
	_waiting = true;

	try
	{
	    Hold(waitTime);
	}
	catch (SimulationException e)
	{
	    throw(new SimulationException("Invalid entity."));
	}

	_waiting = false;

	if (_interrupted)
	{
	    _interrupted = false;
	    throw(new InterruptedException());
	}
    }

    /**
      Suspends the current process until the process in the parameter
      has been terminated. If the calling process is interrupted before
      the 'controller' is terminated, then the InterruptedException is
      thrown. If the boolean parameter is true then the controller is
      reactivated immediately.
      */
    
protected void WaitFor (SimulationEntity controller, boolean reAct) throws SimulationException, RestartException, InterruptedException
    {
	if (controller == this)      // can't wait on self!
	    throw new SimulationException("WaitFor cannot wait on self.");

	controller._isWaiting = this;  // resume when controller terminates

	// make sure this is ready to run

	try
	{
	    if (reAct)
		controller.ReActivateAt(SimulationProcess.CurrentTime(), true);
	}
	catch (SimulationException e)
	{
	}
	
	_waiting = true;

	// we don't go back on to queue as controller will wake us

	Cancel();

	_waiting = _interrupted = false;

	// if we have been successful then terminated = true

	if (!controller.terminated())
	    throw new InterruptedException();
    }

    /**
      Suspends the current process until the process in the parameter
      has been terminated. If the calling process is interrupted before
      the 'controller' is terminated, then the InterruptedException is
      thrown. The controller will not be reactivated immediately.
      */
    
protected void WaitFor (SimulationEntity controller) throws SimulationException, RestartException, InterruptedException
    {
	WaitFor(controller, false);
    }

    /**
      The calling process is placed onto the trigger queue and should only
      be restarted pending some application specific event which uses
      the trigger queue. The InterruptedException is thrown if the caller is
      interrupted rather than being triggered.
      */
      
protected void WaitForTrigger (TriggerQueue _queue) throws SimulationException, RestartException, InterruptedException
    {
	_queue.insert(this);
	
	_interrupted = false;
	_waiting = true;
	
	Cancel();            // remove from queue and suspend

	// indicate whether this was triggered successfully or interrupted

	if (_triggered)
	    _triggered = false;
	else
	    throw(new InterruptedException());
    }

    /**
      Currently, a process which is waiting on a semaphore cannot be
      interrupted - its wait status is not set.
      */
    
protected void WaitForSemaphore (Semaphore _sem) throws RestartException
    {
	_sem.Get(this);
    }

protected SimulationEntity _isWaiting;
    
private boolean _interrupted;
private boolean _triggered;
private boolean _waiting;
    
};
