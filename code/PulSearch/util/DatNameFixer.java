package util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class DatNameFixer
{
	public static void main(String[] args)
	{
		File mainDir = new File(Constants.DAT_PATH);
		File[] files = mainDir.listFiles();

		try
		{
			for (File f : files)
			{
				if (f.getName().contains("p2030"))
				{
					String name = f.getName();
					name = name.substring(name.indexOf(".") + 1);
					String mjd = "" + MJDConverter
							.toJulian(name.substring(0, name.indexOf(".")));
					mjd = mjd.substring(0, mjd.indexOf("."));
					File mjdDir = new File(
							Constants.DAT_PATH + mjd + File.separator);
					if (!mjdDir.exists()) mjdDir.mkdir();
					name = name.substring(name.indexOf("G"));
					File newDir = new File(
							Constants.DAT_PATH + mjd + File.separator + name);
					if (!newDir.exists()) newDir.mkdir();
					for (File d : f.listFiles())
					{
						Files.move(Paths.get(d.getPath()),
								Paths.get(newDir.getPath() + File.separator
										+ d.getName()),
								StandardCopyOption.REPLACE_EXISTING);
					}
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
