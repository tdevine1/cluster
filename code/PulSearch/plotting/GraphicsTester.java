package plotting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import util.Constants;

public class GraphicsTester
{
	private static final String	OUT_PATH	= Constants.PLOT_PATH + "test"
													+ File.separator;

	public static void main(String[] args)
	{
		int count, maxDMs;
		File inFile;
		double[] dmRange;
		double[] dataRange = { Double.MAX_VALUE, -1.0 };
		double[][] data;
		String[] userInput;
		ScatterPlot plot;
		JFileChooser chooser;
		BufferedReader inBuffer;

		try
		{
			count = maxDMs = 0;
			dmRange = new double[2];
			inFile = null;
			chooser = new JFileChooser(Constants.MAIN_PATH);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setMultiSelectionEnabled(false);
			chooser.setDialogTitle("Select Scatterplot Data...");
			chooser.setApproveButtonText("OK");
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
				inFile = chooser.getSelectedFile();
			else
				System.exit(1);
			userInput = JOptionPane.showInputDialog(
					"Enter DM Range [lower],[upper]:").split(",");
			dmRange[0] = Double.parseDouble(userInput[0].trim());
			dmRange[1] = Double.parseDouble(userInput[1].trim());
			inBuffer = new BufferedReader(new FileReader(inFile));
			while (inBuffer.ready())
			{
				String[] line = inBuffer.readLine().split("\t");
				double[] values = new double[line.length];
				for (int i = 0; i < values.length; i++)
					values[i] = Double.parseDouble(line[i]);
				if (values.length > 1 && values[0] >= dmRange[0]
						&& values[0] <= dmRange[1])
					for (int i = 1; i < values.length; i++)
					{
						if (values[i] < dataRange[0]) dataRange[0] = values[i];
						if (values[i] > dataRange[1]) dataRange[1] = values[i];
					}
				maxDMs++;
			}
			data = new double[maxDMs][];
			inBuffer = new BufferedReader(new FileReader(inFile));
			while (inBuffer.ready())
			{
				String[] line = inBuffer.readLine().split("\t");
				double[] values = new double[line.length];
				for (int i = 0; i < values.length; i++)
					values[i] = Double.parseDouble(line[i]);
				data[count++] = values;
			}
			File path = new File(OUT_PATH
					+ inFile.getPath().substring(
							inFile.getPath().indexOf("GBT"),
							inFile.getPath().indexOf(".")) + ".png");
			String type = path.getName().substring(0,
					path.getName().indexOf("."));
			type = type.substring(0, 1).toUpperCase()
					+ type.substring(1, type.length() - 1);
			if (path.exists()) path.delete();
			path.getParentFile().mkdirs();
			path.createNewFile();
			plot = new ScatterPlot(path, type + " vs DM for "
					+ path.getParentFile().getName(), "DM", type, maxDMs);
			plot.setData(data, type);
			plot.setRange(dataRange);
			plot.setDomain(dmRange);
			plot.save();
		} catch (IOException e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
}
