package simmcast.traffic;

import simmcast.network.Packet;
import simmcast.network.PacketType;
import simmcast.node.Node;
import simmcast.node.TerminationException;

public class CBR extends TrafficGenerator {

   private double rate = 1000.0;

   public CBR(Node node_) {
      super(node_);
      running = true;
   }

   public void setRate(double rate_) {
      rate = rate_;
   }

   public void activate() {
      running = true;
      this.wakeUp();
   }

   public void deactivate() {
      running = false;
   }

   public void execute() throws TerminationException {
      PacketType cbrPacketType = new PacketType("CBR");

      for (;;) {
         if (running) {
            Packet p = new Packet(node.getNetworkId(), to, cbrPacketType,
                                  packetSize);
            send(p);
            sleep(packetSize / rate);
         }
         else {
            sleep();
         }
      }
   }
}
