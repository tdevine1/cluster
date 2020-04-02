package filehandlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import datahandlers.FileData;
import plotting.Plotter;
import util.Constants;

public class DatFileReader
{
	protected int			maxDMs;
	protected FileData		data;
	protected String		name;
	protected String		dir;
	protected double[]		scaledRange	= { 0.0, 0.0 };
	protected double[]		dmRange;
	protected double[]		snrRange;
	protected double[][]	snrs;
	protected double[][]	pulseCounts;
	protected double[][]	maxSNRs;
	protected double[]		pulseCountRange;
	protected double[]		timeRange;
	protected double[][]	times;
	protected double		peakDM;

	public DatFileReader()
	{
		scaledRange = null;
		maxDMs = -1;
		data = null;
	}

	public DatFileReader(File dir) throws Exception
	{
		name = dir.getName();
		maxDMs = 0;
		dmRange = new double[] { 0.0, 0.0 };
		pulseCountRange = getRange(dir + "/pulseCounts.dat", true);
		pulseCounts = readHistogramData(dir + "/pulseCounts.dat");
		snrRange = getRange(dir + "/sigmas.dat", false);
		if (snrRange[0] == 0) snrRange[0] = 5.0;
		snrs = readScatterData(dir + "/sigmas.dat");
		maxSNRs = readHistogramData(dir + "/maxSigmas.dat");
		data = new FileData(name, dmRange, pulseCountRange, pulseCounts,
				snrRange, snrs, maxSNRs, timeRange, times);
	}

	public DatFileReader(String id, String inputString) throws Exception
	{
		String[] input = inputString.split(",");
		name = input[0];
		// name = input[0] + "_" + input[1] + "_" + input[2] + input[3];
		System.out.println("Creating plots for " + name + "...");
		dir = Constants.DAT_PATH + name;
		// dmRange = new double[] { Double.parseDouble(input[7]),
		// Double.parseDouble(input[8]) };
		// peakDM = Double.parseDouble(input[19]);
		dmRange = new double[] { Double.parseDouble(input[1]),
				Double.parseDouble(input[2]) };
		peakDM = Double.parseDouble(input[3]);
		if (peakDM < 20)
		{
			scaledRange[0] = 0;
			scaledRange[1] = 30;
		} else
			if (peakDM < 100)
			{
				scaledRange[0] = 0;
				scaledRange[1] = 110;
			} else
				if (peakDM < 300)
				{
					scaledRange[0] = 100;
					scaledRange[1] = 310;
				} else
				{
					scaledRange[0] = 300;
					scaledRange[1] = dmRange[1];
				}
		maxDMs = 0;
		pulseCountRange = getRange(dir + "/pulseCounts.dat", true);
		pulseCounts = readHistogramData(dir + "/pulseCounts.dat");
		snrRange = getRange(dir + "/sigmas.dat", false);
		if (snrRange[0] == 0) snrRange[0] = 5.0;
		snrs = readScatterData(dir + "/sigmas.dat");
		timeRange = getRange(dir + "/times.dat", false);
		times = readScatterData(dir + "/times.dat");
		// data = new SinglePulseFileData(name, dmRange, pulseCountRange,
		// pulseCounts, snrRange, snrs, maxSNRs, timeRange, times,
		// Integer.parseInt(input[12]), Double.parseDouble(input[9]));
		new Plotter(name, dmRange, peakDM, scaledRange, pulseCountRange,
				pulseCounts, snrRange, snrs, timeRange, times)
						.plot(id + "_" + name);
	}

	public FileData getData()
	{
		return data;
	}

	protected void findMaxDMs(String from) throws Exception
	{
		int dmCount = 0;
		BufferedReader inBuffer = new BufferedReader(
				new FileReader(new File(from)));
		while (inBuffer.ready())
		{
			String[] line = inBuffer.readLine().split("\t");
			double[] values = new double[line.length];
			for (int i = 0; i < values.length; i++)
				values[i] = Double.parseDouble(line[i]);
			if (values.length > 1 && values[0] >= scaledRange[0]
					&& values[0] <= scaledRange[1])
				dmCount++;
		}
		inBuffer.close();
		maxDMs = dmCount;
	}

	protected double[] getRange(String from, boolean setMaxDMs) throws Exception
	{
		int dmCount = 0;

		BufferedReader inBuffer = new BufferedReader(
				new FileReader(new File(from)));
		double[] range = { Double.MAX_VALUE, -1.0 };
		while (inBuffer.ready())
		{
			String[] line = inBuffer.readLine().split("\t");
			double[] values = new double[line.length];
			for (int i = 0; i < values.length; i++)
				values[i] = Double.parseDouble(line[i]);
			if (values.length > 1 && values[0] >= scaledRange[0]
					&& values[0] <= scaledRange[1])
			{
				for (int i = 1; i < values.length; i++)
				{
					if (values[i] < range[0]) range[0] = values[i];
					if (values[i] > range[1]) range[1] = values[i];
				}
				dmCount++;
			}
		}
		inBuffer.close();
		if (setMaxDMs) maxDMs = dmCount;
		return range;
	}

	protected double[][] readHistogramData(String from) throws Exception
	{
		int count = 0;
		double[] defaultRange = { 0.0, 0.0 };
		BufferedReader inBuffer = new BufferedReader(
				new FileReader(new File(from)));
		double[][] data = new double[2][maxDMs];
		while (inBuffer.ready())
		{
			String[] line = inBuffer.readLine().split("\t");
			if (scaledRange != defaultRange)
			{
				double testDM = Double.parseDouble(line[0]);
				if (testDM >= scaledRange[0] && testDM <= scaledRange[1])
				{
					data[0][count] = testDM;
					data[1][count++] = Double.parseDouble(line[1]);
				}
			} else
			{
				data[0][count] = Double.parseDouble(line[0]);
				data[1][count++] = Double.parseDouble(line[1]);
			}
		}
		inBuffer.close();
		return data;
	}

	protected double[][] readScatterData(String from) throws Exception
	{
		int count = 0;
		double[] defaultRange = { 0.0, 0.0 };
		double[][] data = new double[maxDMs][];
		BufferedReader inBuffer = new BufferedReader(
				new FileReader(new File(from)));
		while (inBuffer.ready())
		{
			String[] line = inBuffer.readLine().split("\t");
			if (scaledRange != defaultRange)
			{
				double testDM = Double.parseDouble(line[0]);
				if (testDM >= scaledRange[0] && testDM <= scaledRange[1])
				{
					double[] values = new double[line.length];
					if (line.length < 2)
					{
						values = new double[2];
						values[0] = Double.parseDouble(line[0]);
						values[1] = 0.0;
					} else
						for (int i = 0; i < values.length; i++)
							values[i] = Double.parseDouble(line[i]);
					data[count++] = values;
				}
			} else
			{
				double[] values = new double[line.length];
				if (line.length < 2)
				{
					values = new double[2];
					values[0] = Double.parseDouble(line[0]);
					values[1] = 0;
				} else
					for (int i = 0; i < values.length; i++)
						values[i] = Double.parseDouble(line[i]);
				data[count++] = values;
			}
		}
		inBuffer.close();
		return data;
	}
}
