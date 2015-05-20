package simmcast.traffic;

import simmcast.node.Node;
import simmcast.node.NodeThread;
import simmcast.node.TerminationException;

public class SinkThread extends NodeThread {

   public SinkThread(Node node_) {
      super(node_);
      daemon = true;
   }

   public void execute() throws TerminationException {
      for(;;)
         receive();
   }

}
