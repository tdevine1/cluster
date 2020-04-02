package util;

import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JOptionPane;

public class PeriodicityCalculator
{

	private static int	MINIMUM_COUNTS		= 5;
	private static int	NUMBER_OF_DIVISIONS	= 25;

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		DecimalFormat df = new DecimalFormat("#.####");
		String output = "Peak Times : ";
		String[] times;
		Vector<Double[]> periods = new Vector<Double[]>();

		times = JOptionPane.showInputDialog("Enter Peak Times").split("\t");
		if (times.equals(null)) System.exit(0);
		for (String time : times)
			output += df.format(Double.parseDouble(time)) + "\t";
		output += "\nDifferences : ";
		double[] diffs = new double[times.length - 1];
		for (int i = 0; i < diffs.length; i++)
		{
			diffs[i] = Double.parseDouble(times[i + 1])
					- Double.parseDouble(times[i]);
			output += df.format(diffs[i]) + "\t";
		}
		output += "\n\n";
		for (double diff : diffs)
		{
			for (int i = 1; i < NUMBER_OF_DIVISIONS; i++)
			{
				Double[] period = new Double[3];
				period[0] = period[1] = 0.0;
				period[0] = diff / i;
				for (double diff2 : diffs)
				{
					String factor = "" + diff2 / period[0];
					if (factor.split("\\.")[1].startsWith("0")
							|| factor.split("\\.")[1].startsWith("9"))
						period[1]++;
				}
				if (period[1] >= MINIMUM_COUNTS && period[0] >= 0.001)
				{
					double totalErr = 0.0;
					periods.add(period);
					output += "Period : " + df.format(period[0]) + "\n";
					for (double diff3 : diffs)
					{
						double factorErr = Double.parseDouble(("" + diff3
								/ period[0]).substring(("" + diff3 / period[0])
								.indexOf(".")));
						if (factorErr > 0.1) factorErr = 1.0 - factorErr;
						output += df.format(diff3) + "\t"
								+ df.format(diff3 / period[0]) + "\t"
								+ df.format(factorErr) + "\n";
						totalErr += factorErr;
					}
					period[2] = totalErr;
					output += "Total Error = " + totalErr + "\n\n";
				}
			}
		}
		double gcd = -1;
		double mostCounts = -1;
		double leastError = 100.0;
		Enumeration<Double[]> e = periods.elements();
		while (e.hasMoreElements())
		{
			Double[] period = e.nextElement();
			if (period[1] >= mostCounts) mostCounts = period[1];
			if (period[0] >= gcd) gcd = period[0];
			if (period[2] <= leastError) leastError = period[2];
		}
		output += "PERIOD\tCOUNTS\n";
		Vector<Double[]> leastErrorPeriods = new Vector<Double[]>();
		Vector<Double[]> mostCountsPeriods = new Vector<Double[]>();
		e = periods.elements();
		while (e.hasMoreElements())
		{
			Double[] period = e.nextElement();
			if (period[1] == mostCounts) mostCountsPeriods.add(period);
			if (period[2] == leastError) leastErrorPeriods.add(period);
		}
		e = mostCountsPeriods.elements();
		double gcdWithMostCounts = -1.0;
		while (e.hasMoreElements())
		{
			Double[] period = e.nextElement();
			if (period[0] > gcdWithMostCounts) gcdWithMostCounts = period[0];
		}
		e = leastErrorPeriods.elements();
		double gcdWithLeastError = -1.0;
		while (e.hasMoreElements())
		{
			Double[] period = e.nextElement();
			if (period[0] > gcdWithLeastError) gcdWithLeastError = period[0];
		}

		output += "Most Counts = " + mostCounts + "\nLeast Error = "
				+ df.format(leastError) + "\nGCD : " + df.format(gcd)
				+ "\nGCD with Most Counts = " + df.format(gcdWithMostCounts)
				+ "\nGCD with Least Error = " + df.format(gcdWithLeastError);
		System.out.print(output);
	}
}
