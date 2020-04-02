package filehandlers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import javax.swing.JFileChooser;

import executable.MainBatchUnzipper;
import util.Constants;

public class FileValidator
{
	public static void main(String[] args)
	{
		JFileChooser chooser;

		try
		{
			int count = 0;
			int fileCount = 1;
			File[] mjdDirs = null;
			File[] pointingDirs = null;

			chooser = new JFileChooser(Constants.RAW_PATH);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setMultiSelectionEnabled(true);
			chooser.setDialogTitle("Select directory of files to validate...");
			chooser.setApproveButtonText("Validate");
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
				mjdDirs = chooser.getSelectedFiles();
			else
				System.exit(1);
			BufferedWriter out = new BufferedWriter(
					new FileWriter(new File(Constants.RAW_PATH + "validation_"
							+ fileCount++ + ".bat")));
			for (File mjdDir : mjdDirs)
			{
				pointingDirs = mjdDir.listFiles();
				for (File pointingDir : pointingDirs)
				{
					if (FileUtils.isDirEmpty(pointingDir.toPath()))
					{
						System.out.println(pointingDir.getPath());
						String mjd = MainBatchUnzipper
								.getMJD(pointingDir.getName());
						if (mjd != null)
						{
							String path = Constants.RAW_PATH + mjd
									+ File.separator
									+ mjd.substring(0, mjd.lastIndexOf(".0"))
									+ File.separator;
							String cmd = MainBatchUnzipper.UNZIP
									+ Constants.ZIP_PATH + path + " *.* "
									+ Constants.RAW_PATH + mjd + "\\";
							File newDir = new File(path);
							if (!newDir.exists()) newDir.mkdirs();
							out.write(cmd + "\n");
							count++;
							if (count > 1000)
							{
								out.close();
								out = new BufferedWriter(new FileWriter(
										new File(Constants.RAW_PATH
												+ "validation_" + fileCount++
												+ ".bat")));
								count = 0;
							}
						}
					}
				}
			}
			out.close();
		} catch (Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
}
