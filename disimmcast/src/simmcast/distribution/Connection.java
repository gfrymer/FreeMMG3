package simmcast.distribution;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Connection extends Thread implements Runnable {

	private java.util.concurrent.LinkedBlockingQueue<CommandProtocol> in;
	private java.util.concurrent.LinkedBlockingQueue<CommandProtocol> acks;
	private DataInputStream is;
	private DataOutputStream os;
	private boolean connected;
	private int connId;
	private String address;

	public Connection(int mConnId, String mAddress, java.util.concurrent.LinkedBlockingQueue<CommandProtocol> mIn, DataInputStream mIs, DataOutputStream mOs)
	{
		connId = mConnId;
		address = mAddress;
		in = mIn;
		acks = new java.util.concurrent.LinkedBlockingQueue<CommandProtocol>();
		is = mIs;
		os = mOs;
		connected = true;
	}

	public String sendCmd(CommandProtocol cp)
	{
		try {
			os.writeInt(cp.getCmdId());
			os.writeByte(cp.getAction());
			os.writeInt(cp.getParameters().length());
			if (cp.getParameters().length()>0)
			{
				os.writeBytes(cp.getParameters());
			}

			if ((cp.getAction()==CommandProtocol.ACTION_OK) || (cp.getAction()==CommandProtocol.ACTION_ERROR))
			{
				return "";
			}

			while (true)
			{
				CommandProtocol ack = acks.take();
				if (ack.getCmdId()==cp.getCmdId())
				{
					if (ack.getAction()==CommandProtocol.ACTION_OK)
					{
						if (ack.getParameters().length()>0)
						{
							return ack.getParameters();
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
				else
				{
					acks.put(ack);
				}
			}
		} catch (IOException e) {
			return e.toString();
		} catch (InterruptedException e) {
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

	public String getAddress()
	{
		return address;
	}

	public void run() {
		while (connected)
		{
			CommandProtocol cp = null;
			try
			{
				int cmdid = is.readInt();
				byte action = is.readByte();
				int parametersSize = is.readInt();
				byte[] param = new byte[parametersSize];
				int totread = 0;
				while (totread<parametersSize)
				{
					totread += is.read(param, totread, parametersSize - totread);
				}
				String parameters = new String(param);
				cp = CommandProtocol.createFromAction(connId,cmdid,action,parameters);
			} catch (IOException e) {
			}
			if (cp!=null)
			{
				if ((cp.getAction()==CommandProtocol.ACTION_OK) || (cp.getAction()==CommandProtocol.ACTION_ERROR)) 
				{
					acks.add(cp);
				}
				else
				{
					in.add(cp);
				}
			}
		}
	}
}
