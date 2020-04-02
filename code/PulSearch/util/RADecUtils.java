package util;

import java.util.Enumeration;
import java.util.Vector;

public class RADecUtils
{

	public static String[] extractRADec(String name)
	{
		String[] raDec = new String[2];
		int stopIndex = 0;
		stopIndex = (name.indexOf("+") > 0) ? name.indexOf("+")
				: name.indexOf("-");
		int raLength = 0;
		if (Constants.SURVEY.equals("gbt350drift"))
			raLength = 4;
		else
			if (Constants.SURVEY.equals("aodrift")) raLength = 6;
		raDec[0] = name.substring(stopIndex - raLength,
				stopIndex - raLength + 4);
		raDec[1] = name.substring(stopIndex, stopIndex + 5);
		return raDec;
	}

	public static boolean isInRange(String value1, String value2, int range)
	{
		if (value1.equals("NA") || value2.equals("NA")) return true;
		return Math.abs(
				Integer.parseInt(value1) - Integer.parseInt(value2)) < range;
	}

	public static boolean RaDecDMMatch(String[] data,
			Vector<String[]> psrcatKnowns, boolean fromArffFile)
	{

		Enumeration<String[]> e = psrcatKnowns.elements();
		while (e.hasMoreElements())
		{
			String[] psrcatKnown = e.nextElement();
			String psrcatRA = psrcatKnown[0];
			String psrcatDEC = psrcatKnown[2];
			int stopIndex = data[0].indexOf(psrcatKnown[1]);
			if (stopIndex > 0)
			{
				if (Constants.SURVEY.equals("gbt350drift") || stopIndex < 10)
				{
					String dataRA = null;
					String dataDEC = null;
					String[] raDec = extractRADec(data[0]);
					dataRA = raDec[0];
					dataDEC = raDec[1];
					if (isInRange(dataRA, psrcatRA, Constants.DEGREE_RANGE)
							&& isInRange(dataDEC, psrcatDEC,
									Constants.DEGREE_RANGE))
					{
						double psrcatDM, dataDM;
						psrcatDM = dataDM = -1.0;
						if (!psrcatKnown[3].equals("NA"))
						{
							psrcatDM = Float.parseFloat(psrcatKnown[3]);
							if (fromArffFile)
								dataDM = Float.parseFloat(data[8]);
							else
								dataDM = Float.parseFloat(data[3]);
						}
						if (psrcatDM < 0 || Math
								.abs(dataDM - psrcatDM) < Constants.DM_RANGE)
						{
							System.out.println("Found " + psrcatKnown[0]
									+ psrcatKnown[1] + psrcatKnown[2] + " "
									+ psrcatKnown[3] + "\t@ " + data[0] + " "
									+ dataDM);
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public static boolean isInRange(String value1, String value2, double range)
	{

		if (value1.equals("NA") || value2.equals("NA")) return true;
		return Math.abs(Double.parseDouble(value1)
				- Double.parseDouble(value2)) < range;

	}

}
