import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class CellState
{
	public int currentRound = 0;
	public Map<Integer, Avatar> avatars = new HashMap<Integer, Avatar>();
	public Map<Integer, Avatar> ghosts  = new HashMap<Integer, Avatar>();
	public String type = "Otimista";
	
	public synchronized boolean execute(AvatarEvent ae)
	{
		FreeMMGNetwork global = null;
		PrintStream out = System.out;
		return execute(ae, global, out);
	}

	public synchronized boolean execute(AvatarEvent ae, FreeMMGNetwork global, PrintStream out)
	{
///		if (global != null) out.println("[" + this.type + "] >> execute(), ghost = " + ae.ghost);
///		if (global != null) out.println("[" + this.type + "] >> " + avatars.size() + " em avatars, " + ghosts.size() + " em ghosts)");
		
		boolean useful = true;
		Avatar a = new Avatar(ae);
		a.lastRound = currentRound;
		
		Map<Integer, Avatar>  elmapa   = ae.ghost ? ghosts  : avatars;
	
		Avatar tenho_em_avatars = avatars.get(ae.id);
		Avatar tenho_em_ghosts  = ghosts.get(ae.id);
		
		// Se o cara eh ghost e quero inserir no mapa de avatares, deleta ele primeiro do mapa de ghosts
		// Se o cara eh avatar e chegou msg de ghost, deleto ele primeiro do mapa de avatares!
		if (tenho_em_avatars != null)
		{
///			if (global != null) out.println("[" + global.get_time()  + "] aqui, cara, " + tenho_em_avatars.id + " ja ta no mapa de avatars da celula (com seq=" + tenho_em_avatars.seq + "), seq do que chegou eh " + ae.seq);
			if (tenho_em_avatars.seq >= a.seq) return false;
///			if (global != null) out.println("[" + global.get_time()  + "] atualizando (incoming.seq = " + ae.seq + ", old.seq = " + tenho_em_avatars.seq + ")");
			avatars.remove(ae.id);
		}
		else if (tenho_em_ghosts != null)
		{
///			if (global != null) out.println("[" + global.get_time()  + "] aqui, cara, " + tenho_em_ghosts.id + " ja ta no mapa de ghosts da celula (com seq=" + tenho_em_ghosts.seq + "), seq do que chegou eh " + ae.seq);
			// "OTIMIZANDO": se cara conhece o cara como fantasma, mas alguem da celula diz pra ele que o cara pertence a celula, entao coloca ele na lista certa!
			if (tenho_em_ghosts.seq > a.seq) return false;
///			if (global != null) out.println("[" + global.get_time()  + "] atualizando (incoming.seq = " + ae.seq + ", old.seq = " + tenho_em_ghosts.seq + ")");
			ghosts.remove(ae.id);
		}

		elmapa.put(a.id, a);
		return useful;
	}
	
	// SOH EH CHAMADO NO CONSERVADOR
	public synchronized void executeRoundConservative(Map<Integer, Vector<Event>> eventTable) {
	
		for (Vector<Event> lista : eventTable.values()) 
			for (Event e: lista)
			{
				execute((AvatarEvent) e);
			}
		
		currentRound++;
		
		Vector<Integer> keys = new Vector<Integer>( avatars.keySet() );	
		for (Integer avatarId : keys)
		{
			 if ((currentRound - avatars.get(avatarId).lastRound) > 10)
			 {
				 avatars.remove(avatarId);
			 }
		}
	}

	public synchronized void executeRoundOptimist(HappyStream out, int cellId)
	{
		currentRound++;
		
		Vector<Integer> keys = new Vector<Integer>( avatars.keySet() );	
		for (Integer avatarId : keys)
		{
			 if ((currentRound - avatars.get(avatarId).lastRound) > 3)
			 {
				 out.println("######### deletando player " + avatarId + " da celula " + cellId);
				 avatars.remove(avatarId);
			 }
		}
	}
}
