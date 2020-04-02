package filehandlers;

import java.io.File;
import java.util.Enumeration;
import java.util.Vector;

import datahandlers.SinglePulseFileData;
import plotting.Plotter;
import util.ArrayUtils;
import util.Constants;

public class SinglePulseDatFileReader extends DatFileReader
{
	/**
	 * @param input
	 *            A String[] containing:
	 *            <ol>
	 *            <li>file name</li>
	 *            <li>clusterRank</li>
	 *            <li>merged?</li>
	 *            <li>cluster size</li>
	 *            <li>center_DMspacing</li>
	 *            <li>center_DM</li>
	 *            <li>center_time</li>
	 *            <li>center_SNR</li>
	 *            <li>center_sampling</li>
	 *            <li>center_downfact</li>
	 *            <li>min_DM</li>
	 *            <li>max_DM</li>
	 *            <li>min_time</li>
	 *            <li>max_time</li>
	 *            <li>slope threshold (optional)</li>
	 *            <li>bin size (optional)</li>
	 *            </ol>
	 */
	public SinglePulseDatFileReader(String[] input) throws Exception
	{
		initializeCluster(input);
	}

	private void initializeCluster(String[] input) throws Exception
	{
		name = input[0] + "_" + input[1] + "_" + input[2] + "_" + input[3];
		dir = Constants.DAT_PATH + input[1] + File.separator + input[2] + "."
				+ input[3];
		timeRange = new double[] { Double.parseDouble(input[15]),
				Double.parseDouble(input[16]) };
		dmRange = new double[] { Double.parseDouble(input[13]),
				Double.parseDouble(input[14]) };
		scaledRange = dmRange;
		snrRange = new double[2];
		findMaxDMs(dir + "/sigmas.dat");
		snrs = readScatterData(dir + "/sigmas.dat");
		times = readScatterData(dir + "/times.dat");
		filter();
		snrRange[0] = ArrayUtils.minValue(snrs);
		snrRange[1] = ArrayUtils.maxValue(snrs);
		Object[] maxSNRsPulseCounts = SinglePulseFileData
				.computeMaxSNRsPulseCounts(snrs);
		pulseCounts = (double[][]) maxSNRsPulseCounts[1];
		maxSNRs = (double[][]) maxSNRsPulseCounts[0];
		pulseCountRange = ArrayUtils.getRange(pulseCounts);
		peakDM = Double.parseDouble(input[8]);
		data = new SinglePulseFileData(name, dmRange, pulseCountRange,
				pulseCounts, snrRange, snrs, maxSNRs, timeRange, times,
				Integer.parseInt(input[4]), Double.parseDouble(input[7]));
	}

	private void initializePeak(String[] input) throws Exception
	{
		name = input[0] + "_" + input[1] + "_" + input[2] + input[3];
		dir = Constants.DAT_PATH + name;
		timeRange = new double[] { Double.parseDouble(input[10]),
				Double.parseDouble(input[11]) };
		dmRange = new double[] { Double.parseDouble(input[7]),
				Double.parseDouble(input[8]) };
		scaledRange = dmRange;
		snrRange = new double[2];
		findMaxDMs(dir + "/sigmas.dat");
		snrs = readScatterData(dir + "/sigmas.dat");
		times = readScatterData(dir + "/times.dat");
		filter();
		snrRange[0] = ArrayUtils.minValue(snrs);
		snrRange[1] = ArrayUtils.maxValue(snrs);
		Object[] maxSNRsPulseCounts = SinglePulseFileData
				.computeMaxSNRsPulseCounts(snrs);
		pulseCounts = (double[][]) maxSNRsPulseCounts[1];
		maxSNRs = (double[][]) maxSNRsPulseCounts[0];
		pulseCountRange = ArrayUtils.getRange(pulseCounts);
		peakDM = Double.parseDouble(input[19]);
		data = new SinglePulseFileData(name, dmRange, pulseCountRange,
				pulseCounts, snrRange, snrs, maxSNRs, timeRange, times,
				Integer.parseInt(input[12]), Double.parseDouble(input[9]));
	}

	private void filter()
	{
		double[][] filteredSNRs = new double[maxDMs][];
		double[][] filteredTimes = new double[maxDMs][];
		for (int i = 0; i < maxDMs; i++)
		{
			Vector<Integer> validIndices = new Vector<Integer>();
			for (int j = 1; j < snrs[i].length; j++)
			{
				if (times[i][j] >= timeRange[0] && times[i][j] <= timeRange[1])
					validIndices.add(j);
			}
			double[] newSNRValues;
			double[] newTimeValues;
			if (validIndices.isEmpty())
			{
				newSNRValues = new double[2];
				newTimeValues = new double[2];
				newSNRValues[0] = newTimeValues[0] = snrs[i][0];
				newSNRValues[1] = 5.0;
				newTimeValues[1] = 0.0;
			} else
			{
				newSNRValues = new double[validIndices.size() + 1];
				newTimeValues = new double[validIndices.size() + 1];
				newSNRValues[0] = newTimeValues[0] = snrs[i][0];
				Enumeration<Integer> e = validIndices.elements();
				int count = 1;
				while (e.hasMoreElements())
				{
					int validIndex = e.nextElement();
					newSNRValues[count] = snrs[i][validIndex];
					newTimeValues[count++] = times[i][validIndex];
				}
			}
			filteredSNRs[i] = newSNRValues;
			filteredTimes[i] = newTimeValues;
		}
		snrs = filteredSNRs;
		times = filteredTimes;
	}

	public SinglePulseDatFileReader(String id, String input) throws Exception
	{
		String[] inputAry = input.split(",");
		if (Constants.PLOT == "peak")
			initializePeak(inputAry);
		else
			if (Constants.PLOT == "cluster") initializeCluster(inputAry);
		System.out.println("Creating plots for " + name + "...");
		new Plotter(name, dmRange, peakDM, scaledRange, pulseCountRange,
				pulseCounts, snrRange, snrs, timeRange, times)
						.plot(id + "_" + name);
	}

	@Override
	public SinglePulseFileData getData()
	{
		return (SinglePulseFileData) data;
	}

}
