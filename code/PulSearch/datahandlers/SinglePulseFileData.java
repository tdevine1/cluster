package datahandlers;

import util.ArrayUtils;

public class SinglePulseFileData extends FileData
{
	/**
	 * 
	 */
	private int		clusterRank;

	private double	dmSpacing;

	public static Object[] computeMaxSNRsPulseCounts(double[][] snrs)
	{
		double[][] maxSNRs = new double[2][snrs.length];
		double[][] pulseCounts = new double[2][snrs.length];
		for (int i = 0; i < snrs.length; i++)
		{
			if (snrs[i] != null && snrs[i].length > 1)
			{
				pulseCounts[0][i] = maxSNRs[0][i] = snrs[i][0];
				maxSNRs[1][i] = ArrayUtils.maxValue(snrs[i], 1);
				pulseCounts[1][i] += snrs[i].length - 1;
			} else
				maxSNRs[1][i] = 0.0;
		}
		return new Object[] { maxSNRs, pulseCounts };
	}

	public SinglePulseFileData(String name, double[] dmRange,
			double[] pulseCountRange, double[][] pulseCounts, double[] snrRange,
			double[][] snrs, double[][] maxSNRs, double[] timeRange,
			double[][] times, int clusterRank, double dmSpacing)
	{
		super(name, dmRange, pulseCountRange, pulseCounts, snrRange, snrs,
				maxSNRs, timeRange, times);
		this.clusterRank = clusterRank;
		this.dmSpacing = dmSpacing;
	}

	public int getClusterRank()
	{
		return clusterRank;
	}

	public double getDMSpacing()
	{
		return dmSpacing;
	}
}
