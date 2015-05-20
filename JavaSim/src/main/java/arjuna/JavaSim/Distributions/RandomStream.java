package arjuna.JavaSim.Distributions;

import java.io.IOException;

/**
  The class RandomStream is the base class from which the other distribution
  classes are derived.
  It uses a linear congruential generator based on the algorithm from
  "Algorithms", R. Sedgewick, Addison-Wesley, Reading MA, 1983 pp. 36-38.
  The results of the LC generator are shuffled with a multiplicative generator
  as suggested by Maclaren and Marsaglia (See Knuth Vol2, Seminumerical
  Algorithms).
  The multiplicative generator is courtesy I. Mitrani 1992, private
  correspondence:
  Y[i+1] = Y[i] * 5^5 mod 2^26, period is 2^24, initial seed must be odd
  */

public abstract class RandomStream
{

    /**
      In derived classes this method returns the value obtained by the
      stream. It must be redefined by the deriving class.
      */
    
public abstract double getNumber () throws IOException, ArithmeticException;

    /**
      Returns a chi-square error measure on the uniform distribution function.
      */
    
public final double Error ()
    {
	long r = 100;
	long N = 100*r;
	long f[] = new long[100];
	int i;
	
	for (i = 0; i < r; i++) f[i] = 0;
	for (i = 0; i < N; i++) f[(int) (Uniform()*r)]++;
	long t = 0;
	for (i = 0; i < r; i++) t += f[i]*f[i];
	double rt = (double) r*t;
	double rtN = rt / (double) N - (double) N;
	return 1.0 - (rtN / r);
    }

protected RandomStream ()
    {
	series = new double[128];
    
	MSeed = 772531;
	LSeed = 1878892440;
	
	for (int i = 0; i < RandomStream.sizeOfSeries/RandomStream.sizeOfDouble; i++)
	    series[i] = MGen();	
    }

protected RandomStream (long MGSeed, long LCGSeed)
    {
	series = new double[128];
	
	// Clean up input parameters
    
	if ((MGSeed&1) == 0) MGSeed--;
	if (MGSeed < 0) MGSeed = -MGSeed;
	if (LCGSeed < 0) LCGSeed = -LCGSeed;

	// Initialise state
    
	MSeed = MGSeed;
	LSeed = LCGSeed;

	for (int i = 0; i < RandomStream.sizeOfSeries/RandomStream.sizeOfDouble; i++)
	    series[i] = MGen();	
    }
    
protected final double Uniform ()
    {
	// A linear congruential generator based on the algorithm from
	// "Algorithms", R. Sedgewick, Addison-Wesley, Reading MA, 1983.
	// pp. 36-38.
    
	long m = 100000000;
	long b = 31415821;
	long m1 = 10000;

	// Do the multiplication in pieces to avoid overflow
    
	long p0 = LSeed%m1,
	     p1 = LSeed/m1,
	     q0 = b%m1,
	     q1 = b/m1;

	LSeed = (((((p0*q1+p1*q0)%m1)*m1+p0*q0)%m) + 1) % m;

	// The results of the LC generator are shuffled with
	// the multiplicative generator as suggested by
	// Maclaren and Marsaglia (See Knuth Vol2, Seminumerical Algorithms)

	long choose = LSeed % (RandomStream.sizeOfSeries/RandomStream.sizeOfDouble);

	double result = series[(int) choose];
	series[(int) choose] =  MGen();

	return result;
    }

private double MGen ()
    {
	// A multiplicative generator, courtesy I. Mitrani 1992,
	// private correspondence
	// Y[i+1] = Y[i] * 5^5 mod 2^26
	// period is 2^24, initial seed must be odd

	long two2the26th = 67108864;	// 2**26

	MSeed = (MSeed * 25) % two2the26th;
	MSeed = (MSeed * 25) % two2the26th;
	MSeed = (MSeed * 5) % two2the26th;

	return (double) MSeed / (double) two2the26th;	
    }
    
private long MSeed;
private long LSeed;
private double[] series;

    /*
     * We do this so that we can have the same results when running on most
     * Unix boxes with C++. It doesn't make any difference to the randomness
     * of a distribution.
     */
    
static private final long sizeOfSeries = 1024;
static private final long sizeOfDouble = 8;

}
