package aggregators;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;

import javax.swing.JFileChooser;

import filehandlers.ArffFileWriter;
import util.Constants;

public class ArffFileAggregator
{
	public static void main(String[] args)
	{
		JFileChooser chooser;

		try
		{
			DecimalFormat df = new DecimalFormat("0.00");
			String outPath = Constants.DMF_PATH + Constants.SURVEY + "_"
					+ Constants.GRANULARITY + "-m"
					+ df.format(Constants.SLOPE_THRESHOLD) + "w"
					+ df.format(Constants.BIN_WEIGHT) + "-MASTER.arff";
			File[] files = null;

			// files = new File[] {
			// new File("F:\\Hal\\AstroData\\arff\\signals_"
			// + "GBT350drift_54222_1836+1624.arff"),
			// new File("F:\\Hal\\AstroData\\arff\\signals_"
			// + "GBT350drift_54236_1745+2241.arff"),
			// new File("F:\\Hal\\AstroData\\arff\\signals_"
			// + "GBT350drift_54237_1923+2217.arff"),
			// new File("F:\\Hal\\AstroData\\arff\\signals_"
			// + "GBT350drift_54239_1802-0417.arff"),
			// new File("F:\\Hal\\AstroData\\arff\\signals_"
			// + "GBT350drift_54285_1546-0647.arff") };

			chooser = new JFileChooser(Constants.ARFF_PATH);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setMultiSelectionEnabled(true);
			chooser.setDialogTitle("Select files to aggregate...");
			chooser.setApproveButtonText("Aggregate");
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
				files = chooser.getSelectedFiles();
			else
				System.exit(1);

			ArffFileWriter writer = new ArffFileWriter(new File(outPath));
			BufferedReader inBuffer;
			for (File f : files)
			{
				inBuffer = new BufferedReader(new FileReader(f));
				String beam = f.getName().substring(
						f.getName().lastIndexOf(".") - 1,
						f.getName().lastIndexOf("."));
				while (inBuffer.ready())
				{
					String[] line = inBuffer.readLine().split(",");
					for (int i = 0; i < 4; i++)
						writer.write(line[i] + ",");
					writer.write(beam + ",");
					for (int i = 4; i < line.length; i++)
						writer.write(line[i] + ",");
					writer.write(",0\n");
				}
				inBuffer.close();
				// f.delete();
			}
			writer.close();
		} catch (IOException ioe)
		{
			System.out.println("ERROR: " + ioe.getMessage());
			ioe.printStackTrace();
		}
	}
}
