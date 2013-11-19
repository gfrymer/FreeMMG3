package simmcast.testbed;

import java.io.Serializable;

import simmcast.network.Network;

/**
 * Abstracts the notion of an address. In simulation, an address
 * corresponds to a simple numeric identifier. In actual execution
 * an address is an IP address encoded in an InetAddress object.
 *
 * @author Hisham H. Muhammad
 */
public class NetworkAddress implements Serializable {

   /**
    * The internal representation of an address.
    */
   int address;

   /**
    * Constructs an abstract representation, based on the
    * implementation-specific one.
    *
    * @param address_ The implementation-specific representation
    * of the address.
    */
   NetworkAddress(int address_) {
      address = address_;
   }

   /**
    * Returns the actual network id corresponding to this object.
    * To be used only in simulation.
    *
    * @return The implementation-specific representation
    * of the address.
    */
   int getNetworkId() {
      return address;
   }

   /**
    * Check if this is a multicast address.
    *
    * @return Returns true is this address represents a multicast group
    * rather than a node; false otherwise.
    */
   public boolean isMulticastAddress() {
      return Network.isMulticast(address);
   }

   public boolean equals(Object o) {
      if (o instanceof NetworkAddress) {
         NetworkAddress na = (NetworkAddress)o;
         return na.address == address;
      }
      return false;
   }

   public String toString() {
      return "" + address;
   }

}
