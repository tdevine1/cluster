package filehandlers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JFileChooser;

import util.Constants;

public class CSVFixer
{
	public static void main(String[] args)
	{
		try
		{
			File[] mjdDirs = null;
			JFileChooser chooser = new JFileChooser(
					"A:\\AstroData\\data\\palfa\\csv\\");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setMultiSelectionEnabled(true);
			chooser.setDialogTitle("Select directories to aggregate...");
			chooser.setApproveButtonText("Aggregate");
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
				mjdDirs = chooser.getSelectedFiles();
			else
				System.exit(1);
			int count = 0;
			for (File mjdDir : mjdDirs)
			{
				for (File csvFile : mjdDir.listFiles())
				{
					Vector<String> output = new Vector<String>();
					BufferedReader in = new BufferedReader(
							new FileReader(csvFile));
					// throw away header
					in.readLine();
					while (in.ready())
					{
						String line = in.readLine();
						String[] lineAry = line.split(",");
						if (lineAry.length > 5)
						{
							String correctedLine = lineAry[0];
							for (int i = 1; i < lineAry.length; i++)
							{
								correctedLine += "," + lineAry[i];
								if (lineAry[i].contains("NaN")) i++;
							}
							System.out.println(count++ + ": " + line + " -> "
									+ correctedLine);
							line = correctedLine;// 56269\p2030.20121128.G61.08+00.65.S.b5_singlepulse.csv
						}
						output.add(line + "\n");
					}
					writeCSV(csvFile.getPath(), output);
					in.close();
				}
			}
		} catch (Exception e)
		{
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static void writeCSV(String path, Vector<String> output)
			throws IOException
	{
		BufferedWriter out = new BufferedWriter(
				new FileWriter(new File(path), false));
		out.write(Constants.CSV_HEADER);
		Enumeration<String> e = output.elements();
		while (e.hasMoreElements())
			out.write(e.nextElement());
		out.close();
	}
}