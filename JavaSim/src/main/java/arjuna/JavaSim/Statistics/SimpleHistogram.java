package arjuna.JavaSim.Statistics;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
  A simple histogram with a set number of buckets.
  */

public class SimpleHistogram extends PrecisionHistogram
{

    /**
      Create with 'nbuckets' evenly distributed over the range 'min' to 'max'.
      */
    
public SimpleHistogram (double min, double max, long nbuckets)
    {
	if (min < max)
	{
	    minIndex = min;
	    maxIndex = max;
	}
	else
	{
	    minIndex = max;
	    maxIndex = min;
	}

	if (nbuckets > 0)
	    numberBuckets = nbuckets;
	else
	    nbuckets = 1;

	width = (max - min)/numberBuckets;
	super.reset();
    }

    /**
      Create a number of buckets with width 'w' evenly distributed over the
      range 'min' to 'max'.
      */
    
public SimpleHistogram (double min, double max, double w)
    {
	if (min < max)
	{
	    minIndex = min;
	    maxIndex = max;
	}
	else
	{
	    minIndex = max;
	    maxIndex = min;
	}

	if (w > 0)
	    width = w;
	else
	    width = 2.0;

	numberBuckets = (long) ((max - min)/width);

	if ((max-min)/width - numberBuckets > 0)
	    numberBuckets++;
	
	super.reset();	
    }

    /**
      Add 'value' to the histogram. If it is outside the range of the histogram
      then raise an exception, otherwise find the appropriate bucket and
      increment it.
      */    
      
public void setValue (double value) throws IllegalArgumentException
    {
	if ((value < minIndex) || (value > maxIndex))
	    throw(new IllegalArgumentException("Value "+value+" is beyond histogram range [ "+minIndex+", "+maxIndex+" ]"));

	for (Bucket ptr = Head; ptr != null; ptr = ptr.cdr())
	{
	    double bucketValue = ptr.Name();

	    if ((value == bucketValue) || (value <= bucketValue + width))
	    {
		super.setValue(ptr.Name());
		return;
	    }
	}

	// shouldn't get here!!

	throw(new IllegalArgumentException("Something went wrong with "+value));
}

    /**
      Empty the histogram.
      */
    
public void reset ()
    {
	double value = minIndex;

	super.reset();
	
	// pre-create buckets with given width

	for (int i = 0; i < numberBuckets; value += width, i++)
	    super.create(value);
    }

    /**
      Get the number of entries in bucket 'name'.
      */
    
public double sizeByName (double name) throws IllegalArgumentException
    {
	if ((name < minIndex) || (name > maxIndex))
	    throw(new IllegalArgumentException("Argument out of range."));

	for (Bucket ptr = Head; ptr != null; ptr = ptr.cdr())
	{
	    double bucketValue = ptr.Name();

	    if ((name == bucketValue) || (name <= bucketValue + width))
		return ptr.size();
	}

	throw(new IllegalArgumentException("Name "+name+" out of range."));
    }

    /**
      Return the width of each bucket.
      */
    
public double Width ()
    {
	return width;
    }

    /**
      Print out information about the histogram.
      */
    
public void print ()
    {
	System.out.println("Maximum index range  : "+maxIndex);
	System.out.println("Minimum index range  : "+minIndex);
	System.out.println("Number of buckets    : "+numberBuckets);
	System.out.println("Width of each bucket : "+width);

	super.print();
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
	oFile.writeDouble(minIndex);
	oFile.writeDouble(maxIndex);
	oFile.writeDouble(width);
	oFile.writeLong(numberBuckets);
    
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
	minIndex = iFile.readDouble();
	maxIndex = iFile.readDouble();
	width = iFile.readDouble();
	numberBuckets = iFile.readLong();

	return super.restoreState(iFile);
    }

private double minIndex;
private double maxIndex;
private double width;
private long numberBuckets;

};
