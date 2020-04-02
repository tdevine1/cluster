package executable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import javax.swing.JFileChooser;

import util.Constants;

public class ResultsCompiler
{
	static final String[]		ALGORITHMS	= { "MPN", "SMO", "JRip", "PART",
			"J48", "RF" };

	static final String[]		DATASETS	= { "smote" };

	private static final int	REPETITIONS	= 3;

	private static final int	FOLDS		= 5;

	public static void main(String[] args)
	{
		try
		{
			File file = null;
			JFileChooser chooser = new JFileChooser(Constants.MAIN_PATH + "weka"
					+ File.separator + "results" + File.separator);
			chooser.setDialogTitle("Select results file to compile...");
			chooser.setApproveButtonText("Compile");
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
				file = chooser.getSelectedFile();
			else
				System.exit(1);
			BufferedReader inBuffer = new BufferedReader(new FileReader(file));
			float[][][] results = new float[ALGORITHMS.length][][];
			int algorithmCount = 0;
			int datasetCount = 0;
			inBuffer.readLine();
			results[algorithmCount] = new float[DATASETS.length][];
			results[algorithmCount][datasetCount] = new float[] { 0, 0, 0, 0, 0,
					0 };
			while (inBuffer.ready() && algorithmCount < ALGORITHMS.length)
			{
				String[] line = inBuffer.readLine().split(",");
				results[algorithmCount][datasetCount][0] += Float
						.parseFloat(line[30]); // TP for weka, TN for me
				results[algorithmCount][datasetCount][1] += Float
						.parseFloat(line[32]); // FP for weka, FN for me
				results[algorithmCount][datasetCount][3] += Float
						.parseFloat(line[34]); // TN for weka, TP for me
				results[algorithmCount][datasetCount][2] += Float
						.parseFloat(line[36]); // FN for weka, FP for me
				results[algorithmCount][datasetCount][4] += Float
						.parseFloat(line[55]) / (FOLDS * REPETITIONS);
				results[algorithmCount][datasetCount][5] += Float
						.parseFloat(line[56]) / (FOLDS * REPETITIONS);
				int run = Integer.parseInt(line[1]);
				int fold = Integer.parseInt(line[2]);
				if (run == REPETITIONS && fold == FOLDS)
				{
					System.out.println(ALGORITHMS[algorithmCount] + "\t"
							+ datasetCount + "\t" + DATASETS[datasetCount]);
					if (++datasetCount < DATASETS.length)
					{
						results[algorithmCount][datasetCount] = new float[] { 0,
								0, 0, 0, 0, 0 };
					} else
					{
						if (++algorithmCount == ALGORITHMS.length) break;
						datasetCount = 0;
						results[algorithmCount] = new float[DATASETS.length][];
						results[algorithmCount][datasetCount] = new float[] { 0,
								0, 0, 0, 0, 0 };
					}
				}
			}
			inBuffer.close();

			BufferedWriter outBuffer = new BufferedWriter(new FileWriter(
					file.getPath().substring(0, file.getPath().lastIndexOf("."))
							+ "_compiled.csv"));
			outBuffer.write(" ," + ALGORITHMS[0]);
			for (int i = 1; i < ALGORITHMS.length; i++)
				outBuffer.write(", , ," + ALGORITHMS[i]);
			outBuffer.write("\n");
			datasetCount = 0;
			for (int i = 0; i < DATASETS.length; i++)
			{
				outBuffer.write(DATASETS[i]);
				for (int j = 0; j < ALGORITHMS.length; j++)
				{
					if (results[j][i] == null) break;
					outBuffer.write("," + results[j][i][0] + ","
							+ results[j][i][1] + "," + results[j][i][4]);
				}
				outBuffer.write("\n");
				for (int j = 0; j < ALGORITHMS.length; j++)
				{
					if (results[j][i] == null) break;
					outBuffer.write("," + results[j][i][2] + ","
							+ results[j][i][3] + "," + results[j][i][5]);
				}
				outBuffer.write("\n");

			}
			outBuffer.close();
		} catch (Exception e)
		{
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
