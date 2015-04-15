import java.util.Vector;

import simmcast.network.Packet;
import simmcast.network.PacketType;
import simmcast.node.NodeThread;
import simmcast.node.TerminationException;

public class LowerLayer
{
	public Vector<OutgoingPacket> notAckedPackets = new Vector<OutgoingPacket>();
	public Integer seqGenerator = 0;
	public FreeMMGNetwork global = null;
	
	public static final int DOWN_PLAYERS   = 0;
	public static final int UP_PLAYERS     = 1;
	public static final int DOWN_BACKUPS   = 2;
	public static final int UP_BACKUPS     = 3;
	public static final int DOWN_SYNC      = 4;
	public static final int UP_SYNC        = 5;
	public static final int DOWN_NEIGHBORS = 6;
	public static final int UP_NEIGHBORS   = 7;
	public static final int TOTAL_COUNTS   = 8;
	
	
	public void setGlobal(FreeMMGNetwork n)
	{
		global = n;
	}

	public class OutgoingPacket
	{
		public int        to_;
		public PacketType type_;
		public int        size_;
		public Object     data_;
		public int        seq = 0;
		public double     orig_time = 0.0;
		
		public OutgoingPacket(int to_, PacketType type_, int size_, Object data_, double now)
		{
			this.to_ = to_;
			this.type_ = type_;
			this.size_ = size_;
			this.data_ = data_;
			this.seq = seqGenerator;
			this.orig_time = now;

			synchronized (seqGenerator)
			{
				seqGenerator = seqGenerator + 1;
			}
		}
	}
	
	public synchronized void send(int to_, PacketType type_, int size_, Object data_, boolean reliable, double resend_timeout, NodeThread myThread, HappyStream out, int[] vetor) throws TerminationException
	{
		for (int i = 0; i < TOTAL_COUNTS; i++) vetor[i] = 0;
		
		double now = Double.parseDouble(global.get_time());

		OutgoingPacket my_op = null;
		if (reliable) my_op = new OutgoingPacket(to_, type_, size_, data_, now);
		
		for (OutgoingPacket op : notAckedPackets)
		{
			if ( (now - op.orig_time) >= resend_timeout)
			{
				myThread.send(op.to_, op.type_, op.size_, op.data_);
				     if (op.type_ == FreeMMGNetwork.CTB_UPDATE) vetor[UP_BACKUPS] += op.size_;
				else if (op.type_ == FreeMMGNetwork.CTC_SYNC)   vetor[UP_SYNC]    += op.size_;
				op.orig_time = now;
			}
		}

		Object my_data = data_;
		if (reliable)
		{
			Pacote p = new Pacote();
			p.add(my_op.seq);
			for (Object o : (Pacote) data_) p.add(o); 
			my_data = p;
//			out.println("enviando " + type_ + " to " + to_ + " with " + p.size() + " chunks!");
		}
		my_op.data_ = my_data;
		
		myThread.send(to_, type_, size_, my_data);

		     if (type_ == FreeMMGNetwork.CTB_UPDATE)  vetor[UP_BACKUPS]   += size_;
		else if (type_ == FreeMMGNetwork.CTC_SYNC)    vetor[UP_SYNC]      += size_;
		else if (type_ == FreeMMGNetwork.CTP_UPDATE)  vetor[UP_PLAYERS]   += size_;
		else if (type_ == FreeMMGNetwork.ICTC_UPDATE) vetor[UP_NEIGHBORS] += size_;
		
		if (reliable) notAckedPackets.add(my_op);
	}

	public Packet receive(NodeThread myThread, HappyStream out, int vetor[]) throws TerminationException
	{
		for (int i = 0; i < TOTAL_COUNTS; i++) vetor[i] = 0;
		
		while (true)
		{
			Packet     p    = myThread.receive();
			PacketType type = p.getType();
			
			     if (type == FreeMMGNetwork.CTB_UPDATE)   vetor[DOWN_BACKUPS]   += p.getSize();
			else if (type == FreeMMGNetwork.CTC_SYNC)     vetor[DOWN_SYNC]      += p.getSize();
			else if (type == FreeMMGNetwork.PTC_POSITION) vetor[DOWN_PLAYERS]   += p.getSize();
			else if (type == FreeMMGNetwork.ICTC_UPDATE)  vetor[DOWN_NEIGHBORS] += p.getSize();
			else if (type == FreeMMGNetwork.BTC_ACK)      vetor[DOWN_BACKUPS]   += p.getSize();
			else if (type == FreeMMGNetwork.CTC_ACK)      vetor[DOWN_SYNC]      += p.getSize();
		
			boolean reliable = false;
			     if (type == FreeMMGNetwork.CTC_SYNC)   reliable = true;
			else if (type == FreeMMGNetwork.CTB_UPDATE) reliable = true;
			else if (type == FreeMMGNetwork.BTC_ACK)    reliable = true;
			else if (type == FreeMMGNetwork.CTC_ACK)    reliable = true;
			if (reliable == false) return p;
	
			if ( (type == FreeMMGNetwork.BTC_ACK) || (type == FreeMMGNetwork.CTC_ACK) )
			{
				int seq = (Integer) p.getData();
				synchronized (this)
				{
					for (int i = 0; i < notAckedPackets.size(); i++)
					{
						if (notAckedPackets.get(i).seq == seq)
						{
							notAckedPackets.remove(i);
							break;
						}
					}
					continue;
				}
			}
			else
			{
				Pacote real_pacote = (Pacote) p.getData();
///				out.println("packet type = " + p.getType() + ", with number of chunks = " + real_pacote.size());
				int seq = (Integer) real_pacote.get(0);
				Pacote my_pacote = new Pacote();
				for (int i = 1; i < real_pacote.size(); i++) my_pacote.add(real_pacote.get(i));
				
				PacketType t = (type == FreeMMGNetwork.CTC_SYNC) ? FreeMMGNetwork.CTC_ACK : FreeMMGNetwork.BTC_ACK;

				myThread.send(p.getSource(), t, FreeMMGNetwork.HEADER_LEN + 2, seq);
				
				     if (type == FreeMMGNetwork.CTC_SYNC)   vetor[UP_SYNC]    += FreeMMGNetwork.HEADER_LEN + 2;
				else if (type == FreeMMGNetwork.CTB_UPDATE) vetor[UP_BACKUPS] += FreeMMGNetwork.HEADER_LEN + 2;

				return new Packet(p.getSource(), p.getDestination(), p.getType(), p.getSize(), my_pacote);
			}
		}
	}
	
}
