
import simmcast.node.Node;

/**
 * @author Marinho Barcellos
 */
public class SinkNode extends Node {

   /**
    * Id of source, the node that sends me packets.
    */
   int sourceId;

   /**
    * Thread that runs the sink logic.
    */
   SinkThread thr;

   public SinkNode() {
      thr = new SinkThread(this);
   }

   public void begin() {
      thr.launch();
   }

   /**
    * Sets the destination group attribute of the SinkNode object, called from the .sim file.
    *
    * @param  sourceId_  The new sourceId value
    */
   public void setSourceId(int sourceId_) {
      sourceId = sourceId_;
   }

}
