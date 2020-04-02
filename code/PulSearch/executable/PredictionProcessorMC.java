package executable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import javax.swing.JFileChooser;

import util.Constants;

public class PredictionProcessorMC
{
	private static final int	FIRST_CLASS	= 16;
	public static final String	IN_PATH		= Constants.PREDICTION_PATH
			+ "weka_output" + File.separator + Constants.SURVEY
			+ File.separator;
	public static final String	MATCH_PATH	= Constants.DMF_PATH;
	public static final String	OUT_PATH	= Constants.PREDICTION_PATH
			+ Constants.SURVEY + File.separator;
	public static final String	PLOT_PATH	= Constants.PLOT_PATH
			+ Constants.SURVEY + File.separator + "fp_lists" + File.separator;

	public static void main(String[] args)
	{
		int[][] confusion = new int[Constants.CLASS_COUNT][Constants.CLASS_COUNT];
		for (int i = 0; i < Constants.CLASS_COUNT; i++)
			for (int j = 0; j < Constants.CLASS_COUNT; j++)
				confusion[i][j] = 0;
		JFileChooser chooser;

		try
		{
			File inFile = null;
			File matchFile = null;
			File outFile = null;
			File[] fpsFiles = new File[Constants.CLASS_COUNT - 1];

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
			outFile = new File(OUT_PATH + inFile.getName());
			BufferedWriter out = new BufferedWriter(new FileWriter(outFile));
			BufferedWriter[] fps = new BufferedWriter[Constants.CLASS_COUNT
					- 1];
			for (int i = 0; i < Constants.CLASS_COUNT - 1; i++)
			{
				fpsFiles[i] = new File(
						PLOT_PATH + i + "_fps_" + inFile.getName());
				fps[i] = new BufferedWriter(new FileWriter(fpsFiles[i]));
			}

			BufferedReader in = new BufferedReader(new FileReader(inFile));
			BufferedReader match = new BufferedReader(
					new FileReader(matchFile));
			while (!match.readLine().toLowerCase().startsWith("@data"))
				;
			in.readLine();
			while (in.ready())
			{
				String[] prediction = in.readLine().trim().split("[ ]+");
				String instance = match.readLine();
				if (prediction != null && instance != null)
				{
					String[] instanceArray = instance.split(",");
					out.write(instance + ",");
					out.write(prediction[2].split(":")[1] + "\n");
					// rows are predicted values, cols are actual values
					for (int row = 0; row < Constants.CLASS_COUNT; row++)
					{
						if (instanceArray[FIRST_CLASS].contains("" + row))
						{
							int col = 0;
							for (int i = 0; i < Constants.CLASS_COUNT; i++)
							{
								if (prediction[2].split(":")[1].equals("" + i))
								{
									confusion[row][col]++;
									if (row == 0 && col > 0)
									{
										if (instanceArray[0].contains("D"))
											instanceArray[0] = instanceArray[0]
													.substring(0,
															instanceArray[0]
																	.indexOf(
																			"_"));
										fps[col - 1].write(instanceArray[0]
												+ "\t" + instanceArray[1] + "\t"
												+ instanceArray[2] + "\t"
												+ instanceArray[3] + "\n");
									}
								}
								col++;
							}
						}
					}
				}
			}
			in.close();
			match.close();
			out.write("Confusion Matrix:\n");
			for (int i = 0; i < Constants.CLASS_COUNT; i++)
			{
				for (int j = 0; j < Constants.CLASS_COUNT; j++)
					out.write(confusion[i][j] + "\t");
				out.write("\n");
			}
			out.close();
			for (int i = 0; i < Constants.CLASS_COUNT - 1; i++)
				fps[i].close();

			System.out.println("Confusion Matrix:");
			for (int i = 0; i < Constants.CLASS_COUNT; i++)
			{
				for (int j = 0; j < Constants.CLASS_COUNT; j++)
					System.out.print(confusion[i][j] + "\t");
				System.out.println();
			}
		} catch (Exception e)
		{
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}
}