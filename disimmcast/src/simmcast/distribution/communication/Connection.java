package simmcast.distribution.communication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import simmcast.distribution.command.CommandPacketArrival;
import simmcast.distribution.command.CommandProtocol;

import com.sun.xml.internal.ws.api.message.Packet;

public class Connection extends Thread implements Runnable {
	
	private java.util.concurrent.LinkedBlockingQueue<CommandProtocol> in;
	private Vector<Integer> acksMutex;
	private HashMap<Integer,CommandProtocol> acks;
	private DataInputStream is;
	private DataInputStream is2;
	private DataOutputStream os;
	private CommunicationOutputStream os2;
	private boolean connected;
	private int connId;
	private String description;

	public Connection(int mConnId, String mDescription, DataInputStream mIs, DataOutputStream mOs, java.util.concurrent.LinkedBlockingQueue<CommandProtocol> mIn, DataInputStream mIs2, CommunicationOutputStream mOs2)
	{
		connId = mConnId;
		description = mDescription;
		in = mIn;
		acksMutex = new Vector<Integer>();
		acks = new HashMap<Integer, CommandProtocol>();
		is = mIs;
		os = mOs;
		is2 = mIs2;
		os2 = mOs2;
		connected = true;
	}

	public Connection(int mConnId, String mDescription, DataInputStream mIs, DataOutputStream mOs, java.util.concurrent.LinkedBlockingQueue<CommandProtocol> mIn)
	{
		this(mConnId,mDescription,mIs,mOs,mIn,null,null);
	}

	public Connection(int mConnId, String mDescription, DataInputStream mIs, DataOutputStream mOs)
	{
		this(mConnId,mDescription,mIs,mOs,new java.util.concurrent.LinkedBlockingQueue<CommandProtocol>());
	}

	private CommandProtocol waitForAck(int cmdId)
	{
		synchronized (acks) {
			Iterator<Integer> ackskeys = acks.keySet().iterator();
			while (ackskeys.hasNext())
			{
				Integer key = ackskeys.next();
				if (key.intValue() == cmdId)
				{
					CommandProtocol cp = acks.get(key);
					acks.remove(key);
					return cp;
				}
			}
		}
		Integer cmdInt = new Integer(cmdId);
		synchronized (acksMutex)
		{
			acksMutex.add(cmdInt);
		}
		try {
			synchronized (cmdInt) {
				cmdInt.wait();
			}
			synchronized (acksMutex) {
				acksMutex.remove(cmdInt);
			}
			synchronized (acks) {
				CommandProtocol cp = acks.get(cmdInt);
				acks.remove(cmdInt);
				return cp;
			}
		} catch (InterruptedException e) {
			return null;
		}
	}

	public String sendPacket(String where, double relativeTime_, simmcast.network.Packet p)
	{
		if (os2==null)
			return "Not connected to " + where;
		DataOutputStream dos = os2.getOutputStream(where);
		try {
			CommandPacketArrival cpa = new CommandPacketArrival(description, relativeTime_, p);
			String packetData = cpa.getParameters();
			dos.writeInt(cpa.getCmdId());
			dos.writeByte(cpa.getAction());
			dos.writeInt(packetData.length());
			dos.writeBytes(packetData);
			dos.flush();

			while (true)
			{
				CommandProtocol ack = waitForAck(cpa.getCmdId());
				if (ack.getAction()==CommandProtocol.ACTION_OK)
				{
					if (ack.getParameters().length()>0)
					{
						return CommandProtocol.OK_PREFIX + ack.getParameters();
					}
					else
					{
						return null;
					}
				}
				else
				{
					return ack.getParameters();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			return e.toString();
		}
	}


	public String sendCmd(CommandProtocol cp)
	{
		try {
			synchronized (os)
			{
				os.writeInt(cp.getCmdId());
				os.writeByte(cp.getAction());
				os.writeInt(cp.getParameters().length());
				if (cp.getParameters().length()>0)
				{
					os.writeBytes(cp.getParameters());
				}
			}

			if ((cp.getAction()==CommandProtocol.ACTION_OK) || (cp.getAction()==CommandProtocol.ACTION_ERROR))
			{
				return "";
			}

			while (true)
			{
				CommandProtocol ack = waitForAck(cp.getCmdId());
				if (ack.getAction()==CommandProtocol.ACTION_OK)
				{
					if (ack.getParameters().length()>0)
					{
						return CommandProtocol.OK_PREFIX + ack.getParameters();
					}
					else
					{
						return null;
					}
				}
				else
				{
					return ack.getParameters();
				}
			}
		} catch (IOException e) {
			return e.toString();
		}
	}

	public boolean sendPacketOk(String where, int cmdId)
	{
		if (os2==null)
			return true;
		DataOutputStream dos = os2.getOutputStream(where);
		try {
			CommandProtocol cp = new CommandProtocol(connId, cmdId, CommandProtocol.ACTION_OK, "");
			String packetData = cp.getParameters();
			dos.writeInt(cp.getCmdId());
			dos.writeByte(cp.getAction());
			dos.writeInt(packetData.length());
			dos.writeBytes(packetData);
			dos.flush();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void sendOk(int cmdId)
	{
		sendCmd(new CommandProtocol(connId, cmdId, CommandProtocol.ACTION_OK, ""));
	}

	public void sendOk(int cmdId, String params)
	{
		sendCmd(new CommandProtocol(connId, cmdId, CommandProtocol.ACTION_OK, params));
	}

	public void sendError(int cmdId, String err)
	{
		sendCmd(new CommandProtocol(connId, cmdId, CommandProtocol.ACTION_ERROR, err));
	}

	public String getDescription()
	{
		return description;
	}

	public void disconnect()
	{
		try {
			is.close();
			os.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		connected = false;
	}

	public java.util.concurrent.LinkedBlockingQueue<CommandProtocol> getInQueue()
	{
		return in;
	}

	public void run() {
		if (is2!=null)
		{
			Thread t2 = new Thread(new Runnable() {
				@Override
				public void run() {
					while (connected)
					{
						receive(is2);
					}
				}
			});
			t2.start();
		}
		while (connected)
		{
			receive(is);
		}
	}

	private void receive(DataInputStream instr)
	{
		CommandProtocol cp = null;
		try
		{
			int cmdid = 0;
			byte action = 0;
			byte[] param = null;
			synchronized (instr)
			{
				cmdid = instr.readInt();
				action = instr.readByte();
				int parametersSize = instr.readInt();
				param = new byte[parametersSize];
				int totread = 0;
				while (totread<parametersSize)
				{
					totread += instr.read(param, totread, parametersSize - totread);
				}
			}
			String parameters = new String(param);
			cp = CommandProtocol.createFromAction(connId,cmdid,action,parameters);
		} catch (IOException e) {
		}
		if (cp!=null)
		{
			try
			{
				if ((cp.getAction()==CommandProtocol.ACTION_OK) || (cp.getAction()==CommandProtocol.ACTION_ERROR)) 
				{
					boolean haveack = false;
					for (int j=0;j<acksMutex.size();j++)
					{
						Integer cmdInt = acksMutex.get(j);
						if (cmdInt.intValue()==cp.getCmdId())
						{
							haveack = true;
							synchronized (acks) {
								acks.put(cmdInt,cp);
							}
							synchronized (cmdInt) {
								cmdInt.notify();
							}
							break;
						}
					}
					if (!haveack)
					{
						synchronized (acks) {
							acks.put(new Integer(cp.getCmdId()),cp);
						}
					}
				}
				else
				{
					in.put(cp);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}