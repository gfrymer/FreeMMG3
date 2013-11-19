package arjuna.JavaSim.Simulation;

/**
 * This file has bugfixes added by a patch
 * available at http://inf.unisinos.br released
 * under the GNU General Public License.
 */

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

public class SimulationProcessList
{
    
   private Vector theList;

   public SimulationProcessList() {
      theList = new Vector();
    }

   public synchronized void Insert (SimulationProcess p) {
	Insert(p, false);
    }
    
   public synchronized void Insert (SimulationProcess p, boolean prior) {
      Iterator iter = theList.iterator();
      int pos = 0;
      while (iter.hasNext()) {
         SimulationProcess item = (SimulationProcess)iter.next();
         if (prior) {
            if (item.evtime() >= p.evtime()) {
               theList.insertElementAt(p, pos);
	    return;
	}
         } else {
            if (item.evtime() > p.evtime()) {
               theList.insertElementAt(p, pos);
		    return;
		}
	    }
         pos++;
	    }
      theList.addElement(p);
	}
	
   public synchronized boolean InsertBefore (SimulationProcess toInsert, SimulationProcess before) {
      int pos = theList.indexOf(before);
      if (pos == -1)
	return false;	
      theList.insertElementAt(toInsert, pos);
		return true;
	    }
	
   public synchronized boolean InsertAfter (SimulationProcess toInsert, SimulationProcess after) {
      int pos = theList.indexOf(after);
      if (pos == -1)
	return false;
      theList.insertElementAt(toInsert, pos+1);
      return true;
    }
    
   public synchronized SimulationProcess Remove (SimulationProcess element) throws NoSuchElementException {
      if (!theList.remove(element))
         throw new NoSuchElementException();
      //TODO: counting
      return element;
    }

   public synchronized SimulationProcess Remove () throws NoSuchElementException {
      if (!theList.isEmpty())
         return (SimulationProcess)theList.remove(0);
	else
         throw new NoSuchElementException();
    }
    
   public synchronized SimulationProcess getNext (SimulationProcess current) throws NoSuchElementException {
      if (current == null || theList.isEmpty())
         throw new NoSuchElementException();
      int pos = theList.indexOf(current);
      if (pos+1 < theList.size())
         return (SimulationProcess)theList.elementAt(pos+1);
		else
	return null;
      //TODO: .otimiza for ordered list
    }
    
   public void print () {
      Iterator iter = theList.iterator();
      while (iter.hasNext()) {
         SimulationProcess item = (SimulationProcess)iter.next();
         System.out.println(item.evtime());
    }
    }
    
}
