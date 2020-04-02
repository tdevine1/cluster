package algorithms;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.commons.math3.util.FastMath;

import datahandlers.DistributedPulSearcher;
import datahandlers.SinglePulseCandidate;
import util.Constants;

public class DRAPID
{
	/**
	 * 
	 */
	protected int					binSize			= Constants.BIN_SIZE;
	/**
	 * 
	 */
	protected double				M	= Constants.SLOPE_THRESHOLD;
	/**
	 * 
	 */
	protected int					maxSize;
	/**
	 * 
	 */
	protected String				name;
	/**
	 * 
	 */
	private SinglePulseCandidate	spc;
	/**
	 * 
	 */
	private DistributedPulSearcher	ps;
	/**
	 * The rank of this pulse in its candidate plot.
	 */
	private int						clusterRank;
	private double					dmSpacing;

	public DRAPID(String name, int maxSize, DistributedPulSearcher ps,
			int clusterRank, double dmSpacing)
	{
		this.name = name;
		this.maxSize = maxSize;
		this.ps = ps;
		this.clusterRank = clusterRank;
		this.dmSpacing = dmSpacing;
		spc = new SinglePulseCandidate(name, clusterRank, dmSpacing);
		if (maxSize <= 12)
			binSize = 1;
		else
			binSize = (int) FastMath
					.floor(Constants.BIN_WEIGHT * FastMath.pow(maxSize, 0.5));
	}

	private void add(int index)
	{
		ps.addCandidate(spc, index);
		terminate(index);
	}

	public void search(int start, double b_Nminus1)
	{
		int next = start + binSize;
		if (next > maxSize) return;
		SimpleRegression sReg = new SimpleRegression();
		double[][] currentBin = ps.getSNRs(start, next);
		for (int i = 0; i < currentBin[0].length; i++)
		{
			if (currentBin[1][i] > 5.0)
				sReg.addData(currentBin[0][i], currentBin[1][i]);
		}
		double b_N = sReg.getSlope();
		int status = spc.getStatus();
		if (b_Nminus1 < -M)
		{
			if (-M < b_N && b_N  < M) if (status < 1) terminate(next);
			if (b_N > M)
			{
				if (status == 1) add(next);
				start(start);
			}
		} else
			if (-M < b_Nminus1 && b_Nminus1  < M)
			{
				if (b_N < -M)
				{
					if (status == 0) spc.setPeak(true);
					if (status == -1) terminate(next);
				}
				if (-M < b_N && b_N  < M) if (status == 1)
					add(next);
				else
					terminate(next);
				if (b_N > M)
				{
					if (status == -1) start(start);
					if (status == 1)
					{
						add(next);
						start(start);
					}
				}
			} else
				if (b_Nminus1 > M)
				{
					if (b_N < -M) spc.setPeak(true);
					if (-M < b_N && b_N  < M) if (status == -1) start(start);
					if (b_N > M) if (status == -1) start(start);
				}
		search(next, b_N);
	}

	private void start(int index)
	{
		spc = new SinglePulseCandidate(name, clusterRank, dmSpacing, index);
	}

	private void terminate(int index)
	{
		spc = new SinglePulseCandidate(name, clusterRank, dmSpacing);
	}
}
