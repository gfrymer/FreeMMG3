package arjuna.JavaSim.Distributions;

import java.io.IOException;

/**
  Returns a number from an Erlang distribution with the given mean and
  standard deviation.
  */

public class ErlangStream extends RandomStream
{
    /**
      Create a stream with mean 'm' and standard deviation 'sd'.
      */
    
public ErlangStream (double m, double sd)
    {
	super();

	Mean = m;
	StandardDeviation = sd;

	double z = Mean/StandardDeviation;
	k = (long) (z*z);
    }

    /**
      Create a stream with mean 'm' and standard deviation 'sd'. Ignore the
      first 'StreamSelect' values before starting to return values.
      */
    
public ErlangStream (double m, double sd, int StreamSelect)
    {
	super();

	Mean = m;
	StandardDeviation = sd;

	double z = Mean/StandardDeviation;
	k = (long) (z*z);
	for (int ss = 0; ss < StreamSelect*1000; ss++)
	    Uniform();
    }

    /**
      Create a stream with mean 'm' and standard deviation 'sd'. Ignore the
      first 'StreamSelect' values before starting to return values.
      The seeds to the RandomStream are 'MGSeed' and 'LGSeed'.
      */
    
public ErlangStream (double m, double sd, int StreamSelect,
		     long MGSeed, long LCGSeed)
    {
	super(MGSeed, LCGSeed);

	Mean = m;
	StandardDeviation = sd;

	double z = Mean/StandardDeviation;
	k = (long) (z*z);
	for (int ss = 0; ss < StreamSelect*1000; ss++)
	    Uniform();	
    }

    /**
      Return a stream number.
      */
    
public double getNumber () throws IOException, ArithmeticException
    {
	double z = 1.0;
	for (int i = 0; i < k; i++)
	    z *= Uniform();
	
	return -(Mean/k)*Math.log(z);
    }
    
private double Mean;
private double StandardDeviation;
private long k;
    
};
