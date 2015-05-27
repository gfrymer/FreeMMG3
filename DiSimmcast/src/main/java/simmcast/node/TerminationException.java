package simmcast.node;

/**
 * Exceptions of this type are thrown when
 * all threads are killed off in order to 
 * notify the application that the simulation is
 * terminated.
 * 
 * @author Hisham H. Muhammad
 */
public class TerminationException extends Exception {

   // *************************************************
   // CONSTRUCTORS
   // *************************************************

   /**
    * The default constructor.
    */
   public TerminationException() {
      super();
   }

   /**
    * The default constructor, with an error message.
    *
    * @param m_ The error message.
    */
   public TerminationException(String m_) {
      super(m_);
   }
}
