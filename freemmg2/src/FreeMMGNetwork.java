import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.HashMap;

import simmcast.network.Network;
import simmcast.network.PacketType;

public class FreeMMGNetwork extends Network
{
	public FreeMMGNetwork(boolean isManager, String managerHost)
	{
		super(isManager,managerHost);

		cacheable_fields = new HashMap<String, Object>();
		cacheable_fields.put("simulationTotal", null);
		cacheable_fields.put("simulationStart", null);
		cacheable_fields.put("simulationEnd", null);
		cacheable_fields.put("PTC_refresh_tick", null);
		cacheable_fields.put("CTP_refresh_tick", null);
		cacheable_fields.put("CTB_refresh_tick", null);
		cacheable_fields.put("CTC_refresh_tick", null);
		cacheable_fields.put("world_width", null);
		cacheable_fields.put("world_height", null);
		cacheable_fields.put("WH", null);
		cacheable_fields.put("WHN", null);
		cacheable_fields.put("NUM_PLAYERS", null);
		cacheable_fields.put("ncell", null);
		cacheable_fields.put("cell_width", null);
		cacheable_fields.put("cell_height", null);
		cacheable_fields.put("soi_radius", null);
		cacheable_fields.put("latency_mean", null);
	}

	public static PacketType PTC_POSITION = new PacketType("PTC_POSITION");
	public static PacketType CTP_UPDATE   = new PacketType("CTP_UPDATE");
	public static PacketType CTC_SYNC     = new PacketType("CTC_SYNC");
	public static PacketType ICTC_UPDATE  = new PacketType("ICTC_UPDATE");
	public static PacketType CTB_UPDATE   = new PacketType("CTB_UPDATE");
	public static PacketType BTC_ACK      = new PacketType("BTC_ACK");
	public static PacketType CTC_ACK      = new PacketType("CTC_ACK");
	
	
	public static final int HEADER_LEN    = 60;
	public static final int PLAYER_EVENT_SIZE = 32;
	public static final int PLAYER_AVATAR_LEN = 32;
	public static final int MTU = 1500;

	private double tick = 100;
	
	private double      simulationTotal = 0;
	private double      simulationStart = 0;
	private double      simulationEnd   = 0;
	public PrintStream out             = System.out;

	private double PTC_refresh_tick = 100;
	private double CTP_refresh_tick = 100;
	private double CTB_refresh_tick = 100;
	private double CTC_refresh_tick   = 100;
	
	/// LARGURA DO MUNDO EM CELULAS
	private int world_width  = 0; 

	/// ALTURA DO MUNDO EM CELULAS
	private int world_height = 0;

	private int WH           = 0;
	private int WHN          = 0;
	private int NUM_PLAYERS  = 0;
	
	
	/// NUMERO DE PCN's POR CELULA
	private int ncell        = 0;
	
	// LARGURA DE CADA CELULA EM PIXELS
	private int cell_width   = 0;

	// ALTURA DE CADA CELULA EM PIXELS
	private int cell_height  = 0;
	
	
	private int soi_radius   = 0;
	private double latency_mean = 0.0;
	
	DecimalFormat df = new DecimalFormat("000");

	
	public void setNetworkTick(double tick)
	{
		this.tick = tick;

		PTC_refresh_tick = tick;
		CTP_refresh_tick = tick;
		CTB_refresh_tick = tick;
		CTC_refresh_tick = tick;
	}
	
	public void setSimulationTimeouts(double total, double start, double end)
	{
		simulationTotal = total;
		simulationStart = start;
		simulationEnd   = end;
	}
	
	public void setOutput(String filename)
	{
		try
		{
			File f = new File(filename);
			out = new PrintStream(f);
		}
		catch (FileNotFoundException e)
		{
		}
	}
	
	public void setAll(int width, int height, int ncell, int num_players, int cell_width, int cell_height, int soi_radius, int latency_mean)
	{
		world_width       = width;
		world_height      = height;
		this.ncell        = ncell;
		
		WH                = width * height;
		WHN               = WH * ncell;

		this.NUM_PLAYERS  = num_players;

		this.cell_width   = cell_width;
		this.cell_height  = cell_height;
		this.soi_radius   = soi_radius;
		this.latency_mean = ((double) latency_mean) / 1000.0; 

		for (int k = 0; k < 8; k++)
		{
			globalMeanOfMean[k]     = 0;
			globalStdDevOfMean[k]   = 0;
			globalMaxOfMean[k]      = 0;
		
			globalMeanOfStdDev[k]   = 0;
			globalStdDevOfStdDev[k] = 0;
			globalMaxOfStdDev[k]    = 0;
		
			globalMeanOfMax[k]      = 0;
			globalMaxOfMax[k]       = 0;
			globalStdDevOfMax[k]    = 0;
		
			globalCount[k] = 0;
		}
	}
	
	public String get_time()
	{
		int seconds = ((int) simulationTime()) / 1000;
		int milisec = ((int) simulationTime()) % 1000;
		return df.format(seconds) + "." + df.format(milisec);
	}
	
	public double[] globalMeanOfMean     = new double[10];
	public double[] globalStdDevOfMean   = new double[10];
	public double[] globalMaxOfMean      = new double[10];
	public double[] globalAcumOfMean     = new double[10];
	
	public double[] globalMeanOfStdDev   = new double[10];
	public double[] globalStdDevOfStdDev = new double[10];
	public double[] globalMaxOfStdDev    = new double[10];
	public double[] globalAcumOfStdDev   = new double[10];
	
	public double[] globalMeanOfMax      = new double[10];
	public double[] globalMaxOfMax       = new double[10];
	public double[] globalStdDevOfMax    = new double[10];
	public double[] globalAcumOfMax      = new double[10];
	
	public int[]    globalCount          = new int[10];

	public String[] nomesDosResultados = {"Players DOWN", "Players UP", "Backup DOWN", "Backup UP", "Sync DOWN", "Sync UP", "ICTC DOWN", "ICTC UP", "Total DOWN", "Total UP"};
	
	public synchronized void finishSimulation(int id, int type, double mean, double std_dev, double max)
	{
		globalMeanOfMax   [type] += max;
		globalMeanOfMean  [type] += mean;
		globalMeanOfStdDev[type] += std_dev;

		globalAcumOfMean  [type] += (mean    * mean);
		globalAcumOfStdDev[type] += (std_dev * std_dev);
		globalAcumOfMax   [type] += (max     * max);
		
		if (max     > globalMaxOfMax[type])    globalMaxOfMax[type] = max;	
		if (mean    > globalMaxOfMean[type])   globalMaxOfMean[type] = mean;
		if (std_dev > globalMaxOfStdDev[type]) globalMaxOfStdDev[type] = std_dev;

		globalCount[type]++;
		
		if (globalCount[type] == WHN)
		{
			globalMeanOfMean  [type] = globalMeanOfMean  [type] / (double) WHN;
			globalMeanOfStdDev[type] = globalMeanOfStdDev[type] / (double) WHN;
			globalMeanOfMax   [type] = globalMeanOfMax   [type] / (double) WHN;
			
			globalAcumOfMean    [type] = globalAcumOfMean  [type] - (WHN * globalMeanOfMean  [type] * globalMeanOfMean  [type]);
			globalAcumOfStdDev  [type] = globalAcumOfStdDev[type] - (WHN * globalMeanOfStdDev[type] * globalMeanOfStdDev[type]);
			globalAcumOfMax     [type] = globalAcumOfMax   [type] - (WHN * globalMeanOfMax   [type] * globalMeanOfMax   [type]);

			globalStdDevOfMean  [type] = Math.sqrt(globalAcumOfMean  [type] / (double) WHN);
			globalStdDevOfStdDev[type] = Math.sqrt(globalAcumOfStdDev[type] / (double) WHN);
			globalStdDevOfMax   [type] = Math.sqrt(globalAcumOfMax   [type] / (double) WHN);
			
			out.println("Medias do tipo " + nomesDosResultados[type]);
			out.println("Mean   >> mean = " + globalMeanOfMean  [type] + ", std_dev = " + globalStdDevOfMean  [type] + ", max = " + globalMaxOfMean  [type]);
			out.println("StdDev >> mean = " + globalMeanOfStdDev[type] + ", std_dev = " + globalStdDevOfStdDev[type] + ", max = " + globalMaxOfStdDev[type]);
			out.println("Max    >> mean = " + globalMeanOfMax   [type] + ", std_dev = " + globalStdDevOfMax   [type] + ", max = " + globalMaxOfMax   [type]);
			out.println("-------------------------");
		}
	}
}
