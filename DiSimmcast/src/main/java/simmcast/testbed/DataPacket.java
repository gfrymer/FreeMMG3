package simmcast.testbed;

import simmcast.network.Packet;

/**
 * Abstracts a datagram packet. In actual execution, this
 * is a direct correspondence. In simulation, this is an object
 * passed as the 'data' field of a transport packet.
 *
 * @author Hisham H. Muhammad
 */
public class DataPacket {

   /**
    * The actual data contained in this packet. All other
    * information can be considered header overhead. This
    * is the only piece of data that gets sent in actual
    * execution.
    */
   byte[] data;
   
   /**
    * The packet size. It is important in the simulation mode.
    * In actual execution it is the actual size of the data
    * array, but length should contain this at all times.
    */
   int length;
   
   /**
    * The address of the packet.
    */
   NetworkAddress address;
   
   /**
    * Address of the node that sent the packet.
    */
   NetworkAddress source;

   /**
    * Build a packet. In the simulated execution, this
    * kind of packet is built out of a simulator packet.
    *
    * @param packet The simulator packet used as a basis
    * to costruct this packet.
    */
   DataPacket(Packet packet) {
      data = (byte[])packet.getData();
      length = packet.getSize();
      // Don't try to understand this. I don't.
      address = new NetworkAddress(packet.getSource());
      source = new NetworkAddress(packet.getSource());
   }

   /**
    * The actual data contained in this packet. All other
    * information can be considered header overhead. This
    * is the only piece of data that gets sent in actual
    * execution.
    *
    * @return An array of bytes representing the sequence
    * of data enclosed in this datagram.
    */
   public byte[] getData() {
      return data;
   }

   /**
    * The packet size. It is important in the simulation mode.
    * In actual execution it is the actual size of the data
    * array, but length should contain this at all times.
    *
    * @return The packet size.
    */
   public int getLength() {
      return length;
   }

   /**
    * Get the packet source address.
    *
    * @return Address of the node that sent the packet.
    */
   public NetworkAddress getSource() {
      return address;
   }

   /**
    * Get the address packet.
    *
    * @return The address of the packet.
    */
   public NetworkAddress getAddress() {
      return address;
   }

}
