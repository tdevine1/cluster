package datahandlers;

import java.util.Arrays;

import util.ArrayUtils;
import util.DoubleArrayComparator;

/**
 * @author zennisarix
 */
/**
 * @author Tom Devine
 *
 */
public class DistributedFileData
{
	private final double[]		dmRange;
	/**
	 * 
	 */
	private double[][]			maxSNRs;
	/**
	 * 
	 */
	private final String		name;
	/**
	 * 
	 */
	private final double[][]	pulseCount;
	/**
	 * 
	 */
	private final double[]		pulseCountRange;
	/**
	 * 
	 */
	private int					pulseIndex;
	/**
	 * 
	 */
	private int					snrIndex;
	/**
	 * 
	 */
	private final double[]		snrRange;
	/**
	 * The two dimensional storage array containing all SNR values recorded in
	 * the profile. Rather than being represented in an x-array and y-array like
	 * <code>pulseCount</code> and <code>SNRsMax</code>, this array consists of
	 * a variably-sized array element for each DM containing first the DM
	 * itself, followed by the recorded SNR values for that DM.
	 */
	private final double[][]	snrs;
	/**
	 * 
	 */
	private final double[]		timeRange;

	/**
	 * 
	 */
	private final double[][]	times;
	/**
	 * 
	 */
	private int					clusterRank;
	/**
	 * 
	 */
	private double				dmSpacing;

	public DistributedFileData(String cluster, String[] csvData)
			throws Exception
	{
		String[] clusterAry = cluster.split(",");
		name = clusterAry[0] + "," + clusterAry[1] + "," + clusterAry[2] + ","
				+ clusterAry[3];
		clusterRank = Integer.parseInt(clusterAry[4]);
		dmSpacing = Double.parseDouble(clusterAry[7]);
		int maxDMs = csvData.length;
		pulseCount = new double[2][maxDMs];
		times = new double[maxDMs][];
		snrs = new double[maxDMs][];
		dmRange = new double[2];
		snrRange = new double[2];
		pulseCountRange = new double[2];
		timeRange = new double[2];
		dmRange[0] = snrRange[0] = pulseCountRange[0] = timeRange[0] = Double.MAX_VALUE;
		dmRange[1] = snrRange[1] = pulseCountRange[1] = timeRange[1] = Double.MIN_VALUE;
		pulseIndex = snrIndex = 0;
		String dmChunk = "";
		double lastDM = Double.parseDouble(csvData[0].split(",")[0]);
		pulseCount[0][pulseIndex] = lastDM;
		testRange(dmRange, lastDM, 0);
		for (String line : csvData)
		{
			String[] lineAry = line.split(",");
			double nextDM = Double.parseDouble(lineAry[0]);
			if (nextDM == lastDM)
				dmChunk += line + "\n";
			else
			{
				processDMChunk(lastDM, dmChunk.split("\n"));
				testRange(pulseCountRange, pulseCount[1][pulseIndex++], 2);
				snrIndex++;
				lastDM = nextDM;
				pulseCount[0][pulseIndex] = lastDM;
				dmChunk = line + "\n";
			}
		}
		processDMChunk(lastDM, dmChunk.split("\n"));
		pulseCount[0] = Arrays.copyOf(pulseCount[0], pulseIndex + 1);
		pulseCount[1] = Arrays.copyOf(pulseCount[1], pulseIndex + 1);
		sort();
		computeMaxSNRS();
	}

	private void computeMaxSNRS()
	{
		maxSNRs = new double[2][snrs.length];
		maxSNRs[0] = Arrays.copyOf(pulseCount[0], pulseCount[0].length);
		for (int i = 0; i < snrs.length; i++)
		{
			if (snrs[i] != null && snrs[i].length > 1)
				maxSNRs[1][i] = ArrayUtils.maxValue(snrs[i], 1);
			else
				maxSNRs[1][i] = 0.0;
		}
	}

	/**
	 * @return
	 */
	public double[][] getMaxSNRs()
	{
		return maxSNRs;
	}

	/**
	 * @return
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return
	 */
	public double[][] getPulseCount()
	{
		return pulseCount;
	}

	/**
	 * @return
	 */
	public double[] getPulseCountRange()
	{
		return pulseCountRange;
	}

	/**
	 * @return
	 */
	public int getSize()
	{
		return pulseCount[0].length;
	}

	/**
	 * @return
	 */
	public double[] getSNRRange()
	{
		return snrRange;
	}

	/**
	 * @return
	 */
	public double[][] getSNRs()
	{
		return snrs;
	}

	/**
	 * @return
	 */
	public double[][] getTimes()
	{
		return times;
	}

	public double[] getTimeRange()
	{
		return timeRange;
	}

	public int getClusterRank()
	{
		return clusterRank;
	}

	public double getDMSpacing()
	{
		return dmSpacing;
	}

	private void processDMChunk(double dm, String[] dmChunk)
	{
		snrs[snrIndex] = new double[dmChunk.length + 1];
		snrs[snrIndex][0] = dm;
		times[snrIndex] = new double[dmChunk.length + 1];
		times[snrIndex][0] = dm;
		for (int i = 0; i < dmChunk.length; i++)
		{
			String[] tok = dmChunk[i].split(",");
			pulseCount[1][pulseIndex]++;
			double snr, time;
			snrs[snrIndex][i + 1] = snr = Double.parseDouble(tok[1].trim());
			testRange(snrRange, snr, 1);
			times[snrIndex][i + 1] = time = Double.parseDouble(tok[2].trim());
			testRange(timeRange, time, 3);
		}
	}

	/**
	 * 
	 */
	private void sort()
	{
		testsort();
		DoubleArrayComparator dac = new DoubleArrayComparator();
		Arrays.sort(snrs, dac);
		Arrays.sort(times, dac);
	}

	/**
	 * @param range
	 * @param value
	 * @param which
	 */
	private void testRange(double[] range, double value, int which)
	{
		if (value < range[0])
			switch (which)
			{
				case 0:
					dmRange[0] = value;
					break;
				case 1:
					snrRange[0] = value;
					break;
				case 2:
					pulseCountRange[0] = value;
					break;
				case 3:
					timeRange[0] = value;
					break;
			}
		else
			if (value > range[1]) switch (which)
			{
				case 0:
					dmRange[1] = value;
					break;
				case 1:
					snrRange[1] = value;
					break;
				case 2:
					pulseCountRange[1] = value;
					break;
				case 3:
					timeRange[1] = value;
					break;
			}
	}

	/**
	 * 
	 */
	private void testsort()
	{
		boolean flag = true; // set flag to true to begin first pass

		while (flag)
		{
			flag = false; // set flag to false awaiting a possible swap
			for (int i = 0; i < pulseCount[0].length - 1; i++)
				if (pulseCount[0][i] > pulseCount[0][i + 1])
				{
					double temp = pulseCount[0][i]; // swap elements
					pulseCount[0][i] = pulseCount[0][i + 1];
					pulseCount[0][i + 1] = temp;
					temp = pulseCount[1][i]; // swap elements
					pulseCount[1][i] = pulseCount[1][i + 1];
					pulseCount[1][i + 1] = temp;
					flag = true; // shows a swap occurred
				}
		}
	}
}
