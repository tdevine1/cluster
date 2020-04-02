package algorithms;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import datahandlers.Candidate;
import datahandlers.DPGPulSearcher;

public class RAPIDnonRecursive
{
	public static final String	RELATION_NAME		= "n25m1.0";
	/**
	 * 
	 */
	private Candidate			current;
	/**
	 * 
	 */
	private final int			maxSize;
	/**
	 * 
	 */
	private final String		name;
	/**
	 * 
	 */
	private final int			NEIGHBORHOOD_SIZE	= 25;
	/**
	 * 
	 */
	private final DPGPulSearcher	ps;
	/**
	 * 
	 */
	private final double		SLOPE_THRESHOLD		= 0.5;

	public RAPIDnonRecursive(String name, int maxSize, DPGPulSearcher ps)
	{
		this.name = name;
		this.maxSize = maxSize;
		this.ps = ps;
		current = new Candidate(name);
	}

	private void add(int index)
	{
		// System.out.println("adding...");
		ps.addCandidate(current, index);
		terminate(index);
	}

	public void search()
	{
		int start = 0;
		int curM = 0;
		int prevM = 0;

		while (start < maxSize)
		{
			int next = start + NEIGHBORHOOD_SIZE;
			SimpleRegression sReg = new SimpleRegression();
			double[][] neighborhoodS = ps.getSNRs(start, next);
			for (int i = 0; i < neighborhoodS[0].length; i++)
				sReg.addData(neighborhoodS[0][i], neighborhoodS[1][i]);
			double m = sReg.getSlope();
			int status = current.getStatus();
			if (m > SLOPE_THRESHOLD) curM = 1;
			if (m < -SLOPE_THRESHOLD) curM = -1;
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
			start = next;
			prevM = curM;
		}
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
