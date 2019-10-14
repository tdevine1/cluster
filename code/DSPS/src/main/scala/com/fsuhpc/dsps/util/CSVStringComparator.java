package com.fsuhpc.dsps.util;

import java.util.Comparator;

/**
 * @author zennisarix
 */
public class CSVStringComparator implements Comparator<String>
{

	/*
	 * Compares two arrays of type <code>(Double) String[]</code> by comparing
	 * the values in their first positions.
	 */

	public int compare(String arg0, String arg1)
	{
		if (arg0 == null || arg1 == null) return 0;
		arg0 = arg0.substring(0, arg0.indexOf(","));
		arg1 = arg1.substring(0, arg1.indexOf(","));
		if (arg0.equals("") && arg1.equals("")) return 0;
		if (arg0.equals("")) return -1;
		if (arg1.equals("")) return 1;
		double dArg0 = Double.parseDouble(arg0);
		double dArg1 = Double.parseDouble(arg1);
		if (dArg0 - dArg1 < 0) return -1;
		if (dArg0 - dArg1 == 0)
			return 0;
		else
			return 1;

	}
}
