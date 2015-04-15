public class Avatar
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
}
