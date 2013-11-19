package simmcast.testbed;

import java.util.Vector;

import simmcast.network.Packet;
import simmcast.network.PacketType;
import simmcast.node.NodeThread;
import simmcast.node.TerminationException;

/**
 * In actual execution, a ProgramThread corresponds to a real preemptive thread.
 * In simulation, a ProgramThread corresponds to a simulated cooperative thread
 */
abstract public class ProgramThread {

   Vector ports = new Vector();

   Vector joinList = new Vector();

   Program node;

   InternalThread thread;
   
   long interval = 0;

   boolean timerEnabled = false;

   public ProgramThread(Program node_) {
      node = node_;
      thread = new InternalThread(this);
   }

   public DataPacket receive(int port_, int timeout_, int dataSize_) throws TerminationException  {
      Integer P = new Integer(port_);
      if ( ports.indexOf(P) == -1 ) {
         node.declarePort(port_);
         ports.add(P);
      }
      Packet p = thread.receive((double)timeout_);
//      Packet p = thread.receive((double)timeout_, port_);
      if (p != null)
         return new DataPacket(p);
      else
         return null;
   }

   public DataPacket receive(int port_, int dataSize_) throws TerminationException  {
      Integer P = new Integer(port_);
      if ( ports.indexOf(P) == -1 ) {
         node.declarePort(port_);
         ports.add(P);
      }
      Packet p = thread.receive();
//      Packet p = thread.receive(port_);
      if (p != null)
         return new DataPacket(p);
      else
         return null;
   }

   public DataPacket receiveMulticast(int port_, int timeout_, int dataSize_, NetworkAddress group_) throws TerminationException  {
      // Assumes node has already joined relevant groups.
      return receive(port_, timeout_, dataSize_);
   }

   public DataPacket receiveMulticast(int port_, int dataSize_, NetworkAddress group_) throws TerminationException {
      // Assumes node has already joined relevant groups.
      return receive(port_, dataSize_);
   }

   public void send(byte[] data_, int size_, NetworkAddress address_, int port_) throws TerminationException  {
      // TODO: Do I need to declare the port here or only in the receiver?
      Packet p = new Packet(node.getNetworkId(), address_.getNetworkId(), PacketType.DEFAULT, size_, data_);
      thread.send(p);
   }

   abstract public void run();

   public void start() {
      thread.launch();
   }

   public void join() throws simmcast.engine.TerminatedException {
      thread.joinThread();
   }

   public void wakeUp() {
      thread.wakeUp();
   }

   public void sleep(long millis_) throws TerminationException {
      Clock clock = new Clock(node);
      thread.sleep( (double)millis_ );
   }

   public void sleep() throws TerminationException {
      thread.sleep();
   }

   public void setTimer(long time_) {
      interval = time_;
      thread.setTimer(time_, this);
      timerEnabled = true;
   }

   public void stopTimer() {
      if (interval > 0)
         thread.cancelTimer(this);
      timerEnabled = false;
   }

   public void onTimer() {
   }

}

class InternalThread extends NodeThread {

   ProgramThread programThread;

   InternalThread(ProgramThread programThread_) {
      super(programThread_.node);
      programThread = programThread_;
   }

   public void execute() {
      programThread.run();
   }

   public void onTimer(Object message_) {
      programThread.onTimer();
      if (programThread.interval > 0 && programThread.timerEnabled) 
         setTimer(programThread.interval, programThread);
   }

}
