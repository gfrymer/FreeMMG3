import java.util.Vector;

public class Pacote extends Vector<Object>
{
	public int read_index = 0;
		
	public void addEvento(Event e)
	{
		this.add(e);
	}
	
	public void addTimestamp(String ts)
	{
		this.add(ts);
	}
	
	public Object get_next()
	{
		if (read_index == this.size())
		{
			System.err.println("acabou, size = " + size());
			int z = 0;
			z = 1 / z;
			return null;
		}
		Object o = this.get(read_index);
		read_index++;
		return o;
	}
}
