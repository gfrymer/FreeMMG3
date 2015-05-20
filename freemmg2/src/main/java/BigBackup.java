import simmcast.network.Packet;
import simmcast.node.HostNode;
import simmcast.node.Node;
import simmcast.node.NodeThread;
import simmcast.node.TerminationException;

public class BigBackup extends HostNode
{
	public FreeMMGNetwork global             = null;
	
	public int[]          primaryCellNodeRef = null;
	public LowerLayer     lower              = new LowerLayer();
	public HappyStream    logger             = null;
	public String         loggerFilename     = null;
	
	public Integer        lock               = 666;
	public int[]          downloadEstimative = null;
	public int[]          uploadEstimative   = null;
	public int[]          vetor              = new int[LowerLayer.TOTAL_COUNTS];
	
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
				if (global.simulationTime() >= global.getAsDouble("simulationTotal")) break;
				
				Packet p    = lower.receive(this, logger, vetor);
				Pacote pkt  = (Pacote)  p.getData();
				String time = (String)  pkt.get_next();
				int to_     = (Integer) pkt.get_next();
				int k       = to_ - global.getAsInt("WHN") - 1;

				synchronized (lock)
				{
					downloadEstimative[k] += vetor[LowerLayer.DOWN_BACKUPS];
					uploadEstimative[k]   += vetor[LowerLayer.UP_BACKUPS];
				}

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
					for (int k = 0; k < global.getAsInt("WHN"); k++)
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
	
	public void begin()
	{
		global = (FreeMMGNetwork) this.network;

		primaryCellNodeRef = new int[global.getAsInt("WHN")];
		lower.setGlobal(global);

		loggerFilename     = "BCN.txt";
		logger             = new HappyStream();

		downloadEstimative = new int[global.getAsInt("WHN")];
		uploadEstimative   = new int[global.getAsInt("WHN")];
		
		for (int k = 0; k < global.getAsInt("WHN"); k++)
		{
			primaryCellNodeRef[k] = k+1;

			downloadEstimative[k] = 0;
			uploadEstimative[k] = 0;
		}
		
		ReceiveThread recv_thread  = new ReceiveThread(this);
		CountThread   count_thread = new CountThread(this);
		
		recv_thread.launch();
		count_thread.launch();
	}
}
