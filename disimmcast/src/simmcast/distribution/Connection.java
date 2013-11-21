package simmcast.distribution;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class Connection extends Thread implements Runnable {
	
	private java.util.concurrent.LinkedBlockingQueue<CommandProtocol> in;
	private Vector<Integer> acksMutex;
	private HashMap<Integer,CommandProtocol> acks;
	private DataInputStream is;
	private DataOutputStream os;
	private boolean connected;
	private int connId;
	private String description;

	public Connection(int mConnId, String mDescription, DataInputStream mIs, DataOutputStream mOs, java.util.concurrent.LinkedBlockingQueue<CommandProtocol> mIn)
	{
		connId = mConnId;
		description = mDescription;
		in = mIn;
		acksMutex = new Vector<Integer>();
		acks = new HashMap<Integer, CommandProtocol>();
		is = mIs;
		os = mOs;
		connected = true;
	}

	public Connection(int mConnId, String mDescription, DataInputStream mIs, DataOutputStream mOs)
	{
		this(mConnId,mDescription,mIs,mOs,new java.util.concurrent.LinkedBlockingQueue<CommandProtocol>());
	}

	private CommandProtocol waitForAck(int cmdId)
	{
		Integer cmdInt = new Integer(cmdId);
		acksMutex.add(cmdInt);
		try {
			synchronized (cmdInt) {				
				cmdInt.wait();
			}
			acksMutex.remove(cmdInt);
			CommandProtocol cp = acks.get(cmdInt);
			acks.remove(cmdInt);
			return cp;
		} catch (InterruptedException e) {
			return null;
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
		while (connected)
		{
			CommandProtocol cp = null;
			try
			{
				int cmdid = 0;
				byte action = 0;
				byte[] param = null;
				synchronized (is)
				{
					cmdid = is.readInt();
					action = is.readByte();
					int parametersSize = is.readInt();
					param = new byte[parametersSize];
					int totread = 0;
					while (totread<parametersSize)
					{
						totread += is.read(param, totread, parametersSize - totread);
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
						for (int j=0;j<acksMutex.size();j++)
						{
							Integer cmdInt = acksMutex.get(j);
							if (cmdInt.intValue()==cp.getCmdId())
							{
								acks.put(cmdInt ,cp);
								synchronized (cmdInt) {
									cmdInt.notify();
								}
								break;
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
}
