import java.io.File;
import java.io.FileNotFoundException;

import simmcast.network.Packet;
import simmcast.node.HostNode;
import simmcast.node.Node;
import simmcast.node.NodeThread;
import simmcast.node.TerminationException;

public class BigPlayer extends HostNode
{
	FreeMMGNetwork global = null;
	IA[] ia = null;
	public int           mySequenceGenerator = 1;
	public int[]         myId                = null;
	public int[]         ownerCell           = null;
	public HappyStream   logger              = new HappyStream();
	
	public Integer       lock               = 666;
	public int[]         downloadEstimative = null;
	public int[]         uploadEstimative   = null;
	
	public class ReceiveThread extends NodeThread
	{
		protected ReceiveThread(Node node_)
		{
			super(node_);
		}

		@Override
		public void execute() throws TerminationException
		{
			while (true)
			{
//				if (global.simulationTime() >= global.simulationTotal) break;
				
				Packet p    = receive();
				Pacote pkt  = (Pacote)  p.getData();
				String time = (String)  pkt.get_next();
				int to_     = (Integer) pkt.get_next();
///				logger.println("[" + global.get_time() + "] Player " + getNetworkId() + " received message " + p.getType() + " from " + p.getSource() + ", sended at " + time + ", with " + p.getSize() + " bytes");
				int k = to_ - global.getAsInt("WHN") - 1 - 1;

				synchronized (lock)
				{
					downloadEstimative[k] += p.getSize();
				}
				
				int num_avatars = 0;
				try
				{
					num_avatars = (Integer) pkt.get_next();
				}
				catch( java.lang.ClassCastException e)
				{
					System.out.println("Quem enviou eh o " +  p.getSource() + ", quem recebe eh o " + to_ + " e a msg eh " + p.getType() + ", time = " + time);
					throw e;
				}
				
				for (int i = 0; i < num_avatars; i++)
				{
					Avatar a = (Avatar) pkt.get_next();
///					logger.println("\t\tPlayer " + a.id + " esta na posicao <" + a.x + ", " + a.y + ">");
				} 
///				logger.println("Done");
			}
		}		
	}
	
	public class SendThread extends NodeThread
	{
		protected SendThread(Node node_)
		{
			super(node_);
		}

		public void sendUpdate() throws TerminationException
		{
			for (int j = 0; j < global.getAsInt("NUM_PLAYERS"); j++)
			{
				Pacote pkt = new Pacote();
				pkt.add(global.get_time());
				pkt.add(myId[j]);
				
				int[] xy = ia[j].think( ((double) global.getAsDouble("PTC_refresh_tick")) / 1000.0);
				int col = xy[0] / global.getAsInt("cell_width");
				int row = xy[1] / global.getAsInt("cell_height");
								
				AvatarEvent ae = new AvatarEvent();
				ae.x         = xy[0];
				ae.y         = xy[1];
				ae.id        = myId[j];
				ae.seq       = mySequenceGenerator;
				ae.timestamp = global.get_time();
				pkt.add(ae);
				
				int newOwnerCell = row * global.getAsInt("world_width") + col;
				if (newOwnerCell != ownerCell[j])
				{
//					logger.println("[" + global.get_time() + "] Player " + networkId + " tries to change of cell \t\t\t\t\t" + ownerCell + " >>> " + newOwnerCell);
///					System.out.println("row = " + row + ", col = " + col);
///					System.out.println("[" + global.get_time() + "] Player " + networkId + " tries to change of cell \t\t\t\t\t" + ownerCell + " >>> " + newOwnerCell);
					ownerCell[j] = newOwnerCell;
				}
				
//				logger.println("[" + global.get_time() + "] Player " + networkId + " tries to move to <" + xy[0] + ", " + xy[1] + "> (cell " + ownerCell + ")");
				
				int k = (int) (Math.random() * (double) global.getAsInt("ncell")); 

				int BLAH = ownerCell[j] * global.getAsInt("ncell") + k + 1;
				if (BLAH > global.getAsInt("WHN"))
				{
					System.out.println("owner = " + ownerCell[j] + ", ncell = " + global.getAsInt("ncell") + ", k = " + k);
					throw new RuntimeException();
				}
				
				send(BLAH, FreeMMGNetwork.PTC_POSITION, FreeMMGNetwork.HEADER_LEN + FreeMMGNetwork.PLAYER_EVENT_SIZE, pkt);
				
				synchronized (lock)
				{
					uploadEstimative[j] += FreeMMGNetwork.HEADER_LEN + FreeMMGNetwork.PLAYER_EVENT_SIZE;
				}

				mySequenceGenerator++;
			}		
		}

		@Override
		public void execute() throws TerminationException
		{
			while (true)
			{
				if (global.simulationTime() >= global.getAsDouble("simulationTotal")) break;
				
				sendUpdate();
					
				this.sleep(global.getAsDouble("PTC_refresh_tick"));
			}
		}		
	}

	public class CountThread extends NodeThread
	{
		protected CountThread(Node node_)
		{
			super(node_);
		}

		@Override
		public void execute() throws TerminationException
		{
			while (true)
			{
				if (global.simulationTime() >= global.getAsDouble("simulationEnd")) break;
				
				synchronized (lock)
				{
					for (int k = 0; k < global.getAsInt("NUM_PLAYERS"); k++)
					{
						logger.println("[" + global.get_time() + "] download = " + downloadEstimative[k] + " bps, upload = " + uploadEstimative[k] + " bps");
						downloadEstimative[k] = 0;
						uploadEstimative[k]   = 0;
					}
				}

				this.sleep(1000.0);
			}
		}
	}
	
	public void setLoggerName(String filename)
	{
		try
		{
			File f = new File(filename + networkId + ".txt");
			logger   = new HappyStream(f);
		}
		catch (FileNotFoundException e)
		{
		}
	}

	public void begin()
	{
		global = (FreeMMGNetwork) this.network;
		
		int total_width  = global.getAsInt("cell_width")  * global.getAsInt("world_width");
		int total_height = global.getAsInt("cell_height") * global.getAsInt("world_height");

		ia                  = new IA[global.getAsInt("NUM_PLAYERS")];
		myId                = new int[global.getAsInt("NUM_PLAYERS")];
		ownerCell           = new int[global.getAsInt("NUM_PLAYERS")];
		downloadEstimative  = new int[global.getAsInt("NUM_PLAYERS")];
		uploadEstimative    = new int[global.getAsInt("NUM_PLAYERS")];
	
		int idBase = global.getAsInt("WHN") + 1 + 1;
		for (int k = 0; k < global.getAsInt("NUM_PLAYERS"); k++)
		{
			myId[k]      = idBase + k;  
			ia[k]        = new IARandomWalk(total_width, total_height, 128.0);		
			
			int[] xy     = ia[k].get_pos();
			int col      = xy[0] / global.getAsInt("cell_width");
			int row      = xy[1] / global.getAsInt("cell_height");
			ownerCell[k] = row * global.getAsInt("world_width") + col;
			
			downloadEstimative[k] = 0;
			uploadEstimative[k]   = 0;
		}
		
		
		ReceiveThread recv_thread  = new ReceiveThread(this);
		SendThread    send_thread  = new SendThread(this);
		CountThread   count_thread = new CountThread(this);

		recv_thread.launch();
		send_thread.launch();
		count_thread.launch();
	}
}
