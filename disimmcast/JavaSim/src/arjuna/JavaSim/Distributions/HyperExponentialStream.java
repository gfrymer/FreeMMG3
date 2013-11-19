package arjuna.JavaSim.Distributions;

import java.io.IOException;

/**
 Returns a number from a hyperexpontial distribution with the given mean
 and standard deviation.
 */

public class HyperExponentialStream extends RandomStream
{

    /**
      Create stream with mean 'm' and standard deviation 'sd'.
      */
    
public HyperExponentialStream (double m, double sd)
    {
	super();

	Mean = m;
	StandardDeviation = sd;

	double cv, z;
	cv = StandardDeviation/Mean;
	z = cv*cv;
	p = 0.5*(1.0-Math.sqrt((z-1.0)/(z+1.0)));
    }

    /**
      Create stream with mean 'm' and standard deviation 'sd'. Skip the first
      'StreamSelect' values.
      */
    
public HyperExponentialStream (double m, double sd, int StreamSelect)
    {
	super();

	Mean = m;
	StandardDeviation = sd;

	double cv, z;
	cv = StandardDeviation/Mean;
	z = cv*cv;
	p = 0.5*(1.0-Math.sqrt((z-1.0)/(z+1.0)));

	for (int ss = 0; ss < StreamSelect*1000; ss++)
	    Uniform();
    }

    /**
      Create stream with mean 'm' and standard deviation 'sd'. Skip the first
      'StreamSelect' values. Pass seeds 'MGSeed' and 'LCGSeed' to the base
      class.
      */
    
public HyperExponentialStream (double m, double sd, int StreamSelect,
			       long MGSeed, long LCGSeed)
    {
	super(MGSeed, LCGSeed);

	Mean = m;
	StandardDeviation = sd;

	double cv, z;
	cv = StandardDeviation/Mean;
	z = cv*cv;
	p = 0.5*(1.0-Math.sqrt((z-1.0)/(z+1.0)));
	
	for (int ss = 0; ss < StreamSelect*1000; ss++)
	    Uniform();	
    }

    /**
      Return a value from the stream.
      */
    
public double getNumber () throws IOException, ArithmeticException
    {
	double z = 0;

	if (Uniform() > p)
	    z = Mean/(1.0-p);
	else
	    z = Mean/p;
	
	return -0.5*z*Math.log(Uniform());
    }
    
private double Mean;
private double StandardDeviation;
private double p;
    
};
