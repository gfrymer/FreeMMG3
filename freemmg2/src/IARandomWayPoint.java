public class IARandomWayPoint implements IA
{
	public int    maxX = 0;
	public int    maxY = 0;
	public double avatarSpeed = 0; 

	public double currentX = 0.0;
	public double currentY = 0.0;
	
	public double targetX = 0.0;
	public double targetY = 0.0;
	

	public IARandomWayPoint(int maxX, int maxY, double avatarSpeed)
	{
		this.maxX = maxX;
		this.maxY = maxY;
		this.avatarSpeed = avatarSpeed;
		
		currentX = Math.random() * (double) maxX;
		currentY = Math.random() * (double) maxY;
		
		targetX = Math.random() * (double) maxX;
		targetY = Math.random() * (double) maxY;		
	}

	public int[] think(double time)
	{
		double diffX = targetX - currentX;
		double diffY = targetY - currentY;
		double dist = Math.sqrt(diffX*diffX + diffY*diffY);
		if (dist < (avatarSpeed * time) )
		{
			targetX = Math.random() * (double) maxX;
			targetY = Math.random() * (double) maxY;
		}
		else
		{
			currentX += avatarSpeed * time * (diffX / dist);
			currentY += avatarSpeed * time * (diffY / dist);
		}
		
		return get_pos();
	}

	public int[] get_pos()
	{
		int[] xy = new int[2];
		xy[0] = (int) currentX;
		xy[1] = (int) currentY;
		return xy;
	}
	
}
