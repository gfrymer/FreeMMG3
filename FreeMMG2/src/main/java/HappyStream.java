import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;


public class HappyStream
{
	PrintStream ps = null;

	public HappyStream(File f) throws FileNotFoundException
	{
		ps = new PrintStream(f);
	}

	public HappyStream()
	{
	}

	public void println(String s)
	{
		if (ps != null) ps.println(s);
	}
}
