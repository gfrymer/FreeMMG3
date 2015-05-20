package arjuna.JavaSim.Statistics;


public class Bucket
{

    /**
      Create bucket with name 'n' and number of entries 'initEntries'.
      */
    
public Bucket (double n, long initEntries)
    {
	numberOfEntries = initEntries;
	name = n;
	next = null;
    }

    /**
      Create bucket with name 'n' and 1 entry.
      */
    
public Bucket (double n)
    {
	numberOfEntries = 1;
	name = n;
	next = null;
    }

    /**
      Copy constructor.
      */
    
public Bucket (Bucket b)
{
    numberOfEntries = b.size();
    name = b.Name();
    next = null;
}

    /**
      Is the name of the bucket equal to 'value'?
      */
    
public boolean equals (double value)
    {
	if (name == value)
	    return true;
	else
	    return false;
    }

    /**
      Is the name of the bucket greater than 'value'?
      */
    
public boolean greaterThan (double value)
    {
	if (name > value)
	    return true;
	else
	    return false;
    }

    /**
      Is the name of the bucket greater than or equal to 'value'?
      */
    
public boolean greaterThanOrEqual (double value)
    {
	if (name >= value)
	    return true;
	else
	    return false;
    }

    /**
      Is the name of the bucket less than 'value'?
      */
    
public boolean lessThan (double value)
    {
	if (name < value)
	    return true;
	else
	    return false;
    }

    /**
      Is the name of the bucket less than or equal to 'value'?
      */
    
public boolean lessThanOrEqual (double value)
    {
	if (name <= value)
	    return true;
	else
	    return false;
    }

    /**
      Returns the name of the bucket.
      */
    
public double Name ()
    {
	return name;
    }

    /**
      Increment the number of entries by 'value'.
      */
    
public void incrementSize (long value)
    {
	numberOfEntries += value;
    }

    /**
      Set the number of entries to 'value'.
      */
    
public void size (long value)
    {
	numberOfEntries = value;
    }

    /**
      Return the number of entries.
      */
    
public long size ()
    {
	return numberOfEntries;
    }

    /**
      Return the next bucket.
      */
    
public Bucket cdr ()
    {
	return next;
    }

    /**
      Set the next bucket.
      */
    
public void setCdr (Bucket n)
    {
	next = n;
    }
    
private long numberOfEntries;
private double name;
private Bucket next;
    
};
