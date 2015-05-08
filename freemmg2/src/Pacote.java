import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Vector;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import simmcast.distribution.CloneOnWorker;
import simmcast.network.Packet;
import simmcast.network.PacketType;

public class Pacote extends Vector<Object> implements CloneOnWorker
{
	public int read_index = 0;

	public Pacote()
	{
		
	}

	public Pacote(int ridx)
	{
		read_index = ridx;
	}

	public void addEvento(Event e)
	{
		this.add(e);
	}

	public void addTimestamp(String ts)
	{
		this.add(ts);
	}

	public Object get_next()
	{
		if (read_index == this.size())
		{
			System.err.println("acabou, size = " + size());
			int z = 0;
			z = 1 / z;
			return null;
		}
		Object o = this.get(read_index);
		read_index++;
		return o;
	}

	public final static String READIDX = "readidx";
	public final static String EVENTS = "events";

	public final static String CLASS_NAME = "cname";
	public final static String DATA = "data";

	@Override
	public String getConstructorParameters()
	{
		JsonObject gson = new JsonObject();
		gson.addProperty(READIDX, read_index);
		JsonArray ja = new JsonArray();
		for (int i=0; i<this.size(); i++)
		{
			Object o = this.get(i);
			String cname = o.getClass().getName();
			JsonObject jo = new JsonObject();
			jo.addProperty(CLASS_NAME, cname);
			if (o instanceof CloneOnWorker)
			{
				jo.addProperty(DATA, ((CloneOnWorker) o).getConstructorParameters());
			}
			else
			{
				if (o instanceof String)
				{
					jo.addProperty(DATA, (String) o);
				}
				else if (o instanceof Number)
				{
					jo.addProperty(DATA, (Number) o);
				}
				else if (o instanceof Boolean)
				{
					jo.addProperty(DATA, (Boolean) o);
				}
				else if (o instanceof Character)
				{
					jo.addProperty(DATA, (Character) o);
				}
				else
				{
					jo.addProperty(DATA, o.toString());
				}
			}
			ja.add(jo);
		}
		gson.add(EVENTS, ja);
		return gson.toString();
	}

	public static Pacote fromJson(JsonObject jo)
	{
		int readidx = jo.get(READIDX).getAsInt();
		Pacote pct = new Pacote(readidx);
		JsonArray ja = jo.get(EVENTS).getAsJsonArray();
		JsonParser jp = new JsonParser();
		for (int i=0; i<ja.size(); i++)
		{
			JsonObject js = ja.get(i).getAsJsonObject();
			String cname = js.get(CLASS_NAME).getAsString();			
			try {
				Object n = null;
				Class r = Class.forName(cname);
				try {
					java.lang.reflect.Method mt = r.getMethod("fromJson", JsonObject.class);
					n = mt.invoke(null, new JsonParser().parse(js.get(DATA).getAsString()).getAsJsonObject());
				} catch (NoSuchMethodException ne)
				{
				}
				if (n==null)
				{
					if (r.equals(java.lang.Integer.class))
					{
						n = new java.lang.Integer(js.get(DATA).getAsInt());
					}
					else if (r.equals(java.lang.String.class)) {
						n = js.get(DATA).getAsString();
					}
					else if (r.equals(java.lang.Float.class)) {
						n = new java.lang.Float(js.get(DATA).getAsFloat());
					}					
					else if (r.equals(java.lang.Double.class)) {
						n = new java.lang.Double(js.get(DATA).getAsDouble());
					}
					else if (r.equals(java.lang.Long.class)) {
						n = new java.lang.Long(js.get(DATA).getAsLong());
					}
					else if (r.equals(java.lang.Byte.class)) {
						n = new java.lang.Byte(js.get(DATA).getAsByte());
					}
					else if (r.equals(java.lang.Character.class)) {
						n = new java.lang.Character(js.get(DATA).getAsCharacter());
					}
					else if (r.equals(java.lang.Short.class)) {
						n = new java.lang.Short(js.get(DATA).getAsShort());
					}
					else if (r.equals(java.lang.Boolean.class)) {
						n = new java.lang.Boolean(js.get(DATA).getAsBoolean());
					}
					else if (r.equals(java.math.BigInteger.class)) {
						n = js.get(DATA).getAsBigInteger();
					}
					else if (r.equals(java.math.BigDecimal.class)) {
						n = js.get(DATA).getAsBigDecimal();
					}
					else {
						n = js.get(DATA);
						System.out.println("Unrecognized class: " + r.getName());
					}
				}
				pct.add(n);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
		return pct;
	}
}
