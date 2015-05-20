package simmcast.distribution.communication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;

import simmcast.distribution.command.CommandProtocol;

public class CommunicationServerNamedPipe implements CommunicationServer {

	public static final byte CONNECT_BYTE = 'C';
	public static final byte FINISH_BYTE = 'F';

	public static final String CLIENT_SUFFIX = "_client";
	public static final String IN_SUFFIX = "_in";
	public static final String OUT_SUFFIX = "_out";

	public static final String CREATE_PIPE_COMMAND = "mkfifo ";
	public static final String REMOVE_PIPE_COMMAND = "rm ";

	private String mAddress;

	@Override
	public boolean create() {
		return false;
	}

	@Override
	public boolean create(String address) {
		try {
			Runtime.getRuntime().exec(CREATE_PIPE_COMMAND + address);
			Runtime.getRuntime().exec(CREATE_PIPE_COMMAND + address + CLIENT_SUFFIX);
			mAddress = address;
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public Connection listen(int connNumber,
			LinkedBlockingQueue<CommandProtocol> inqueue) {
		if (mAddress==null)
		{
			return null;
		}
		try {
			DataInputStream is = new DataInputStream(new FileInputStream(mAddress));
			byte c = is.readByte();
			if (c==CONNECT_BYTE)
			{
				byte n = is.readByte();
				try {
					is.close();

					Runtime.getRuntime().exec(CREATE_PIPE_COMMAND + mAddress + "_" + connNumber + IN_SUFFIX);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					Runtime.getRuntime().exec(CREATE_PIPE_COMMAND + mAddress + "_" + connNumber + OUT_SUFFIX);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					DataOutputStream os = new DataOutputStream(new FileOutputStream(mAddress + CLIENT_SUFFIX));
					os.writeByte(connNumber);
					os.flush();
					os.close();

					DataInputStream ins = new DataInputStream(new FileInputStream(mAddress + "_" + connNumber + IN_SUFFIX));
					DataOutputStream ous = new DataOutputStream(new FileOutputStream(mAddress + "_" + connNumber + OUT_SUFFIX));

					Connection cn = new Connection(connNumber, "" + n, ins, ous, inqueue);
					cn.start();

					return cn;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			is.close();
			if (c==FINISH_BYTE)
			{
				Runtime.getRuntime().exec(REMOVE_PIPE_COMMAND + mAddress);
				Runtime.getRuntime().exec(REMOVE_PIPE_COMMAND + mAddress + CLIENT_SUFFIX);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getDescription() {
		return mAddress;
	}

	@Override
	public boolean disconnect() {
		if (mAddress==null)
		{
			return false;
		}
		try {
			DataOutputStream os = new DataOutputStream(new FileOutputStream(mAddress));
			os.write(FINISH_BYTE);
			os.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

}
