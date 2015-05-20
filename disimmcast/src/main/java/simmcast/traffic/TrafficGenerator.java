
package simmcast.traffic;

import simmcast.node.Node;
import simmcast.node.NodeThread;

abstract class TrafficGenerator extends NodeThread {

   protected int packetSize;

   protected int to;

   protected boolean running;

   public void setPacketSize(int packetSize_) {
      packetSize = packetSize_;
   }

   public void setDestination(int to_) {
      to = to_;
   }

   public TrafficGenerator(Node node_) {
      super(node_);
   }

}
