package labelers;

import java.io.File;
import java.util.Vector;

import javax.swing.JFileChooser;

import filehandlers.FileUtils;
import util.Constants;
import util.RADecUtils;

public class FalsePositiveMatcher
{
	public static final String		UNKNOWN_PATH	= Constants.PLOT_PATH
															+ Constants.SURVEY
															+ File.separator
															+ "fp_lists"
															+ File.separator;
	public static final String		OUT_PATH		= Constants.LABEL_PATH
															+ Constants.SURVEY
															+ File.separator;
	private static final boolean	FROM_ARFF_FILE	= false;

	public static void main(String[] args)
	{
		int knownsCount, unknownsCount;
		String outName = "";
		String knowns = "";
		String unknowns = "";
		String[] unknown = null;
		Vector<String[]> known = null;
		knownsCount = unknownsCount = 0;
		try
		{
			JFileChooser chooser = new JFileChooser(UNKNOWN_PATH);
			chooser.setMultiSelectionEnabled(false);
			chooser.setDialogTitle("Select unknown file...");
			chooser.setApproveButtonText("Next");
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
			{
				System.out.println("Reading file...");
				unknown = FileUtils.readTxtFile(chooser.getSelectedFile());
				outName = chooser.getSelectedFile().getName();
				outName = outName.substring(0, outName.indexOf("."));
				System.out.println("File read...");
			} else
				System.exit(1);
			chooser = new JFileChooser(Constants.LABEL_PATH);
			chooser.setMultiSelectionEnabled(false);
			chooser.setDialogTitle("Select known file...");
			chooser.setApproveButtonText("Process");
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
				known = FileUtils.parsePsrcatKnowns(chooser.getSelectedFile()
						.getPath());
			else
				System.exit(1);
			for (String u : unknown)
			{
				if (RADecUtils.RaDecDMMatch(u.split("\t"), known,
						FROM_ARFF_FILE))
				{
					knowns += u + "\n";
					knownsCount++;
				} else
				{
					unknowns += u + "\n";
					unknownsCount++;
				}
			}
			System.out.println("Matched " + knownsCount
					+ " known candidates.\nUnable to match " + unknownsCount
					+ " unknown candidates.");
			FileUtils.writeFile(knowns, OUT_PATH + outName + "_known.txt");
			FileUtils.writeFile(unknowns, OUT_PATH + outName + "_unknown.txt");
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
