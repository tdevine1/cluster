package labelers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Enumeration;
import java.util.Vector;

import filehandlers.ArffFileWriter;
import filehandlers.FileUtils;
import util.Constants;
import util.RADecUtils;

public class ReLabeler
{
	public static final String	ARFF_PATH	= Constants.DMF_PATH
			+ Constants.SURVEY + "_" + Constants.GRANULARITY
			+ "-m0.50w0.75snr5.1-pulsars.arff";
	public static final String	ADD_PATH	= Constants.LABEL_PATH
			+ "rratalog_knowns_07-21-2015.txt";
	public static final String	OUT_PATH	= Constants.DMF_PATH
			+ Constants.SURVEY + "_" + Constants.GRANULARITY
			+ "-m0.50w0.75snr5.1-pulsars-TEST.arff";

	public static void main(String[] args)
	{
		try
		{
			int count = 0;
			Vector<String> toAdd = FileUtils.readFileToVector(ADD_PATH);
			BufferedReader in = new BufferedReader(
					new FileReader(new File(ARFF_PATH)));
			ArffFileWriter out = new ArffFileWriter(new File(OUT_PATH));
			while (!in.readLine().startsWith("@DATA"))
				;
			while (in.ready())
			{
				String line = in.readLine();
				boolean match = false;
				String[] arffLine = line.split(",");
				Enumeration<String> e = toAdd.elements();
				while (e.hasMoreElements())
				{
					String addLine = e.nextElement();
					String[] addLineAry = addLine.split("\t");

					if (RADecUtils.isInRange(addLineAry[0], arffLine[2], 200)
							&& RADecUtils.isInRange(addLineAry[1], arffLine[3],
									200)
							&& RADecUtils.isInRange(addLineAry[2],
									arffLine[Constants.PEAK_INDEX], 5.0))
					{
						out.write(line.substring(0, line.lastIndexOf(","))
								+ ",1\n");
						match = true;
						count++;
						System.out.println("Found match: " + arffLine[0] + "\t"
								+ arffLine[1] + "\t" + arffLine[2] + "\t"
								+ arffLine[3] + "\t"
								+ arffLine[Constants.PEAK_INDEX]);
						break;
					}
					// int matchCount = 0;
					// if (arffLine[0].equals(addLineAry[0]))
					// {
					// for (int i = 1; i < addLineAry.length; i++)
					// {
					// if (Double.parseDouble(arffLine[i]) == Double
					// .parseDouble(addLineAry[i]))
					// matchCount++;
					// }
					// }
					// if (matchCount >= Constants.MATCH_COUNT)
					// {
					// out.write(line.substring(0, line.lastIndexOf(",")) + ","
					// + addLineAry[addLineAry.length - 1] + "\n");
					// match = true;
					// toAdd.remove(addLine);
					// count++;
					// System.out.println("Found match: " + arffLine[0] + "\t"
					// + arffLine[1] + "\t" + arffLine[2] + "\t"
					// + arffLine[3]);
					// break;
					// }
				}
				// if (!match) out.write(line + "\n");
				if (!match)
				{
					if (Double.parseDouble(
							arffLine[Constants.PEAK_INDEX]) < 100.0)
					{
						if (Double.parseDouble(arffLine[20]) < 20) // near dim
							out.write(line.substring(0, line.lastIndexOf(","))
									+ ",2\n");
						else // near bright
							out.write(line.substring(0, line.lastIndexOf(","))
									+ ",3\n");
					} else
						if (Double.parseDouble(
								arffLine[Constants.PEAK_INDEX]) < 175.0)
						{
							if (Double.parseDouble(arffLine[20]) < 20) // mid
																		// dim
								out.write(
										line.substring(0, line.lastIndexOf(","))
												+ ",4\n");
							else // mid bright
								out.write(
										line.substring(0, line.lastIndexOf(","))
												+ ",5\n");
						} else
						{
							if (Double.parseDouble(arffLine[20]) < 20) // far
																		// dim
								out.write(
										line.substring(0, line.lastIndexOf(","))
												+ ",6\n");
							else // far bright
								out.write(
										line.substring(0, line.lastIndexOf(","))
												+ ",7\n");
						}

				}

			}
			in.close();
			out.close();
			System.out.println("Found " + count + " Matches!");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
