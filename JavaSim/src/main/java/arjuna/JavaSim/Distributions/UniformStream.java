package arjuna.JavaSim.Distributions;

import java.io.IOException;

/**
  Returns a  number drawn from a uniform distribution with the given lower
  and upper bounds.
  */

public class UniformStream extends RandomStream
{

    /**
      Create stream with low bound 'l' and high bound 'h'.
      */
    
public UniformStream (double l, double h) 
    {
	super();
	
	lo = l;
	hi = h;
	range = hi-lo;
    }

    /**
      Create stream with low bound 'l' and high bound 'h'. Skip the first
      'StreamSelect' values before returning numbers from the stream.
      */
    
public UniformStream (double l, double h, int StreamSelect)
    {
	super();

	lo = l;
	hi = h;
	range = hi-lo;

	for (int i = 0; i < StreamSelect*1000; i++)
	    Uniform();
    }

    /**
      Create stream with low bound 'l' and high bound 'h'. Skip the first
      'StreamSelect' values before returning numbers from the stream. Pass
      the seeds 'MGSeed' and 'LCGSeed' to the base class.
      */
    
public UniformStream (double l, double h, int StreamSelect,
		      long MGSeed, long LCGSeed)
    {
	super(MGSeed, LCGSeed);

	lo = l;
	hi = h;
	range = hi-lo;

	for (int i = 0; i < StreamSelect*1000; i++)
	    Uniform();
    }

    /**
      Return a number from the stream.
      */
    
public double getNumber () throws IOException, ArithmeticException
    {
	return lo+(range*Uniform());
    }

private double lo;
private double hi;
private double range;
    
};
