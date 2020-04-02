package algorithms;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import datahandlers.Candidate;
import datahandlers.DPGPulSearcher;
import util.Constants;

public class RAPID
{
	/**
	 * 
	 */
	protected int			binSize			= Constants.BIN_SIZE;
	/**
	 * 
	 */
	protected double		slopeThreshold	= Constants.SLOPE_THRESHOLD;
	/**
	 * 
	 */
	private Candidate		current;
	/**
	 * 
	 */
	protected int			maxSize;
	/**
	 * 
	 */
	protected String		name;
	/**
	 * 
	 */
	private DPGPulSearcher	ps;

	public RAPID()
	{
		name = null;
		maxSize = -1;
		ps = null;
		current = null;
	}

	public RAPID(String name, int maxSize, DPGPulSearcher ps)
	{
		this.name = name;
		this.maxSize = maxSize;
		this.ps = ps;
		current = new Candidate(name);
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
			if (neighborhoodS[1][i] != 0.0)
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
				if (status == 1) add(start);
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
					add(start);
				else
					terminate(next);
				if (curM == 1)
				{
					if (status == -1) start(start);
					if (status == 1)
					{
						add(start);
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
		current = new Candidate(name, index);
		// System.out.println("start = " + index);
	}

	private void terminate(int index)
	{
		current = new Candidate(name);
		// System.out.println("terminate = " + index);
	}
}
