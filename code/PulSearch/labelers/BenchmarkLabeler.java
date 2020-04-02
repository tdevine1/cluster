package labelers;

import java.io.File;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JFileChooser;

import filehandlers.FileUtils;
import util.Constants;
import util.RADecUtils;

public class BenchmarkLabeler
{
	public static void main(String[] args)
	{
		int matchedKnownsCount = 0;
		JFileChooser chooser;
		String outName = "";
		String knowns = "";
		File arffFile = null;
		Vector<String[]> known = null;

		try
		{
			chooser = new JFileChooser(Constants.DMF_PATH);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setMultiSelectionEnabled(false);
			chooser.setDialogTitle("Select file to label...");
			chooser.setApproveButtonText("OK");
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
			{
				arffFile = chooser.getSelectedFile();
				outName = arffFile.getName();
			} else
				System.exit(1);
			chooser = new JFileChooser(Constants.LABEL_PATH);
			chooser.setMultiSelectionEnabled(false);
			chooser.setDialogTitle("Select known file...");
			chooser.setApproveButtonText("Process");
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
				known = FileUtils
						.parsePsrcatKnowns(chooser.getSelectedFile().getPath());
			else
				System.exit(1);
			Vector<String[]> arffData = FileUtils.readArffFile(arffFile);
			Enumeration<String[]> eArffData = arffData.elements();
			while (eArffData.hasMoreElements())
			{
				String[] unknown = eArffData.nextElement();
				if (RADecUtils.RaDecDMMatch(unknown, known, true))
				{
					if (Constants.SURVEY.equals("gbt350drift"))
						knowns += unknown[0] + "\t";
					else
						if (Constants.SURVEY.equals("aodrift"))
							knowns += unknown[0].substring(0,
									unknown[0].indexOf("_")) + "\t";
					knowns += unknown[1] + "\t" + unknown[2] + "\t" + unknown[8]
							+ "\n";
					matchedKnownsCount++;
				}
			}
			System.out.println(
					"Matched " + matchedKnownsCount + " known candidates.");
			FileUtils.writeFile(knowns,
					Constants.DMF_PATH + outName + "_knowns_matched.txt");
		} catch (Exception e)
		{
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
