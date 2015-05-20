package arjuna.JavaSim.Statistics;


/**
  Provides a means of obtaining the p-quantile of a distribution, i.e., the
  value below which p-percent of the distribution lies.
  */

public class Quantile extends PrecisionHistogram
{

    /**
      Create with 95% probability.
      */
    
public Quantile ()
    {
	qProb = 0.95;
    }

    /**
      Create with the specified probability. If the probability it greater
      than 100% (1.0) or less than or equal to 0% then throw an exception.
      */
    
public Quantile (double q) throws IllegalArgumentException
    {
	qProb = q;

	if ((q <= 0.0) || (q > 1.0))
	    throw(new IllegalArgumentException("Quantile::Quantile ( "+q+" ) : bad value."));
    }

    /**
      Return the p-quantile.
      */
      
public double getValue ()
    {
	double pSamples = numberOfSamples() * qProb;
	long nEntries = 0;
	Bucket ptr = Head, trail = null;

	while ((nEntries < pSamples) && (ptr != null))
	{
	    nEntries += ptr.size();
	    trail = ptr;
	    ptr = ptr.cdr();
	}

	return trail.Name();
    }

    /**
      Return the p-quantile percentage.
      */
    
public double range ()
    {
	return qProb;
    }

    /**
      Print out the quantile information.
      */
    
public void print ()
    {
	System.out.println("Quantile precentage : "+qProb);
	System.out.println("Value below which percentage occurs "+this.getValue());
	super.print();
    }
    
private double qProb;
    
};
