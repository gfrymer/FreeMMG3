import java.text.DecimalFormat;
import java.util.TreeSet;

import simmcast.network.Packet;
import simmcast.network.PacketType;
import simmcast.node.Node;
import simmcast.node.NodeThread;
import simmcast.node.TerminationException;

/**
 * @author Marinho Barcellos
 */
public class SourceThread extends NodeThread {

   SourceNode sourceNode;
   int nc = 1;

   /**
    * Constructor for the SourceThread object.
    *
    * @param node_ Description of the Parameter.
    */
   public SourceThread(Node node_) {
      super(node_);
      sourceNode = (SourceNode) node_;
   }

   public void execute() throws TerminationException {
      setName("SourceThread");
      DecimalFormat f2d = new DecimalFormat("0000");
      f2d.setMaximumFractionDigits(3);

      // Send x packets and for each of them, wait for an acknowledgement from
      // each receiver. When the timeout is triggered, it restarts the loop and
      // transmits everything again.

      int source = sourceNode.getNetworkId();
      int destination = sourceNode.gid;
      PacketType packetType = new PacketType("DATA_PACKET");
      int size = 1;

      for (int i = 1; i <= sourceNode.numPktsToSend; i++) {
         Integer msg = new Integer(i);
         boolean isRetx = false;
         boolean isAckSetComplete = false;
         TreeSet ackList = new TreeSet();
         double sendingTime = simulationScheduler.currentTime();
         while (!isAckSetComplete) {
            send(new Packet(source, destination, packetType, size, msg));
            if (isRetx) {
               System.out.println(f2d.format(simulationScheduler.currentTime()) + ": source re-sending packet and waiting for a reply");
            } else {
               System.out.println(f2d.format(simulationScheduler.currentTime()) + ": source sending packet and waiting for a reply");
               isRetx = true;
            }
            int numTimeouts = 0;
            while (!isAckSetComplete && (numTimeouts + ackList.size()) < sourceNode.gids.length) {
               // timeout is inefficiently implemented (for didactical reasons), though correctly:
               // multiple receive operations with large timeout; after last packet has arrived, source waits
               // for a period equals the timeout period set through the sim file
               Packet reply = (Packet) receive(sourceNode.timerLength);
               // received a reply
               if (reply != null) {
                  System.out.println(f2d.format(simulationScheduler.currentTime()) + ": source received reply " + reply);
                  if (!reply.getType().toString().equals("ACK")) {
                     System.out.println(f2d.format(simulationScheduler.currentTime()) +
                                        ": source received invalid response, should be ack");
                     terminateSimulation();
                     System.exit(0);
                  }
                  // checks if sequence is the same (not a late response)
                  Integer seq_r = (Integer) (reply.getData());
                  if (seq_r.equals(msg)) {
                     // adds the receiver that sent such ack to the list (do not duplicate and keep order)
                     // which one
                     int receiver = reply.getSource();
                     ackList.add(new Integer(receiver));
                     System.out.println(f2d.format(simulationScheduler.currentTime()) + ": current ackList " + ackList);
                     if (ackList.size() == sourceNode.gids.length) {
                        isAckSetComplete = true;
                     }
                  }
                  // did not receive a reply; it timed out
               } else {
                  numTimeouts++;
                  System.out.println(f2d.format(simulationScheduler.currentTime()) + ": timeout at source");
               }
            }
         }
      }
      System.err.println("SOURCE THREAD TERMINOU");
   }
}
