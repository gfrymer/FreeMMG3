package simmcast.distribution.command;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import simmcast.network.Network;
import simmcast.network.Packet;
import simmcast.network.PacketType;
import simmcast.network.PathAccountQueue;

public class CommandPacketArrival extends CommandProtocol {

	public final static String FROM_WORKER = "worker";
	public final static String RELATIVE_TIME = "rtime";
	public final static String PACKET = "packet";

	String fromWorker;
	double relativeTime;
	Packet packet;

	public CommandPacketArrival(int mClientId, int mCmdId, byte mAction, String mParameter)
	{
		super(mClientId, mCmdId, mAction, mParameter);
		JsonObject jo = getJsonParameters();
		if (jo!=null)
		{
			fromWorker = jo.get(FROM_WORKER).getAsString();
			relativeTime = jo.get(RELATIVE_TIME).getAsDouble();
			try {
				JsonObject po = new JsonParser().parse(jo.get(PACKET).getAsString()).getAsJsonObject();
				packet = Packet.fromJson(po);
			} catch (JsonSyntaxException ex)
			{
			}
		}
	}

	public CommandPacketArrival(String mFromWorker, double mRelativeTime, Packet mPacket)
	{
		super(0, CommandProtocol.getNextCmdId(), ACTION_PACKET_ARRIVAL, "");
		fromWorker = mFromWorker;
		relativeTime = mRelativeTime;
		packet = mPacket;
		JsonObject gson = new JsonObject();
		gson.addProperty(FROM_WORKER, fromWorker);
		gson.addProperty(RELATIVE_TIME, relativeTime);
		gson.addProperty(PACKET, packet.getConstructorParameters());
		parameters = gson.toString();
	}

	public String run(Network network)
	{
		if (packet!=null)
		{
			PathAccountQueue a = new PathAccountQueue();
			a.enqueue();
			network.getNodeById(packet.getDestination()).getScheduler().schedulePacketArrival(relativeTime, packet, a);
		}
		return null;
	}

	public String getFromWorker()
	{
		return fromWorker;
	}
}
