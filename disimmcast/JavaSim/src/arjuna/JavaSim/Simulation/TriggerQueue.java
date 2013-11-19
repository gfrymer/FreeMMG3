package arjuna.JavaSim.Simulation;

import java.util.NoSuchElementException;

public class TriggerQueue
{

public TriggerQueue ()
    {
	head = new SimulationEntityList();
    }

    /**
     If there is anything left on the queue when this goes
     out-of-scope then we will place them back on to the
     simulation queue, rather than doing nothing - this is the
     safest thing to do. THIS SHOULD NOT HAPPEN THOUGH!*
     */
    
public void finalize ()
    {
	if (head != null)
	{
	    if (head.numberOfElements() > 0)
	    {
		System.out.println("TriggerQueue destructor called with non-zero list!");
		
		try
		{
		    triggerAll();
		}
		catch (NoSuchElementException e)
		{
		}
	    }
	}
    }

public synchronized void triggerFirst (boolean setTrigger) throws NoSuchElementException
    {
	if (head.numberOfElements() == 0)
	    throw(new NoSuchElementException());

	SimulationEntity removed = this.remove();
	
	if (setTrigger)
	    removed.trigger();

	try
	{
	    removed.ReActivateAt(SimulationProcess.CurrentTime());
	}
	catch (RestartException e)
	{
	}
	catch (SimulationException e)
	{
	}
    }

public synchronized void triggerFirst () throws NoSuchElementException
    {
	triggerFirst(true);
    }
    
public synchronized void triggerAll () throws NoSuchElementException
    {
	long currentNumber = head.numberOfElements();

	if (currentNumber == 0)
	    throw(new NoSuchElementException());

	for (int i = 0; i < currentNumber; i++)
	    triggerFirst();
    }
    
protected synchronized void insert (SimulationEntity toAdd) throws SimulationException
    {
	if (toAdd._isWaiting != null)
	    throw(new SimulationException("Entity already waiting on event."));

	head.add(toAdd);
    }
    
protected synchronized SimulationEntity remove () throws NoSuchElementException
    {
	return head.removeFirst();
    }

private SimulationEntityList head;
private long numberOfElements;
    
};

class SimulationEntityElement
{

public SimulationEntityElement (SimulationEntity toAdd)
    {
	_element = toAdd;
	_next = null;
    }
    
public SimulationEntity _element;
public SimulationEntityElement _next;

};
    
class SimulationEntityList
{

public SimulationEntityList ()
    {
	headOfList = null;
	_numberOfElements = 0;
    }

public final long numberOfElements ()
    {
	return _numberOfElements;
    }

public final synchronized void add (SimulationEntity toAdd)
    {
	SimulationEntityElement ptr = new SimulationEntityElement(toAdd);

	ptr._next = headOfList;
	headOfList = ptr;
	_numberOfElements++;
    }
    
public final synchronized SimulationEntity removeFirst () throws NoSuchElementException
    {
	if (_numberOfElements == 0)
	    throw(new NoSuchElementException());

	SimulationEntityElement ptr = headOfList;
	
	headOfList = headOfList._next;
	_numberOfElements--;

	return ptr._element;
    }

private SimulationEntityElement headOfList;
private long _numberOfElements;

};
