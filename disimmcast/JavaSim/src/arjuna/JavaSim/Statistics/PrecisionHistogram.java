package arjuna.JavaSim.Statistics;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
  This is the base histogram class which keeps an exact tally of
  all values input, i.e., a bucket is created for each new value.
  This can take up a lot of space in a given simulation, so other
  (less precise) histogram classes are also provided.
 */

public class PrecisionHistogram extends Variance
{

    /**
      Create an empty histogram.
      */
    
public PrecisionHistogram ()
    {
	length = 0;
	Head = null;

	reset();
    }

public void finalize ()
    {
	reset();
    }

    /**
      Add 'value' to the histogram. If a bucket already exists for this
      then it is incremented, otherwise a new bucket will be created.
      */
    
public void setValue (double value) throws IllegalArgumentException
    {
	super.setValue(value);

	Bucket trail = null;
    
	for (Bucket ptr = Head; ptr != null; trail = ptr, ptr = ptr.cdr())
	{
	    if (ptr.Name() == value)
	    {
		ptr.incrementSize(1);
		return;
	    }
	    else if (ptr.greaterThan(value))
		break;
	}
    
	// we need to add a new bucket.

	add(trail, value, false);
    }

    /**
      Empty the histogram.
      */
    
public void reset ()
    {
	if (length > 0)  // delete old list
	{
	    Bucket trail = Head;
	    for (long i = 0; i < length; i++)
	    {
		Head = Head.cdr();
		trail = Head;
	    }
	
	    length = 0;
	    Head = null;
	}
    
	super.reset();
    }

    /**
      Return the number of buckets in the histogram.
      */
    
public long numberOfBuckets ()
    {
	return length;
    }

    /**
      There are two ways of getting the number of entries in a bucket:
      (i) give the index number of the bucket, or
      (ii) give the name of the bucket.
      If the bucket is not present then false is returned.
     */

public double sizeByIndex (long index) throws StatisticsException, IllegalArgumentException
    {
	Bucket ptr = Head;

	if ((index < 0) || (index > length))
	    throw(new IllegalArgumentException("index out of range."));

	for (long i = 0; (i < index) && (ptr != null); i++)
	    ptr = ptr.cdr();

	if (ptr != null)
	    return ptr.size();

	// we should never get here!

	throw(new StatisticsException("sizeByIndex went off end of list."));
    }
    
public double sizeByName (double name) throws IllegalArgumentException
    {
	for (Bucket ptr = Head; ptr != null; ptr = ptr.cdr())
	{
	    if (ptr.Name() == name)
		return ptr.size();

	    if (ptr.greaterThan(name))  // bucket is not present
		break;
	}

	throw(new IllegalArgumentException("Bucket name "+name+" not found."));
    }

    /**
      Save the state of the histogram to the file named 'fileName'.
      */
    
public boolean saveState (String fileName) throws IOException
    {
	FileOutputStream f = new FileOutputStream(fileName);
	DataOutputStream oFile = new DataOutputStream(f);

	boolean res = saveState(oFile);

	f.close();

	return res;
    }

    /**
      Save the state of the histogram to the stream 'oFile'.
      */
    
public boolean saveState (DataOutputStream oFile) throws IOException
    {
	oFile.writeLong(length);

	for (Bucket ptr = Head; ptr != null; ptr = ptr.cdr())
	{
	    oFile.writeDouble(ptr.Name());
	    oFile.writeLong(ptr.size());
	}
    
	return super.saveState(oFile);	
    }

    /**
      Restore the histogram state from the file 'fileName'.
      */
    
public boolean restoreState (String fileName) throws FileNotFoundException, IOException
    {
	FileInputStream f = new FileInputStream(fileName);
	DataInputStream iFile = new DataInputStream(f);

	boolean res = restoreState(iFile);

	f.close();

	return res;	
    }

    /**
      Restore the histogram state from the stream 'iFile'.
      */
    
public boolean restoreState (DataInputStream iFile) throws IOException
    {
	long numberEntries;
	double bucketName;
	Bucket ptr = null;
    
	reset();

	length = iFile.readLong();
	
	for (int i = 0; i < length; i++)
	{
	    bucketName = iFile.readDouble();
	    numberEntries = iFile.readLong();
	    
	    Bucket toAdd = new Bucket(bucketName, numberEntries);
	    if (Head != null)
		ptr.setCdr(toAdd);
	    else
		Head = toAdd;

	    ptr = toAdd;
	}

	return super.restoreState(iFile);	
    }
    
    /**
      Print the contents of the histogram.
      */
    
public void print ()
    {
	if (length == 0)
	    System.out.println("Empty histogram.");
	else
	    for (Bucket ptr = Head; ptr != null; ptr = ptr.cdr())
		System.out.println("Bucket : < "+ptr.Name()+", "+ptr.size()+" >");

	super.print();
    }
    
protected boolean isPresent (double value)
    {
	try
	{
	    double dummy = sizeByName(value);

	    return true;
	}
	catch (IllegalArgumentException e)
	{
	    return false;
	}
    }

protected void create (double value)
    {
	Bucket trail = null;
    
	for (Bucket ptr = Head; ptr != null; trail = ptr, ptr = ptr.cdr())
	{
	    if (ptr.Name() == value)
		return;
	    else
	    if (ptr.greaterThan(value))  // bucket is not present
		break;
	}
    
	add(trail, value, true);	
    }
    
private void add (Bucket addPosition, double value, boolean createOnly)
    {
	long initEntries;

	if (createOnly)
	    initEntries = 0;
	else
	    initEntries = 1;
	
	Bucket newBucket = new Bucket(value, initEntries);

	if (addPosition == null)  // head of list
	{
	    newBucket.setCdr(Head);
	    Head = newBucket;
	}
	else
	{
	    newBucket.setCdr(addPosition.cdr());
	    addPosition.setCdr(newBucket);
	}

	length++;
    }
    
protected long length;
protected Bucket Head;

};
