package filters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Random;
import java.util.Vector;

import javax.swing.JFileChooser;

import filehandlers.ArffFileWriter;
import util.Constants;

public class RandomSelector
{
	public static final int HOW_MANY = 100000;

	public static void main(String[] args)
	{
		JFileChooser chooser;

		try
		{
			File inFile = null;
			int total, index;
			total = index = 0;
			Vector<Integer> indices = new Vector<Integer>();
			Random rand = new Random(System.currentTimeMillis());
			chooser = new JFileChooser(Constants.DMF_PATH);
			chooser.setMultiSelectionEnabled(true);
			chooser.setDialogTitle("Select file to choose from...");
			chooser.setApproveButtonText("Randomize");
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
				inFile = chooser.getSelectedFile();
			else
				System.exit(1);
			BufferedReader in = new BufferedReader(new FileReader(inFile));
			ArffFileWriter out = new ArffFileWriter(new File(inFile.getPath()
					.substring(0, inFile.getPath().lastIndexOf("."))
					+ "_random-" + HOW_MANY + ".arff"));
			index = 0;
			// while (in.ready())
			// {
			// String line = in.readLine();
			// if (line.substring(line.length() - 1).contains("1"))
			// {
			// out.write(line + "\n");
			// indices.add(index);
			// total++;
			// }
			// index++;
			// }
			// in.close();
			// in = new BufferedReader(new FileReader(inFile));
			while (total < HOW_MANY)
			{
				if (!in.ready())
				{
					in.close();
					in = new BufferedReader(new FileReader(inFile));
					index = 0;
				}
				String line = in.readLine();
				if (line.substring(line.length() - 1).contains("0")
						&& line.contains(".") && !indices.contains(index)
						&& rand.nextFloat() <= 0.2)
				{
					out.write(line + "\n");
					indices.add(index);
					total++;
				}
				index++;
			}
			in.close();
			out.close();
		} catch (Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
}
