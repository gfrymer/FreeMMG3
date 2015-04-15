import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Vector;

import simmcast.network.Packet;
import simmcast.node.HostNode;
import simmcast.node.Node;
import simmcast.node.NodeThread;
import simmcast.node.TerminationException;

public class PrimaryCellNode extends HostNode
{
	public FreeMMGNetwork global = null;
	public HappyStream    logger = new HappyStream();
	public String         loggerFilename = "PCN_";
	
	public int             backupCellNodeRef = 0;
	public int             myCellNumber      = 0;
	public int             myRowNumber       = 0;
	public int             myColNumber       = 0;
	public Vector<Integer> cellMembers       = new Vector<Integer>();
	public Integer[]       worldNeighbors    = new Integer[8];

	public Vector<Event>   outgoing_events   = new Vector<Event>();
	
	public Integer         toBackupBytesCruft = 0;
	
	public Integer lock = 0;
	public int downloadEstimativeFromPlayers = 0;
	public int uploadEstimativeFromPlayers   = 0;
	public int downloadEstimativeFromBackup  = 0;
	public int uploadEstimativeFromBackup    = 0;
	public int downloadEstimativeFromSync    = 0;
	public int uploadEstimativeFromSync      = 0;
	public int downloadEstimativeFromICTC    = 0;
	public int uploadEstimativeFromICTC      = 0;
	
	public Vector<Integer> downloadPlayerVector = new Vector<Integer>();
	public Vector<Integer> uploadPlayerVector   = new Vector<Integer>();
	public Vector<Integer> downloadBackupVector = new Vector<Integer>();
	public Vector<Integer> uploadBackupVector   = new Vector<Integer>();
	public Vector<Integer> downloadSyncVector   = new Vector<Integer>();
	public Vector<Integer> uploadSyncVector     = new Vector<Integer>();
	public Vector<Integer> downloadICTCVector   = new Vector<Integer>();
	public Vector<Integer> uploadICTCVector     = new Vector<Integer>();
	public Vector<Integer> downloadTotalVector   = new Vector<Integer>();
	public Vector<Integer> uploadTotalVector     = new Vector<Integer>();
	
	
	
	// map<round, map<nodeid, lista<evento>>>
	public Map<Integer, Map<Integer, Vector<Event>> > tabelao = new HashMap<Integer, Map<Integer, Vector<Event>>>();
	
	public CellState cs = new CellState();  // Conservatively-synchronized cell state
	public CellState os = new CellState();  // Optimistically-synchronized cell state 
	
	LowerLayer lower = new LowerLayer();
	public boolean DEBUG = true;
	
	public double dist(Avatar a, Avatar b)
	{
		return Math.sqrt((a.x-b.x)*(a.x-b.x)+(a.y-b.y)*(a.y-b.y));
	}

	// Verifica se pode avancar simulacao (conservador)
	public boolean checkTabelao()
	{
		Map<Integer, Vector<Event>> coluna_do_round = tabelao.get(cs.currentRound + 1);
		if (coluna_do_round == null) return false;
		
		for (Integer member : cellMembers)
		{
			if (coluna_do_round.get(member) == null) return false; 
		}
	
//		logger.println("[" + global.get_time() + "] Cell " + networkId + " executando passo do simulador conservador");
		cs.executeRoundConservative( coluna_do_round );
		
		return true;
	}
	
	public class ReceiveThread extends NodeThread
	{
		int[] vetor = new int[LowerLayer.TOTAL_COUNTS];

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
///				System.gc();
				
				Packet p = null;
				p = lower.receive(this, logger, vetor);
				synchronized (lock)
				{
					downloadEstimativeFromPlayers += vetor[LowerLayer.DOWN_PLAYERS];
					downloadEstimativeFromBackup  += vetor[LowerLayer.DOWN_BACKUPS];
					uploadEstimativeFromBackup    += vetor[LowerLayer.UP_BACKUPS];
					downloadEstimativeFromSync    += vetor[LowerLayer.DOWN_SYNC];
					uploadEstimativeFromSync      += vetor[LowerLayer.UP_SYNC];
					downloadEstimativeFromICTC    += vetor[LowerLayer.DOWN_NEIGHBORS];
				}

				Pacote pkt = (Pacote) p.getData();
				pkt.read_index = 0;
				
				String time = (String) pkt.get_next();
			
				if (p.getType() == FreeMMGNetwork.PTC_POSITION)
				{
					int to_ = (Integer) pkt.get_next();
					AvatarEvent ae = (AvatarEvent) pkt.get_next();
					if (DEBUG) logger.println("[" + global.get_time() + "] Cell   " + networkId + " recebeu PTC_POSITION from " + to_ + ", sended at " + time);
					boolean useful = os.execute(ae, global, logger.ps);
					if (DEBUG) if (useful) logger.println("[" + global.get_time() + "] Cell " + networkId + " adicionando player " + to_ + " em os.avatars, em funcao de um PTC_POSITION");
					synchronized (outgoing_events)
					{
						if (useful) outgoing_events.add(ae);
					}
				}
				else if (p.getType() == FreeMMGNetwork.CTC_SYNC)
				{
					synchronized (tabelao)
					{
						int round = (Integer) pkt.get_next();
						int num_avatar_events = (Integer) pkt.get_next();
						if (round <= cs.currentRound) continue;
						
						Map<Integer, Vector<Event>> coluna_do_round = tabelao.get(round);
						if (coluna_do_round == null)
						{
							tabelao.put(round, new HashMap<Integer, Vector<Event>>());
							coluna_do_round = tabelao.get(round);
						}
						Vector<Event> lista_de_eventos = coluna_do_round.get(p.getSource());
						if (lista_de_eventos == null)
						{
							coluna_do_round.put(p.getSource(), new Vector<Event>());
							lista_de_eventos = coluna_do_round.get(p.getSource());
						}

						synchronized (toBackupBytesCruft)
						{
							toBackupBytesCruft += 6;	// sender ID + round  + numero de avatars
							if (DEBUG) if (num_avatar_events != 0) logger.println("[" + global.get_time() + "] Cell   " + networkId + " recebeu CTC_SYNC with " + num_avatar_events + " AvatarEvents, from " + p.getSource() + ", sended at " + time);
							for (int i = 0; i < num_avatar_events; i++)
							{
								AvatarEvent ae = (AvatarEvent) pkt.get_next();
								toBackupBytesCruft += ae.getSize();
								lista_de_eventos.add(ae);
								if (DEBUG) logger.println("[" + global.get_time() + "] evento " + i + ", ae.id = " + ae.id);
								boolean useful = os.execute(ae, global, logger.ps);
								if (DEBUG) if (useful) logger.println("[" + global.get_time() + "] Cell " + networkId + " adicionando (ou atualizando) player " + ae.id + " em os.avatars, em funcao de um CTC_SYNC");
							}
						}

						while (checkTabelao());
					}
					
					// TODO: Um dia o Fabio vai processar o resto do pacote.
				}
				else if (p.getType() == FreeMMGNetwork.ICTC_UPDATE)
				{
					synchronized (tabelao)
					{
						int num_avatars = (Integer) pkt.get_next();
					
						if (DEBUG) if (num_avatars != 0) logger.println("[" + global.get_time() + "] Cell   " + networkId + " recebeu ICTC_UPDATE with " + num_avatars + " AVATARS, from " + p.getSource() + ", sended at " + time);
						for (int i = 0; i < num_avatars; i++)
						{
							Avatar a = (Avatar) pkt.get_next();
							AvatarEvent ae = new AvatarEvent(a);
							ae.ghost = true;
///							logger.println("[" + global.get_time() + "] Cell " + networkId + " recebeu ICTC_UPDATE");
							boolean useful = os.execute(ae, global, logger.ps);
///							if (useful) logger.println("[" + global.get_time() + "] Cell " + networkId + " adicionando (ou atualizando) player " + a.id + " em os.ghosts, em funcao de um ICTC_UPDATE");
							synchronized (outgoing_events)
							{
								if (useful) outgoing_events.add(ae);
							}
						}
						
						while (checkTabelao());
					}
					
					// TODO: Um dia o Fabio vai processar o resto do pacote.
				}	
			}
		}		
	}

	public class AvatarComparator implements Comparator<Avatar>
	{
		public Avatar a = null;

		public AvatarComparator(Avatar a)
		{
			this.a = a;
		}
		
		@Override
		public int compare(Avatar o1, Avatar o2)
		{
			double dist1 = dist(a, o1);
			double dist2 = dist(a, o2);
			     if (dist1 < dist2) return 0;
			else if (dist1 > dist2) return 0;
			else return 0;
		}
		
	}
	
	public class SendThread extends NodeThread
	{
		protected SendThread(Node node_)
		{
			super(node_);
		}
		
		// SYNC
		int[] vetor = new int[LowerLayer.TOTAL_COUNTS];

		public void stepSync() throws TerminationException
		{
			os.executeRoundOptimist(logger, networkId);
			
			int pktLenEstimative = FreeMMGNetwork.HEADER_LEN;
			Pacote pkt = new Pacote();
			pkt.add(global.get_time());

			int len = outgoing_events.size();
			int events_size = 0;
			synchronized (outgoing_events)
			{
				pkt.add(os.currentRound);
				pktLenEstimative += 2;
				pkt.add(len);
				pktLenEstimative += 2;
				for (Event e: outgoing_events) { pkt.addEvento(e); events_size += e.getSize(); }
				outgoing_events.clear();
			}
			pktLenEstimative += events_size;
	
			
			for (int id : cellMembers)
			{
				if (id == networkId) 
				{
					synchronized (toBackupBytesCruft)
					{
						// ID sender + current round + outgoing_events.size()
						toBackupBytesCruft += 6;
						toBackupBytesCruft += events_size;
					}
					continue;
				}

				if (DEBUG) if (len != 0) logger.println("[" + global.get_time() + "] Cell " + networkId + " ta enviando CTC_SYNC para " + id + " com " + len + " AvatarEvents");
				
				// envio eh reliable, entao no send existe reenvio de pacotes, ja computa aqui os custos!				
				if ( (id < 1) || (id > global.getAsInt("WHN")) )
				{
					throw new RuntimeException("AAAAAAAAA deu pau no SYNC!");					
				}
	
				lower.send(id, FreeMMGNetwork.CTC_SYNC, pktLenEstimative, pkt, true, 1.25*(4.0 * global.getAsDouble("latency_mean")), this, logger, vetor);
				synchronized (lock)
				{
					uploadEstimativeFromBackup    += vetor[LowerLayer.UP_BACKUPS];
					uploadEstimativeFromSync      += vetor[LowerLayer.UP_SYNC];
				}
			} // do for
		}

		// ICTC
		int x = 0;
		int y = 0;
		Random gerador = new Random();
		
		public void stepICTC() throws TerminationException
		{
			int ncell = cellMembers.size();
			
			int first_node_in_the_cell = myCellNumber * ncell + 1;
			
			int leader = first_node_in_the_cell + y;

			boolean tenho_que_mandar_ictc = false;
			int y2 = leader;
			
			// minha diferenca em relacao ao lider da celula 
			int my_diff = 0;
			
			for (my_diff = 0; my_diff < worldNeighbors.length; my_diff++)
			{
				if (networkId == y2)
				{
					tenho_que_mandar_ictc = true;
					break;
				}
				
				y2++;
				if (y2 >= (first_node_in_the_cell + ncell) ) y2 = first_node_in_the_cell;
			}

			if (tenho_que_mandar_ictc)
			{				
				int index_da_matriz = (my_diff + x) % worldNeighbors.length;
	
				int cell_que_eu_vou_atualizar = worldNeighbors[index_da_matriz];
	
				int target_na_celula_que_eu_vou_atualizar = gerador.nextInt(ncell);
				int BLAH = cell_que_eu_vou_atualizar * ncell + target_na_celula_que_eu_vou_atualizar + 1;
	
				
				////// AGORA GERANDO PACOTE QUE EU VOU MANDAR PRO INFELIZ
				
				Pacote pkt = new Pacote();
				pkt.add(global.get_time());
				
				int t_row[] = {-1, -1, -1,  0,  0, +1, +1, +1};
				int t_col[] = {-1,  0, +1, -1, +1, -1,  0, +1};			

				int targetRow = myRowNumber + t_row[index_da_matriz];
				if (targetRow <  0) targetRow += global.getAsInt("world_height");
				if (targetRow >= global.getAsInt("world_height")) targetRow -= global.getAsInt("world_height");
				
				int targetCol = myColNumber + t_col[index_da_matriz];
				if (targetCol <  0) targetCol += global.getAsInt("world_width");
				if (targetCol >= global.getAsInt("world_width")) targetCol -= global.getAsInt("world_width");

				
				
				int limSup   =  targetRow    * global.getAsInt("cell_height") - global.getAsInt("soi_radius");				
				int limInf   = (targetRow+1) * global.getAsInt("cell_height") + global.getAsInt("soi_radius");
				int limLeft  =  targetCol    * global.getAsInt("cell_width") - global.getAsInt("soi_radius");
				int limRight = (targetCol+1) * global.getAsInt("cell_width") + global.getAsInt("soi_radius");
				
				// TODO: FAZER FUNCIONAR SOI NA BORDA
				if (limSup   <                                        0) limSup   = 0;
				if (limInf   > global.getAsInt("world_height") * global.getAsInt("cell_height")) limInf   = global.getAsInt("world_height") * global.getAsInt("cell_height");
				if (limLeft  <                                        0) limLeft  = 0;
				if (limRight > global.getAsInt("world_width")  * global.getAsInt("cell_width") ) limRight = global.getAsInt("world_width") * global.getAsInt("cell_width");

				int estimativeLen = FreeMMGNetwork.HEADER_LEN;
				int avatar_count = 0;

				estimativeLen += 2;
					
				for (Avatar a : os.avatars.values())
				{
					if (a.y < limSup)   continue;
					if (a.y > limInf)   continue;
					if (a.x < limLeft)  continue;
					if (a.x > limRight) continue;

					if ( (estimativeLen + a.getSize()) > global.MTU) break;
					estimativeLen += a.getSize();
					avatar_count++;
				}

				pkt.add(avatar_count);
				for (Avatar a : os.avatars.values())
				{
					if (a.y < limSup)   continue;
					if (a.y > limInf)   continue;
					if (a.x < limLeft)  continue;
					if (a.x > limRight) continue;

					if (avatar_count == 0) break; 
					pkt.add(a);
					avatar_count--;
				}
				
				if ( (BLAH < 1) || (BLAH > global.getAsInt("WHN")) )
				{
					throw new RuntimeException("AAAAAAAAA deu pau no ICTC! " + networkId + " mandou pra " + BLAH + ", que tah na celula " + cell_que_eu_vou_atualizar + " e eh o carinha na posicao " + target_na_celula_que_eu_vou_atualizar);
				}
				
				
				send(BLAH, FreeMMGNetwork.ICTC_UPDATE, estimativeLen, pkt);
				synchronized (lock)
				{
					uploadEstimativeFromICTC += estimativeLen;
				}
			}
			
			if (cellMembers.size() < worldNeighbors.length)
			{
				x = (x + cellMembers.size()) % worldNeighbors.length;
			}
			else if (cellMembers.size() > worldNeighbors.length)
			{
				y = (y + worldNeighbors.length) % cellMembers.size();
			}

		}

		// PLAYERS
		public void stepPlayers() throws TerminationException
		{
			for (Avatar a: os.avatars.values())
			{
				if (a == null) throw new RuntimeException("AAAARG, quem colocou um pointer null num mapa???");
				
				Pacote pkt = new Pacote();
				pkt.add(global.get_time()); 

				pkt.add(a.id);

				Vector<Avatar> dists = new Vector<Avatar>();
				dists.addAll(os.avatars.values());
				dists.addAll(os.ghosts.values());
				AvatarComparator ac = new AvatarComparator(a);
				Collections.sort(dists, ac);

				int pkt_len_estimative = FreeMMGNetwork.HEADER_LEN;
				int num_updates = 0;

				pkt_len_estimative += 2;
				for (int i = 0; i < dists.size(); i++)
				{
					if (dists.get(i) == null) throw new RuntimeException("AAAARG 2, quem colocou um pointer null num mapa???");
					if (dist(a, dists.get(i)) > global.getAsInt("soi_radius")) break;
					if ( (pkt_len_estimative + FreeMMGNetwork.PLAYER_AVATAR_LEN) > global.MTU) break;
					num_updates++;
					pkt_len_estimative += FreeMMGNetwork.PLAYER_AVATAR_LEN;
				}
				
				pkt.add(num_updates);
				for (int i = 0; i < num_updates; i++)
				{
					pkt.add(dists.get(i));
				}

				if ((a.id % global.getAsInt("ncell")) == (networkId % global.getAsInt("ncell")))
				{
					if ( (a.id < (global.getAsInt("WHN")+1)) )
					{
						throw new RuntimeException("AAAAAAAAA deu pau no PLAYER_UPDATE! " + a.id);					
					}
					
					if ( (a.id < (global.getAsInt("WHN")+1)) )
					{
						System.out.println("EXPLODIU AQUI, tentando mandar pra um jogador numero " + a.id);
						System.exit(0);
					}
					
					send((global.getAsInt("WHN")+1) + 1, FreeMMGNetwork.CTP_UPDATE, pkt_len_estimative, pkt);
//					if (DEBUG) logger.println("[" + global.get_time() + "] Cell   " + getNetworkId() + " sending  message CTP_UPDATE to Player " + a.id + " with "  + num_updates + " updates.");
				
					synchronized (lock)
					{
						// envio eh unreliable, entao nao computa custos de re-envios...
						uploadEstimativeFromPlayers += pkt_len_estimative;
					}
				}
			}

		}
		
		// BACKUP
		int[] backup_vetor = new int[LowerLayer.TOTAL_COUNTS];
		
		public void stepBackup() throws TerminationException
		{
			Pacote pkt = new Pacote();
			pkt.add(global.get_time());
			pkt.add(backupCellNodeRef);

			synchronized (toBackupBytesCruft)
			{
				toBackupBytesCruft += FreeMMGNetwork.HEADER_LEN;
				// envio eh reliable, computa custos aqui!
				if ( (backupCellNodeRef < global.getAsInt("WHN")+1) || (backupCellNodeRef > (2*global.getAsInt("WHN"))) )
				{
					throw new RuntimeException("AAAAAAAAA deu pau no BACKUPEEEE! backup " + networkId + " aponta pra PCN numero " + backupCellNodeRef);					
				}
				
				lower.send(global.getAsInt("WHN") + 1, FreeMMGNetwork.CTB_UPDATE, toBackupBytesCruft, pkt, true, 0.300, this, logger, backup_vetor);
				toBackupBytesCruft = 0;
			}

			synchronized (lock)
			{
				uploadEstimativeFromBackup += backup_vetor[LowerLayer.UP_BACKUPS];
				uploadEstimativeFromSync   += backup_vetor[LowerLayer.UP_SYNC];
			}
		}
	
		@Override
		public void execute() throws TerminationException
		{		
			while (true)
			{
				if (global.simulationTime() >= global.getAsDouble("simulationTotal")) break;
				
				stepSync();
				stepICTC();
				stepPlayers();
				stepBackup();
				
///				System.gc();
				this.sleep(global.getAsDouble("CTC_refresh_tick"));
			}
		}
	}

	public class CountThread extends NodeThread
	{
		protected CountThread(Node node_)
		{
			super(node_);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void execute() throws TerminationException
		{	
			this.sleep(global.getAsDouble("simulationStart"));
			
			while (true)
			{
				if (global.simulationTime() >= global.getAsDouble("simulationEnd"))
				{
					synchronized (lock)
					{
						double sum  = 0;
						double mean = 0;
						double max  = 0;
						double acum;
						double std_dev = 0.0;
	
						Vector diego[] = new Vector[10];
						diego[0] = downloadPlayerVector;
						diego[1] = uploadPlayerVector;
						diego[2] = downloadBackupVector;
						diego[3] = uploadBackupVector;
						diego[4] = downloadSyncVector;
						diego[5] = uploadSyncVector;
						diego[6] = downloadICTCVector;
						diego[7] = uploadICTCVector;
						diego[8] = downloadTotalVector;
						diego[9] = uploadTotalVector;
						
						String[] fabio = new String[10];
						fabio[0] = "downloadPlayerVector";
						fabio[1] = "uploadPlayerVector  ";
						fabio[2] = "downloadBackupVector";
						fabio[3] = "uploadBackupVector  ";
						fabio[4] = "downloadSyncVector  ";
						fabio[5] = "uploadSyncVector    ";
						fabio[6] = "downloadICTCVector  ";
						fabio[7] = "uploadICTCVector    ";
						fabio[8] = "downloadTotalVector ";
						fabio[9] = "uploadTotalVector   ";
						
						for (int j = 0; j < 10; j++)
						{
							sum  =  0.0;
							acum =  0.0;
							max  = -1.0;
							for (int i : (Vector<Integer>) diego[j])
							{
								if (i > max) max = i;
								sum  +=  (double) i;
								acum += ((double) i)  * (double) i;
							}
							
							double N = downloadPlayerVector.size();
							mean = sum / N;
							std_dev = acum - (N * mean * mean);
							std_dev = Math.sqrt(std_dev / N);
							
							logger.println(fabio[j] + ": mean = " + mean + " bps, std_dev = " + std_dev + ", max = " + max);
							global.finishSimulation(networkId, j, mean, std_dev, max);
						}	

						break;
					}
				}

				synchronized (lock)
				{
					downloadEstimativeFromPlayers = 0;
					uploadEstimativeFromPlayers   = 0;
					downloadEstimativeFromBackup  = 0;
					uploadEstimativeFromBackup    = 0;
					downloadEstimativeFromSync    = 0;
					uploadEstimativeFromSync      = 0;
					downloadEstimativeFromICTC    = 0;
					uploadEstimativeFromICTC      = 0;
				}

///				System.gc();
				this.sleep(1000.0);

				synchronized (lock)
				{
					DecimalFormat df = new DecimalFormat("000000");

					if (networkId == 1)
					{
						global.out.println("[" + global.get_time() + "] PCN " + networkId +
						                   " PLAYERS: {D = " + df.format(downloadEstimativeFromPlayers) + " bps, " + 
						                   "U = " + df.format(uploadEstimativeFromPlayers) + " bps}, " + 
						                   "SYNC = {D = " + df.format(downloadEstimativeFromSync) + " bps, " + 
						                   "U = " + df.format(uploadEstimativeFromSync) + " bps}, " + 
						                   "BACKUP = {D = " + df.format(downloadEstimativeFromBackup) + " bps, " + 
						                   "U = " + df.format(uploadEstimativeFromBackup) + " bps}, " + 
						                   "ICTC = {D = " + df.format(downloadEstimativeFromICTC) + " bps, " + 
						                   "U = " + df.format(uploadEstimativeFromICTC) + " bps}");
					}

					downloadPlayerVector.add(downloadEstimativeFromPlayers);
					uploadPlayerVector  .add(uploadEstimativeFromPlayers);
					downloadBackupVector.add(downloadEstimativeFromBackup);
					uploadBackupVector  .add(uploadEstimativeFromBackup);
					downloadSyncVector  .add(downloadEstimativeFromSync);
					uploadSyncVector    .add(uploadEstimativeFromSync);
					downloadICTCVector  .add(downloadEstimativeFromICTC);
					uploadICTCVector    .add(uploadEstimativeFromICTC);
					downloadTotalVector .add(downloadEstimativeFromPlayers + downloadEstimativeFromBackup + downloadEstimativeFromSync + downloadEstimativeFromICTC);
					uploadTotalVector   .add(  uploadEstimativeFromPlayers +   uploadEstimativeFromBackup +   uploadEstimativeFromSync +   uploadEstimativeFromICTC);
				}
			}
		}		
	}

	public void setLoggerName(String filename)
	{
		loggerFilename = filename;
	}

	public void begin()
	{
		global = (FreeMMGNetwork) this.network;
		lower.setGlobal(global);

		cs.type = "Conservador";
		
		backupCellNodeRef = networkId + global.getAsInt("WHN"); 
		myCellNumber = (networkId - 1) / global.getAsInt("ncell"); 
		for (int i = 0; i < global.getAsInt("ncell"); i++)
		{
			cellMembers.add(myCellNumber * global.getAsInt("ncell") + i+ 1); 
		}
/*
		try
		{
			File f = new File(loggerFilename + (myCellNumber+1) + "_" + networkId + ".txt");
			logger  = new HappyStream(f);
		}
		catch (FileNotFoundException e)
		{
			System.exit(0);
		}
*/
		
		myRowNumber = myCellNumber / global.getAsInt("world_width");
		myColNumber = myCellNumber % global.getAsInt("world_width");
		
		int t_row[] = {-1, -1, -1,  0,  0, +1, +1, +1};
		int t_col[] = {-1,  0, +1, -1, +1, -1,  0, +1};
		
		for (int k = 0; k < 8; k++)
		{
			int a = ((myRowNumber+t_row[k])%global.getAsInt("world_height"));
			if (a <  0) a += global.getAsInt("world_height");
			if (a >= global.getAsInt("world_height")) a -= global.getAsInt("world_height");

			int b = ((myColNumber+t_col[k])%global.getAsInt("world_width"));
			if (b <  0) b += global.getAsInt("world_width");
			if (b >= global.getAsInt("world_width")) b -= global.getAsInt("world_width");
			
			worldNeighbors[k] = a * global.getAsInt("world_width") + b;
		}

		ReceiveThread recv_thread = new ReceiveThread(this);
		SendThread    send_thread = new SendThread(this);
		CountThread   count_th    = new CountThread(this);

		recv_thread.launch();
		send_thread.launch();
		count_th.launch();
/*
		SendToPrimaryCellsThread send_cells_th   = new SendToPrimaryCellsThread(this);
		SendToBackupThread       send_backup_th  = new SendToBackupThread(this);
		SendToPlayersThread      send_players_th = new SendToPlayersThread(this);
		SendToNeighborCellThread send_ictc_th    = new SendToNeighborCellThread(this); 
		send_cells_th.launch();
		send_backup_th.launch();
		send_players_th.launch();
		send_ictc_th.launch();
*/
	}
}
