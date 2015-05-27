import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

import simmcast.network.Packet;
import simmcast.node.HostNode;
import simmcast.node.Node;
import simmcast.node.NodeThread;
import simmcast.node.TerminationException;

public class BackupCellNode extends HostNode
{
	public int            primaryCellNodeRef = 0;
	public FreeMMGNetwork global         = null;
	public LowerLayer     lower          = new LowerLayer();
	public HappyStream    logger         = new HappyStream();
	public String         loggerFilename = "BCN_";
	
	public Integer lock = 0;
	public int downloadEstimative = 0;
	public int uploadEstimative = 0;
	int[] vetor = new int[LowerLayer.TOTAL_COUNTS];
	
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
				
				Packet p = lower.receive(this, logger, vetor);
				downloadEstimative += vetor[LowerLayer.DOWN_BACKUPS];
				uploadEstimative   += vetor[LowerLayer.UP_BACKUPS];
				
				Pacote pkt = (Pacote) p.getData();
////			logger.println("[" + global.get_time() + "] Backup " + primaryCellNodeRef + " received message " + p.getType() + " from " + p.getSource() + ", sended at " + (String) pkt.get_next());
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
					logger.println("[" + global.get_time() + "] download = " + downloadEstimative + " bps, upload = " + uploadEstimative + " bps");
					downloadEstimative = 0;
					uploadEstimative   = 0;
				}

				this.sleep(1000.0);
			}
		}		
	}
/*	
	public void setPrimaryNode(Node n)
	{
		primaryCellNodeRef = n.getNetworkId();
	}
*/
	public void setLoggerName(String filename)
	{
		loggerFilename = filename;
	}
	
	public void begin()
	{
		global = (FreeMMGNetwork) this.network;
		lower.setGlobal(global);
		primaryCellNodeRef = networkId - (global.getAsInt("world_width") * global.getAsInt("world_height") * global.getAsInt("ncell")); 
		
		try
		{
			File f = new File(loggerFilename + primaryCellNodeRef + ".txt");
			logger  = new HappyStream(f);
		}
		catch (FileNotFoundException e)
		{
			System.exit(0);
		}
		
		
		
		ReceiveThread recv_thread  = new ReceiveThread(this);
		CountThread   count_thread = new CountThread(this);
		
		recv_thread.launch();
		count_thread.launch();
	}
}
