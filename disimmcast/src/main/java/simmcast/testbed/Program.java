package simmcast.testbed;

import simmcast.node.HostNode;

/**
 * In actual execution, each instance of this class corresponds to a stand-alone program
 * running in a separate machine.
 * In simulation, each instance of this class is assigned to a node object,
 * and a main thread is created.
 */
abstract public class Program extends HostNode {

   MainThread thread;

   public void start() {
      if (thread == null)
         setArgc(0);
      thread.launch();
   }

   public void setArgc(int n) {
      thread = new MainThread(this);
      thread.setArgc(n);
   }

   public void addArg(String s) {
      thread.addArg(s);
   }

   public NetworkAddress getAddressByName(String s) {
      return new NetworkAddress(Integer.parseInt(s));
   }

   /**
    * In actual execution, returns the localhost address.
    * In simulation, returns the address corresponding to the node's network id.
    */
   public NetworkAddress getLocalHost() {
      return new NetworkAddress(getNetworkId());
   }

   abstract public void main(String S[]);

}

class MainThread extends simmcast.node.NodeThread {

   String[] args;
   int lastArg = 0;
   
   public MainThread(Program node_) {
      super(node_);
   }
   
   public void setArgc(int n) {
      args = new String[n];
   }

   public void addArg(String s) {
      args[lastArg] = s;
      lastArg++;
   }

   public void execute() {
      ((Program)node).main(args);
   }

}