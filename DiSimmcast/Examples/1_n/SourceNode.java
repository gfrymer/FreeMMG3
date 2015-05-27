import simmcast.group.Group;
import simmcast.node.Node;

/**
 * @author Marinho Barcellos
 */
public class SourceNode extends Node {
   SourceThread thr;
   
   /**
    * Default value must be very large to prevent false timeouts.
    */
   double timerLength = 10000.0;

   int numPktsToSend = 10;

   /**
    * Number of times an ending packet will be sent to receivers before quiting.
    */
   int numEndingPackets = 3;

   public int gid;

   public int gids[];

   public void begin() {
      thr.launch();
   }

   public SourceNode() {
      thr = new SourceThread(this);
   }

   /**
    * Sets the group attribute.
    *
    * @param g_ The new group value
    */
   public void setDestinationGroup(Group g_) {
      gid = g_.getNetworkId();
      gids = g_.getNetworkIds();
   }

   public void setDestinationGroupIds(int gid_,int[] gids_) {
      gid = gid_;
      gids = gids_;
   }

   public void setTimerLength(double timerLength_) {
      timerLength = timerLength_;
   }

   public void setNumPktsToSend(int numPktsToSend_) {
      numPktsToSend = numPktsToSend_;
   }

   public void setNumEndingPackets(int numEndingPackets_) {
      numEndingPackets = numEndingPackets_;
   }

}
