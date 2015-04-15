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
}
