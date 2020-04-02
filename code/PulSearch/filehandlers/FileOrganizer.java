package filehandlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.swing.JFileChooser;

import util.Constants;

public class FileOrganizer
{
	private static final int	HOW_MANY	= 750000;

	public static void main(String[] args)
	{
		JFileChooser chooser;

		try
		{
			int fileCount = 0;
			int count = 0;
			File inFile = null;

			chooser = new JFileChooser(Constants.DMF_PATH);
			chooser.setDialogTitle("Select file to organize...");
			chooser.setApproveButtonText("Organize");
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
				inFile = chooser.getSelectedFile();
			else
				System.exit(1);
			BufferedReader in = new BufferedReader(new FileReader(inFile));
			ArffFileWriter out = new ArffFileWriter(new File(inFile.getPath()
					.substring(0, inFile.getPath().lastIndexOf("."))
					+ fileCount++ + ".arff"));
			while (in.ready())
			{
				if (count++ >= HOW_MANY)
				{
					out.close();
					out = new ArffFileWriter(new File(inFile.getPath()
							.substring(0, inFile.getPath().lastIndexOf("."))
							+ fileCount++ + ".arff"));
					count = 0;
				}
				String line = in.readLine();
				if (line.contains("?")) out.write(line + "\n");
			}
			out.close();
			in.close();
		} catch (Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
}
