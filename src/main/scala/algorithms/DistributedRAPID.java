package algorithms;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.commons.math3.util.FastMath;

import datahandlers.DistributedPulSearcher;
import datahandlers.SinglePulseCandidate;
import util.Constants;

public class DistributedRAPID
{
	/**
	 * 
	 */
	protected int							binSize			= Constants.BIN_SIZE;
	/**
	 * 
	 */
	protected double						slopeThreshold	= Constants.SLOPE_THRESHOLD;
	/**
	 * 
	 */
	protected int							maxSize;
	/**
	 * 
	 */
	protected String						name;
	/**
	 * 
	 */
	private SinglePulseCandidate			current;
	/**
	 * 
	 */
	private DistributedPulSearcher	ps;
	/**
	 * The rank of this pulse in its candidate plot.
	 */
	private int								clusterRank;
	private double							dmSpacing;

	public DistributedRAPID(String name, int maxSize,
			DistributedPulSearcher ps, int clusterRank, double dmSpacing)
	{
		this.name = name;
		this.maxSize = maxSize;
		this.ps = ps;
		this.clusterRank = clusterRank;
		this.dmSpacing = dmSpacing;
		current = new SinglePulseCandidate(name, clusterRank, dmSpacing);
		if (maxSize <= 12)
			binSize = 1;
		else
			binSize = (int) FastMath
					.floor(Constants.BIN_WEIGHT * FastMath.pow(maxSize, 0.5));
	}

	private void add(int index)
	{
		ps.addCandidate(current, index);
		terminate(index);
	}

	public void search(int start, int prevM)
	{
		int next = start + binSize;
		if (next > maxSize) return;
		SimpleRegression sReg = new SimpleRegression();
		double[][] neighborhoodS = ps.getSNRs(start, next);
		for (int i = 0; i < neighborhoodS[0].length; i++)
		{
			if (neighborhoodS[1][i] > 5.0)
				sReg.addData(neighborhoodS[0][i], neighborhoodS[1][i]);
		}
		double m = sReg.getSlope();
		int status = current.getStatus();
		int curM = 0;
		if (m > slopeThreshold) curM = 1;
		if (m < -slopeThreshold) curM = -1;
		if (prevM == -1)
		{
			if (curM == 0) if (status < 1) terminate(next);
			if (curM == 1)
			{
				if (status == 1) add(next);
				start(start);
			}
		} else
			if (prevM == 0)
			{
				if (curM == -1)
				{
					if (status == 0) current.setPeak(true);
					if (status == -1) terminate(next);
				}
				if (curM == 0) if (status == 1)
					add(next);
				else
					terminate(next);
				if (curM == 1)
				{
					if (status == -1) start(start);
					if (status == 1)
					{
						add(next);
						start(start);
					}
				}
			} else
				if (prevM == 1)
				{
					if (curM == -1) current.setPeak(true);
					if (curM == 0) if (status == -1) start(start);
					if (curM == 1) if (status == -1) start(start);
				}
		search(next, curM);
	}

	private void start(int index)
	{
		current = new SinglePulseCandidate(name, clusterRank, dmSpacing, index);
	}

	private void terminate(int index)
	{
		current = new SinglePulseCandidate(name, clusterRank, dmSpacing);
	}
}
