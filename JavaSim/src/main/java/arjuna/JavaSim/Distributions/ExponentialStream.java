package arjuna.JavaSim.Distributions;

import java.io.IOException;

/**
  Returns a number from an exponential distribution with the given mean.
  */

public class ExponentialStream extends RandomStream
{

    /**
      Create stream with mean 'm'.
      */
    
public ExponentialStream (double m)
    {
	super();

	Mean = m;
    }

    /**
      Create stream with mean 'm'. Skip the first 'StreamSelect' stream values.
      */
    
public ExponentialStream (double m, int StreamSelect)
    {
	super();

	Mean = m;

	for (int i = 0; i < StreamSelect*1000; i++)
	    Uniform();
    }
    
    /**
      Create stream with mean 'm'. Skip the first 'StreamSelect' stream values.
      Pass seeds 'MGSeed' and 'LCGSeed' to the base class.
      */
    
public ExponentialStream (double m, int StreamSelect,
			  long MGSeed, long LCGSeed)
    {
	super(MGSeed, LCGSeed);

	Mean = m;

	for (int i = 0; i < StreamSelect*1000; i++)
	    Uniform();
    }

    /**
      Return stream number.
      */
    
public double getNumber () throws IOException, ArithmeticException
    {
	return -Mean*Math.log(Uniform());
    }

private double Mean;
    
};
