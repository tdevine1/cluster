package filters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.commons.math3.util.FastMath;

import filehandlers.ArffFileWriter;
import filehandlers.FileUtils;
import util.Constants;

public class ArffDuplicateFilter
{
	private static final int	MASTER_CLASS_INDEX	= 4;
	private static final String	TYPE				= "noise";
	public static final String	MASTER_PATH			= Constants.LABEL_PATH
			+ Constants.SURVEY + File.separator + "noise.txt";
	public static final String	SLAVE_PATH			= Constants.DMF_PATH
			+ Constants.SURVEY + "_" + Constants.GRANULARITY + "-MASTER.arff";

	public static void main(String[] args)
	{
		try
		{
			int matchCount = 0;
			String[] master = FileUtils.readTxtFile(new File(MASTER_PATH));
			BufferedReader slave = new BufferedReader(
					new FileReader(new File(SLAVE_PATH)));
			ArffFileWriter out = new ArffFileWriter(
					new File(Constants.LABEL_PATH + Constants.SURVEY
							+ File.separator + Constants.SURVEY + "_"
							+ Constants.GRANULARITY + "-" + TYPE + ".arff"));
			while (!slave.readLine().startsWith("@DATA"))
				;
			while (slave.ready())
			{
				String[] arffCell = slave.readLine().split(",");
				for (String masterLine : master)
				{
					String[] masterCell = masterLine.split("\t");
					String name = arffCell[0];
					if (Constants.SURVEY == "aodrift")
						name = name.substring(0, name.indexOf("_"));
					if (name.equals(masterCell[0]))
					{
						if (FastMath.abs(Double.parseDouble(arffCell[8])
								- Double.parseDouble(masterCell[3])) < 0.5)
						// if (Double.parseDouble(arffCell[1]) >= Double
						// .parseDouble(masterCell[1])
						// && Double.parseDouble(arffCell[2]) <= Double
						// .parseDouble(masterCell[2]))
						{

							for (int i = 0; i < arffCell.length - 1; i++)
								out.write(arffCell[i] + ",");
							if (TYPE.equals("noise"))
								out.write("0\n");
							else
								out.write(
										masterCell[MASTER_CLASS_INDEX] + "\n");
							matchCount++;
							System.out.println("Found match: " + name + "\t"
									+ arffCell[1] + "\t" + arffCell[2] + "\t"
									+ arffCell[8]);
							System.out.println("       with: " + masterCell[0]
									+ "\t" + masterCell[1] + "\t"
									+ masterCell[2] + "\t" + masterCell[3]);
							break;
						}
					}
				}
			}
			slave.close();
			out.close();
			System.out.println("Found " + matchCount + " matches!");
		} catch (

		Exception e)

		{
			e.printStackTrace();
		}
	}
}
