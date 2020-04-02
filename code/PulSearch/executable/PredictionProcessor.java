package executable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import util.Constants;

public class PredictionProcessor
{
	public static final String	IN_PATH		= Constants.PREDICTION_PATH
			+ "weka_output" + File.separator + Constants.SURVEY
			+ File.separator;
	public static final String	MATCH_PATH	= Constants.DMF_PATH + "n25m0.5"
			+ File.separator;
	public static final String	OUT_PATH	= Constants.PREDICTION_PATH
			+ Constants.SURVEY + File.separator;
	public static final String	PLOT_PATH	= Constants.PLOT_PATH
			+ Constants.SURVEY + File.separator + "fp_lists" + File.separator;

	public static void main(String[] args)
	{
		int pulsarIndex = 0;
		int trueNeg = 0;
		int falseNeg = 0;
		int falsePos = 0;
		int truePos = 0;
		JFileChooser chooser;

		try
		{
			File inFile = null;
			File matchFile = null;
			File outFile = null;
			File fpsFile = null;

			chooser = new JFileChooser(IN_PATH);
			chooser.setMultiSelectionEnabled(false);
			chooser.setDialogTitle("Select prediction file...");
			chooser.setApproveButtonText("Next");
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
				inFile = chooser.getSelectedFile();
			else
				System.exit(1);
			chooser = new JFileChooser(MATCH_PATH);
			chooser.setMultiSelectionEnabled(false);
			chooser.setDialogTitle("Select known file...");
			chooser.setApproveButtonText("Process");
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
				matchFile = chooser.getSelectedFile();
			else
				System.exit(1);
			pulsarIndex = Integer.parseInt(
					JOptionPane.showInputDialog("Enter the class index:"));
			outFile = new File(OUT_PATH + inFile.getName());
			BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
			fpsFile = new File(PLOT_PATH + "fps_" + inFile.getName());
			BufferedWriter fps = new BufferedWriter(new FileWriter(fpsFile));
			BufferedReader in = new BufferedReader(new FileReader(inFile));
			BufferedReader match = new BufferedReader(
					new FileReader(matchFile));
			while (!match.readLine().toLowerCase().startsWith("@data"))
				;
			while (in.ready())
			{
				String[] prediction = in.readLine().trim().split("[ ]+");
				String instance = match.readLine();
				if (instance.indexOf(",") < 0) break;
				String[] instanceArray = instance.split(",");
				String pulsar = instanceArray[pulsarIndex];
				out.write(instance + "," + prediction[prediction.length - 2]
						+ "," + prediction[prediction.length - 1] + "\n");
				if (pulsar.contains("0"))
				{
					if (prediction[prediction.length - 2].startsWith("*"))
						trueNeg++;
					else
					{
						falsePos++;
						if (instanceArray[0].contains("D"))
							instanceArray[0] = instanceArray[0].substring(0,
									instanceArray[0].indexOf("_"));
						fps.write(instanceArray[0] + "\t" + instanceArray[1]
								+ "\t" + instanceArray[2] + "\t"
								+ instanceArray[3] + "\n");
					}
				} else
					if (pulsar.contains("1"))
					{
						if (prediction[4].startsWith("*"))
							falseNeg++;
						else
							truePos++;
					}
			}
			in.close();
			match.close();
			out.write("Confusion Matrix:\n\t" + trueNeg + "\t" + falseNeg
					+ "\n\t" + falsePos + "\t" + truePos + "\n");
			out.close();
			fps.close();

			System.out.print("Confusion Matrix:\n" + trueNeg + "\t" + falseNeg
					+ "\n" + falsePos + "\t" + truePos + "\n");

		} catch (Exception e)
		{
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
