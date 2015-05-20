import java.util.Random;


public class IARandomWalk implements IA
{
	public int    maxX = 0;
	public int    maxY = 0;
	public double avatarSpeed = 0; 

	public double currentX = 0.0;
	public double currentY = 0.0;
	
	public double targetX = 0.0;
	public double targetY = 0.0;
	public Random r = new Random();

	public IARandomWalk(int maxX, int maxY, double avatarSpeed)
	{
		this.maxX = maxX;
		this.maxY = maxY;
		this.avatarSpeed = avatarSpeed;
		
		currentX = Math.random() * (double) maxX;
		currentY = Math.random() * (double) maxY;	
	}

	public int[] think(double time)
	{
		double newAngle = 2 * Math.PI * ((double) r.nextInt(360)) / 360.0;
		
		currentX += (avatarSpeed * time) * Math.cos(newAngle);
		currentY += (avatarSpeed * time) * Math.sin(newAngle);	

		if (currentX <            0.0) currentX += (double) maxX;
		if (currentX >= (double) maxX) currentX -= (double) maxX;
		if (currentY <            0.0) currentY += (double) maxY;
		if (currentY >= (double) maxY) currentY -= (double) maxY;

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
