package simmcast.util;

import simmcast.node.NodeThread;
import simmcast.node.NodeThreadQueue;
import simmcast.node.TerminationException;

/**
 * In this resource model, threads are waken up
 * in the same order their acquire requests were
 * issued.
 */

public class QueuedResource extends Resource {

   // *****************************************************
   // ATTRIBUTES
   // *****************************************************

   /**
    * The queue of threads that are to be awaken.
    * This is managed in a FIFO policy.
    */

   NodeThreadQueue queue;

   // *****************************************************
   // CONSTRUCTORS
   // *****************************************************

   /**
    * Creates a new resource.
    * 
    * @param instances_ The number of instances of
    * the resource. Use 1 for a simple resource, or
    * a larger number to simulate a pool of resources.
    */

   public QueuedResource(int instances_) {
      super(instances_);
      queue = new NodeThreadQueue();
   }

   /**
    * When the acquire primitive fails, put the thread
    * in a queue and block it.
    */

   public boolean failedAcquire() throws TerminationException {
      boolean result;
      NodeThread caller = NodeThread.currentNodeThread();
      queue.push(caller);
      do {
         caller.sleep();
      } while (!tryAcquire());
      return true;
   }

   /**
    * When the resource is released, unblock the first
    * thread in the queue.
    */

   public void uponRelease() {
      NodeThread next = queue.pop();
      next.wakeUp();
   }

}
