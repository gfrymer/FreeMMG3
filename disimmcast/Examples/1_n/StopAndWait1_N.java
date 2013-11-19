
import simmcast.network.Network;

/**
 * @author     Marinho Barcellos
 */
public class StopAndWait1_N {

   public static void main(String args[]) {

      if (args.length < 1) {
         System.out.println("Usage: java StopAndWait <file.sim>");
         System.exit(1);
      }
      // creates tree network and starts the experiment
      Network myNetwork = new Network(args[0].equals("SERVER") ? null : args[1]);
      myNetwork.runSimulation(args[1]);
   }
}
