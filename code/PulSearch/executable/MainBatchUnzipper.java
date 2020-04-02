package executable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import javax.swing.JFileChooser;

import util.Constants;
import util.MJDConverter;

public class MainBatchUnzipper
{
	public static final String		UNZIP		= "\"C:\\Program Files\\WinRAR\\WinRAR.exe\" X -inul -o- -ed ";
	private static int				fileCount	= 1;
	private static BufferedWriter	out;

	public static String getMJD(String fileName)
	{
		if (!fileName.contains("singlepulse")) return null;
		String fixedFileName = "";
		if (Constants.SURVEY.equals("palfa"))
		{
			String gregorian = fileName.substring(fileName.indexOf(".") + 1);
			gregorian = gregorian.substring(0, gregorian.indexOf("."));
			int[] mjd = new int[3];
			mjd[0] = Integer.parseInt(gregorian.substring(0, 4));
			mjd[1] = Integer.parseInt(gregorian.substring(4, 6));
			mjd[2] = Integer.parseInt(gregorian.substring(6));
			fixedFileName += (int) MJDConverter.toJulian(mjd);
		} else
			fixedFileName += fileName.substring(0, fileName.lastIndexOf("_"));
		return fixedFileName;
	}

	public static void main(String[] args)
	{
		JFileChooser chooser;

		try
		{
			File[] dirs = null;

			chooser = new JFileChooser(Constants.ZIP_PATH);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setMultiSelectionEnabled(true);
			chooser.setDialogTitle("Select directory of files to unzip...");
			chooser.setApproveButtonText("Unzip");
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
				dirs = chooser.getSelectedFiles();
			else
				System.exit(1);
			out = new BufferedWriter(new FileWriter(new File(Constants.RAW_PATH
					+ Constants.SURVEY + "_" + fileCount++ + ".bat")));
			for (File dir : dirs)
			{
				if (Constants.SURVEY == "aodrift")
				{
					File[] strips = dir.listFiles();
					for (File strip : strips)
					{
						File[] stripDirs = strip.listFiles();
						for (File stripDir : stripDirs)
							process(stripDir.listFiles());
					}
				} else
					process(dir.listFiles());
			}
			out.close();
		} catch (Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private static void process(File[] files) throws Exception
	{
		int count = 0;
		for (File file : files)
		{
			if (count++ > 9000)
			{
				out.close();
				out = new BufferedWriter(new FileWriter(
						new File(Constants.RAW_PATH + Constants.SURVEY + "_"
								+ fileCount++ + ".bat")));
				count = 0;
			}
			if (Constants.SURVEY == "palfa")
			{
				String fileName = file.getName();
				String mjd = getMJD(fileName);
				if (mjd != null)
				{
					String path = Constants.RAW_PATH + mjd + File.separator
							+ fileName.substring(0, fileName.lastIndexOf(".0"))
							+ File.separator;
					File newDir = new File(path);
					if (!newDir.exists()) newDir.mkdirs();
					String cmd = UNZIP + file.getPath() + " *.* " + path;
					out.write(cmd + "\n");
				}
			} else
			{
				String fromPath = file.getParent();
				String path = Constants.RAW_PATH
						+ fromPath.substring(fromPath.indexOf("aodrift") + 9,
								fromPath.indexOf("aodrift") + 13)
						+ File.separator
						+ fromPath.substring(fromPath.lastIndexOf("\\") + 1)
						+ File.separator;
				File newDir = new File(path);
				if (!newDir.exists()) newDir.mkdirs();
				String cmd = UNZIP + file.getPath() + " *.* " + path;
				out.write(cmd + "\n");

			}
		}
	}
}
