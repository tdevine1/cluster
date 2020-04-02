package edu.fsuhpc.dsps.util;

import java.util.Comparator;

/**
 * @author zennisarix
 */
public class DoubleArrayComparator implements Comparator<double[]>
{

	/*
	 * Compares two arrays of type <code>double[]</code> by comparing the values
	 * in their first positions.
	 */
	public int compare(double[] arg0, double[] arg1)
	{
		if (arg0 == null || arg1 == null) return 0;
		if (arg0[0] - arg1[0] < 0) return -1;
		if (arg0[0] - arg1[0] == 0)
			return 0;
		else
			return 1;
	}
}
