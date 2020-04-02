package filehandlers;

import java.io.File;

import javax.swing.JFileChooser;

import util.Constants;

public class FileDeleter
{

	public static void main(String[] args)
	{
		JFileChooser chooser;

		try
		{
			File[] toDelete = null;

			chooser = new JFileChooser(Constants.RAW_PATH);
			chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			chooser.setMultiSelectionEnabled(true);
			chooser.setDialogTitle(
					"Select file and/or directories to delete...");
			chooser.setApproveButtonText("Delete");
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
				toDelete = chooser.getSelectedFiles();
			else
				System.exit(1);
			for (File gone : toDelete)
			{
				System.out.print("Deleting " + gone.getPath() + "...");
				org.apache.commons.io.FileUtils.deleteQuietly(gone);
				System.out.println("DONE");
			}
		} catch (Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

}
