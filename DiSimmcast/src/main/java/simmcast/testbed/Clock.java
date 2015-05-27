package simmcast.testbed;

import simmcast.network.Network;

/**
 * Abstraction of the system timing utilities.
 */
public class Clock {

   /**
    * In simulation, time is managed by the network object.
    */
   Network network;
   
   /**
    * Initialize a clock.
    *
    * @param node_ In simulation, this can be any node, because all
    * clocks are synchronized. In actual execution, this corresponds
    * to the node (ie, the machine) this clock is relative to.
    */
   public Clock(Program node_) {
      network = node_.network;
   }

   /**
    * In actual execution, returns the number of milliseconds since
    * January 1, 1970, 00:00:00 GMT.
    * In simulation, returns the current simulated time. 
    *
    * @return The current time, in a suitable representation for the
    * selection execution method.
    */
   public double getTime() {
      if (Double.isNaN(network.simulationTime())) {
         System.err.println("Invalid network time!");
         System.exit(1);
      }
      return network.simulationTime();
   }

}
