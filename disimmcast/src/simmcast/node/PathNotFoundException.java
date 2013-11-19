package simmcast.node;

/**
 * Exceptions of this type are thrown when a request for
 * a path is made as a packet is flowing through nodes
 * and there is no such path connecting the node the packet
 * is at to the node the packet should be directed to.
 * 
 * @author Hisham H. Muhammad
 */
public class PathNotFoundException extends Exception {

   // *************************************************
   // CONSTRUCTORS
   // *************************************************

   /**
    * The default constructor.
    */
   public PathNotFoundException() {
      super();
   }

   /**
    * The default constructor, with an error message.
    *
    * @param m_ The error message.
    */
   public PathNotFoundException(String m_) {
      super(m_);
   }
}
