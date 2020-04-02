package labelers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JFileChooser;

import org.apache.commons.math3.util.FastMath;

import filehandlers.ArffFileWriter;
import filehandlers.FileUtils;
import util.Constants;
import util.RADecUtils;

public class ClusterPeakLabeler
{
	public static final String ADD_PATH = Constants.LABEL_PATH
			+ Constants.SURVEY + File.separator
			+ "n25m0.5\\known_pulsarsRRATS_09-18-2015.txt";

	public static void main(String[] args)
	{
		try
		{
			File chosen = null;
			JFileChooser chooser = new JFileChooser(Constants.DMF_PATH);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setDialogTitle("Select file to label...");
			chooser.setApproveButtonText("OK");
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
				chosen = chooser.getSelectedFile();
			else
				System.exit(1);
			int count = 0;
			Vector<String> toAdd = FileUtils.readFileToVector(ADD_PATH);
			BufferedReader in = new BufferedReader(new FileReader(chosen));
			ArffFileWriter out = new ArffFileWriter(new File(chosen.getPath()
					.substring(0, chosen.getPath().lastIndexOf("."))
					+ "-TRAIN.arff"));
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
					String[] nameAry = addLineAry[0].split("_");
					String[] raDec = RADecUtils.extractRADec(addLineAry[0]);
					if (arffLine[0].equals(nameAry[0])
							&& arffLine[1].equals(nameAry[1])
							&& arffLine[2].equals(raDec[0])
							&& arffLine[3].contains(raDec[1]))
					{
						double unknownPeakDM = Double
								.parseDouble(arffLine[Constants.PEAK_INDEX]);
						double knownPeakDM = Double.parseDouble(addLineAry[3]);
						double difference = FastMath
								.abs(knownPeakDM - unknownPeakDM);
						if ((Double.parseDouble(
								arffLine[Constants.DM_START_INDEX]) < knownPeakDM
								&& Double.parseDouble(
										arffLine[Constants.DM_STOP_INDEX]) > knownPeakDM)
								&& difference > 2.0)

						// difference < Constants.DM_THRESHOLD_MAX
						// && difference > Constants.DM_THRESHOLD_MIN)
						{
							out.write(line.substring(0, line.lastIndexOf(","))
									+ "," + addLineAry[addLineAry.length - 1]
									+ "\n");
							match = true;
							count++;
							System.out.print("Found match: ");
							for (int i = 0; i < 4; i++)
								System.out.print(arffLine[i] + "\t");
							System.out.println(arffLine[7] + "\t" + arffLine[8]
									+ "\t" + arffLine[19]);
						}
					}
				}
				// if (!match) out.write(line + "\n");
			}
			in.close();
			out.close();
			System.out.println("Found " + count + " Matches!");
		} catch (

		Exception e)

		{
			e.printStackTrace();
		}
	}
}
