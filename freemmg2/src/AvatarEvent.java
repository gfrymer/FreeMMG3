import com.google.gson.JsonObject;

public class AvatarEvent extends Event
{
	public int x   = 0;
	public int y   = 0;
	public int id  = 0;
	public int seq = 0;
	public boolean ghost = false;
	public String timestamp = null;
	
	public AvatarEvent() {}
	
	public AvatarEvent(Avatar a)
	{
		x = a.x;
		y = a.y;
		id = a.id;
		seq = a.seq;
		timestamp = a.timestamp;
	}

	@Override
	public int getSize()
	{
		return FreeMMGNetwork.PLAYER_EVENT_SIZE;
	}

	public final static String X = "x";
	public final static String Y = "y";
	public final static String ID = "id";
	public final static String SEQ = "seq";
	public final static String GHOST = "ghost";
	public final static String TS = "ts";

	@Override
	public String getConstructorParameters()
	{
		JsonObject gson = new JsonObject();
		gson.addProperty(X, x);
		gson.addProperty(Y, y);
		gson.addProperty(ID, id);
		gson.addProperty(SEQ, seq);
		gson.addProperty(GHOST, ghost);
		gson.addProperty(TS, timestamp);
		return gson.toString();
	}
	
	public static AvatarEvent fromJson(JsonObject jo)
	{
		AvatarEvent ae = new AvatarEvent();
		ae.x = jo.get(X).getAsInt();
		ae.y = jo.get(Y).getAsInt();
		ae.id = jo.get(ID).getAsInt();
		ae.seq = jo.get(SEQ).getAsInt();
		ae.ghost = jo.get(GHOST).getAsBoolean();
		ae.timestamp = jo.get(TS).getAsString();
		return ae;
	}
}
