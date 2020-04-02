package aggregators;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import javax.swing.JFileChooser;

import util.Constants;

public class CSVAggregator
{
	private final static String	OUT_PATH		= Constants.CSV_PATH + "palfa";
	private static final int	NUMBER_OF_DAYS	= 14;

	public static void main(String[] args)
	{
		try
		{
			int count = 196;
			String mjd = null;
			boolean setMJD = true;
			BufferedWriter out = new BufferedWriter(
					new FileWriter(new File(OUT_PATH + "_" + count + ".csv")));
			out.write("Survey,MJD,Pointing,Beam," + Constants.CSV_HEADER);
			File[] mjdDirs = null;
			JFileChooser chooser = new JFileChooser(Constants.CSV_PATH);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setMultiSelectionEnabled(true);
			chooser.setDialogTitle("Select directories to aggregate...");
			chooser.setApproveButtonText("Aggregate");
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
				mjdDirs = chooser.getSelectedFiles();
			else
				System.exit(1);
			for (File mjdDir : mjdDirs)
			{
				System.out.print("Processing " + mjdDir.getName() + "...");
				if (setMJD)
				{
					mjd = mjdDir.getName();
					setMJD = false;
				}
				for (File csvFile : mjdDir.listFiles())
				{
					BufferedReader in = new BufferedReader(
							new FileReader(csvFile));
					// throw away header
					in.readLine();
					while (in.ready())
					{
						String pointing = csvFile.getName();
						if (Constants.SURVEY == "gbt350drift")
						{
							out.write(Constants.SURVEY + ","
									+ pointing.substring(pointing.indexOf("_")
											+ 1, pointing.lastIndexOf("_"))
									+ ","
									+ pointing.substring(
											pointing.lastIndexOf("_") + 1,
											pointing.lastIndexOf("_") + 10)
									+ ",0,");
						}
						if (Constants.SURVEY == "aodrift")
						{
							out.write(Constants.SURVEY + "," + mjdDir.getName()
									+ ","
									+ pointing.substring(
											pointing.indexOf("D") + 1,
											pointing.indexOf("_"))
									+ ",0,");
						}
						if (Constants.SURVEY == "palfa")
						{
							out.write(Constants.SURVEY + "," + mjdDir.getName()
									+ ","
									+ pointing.substring(pointing.indexOf("G"),
											pointing.indexOf("b") - 3)
									+ ","
									+ pointing.substring(
											pointing.indexOf("b") + 1,
											pointing.indexOf("b") + 2)
									+ ",");
						}
						out.write(in.readLine() + "\n");
					}
					in.close();
					out.flush();
				}
				if (++count % NUMBER_OF_DAYS == 0)
				{
					out.close();
					System.out.println(OUT_PATH + "_" + (count - NUMBER_OF_DAYS)
							+ ".csv\tcontains: " + mjd + "-"
							+ mjdDir.getName());
					out = new BufferedWriter(new FileWriter(
							new File(OUT_PATH + "_" + count + ".csv")));
					out.write(
							"Survey,MJD,Pointing,Beam," + Constants.CSV_HEADER);
					setMJD = true;
				}

				System.out.println("FINISHED!!!");
			}
			out.close();
		} catch (Exception e)
		{
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
