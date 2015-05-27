import com.google.gson.JsonObject;

import simmcast.distribution.CloneOnWorker;

public class Avatar implements CloneOnWorker
{
	public int id        = 0;
	public int x         = 0;
	public int y         = 0;
	public int seq       = 0;
	
	public int lastRound = 0;
	public String timestamp = null;
	
	public Avatar(AvatarEvent ev) //int id, int x, int y, int 
	{
		id = ev.id;
		x = ev.x;
		y = ev.y;
		seq = ev.seq;
		timestamp = ev.timestamp;
	}

	public int getSize()
	{
		return FreeMMGNetwork.PLAYER_AVATAR_LEN;
	}

	public final static String X = "x";
	public final static String Y = "y";
	public final static String ID = "id";
	public final static String SEQ = "seq";
	public final static String LASTROUND = "lround";
	public final static String TS = "ts";

	public static Avatar fromJson(JsonObject jo)
	{
		AvatarEvent ae = new AvatarEvent();
		ae.x = jo.get(X).getAsInt();
		ae.y = jo.get(Y).getAsInt();
		ae.id = jo.get(ID).getAsInt();
		ae.seq = jo.get(SEQ).getAsInt();
		ae.timestamp = jo.get(TS).getAsString();
		Avatar a = new Avatar(ae);
		a.lastRound = jo.get(LASTROUND).getAsInt();
		return a;
	}

	public String getConstructorParameters()
	{
		JsonObject gson = new JsonObject();
		gson.addProperty(X, x);
		gson.addProperty(Y, y);
		gson.addProperty(ID, id);
		gson.addProperty(SEQ, seq);
		gson.addProperty(LASTROUND, lastRound);
		gson.addProperty(TS, timestamp);
		return gson.toString();
	}
}
