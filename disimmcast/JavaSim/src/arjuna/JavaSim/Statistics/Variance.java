package arjuna.JavaSim.Statistics;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
  Used to obtain the variance of the samples given.
  */

public class Variance extends Mean
{
    
public Variance ()
    {
	reset();
    }

    /**
      Add 'value', updating the variance.
      */
    
public void setValue (double value) throws IllegalArgumentException
    {
	_sqr += value*value;
	super.setValue(value);
    }

    /**
      Zero the statistics.
      */
    
public void reset ()
    {
	_sqr = 0.0;
	super.reset();
    }

    /**
      Returns the variance.
      */
    
public double variance ()
    {
	if (_Number > 1)
	    return ((_sqr - ((_Sum * _Sum) / _Number)) / (_Number -1));
	else
	    return 0.0;
    }

    /**
      Returns the standard deviation of the samples.
      */
      
public double stdDev ()
    {
	if (_Number == 0 || variance() <= 0)
	    return 0.0;
	else
	    return Math.sqrt(variance());	
    }

    /**
      Returns the confidence.
      */
      
public double confidence (double value)
    {
	System.out.println("Variance::confidence not implemented yet.");
	return 0.0;	
    }

    /**
      Prints out the statistics information.
      */
    
public void print ()
    {
	System.out.println("Variance          : "+variance());
	System.out.println("Standard Deviation: "+stdDev());

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
	oFile.writeDouble(_sqr);
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
	_sqr = iFile.readDouble();

	return super.restoreState(iFile);
    }
    
private double _sqr;

};
