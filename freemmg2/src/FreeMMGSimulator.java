import simmcast.network.Network;

public class FreeMMGSimulator
{
	public static void main(String[] args)
	{
      if (args.length < 1) {
          System.out.println("Usage: java FreeMMGSimulator <file.sim>");
          System.exit(1);
       }

		FreeMMGNetwork n = new FreeMMGNetwork(args[0].equals("MANAGER"),args[1]);
		n.runSimulation(args[0].equals("MANAGER") ? args[2] : null);
	}
}
