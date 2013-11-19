package simmcast.util;

/**
 * A non-blocking resource. This is the simplest possible
 * resource policy, where acquire() behaves exactly like
 * tryAcquire(): when the resource is busy, it simply
 * returns false.
 *
 * @author Hisham H. Muhammad
 */
 
public class NonBlockingResource extends Resource {

   // **************************************************
   // CONSTRUCTOR
   // **************************************************

   /**
    * Create a resource with the given number of available
    * instances (in practice, the number of threads that
    * may access the resource at a time).
    * 
    * @param instances_ The number of instances of
    * the resource. Use 1 for a simple resource, or
    * a larger number to simulate a pool of resources.
    */

   public NonBlockingResource(int instances_) {
      super(instances_);
   }

   // **************************************************
   // RESOURCE POLICY
   // **************************************************

   public boolean failedAcquire() {
      return false;
   }

   public void uponRelease() {
   }

}