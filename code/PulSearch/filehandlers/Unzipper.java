package filehandlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;

import util.Constants;

public class Unzipper implements Runnable
{

	private static final String		UNZIP	= "\"C:\\Program Files\\WinRAR\\WinRAR.exe\" X ";
	private final String			dirPath;
	private final File				file;
	private final CountDownLatch	doneSignal;

	public Unzipper(String dirPath, File file, CountDownLatch doneSignal)
	{
		this.dirPath = dirPath;
		this.file = file;
		this.doneSignal = doneSignal;
	}

	@Override
	public void run()
	{
		try
		{
			String cmd = UNZIP + file.getPath() + " *.* " + Constants.RAW_PATH
					+ dirPath + "\\";
			System.out.println(cmd);
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader inBuffer = new BufferedReader(
					new InputStreamReader(p.getInputStream()));
			while (inBuffer.ready())
				System.out.println(inBuffer.readLine());
			inBuffer.close();
			inBuffer = new BufferedReader(
					new InputStreamReader(p.getErrorStream()));
			while (inBuffer.ready())
				System.out.println(inBuffer.readLine());
			inBuffer.close();
			if (doneSignal != null) doneSignal.countDown();
		} catch (IOException e)
		{
			System.out.println("\nError unzipping " + file.getPath());
			e.printStackTrace();
		}
	}
}
