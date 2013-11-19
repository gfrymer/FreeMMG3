package simmcast.util;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import simmcast.node.NodeThread;
import simmcast.node.TerminationException;
import arjuna.JavaSim.Distributions.RandomStream;
import arjuna.JavaSim.Distributions.UniformStream;

/**
 * In this resource model, when a resource is released,
 * a thread is randomly chosen among the ones that are
 * blocked.
 */

public class RandomResource extends Resource {

   // *****************************************************
   // ATTRIBUTES
   // *****************************************************

   /**
    * The stream that controls the uniform random distribution
    * of the thread unlocking.
    */

   RandomStream stream;

   /**
    * The list of threads that are waiting to be unlocked.
    */

   List waitingList;

   // *****************************************************
   // CONSTRUCTORS
   // *****************************************************

   /**
    * Declare a resource with the given number of instances
    * available.
    * 
    * @param instances_ The number of instances of
    * the resource. Use 1 for a simple resource, or
    * a larger number to simulate a pool of resources.
    */

   public RandomResource(int instances_) {
      super(instances_);
      stream = new UniformStream(0.0, 1.0);
      waitingList = new Vector();
   }

   /**
    * Declare a resource, with a custom seed.
    * 
    * @param instances_ The number of instances of
    * the resource. Use 1 for a simple resource, or
    * a larger number to simulate a pool of resources.
    * @param mgSeed_ The seed for the uniform random
    * stream on which this resource is based (MG
    * parameter).
    * @param lcgSeed_ The seed for the uniform random
    * stream on which this resource is based (LCG
    * parameter).
    */

   public RandomResource(int instances_, long mgSeed_, long lcgSeed_) {
      super(instances_);
      stream = new UniformStream(0.0, 1.0, 0, mgSeed_, lcgSeed_);
      waitingList = new Vector();
   }

   // *****************************************************
   // RESOURCE POLICY
   // *****************************************************

   /**
    * When the request for a resource fails, the thread
    * enters a list and keeps blocked until the resource
    * is available. Therefore, acquire operations, sooner
    * or later, will always return true (unless, of course,
    * the resource holder deadlocks -- in this case, the
    * requester will starve).
    *
    * @return Always returns true, since eventually the
    * thread will be unblocked and succeed.
    */

   public boolean failedAcquire() throws TerminationException {
      boolean result;
      NodeThread caller = NodeThread.currentNodeThread();
      waitingList.add(caller);
      do {
         caller.sleep();
      } while (!tryAcquire());
      waitingList.remove(caller);
      return true;
   }

   /**
    * When a thread releases a resource, it picks
    * a thread from the list of waiting threads at random
    * (based on an uniformly distributed number stream)
    * and passes it on.
    */

   public void uponRelease() {
      try {

         int size = waitingList.size();
         if (size > 0) {
            int random = (int) (stream.getNumber() * size);
            NodeThread next = (NodeThread)waitingList.get(random);
            next.wakeUp();
         }

      } catch (IOException e) {}
   }

}
