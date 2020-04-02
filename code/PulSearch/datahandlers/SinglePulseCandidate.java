package datahandlers;

import java.text.DecimalFormat;

import util.Constants;

public class SinglePulseCandidate extends Candidate
		implements Comparable<SinglePulseCandidate>
{
	/**
	 * The rank of this pulse in its candidate plot.
	 */
	private int		clusterRank;
	/**
	 * The rank this candidate in the pulse by SNR. 1 is the brightest pulse, 2
	 * the next, etc.
	 */
	private int		pulseRank;
	/**
	 * The unit distance in DM between 2 signals.
	 */
	private double	dmSpacing;

	public SinglePulseCandidate(String name, int clusterRank, double dmSpacing)
	{
		super(name);
		this.clusterRank = clusterRank;
		this.dmSpacing = dmSpacing;
		pulseRank = -1;
	}

	public SinglePulseCandidate(String name, int clusterRank, double dmSpacing,
			int index)
	{
		super(name, index);
		this.clusterRank = clusterRank;
		this.dmSpacing = dmSpacing;
		pulseRank = -1;
	}

	@Override
	public int compareTo(SinglePulseCandidate candidate)
	{
		if (this.snrMax < candidate.snrMax) return -1;
		if (this.snrMax > candidate.snrMax) return 1;
		return 0;
	}

	public int getPulseRank()
	{
		return pulseRank;
	}

	public void setPulseRank(int pulseRank)
	{
		this.pulseRank = pulseRank;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		DecimalFormat df = new DecimalFormat("#0.00");
		String output = "";
		try
		{
			output += survey + "," + mjd + "," + raDec[0] + "," + raDec[1] + ","
					+ beam + "," + centralFrequency + "," + channelBandwidth
					+ "," + numChannels + "," + df.format(startDM) + ","
					+ df.format(stopDM) + "," + df.format(dmSpacing) + ","
					+ startTime + "," + stopTime + "," + clusterRank + ","
					+ pulseRank + "," + df.format(pulseCountPeakDM) + ","
					+ pulseCountMax + "," + df.format(pulseCountLocalPeakHeight)
					+ "," + pulseCountIntensity + ","
					+ df.format(pulseCountAvgIntensity) + ","
					+ df.format(snrPeakDM) + "," + df.format(snrMax) + ","
					+ df.format(snrLocalPeakHeight) + ","
					+ df.format(snrIntensity) + "," + df.format(snrAvgIntensity)
					+ "," + df.format(snrRatio) + "," + printNaN(fittedWidth)
					+ "," + printNaN(fittedPeakFlux) + ","
					+ printNaN(snrMaxFitChiSquare);
		} catch (Exception e)
		{
			System.out.println("ERROR Writing " + Constants.SURVEY + "_"
					+ raDec[0] + raDec[1]);
			e.printStackTrace();
		}
		return output;
	}
}
