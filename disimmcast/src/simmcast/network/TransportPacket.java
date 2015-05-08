/*
 * Simmcast - a network simulation framework
 * TransportPacket.java
 * Copyright (C) 2002-2003 Hisham H. Muhammad
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package simmcast.network;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

import simmcast.distribution.CloneOnWorker;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * A transport-layer packet. This kind of packet can be used by
 * HostNodes to distinguish receptions by port. Using the
 * port field supplied by this class, you can declare reception
 * ports at hosts and issue a receive() request specifically for
 * that port.
 *
 * @author Hisham H. Muhammad
 */
public class TransportPacket extends Packet implements Cloneable {

   // *************************************************
   // ATTRIBUTES
   // *************************************************

   /**
    * A numeric identifier to identify a transport-level "port".
    * The management of the ports address space should be done
    * by the user: no controls are made by Simmcast.
    */
   protected int port;

   // *****************************************************
   // CONSTRUCTORS
   // *****************************************************

   /**
    * Constructs a new packet with the specified characteristics.
    * No consistencies are made on the validity of the entered data.
    * 
    * @param from_ Source node. 
    * @param to_ Destination node. 
    * @param port_ Destination port.
    * @param type_ An object that identifies the packet type. 
    * @param size_ The packet simulated size. 
    */
   public TransportPacket(int from_, int to_, int port_, PacketType type_, int size_) {
      super(from_, to_, type_, size_);
      port = port_;
   }

   /** 
    * Constructs a new packet with the specified characteristics. 
    * The packet holds a generic object. 
    * No consistencies are made on the validity of the entered data. 
    * 
    * @param from_ Source node. 
    * @param to_ Destination node. 
    * @param port_ Destination port.
    * @param type_ An object that identifies the packet type. 
    * @param size_ The packet simulated size. 
    * @param data_ An object, representing arbitrary data stored 
    * inside the packet. 
    */
   public TransportPacket(int from_, int to_, int port_, PacketType type_, int size_, Object data_) {
      super(from_, to_, type_, size_, data_);
      port = port_;
   }

   // ***************************************************** 
   // GETTERS/SETTERS 
   // ***************************************************** 

   /** 
    * Contents of the "port" field. Corresponds to a
    * numeric identifier to identify a transport-level "port".
    * The management of the ports address space should be done
    * by the user: no controls are made by Simmcast.
    * 
    * @return The destination port of the packet.
    */
   public int getPort() {
      return port;
   }

   // ***************************************************** 
   // TRACE OUTPUT 
   // ***************************************************** 

   /** 
    * The string representation of the packet, as used in the 
    * trace generation. 
    * 
    * @return A string that shows the packet's "type", "id", "from", 
    * "to", "port" and "data" fields. The packet's output is parenthesized 
    * to allow easy visualization when the data field contains 
    * another packet. 
    */
   public String toString() {
      return "(type "+type
            +" id "+seq
            +" from "+from
            +" to "+to
            +" port "+port
            +" (data "+data+"))";
   }

	public final static String PORT = "port";

	@Override
	public String getConstructorParameters() {
		String jstring = super.getConstructorParameters();
		JsonObject jt = new JsonParser().parse(jstring).getAsJsonObject();
		jt.addProperty(PORT, port);
		return jt.toString();
	}

	public static TransportPacket fromJson(JsonObject jo)
	{
		int from = jo.get(FROM).getAsInt();
		int to = jo.get(TO).getAsInt();
		String type = jo.get(TYPE).getAsString();
		int size = jo.get(SIZE).getAsInt();
		long sequence = jo.get(SEQUENCE).getAsLong();
		String dataClassName = jo.get(CLASS).getAsString();
		int port = jo.get(PORT).getAsInt();
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
			return new TransportPacket(from, to, port, new PacketType(type), size, n);
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
