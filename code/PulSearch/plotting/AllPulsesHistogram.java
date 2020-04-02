package plotting;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import util.Constants;

public class AllPulsesHistogram
{
	public static final String	OUT_PATH	= Constants.MAIN_PATH + "test"
													+ File.separator;

	public static void main(String[] args)
	{
		try
		{
			BufferedWriter outBuffer = new BufferedWriter(new FileWriter(
					new File(OUT_PATH + "AllPulses.txt")));
			BufferedReader inBuffer;
			File mainDir = null;
			mainDir = new File(Constants.DAT_PATH);
			for (File dir : mainDir.listFiles())
			{
				if (dir.listFiles().length != 0)
				{
					System.out.println("Processing " + dir.getName() + "...");
					inBuffer = new BufferedReader(new FileReader(new File(
							dir.getPath() + "\\sigmas.dat")));
					while (inBuffer.ready())
					{
						String[] line = inBuffer.readLine().split("\t");
						if (line.length > 1)
						{
							for (int i = 1; i < line.length; i++)
							{
								if (Float.parseFloat(line[i]) >= 10.0)
								{
									outBuffer.write(line[0] + "\t" + line[i]
											+ "\n");
								}
							}
						}
						outBuffer.flush();
					}
					inBuffer.close();
				}
			}
			outBuffer.close();

		} catch (Exception e)
		{
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
