package simmcast.util;

import java.util.Iterator;
import java.util.Vector;

import simmcast.node.Node;
import simmcast.node.NodeThread;

class NodeThreadItem {
   NodeThread thread;
   boolean daemon;
}

public class SimpleNode extends Node {
   Vector threads;

   public SimpleNode() {
      threads = new Vector();
   }

   public void attachThread(NodeThread nodeThread_, boolean daemon_) {
      NodeThreadItem item = new NodeThreadItem();
      item.thread = nodeThread_;
      item.daemon = daemon_;
      threads.add(item);
   }

   public void begin() {
      for (Iterator i = threads.iterator(); i.hasNext(); ) {
         NodeThreadItem item = (NodeThreadItem)i.next();
         item.thread.launch(item.daemon);
      }
   }
}
