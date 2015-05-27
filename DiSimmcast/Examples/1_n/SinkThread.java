
import java.text.DecimalFormat;

import simmcast.network.Packet;
import simmcast.network.PacketType;
import simmcast.node.Node;
import simmcast.node.NodeThread;
import simmcast.node.TerminationException;

/**
 *
 * @author Marinho Barcellos
 */
public class SinkThread extends NodeThread {

   SinkNode myself;
   
   /**
    * create ack packet type
    */
   PacketType ackPacketType = new PacketType("ACK");

   public SinkThread(Node node_) {
      super(node_);
      myself = (SinkNode) node_;
      daemon = true;
   }
   
   /**
    * Overall description of the protocol, sink side: it waits for packets from the source and,
    * for each packet received, it sends back an acknowledgment.
    */
   public void execute() throws TerminationException {
      setName("SinkThread "+node.getName());
      System.out.println("Executing sink thread " + node.getName() + ", node " + node.getNetworkId());
      DecimalFormat f2d = new DecimalFormat("0000");
      int counter = 0;

      while(true) {

         Packet packet = receive();
         
         System.out.println(f2d.format(simulationScheduler.currentTime()) + ": sink " +
                            node.getName() + " " + myself.getNetworkId() + " received " + packet + ", sending ACK as reply");

         String packet_type = packet.getType().toString();
         // received a packet that indicates the end of the communication
         if (packet_type.equals("END")) {
            // create reply to end packet
            Packet pkt = new Packet(myself.getNetworkId(), myself.sourceId, ackPacketType, 1);
            // and send it
            send(pkt);

            // data packet from source
         } else if (packet_type.equals("DATA_PACKET")) {
            // one more recvd
            counter++;
            // get source net id
            int source = myself.getNetworkId();
            // determine the id of the node I want to reply to
            int destination = myself.sourceId;
            // the info sent in the packet, a sequence
            Integer seq = (Integer) (packet.getData());
            // create reply packet of ack type
            Packet reply = new Packet(source, destination,
                                      ackPacketType, 1, seq);
            // and send it
            send(reply);
         } else {
            System.out.println("error: sink " + myself.getName() + " received packet of unknown type " + packet.getType());
            System.exit(1);
         }
      }
   }

}
