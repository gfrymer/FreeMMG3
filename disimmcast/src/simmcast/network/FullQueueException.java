package simmcast.network;

/**
 * This exception is thrown when a size limit that was explicitly
 * set on a queue overflows (ie, an attempt to add an element on
 * a full queue occurs).
 *
 * @author Hisham H. Muhammad
 */
public class FullQueueException extends QueueException {

   // **************************************************
   // CONSTRUCTORS
   // **************************************************

   /**
    * Implements the default constructor.
    */
   public FullQueueException() {
      super();
   }

   /**
    * Implements the default constructor with a message.
    */
   public FullQueueException(String m_) {
      super(m_);
   }
}