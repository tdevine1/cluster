package labelers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Enumeration;
import java.util.Vector;

import filehandlers.ArffFileWriter;
import util.Constants;

public class GenericLabeler
{
	public static final String	DESTINATION	= Constants.MAIN_PATH + "test"
			+ File.separator + "names.txt";
	private static final String	SOURCE		= Constants.DMF_PATH
			+ "labeled_all.txt";

	public static void main(String[] args)
	{
		try
		{
			Vector<String[]> sourceData = new Vector<String[]>();
			BufferedReader sourceIn = new BufferedReader(
					new FileReader(SOURCE));
			while (sourceIn.ready())
				sourceData.add(sourceIn.readLine().split(","));
			sourceIn.close();
			Vector<String[]> unknownData = new Vector<String[]>();
			BufferedReader unknownIn = new BufferedReader(
					new FileReader(DESTINATION));
			while (unknownIn.ready())
				unknownData.add(unknownIn.readLine().split(","));
			unknownIn.close();
			ArffFileWriter out = new ArffFileWriter(new File(
					DESTINATION.substring(0, DESTINATION.lastIndexOf("."))
							+ "_labeled.arff"));
			Enumeration<String[]> eUnknown = unknownData.elements();
			while (eUnknown.hasMoreElements())
			{
				String[] unknown = eUnknown.nextElement();
				boolean found = false;
				Enumeration<String[]> eKnown = sourceData.elements();
				while (eKnown.hasMoreElements())
				{
					String[] known = eKnown.nextElement();
					if (unknown[0].equals(known[0])
							&& unknown[1].equals(known[1])
							&& unknown[2].equals(known[2])
							&& unknown[3].equals(known[3])
							&& Double.parseDouble(unknown[4]) == Double
									.parseDouble(known[7])
							&& Double.parseDouble(unknown[5]) == Double
									.parseDouble(known[8])
							&& Double.parseDouble(unknown[6]) == Double
									.parseDouble(known[19]))
					{
						found = true;
						out.write(known[0]);
						for (int i = 1; i < known.length; i++)
							out.write("," + known[i]);
						out.write("\n");
						break;
					}
				}
				if (!found)
				{
					System.out.print("Can't find ");
					for (String s : unknown)
						System.out.print(s + ",");
					System.out.println();
				}
			}
			out.close();
		} catch (Exception e)
		{
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
