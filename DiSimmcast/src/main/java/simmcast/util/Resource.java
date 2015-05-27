package simmcast.util;

import java.util.List;
import java.util.Vector;

import simmcast.node.NodeThread;
import simmcast.node.TerminationException;

/**
 * This models a resource shared between threads of a node.
 * Threads can block themselves based on the availability
 * of a resource using the aquire() and release() primitives.
 * There is also a non-blocking primitive, tryAcquire().
 * Each subclass of Resource defines a policy which dictates
 * which of the blocked threads gets awaken when the thread
 * that is holding the resource releases it.
 * To use a resource, use the primitives API. To create a 
 * subclass, however, the primitives do not need to be
 * overriden: there are abstract methods that are called
 * by the primitives API.
 * A Resource object can also model a pool of resources, with
 * several instances.
 *
 * @author Hisham H. Muhammad
 */

abstract public class Resource {

   // *****************************************************
   // ATTRIBUTES
   // *****************************************************

   /**
    * A counter to control instances, in order to
    * simulate a pool of resources.
    */

   private int instances;

   /**
    * A control list of resource holders,
    * to mantain consistency checks.
    */

   List holders;

   // *****************************************************
   // CONSTRUCTORS
   // *****************************************************

   /**
    * Create a resource with the given number of available
    * instances (in practice, the number of threads that
    * may access the resource at a time).
    * 
    * @param instances_ The number of instances of
    * the resource. Use 1 for a simple resource, or
    * a larger number to simulate a pool of resources.
    */

   public Resource(int instances_) {
      assert instances_ > 0;
      instances = instances_;
      holders = new Vector();
   }

   // *****************************************************
   // INSTANCE MANAGEMENT
   // *****************************************************

   /**
    * General function for locking a resource, supporting
    * multiple instances.
    */

   private void lockInstance() {
      holders.add( NodeThread.currentNodeThread() );
      instances--;
   }

   /**
    * General function for unlocking a resource, supporting
    * multiple instances.
    *
    * @return Returns true if the thread was holding an
    * instance, or false otherwise.
    */
   
   private boolean unlockInstance() {
      if (!holders.remove( NodeThread.currentNodeThread() ))
         return false;
      instances++;
      return true;
   }

   // *****************************************************
   // PRIMITIVES
   // *****************************************************

   /**
    * Attempt to acquire a resource. If the resource
    * is not available, the behaviour is to be
    * determined by the subclass overriding failedAcquire().
    *
    * @return Returns true if there was an available
    * instance in the resource, or a result determined
    * by the instance's semantics.
    */

   public boolean acquire() throws TerminationException {
      if (instances > 0) {
         lockInstance();
         return true;
      } else {
         return failedAcquire();
      }
   }

   /**
    * Attempt to acquire a resource. If the resource
    * is not available, simply return false.
    * This guarantees that this call will never lock
    * the thread.
    *
    * @return Returns true if there was an available
    * instance in the resource, or false otherwise.
    */

   public boolean tryAcquire() {
      if (instances > 0) {
         lockInstance();
         return true;
      } else
         return false;
   }

   /**
    * Release the lock. The semantics of the subclass
    * will be guaranteed by calling uponRelease() on a
    * successful release.
    *
    * @return Returns true if the caller thread was indeed
    * holding the resource, false otherwise.
    */

   public boolean release() throws TerminationException {
      if (!unlockInstance())
         return false;
      uponRelease();
      return true;
   }

   // *****************************************************
   // RESOURCE POLICY
   // *****************************************************

   /**
    * Code to be executed when an acquire() is attempted
    * but the resource is unavailable. 
    * This is intended to be augmented by subclasses in
    * order to implement the resource's policy.
    *
    * @return The return value of acquire()
    * will be that of failedAcquire().
    */

   abstract public boolean failedAcquire() throws TerminationException;

   /**
    * Code to be executed after the resource is released.
    * This is intended to be augmented by subclasses in
    * order to implement the resource's policy.
    */

   abstract public void uponRelease() throws TerminationException;

}
