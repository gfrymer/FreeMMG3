package simmcast.network;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import simmcast.distribution.CloneOnWorker;

/**
 * A more sophisticated type of packet that allows, for example,
 * deferring definition and posterior redefining of the
 * packet fields, as well as control of a Time-To-Live parameter.
 *
 * @author Hisham H. Muhammad
 */
public class NetworkPacket extends Packet {

   // *****************************************************
   // CONSTANTS
   // *****************************************************

   /**
    * A flag to indicate a nonzero value is temporarily unset.
    */
   protected static final int TEMP_UNSET = 0;

   /**
    * The maximum accepted Time-To-Live for network-layer packets,
    * ie, the maximum allowed number of hops a network-layer packet
    * can flow through.
    */
   // TODO: shouldn't there be only one MAX_TTL constant in
   // the entire simulator (perhaps at the Network ;) )?
   public static final int MAX_TTL = 128;


   /**
    * A global packet type to indicate network-layer packets.
    * Network-layer packets encapsulate transport-layer packets.
    */
   public static final PacketType PACKET_TYPE = new PacketType("NETWORK");

   // *****************************************************
   // ATTRIBUTES
   // *****************************************************

   /**
    * "Time to live". Number of hops a packet can travel through before
    * it is automatically discarded.
    */
   int ttl;

   // *****************************************************
   // CONSTRUCTORS
   // *****************************************************

   /**
    * Construct a packet, deferring the definition of the destination
    * field.
    *
    * @param from_ Source field.
    * @param size_ The simulated size.
    * @param data_ An arbitrary object representing data stored within
    * the packet.
    */
   public NetworkPacket(int from_, int size_, CloneOnWorker data_) {
      super(from_, TEMP_UNSET, PACKET_TYPE, size_, data_);
   }
   
   /**
    * Construct a packet, specifing a specific Time-To-Live value.
    *
    * @param from_ Source field.
    * @param to_ Destination field. Setting this as NetworkPacket.TEMP_UNSET
    * defers its definition.
    * @param size_ The simulated size.
    * @param data_ An arbitrary object representing data stored within
    * the packet.
    */
   public NetworkPacket(int from_, int to_, int size_, CloneOnWorker data_, int ttl_) {
      super(from_, to_, PACKET_TYPE, size_, data_);
      ttl = (ttl_ <= MAX_TTL ? ttl_ : MAX_TTL);
   }

   // *****************************************************
   // GETTERS/SETTERS
   // *****************************************************

   /**
    * Redefines the destination field of the packet.
    *
    * @param to_ The new value for the destination field.
    */
   public void setDestination(int to_) {
      to = to_;
   }

   /**
    * Redefines the source field of the packet.
    *
    * @param from_ The new value for the source field.
    */
   public void setSource(int from_) {
      from = from_;
   }
   
   /**
    * Sets the packet "time to live".
    *
    * @param ttl_ The new value for the packet's "Time-To-Live".
    */
   public void setTTL(int ttl_) {
      ttl = (ttl_ <= MAX_TTL ? ttl_ : MAX_TTL);
   }
   
   /**
    * Inspect the packet's "Time-To-Live".
    *
    * @return The packet's TTL.
    */
   public int getTTL() {
      return ttl;
   }

	public final static String TTL = "ttl";

	@Override
	public String getConstructorParameters() {
		String jstring = super.getConstructorParameters();
		JsonObject jt = new JsonParser().parse(jstring).getAsJsonObject();
		jt.addProperty(TTL, ttl);
		return jt.toString();
	}

	public static NetworkPacket fromJson(JsonObject jo)
	{
		int from = jo.get(FROM).getAsInt();
		int to = jo.get(TO).getAsInt();
		String type = jo.get(TYPE).getAsString();
		int size = jo.get(SIZE).getAsInt();
		long sequence = jo.get(SEQUENCE).getAsLong();
		String dataClassName = jo.get(CLASS).getAsString();
		int ttl = jo.get(TTL).getAsInt();
		try {
			Object n = null;
			Class r = Class.forName(dataClassName);
			try {
				java.lang.reflect.Method mt = r.getMethod("fromJson", JsonObject.class);
				n = mt.invoke(null, new JsonParser().parse(jo.get(DATA).getAsString()).getAsJsonObject());
			} catch (NoSuchMethodException ne)
			{
			}
			if (n==null)
			{
				Constructor[] c = r.getConstructors();
				for (int i=0;i<c.length;i++)
				{
					Type[] pt = c[i].getParameterTypes();
					if (pt.length==1)
					{
						if (pt[0].equals(String.class))
						{
							n = c[i].newInstance(jo.get(DATA).getAsString());
							break;
						}
						if (pt[0].equals(JsonObject.class))
						{
							n = c[i].newInstance(new JsonParser().parse(jo.get(DATA).getAsString()).getAsJsonObject());
							break;
						}
					}
				}
			}
			return new NetworkPacket(from, to, size, (CloneOnWorker) n, ttl);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
}
