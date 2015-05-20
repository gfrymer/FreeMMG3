package arjuna.JavaSim.Distributions;

import java.io.IOException;

/**
  Returns a number from a normal distribution with the given mean and
  standard deviation.
  */

public class NormalStream extends RandomStream
{

    /**
      Create stream with mean 'm' and standard deviation 'sd'.
      */
    
public NormalStream (double m, double sd)
    {
	super();
	
	Mean  = m;
	StandardDeviation = sd;
	z = 0.0;
    }

    /**
      Create stream with mean 'm' and standard deviation 'sd'. Skip the first
      'StreamSelect' values.
      */
    
public NormalStream (double m, double sd, int StreamSelect)
    {
	super();

	Mean  = m;
	StandardDeviation = sd;
	z = 0.0;

	for (int ss = 0; ss < StreamSelect*1000; ss++)
	    Uniform();
    }

    /**
      Create stream with mean 'm' and standard deviation 'sd'. Skip the first
      'StreamSelect' values. Pass seeds 'MGSeed' and 'LCGSeed' to the base
      class.
      */
    
public NormalStream (double m, double sd, int StreamSelect,
		     long MGSeed, long LCGSeed)
    {
	super(MGSeed, LCGSeed);

	Mean = m;
	StandardDeviation = sd;
	z = 0.0;

	for (int ss = 0; ss < StreamSelect*1000; ss++)
	    Uniform();	
    }

    /**
      Return a stream number. Use the polar method, due to Box, Muller and
      Marsaglia.Taken from Seminumerical Algorithms, Knuth,
      Addison-Wesley, p.117.
      */
    
public double getNumber () throws IOException, ArithmeticException
    {
	// Use the polar method, due to Box, Muller and Marsaglia
	// Taken from Seminumerical Algorithms, Knuth, Addison-Wesley, p.117

	double X2;

	if (z != 0.0)
	{
	    X2 = z;
	    z = 0.0;
	}
	else
	{
	    double S, v1, v2;
	    do
	    {
		v1 = 2.0*Uniform()-1.0;
		v2 = 2.0*Uniform()-1.0;
		S = v1*v1 + v2*v2;
	    } while (S>=1.0);

	    S = Math.sqrt((-2.0*Math.log(S))/S);
	    X2 = v1*S;
	    z  = v2*S;
	}

	return Mean + X2*StandardDeviation;	
    }
    
private double Mean;
private double StandardDeviation;
private double z;
    
};
