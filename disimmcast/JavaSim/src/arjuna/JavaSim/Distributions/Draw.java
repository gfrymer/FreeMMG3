package arjuna.JavaSim.Distributions;

import java.io.IOException;

/**
  Return true or false with probability given when constructed. Uses a
  UniformStream.
  */

public class Draw
{

    /**
      Probability of true is 'p'.
      */
    
public Draw (double p)
    {
	s = new UniformStream(0, 1);
	prob = p;
    }

    /**
      Probability 'p'. Ignore the first 'StreamSelect' values before starting
      to return values.
      */
    
public Draw (double p, int StreamSelect)
    {
	s = new UniformStream(0, 1, StreamSelect);
	prob = p;
    }
	
    /**
      Probability 'p'. Ignore the first 'StreamSelect' values before starting
      to return values. The seeds to the UniformStream are 'MGSeed'
      and 'LGSeed'.
      */
    
public Draw (double p, int StreamSelect, long MGSeed, long LCGSeed)
    {
	s = new UniformStream(0, 1, StreamSelect, MGSeed, LCGSeed);
	prob = p;
    }

    /**
      Return true with specified probability.
      */
    
public boolean getBoolean () throws IOException
    {
	if (s.getNumber() >= prob)
	    return true;
	else
	    return false;
    }
    
private UniformStream s;
private double prob;

};
