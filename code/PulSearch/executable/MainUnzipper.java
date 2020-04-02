package executable;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFileChooser;

import filehandlers.Unzipper;
import util.Constants;

public class MainUnzipper
{
	public static void main(String[] args)
	{
		JFileChooser chooser;

		try
		{
			File[] dirs = null;
			File[] files = null;

			chooser = new JFileChooser(Constants.ZIP_PATH);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setMultiSelectionEnabled(true);
			chooser.setDialogTitle("Select directory of files to unzip...");
			chooser.setApproveButtonText("Unzip");
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
				dirs = chooser.getSelectedFiles();
			else
				System.exit(1);
			for (File dir : dirs)
			{
				files = dir.listFiles();
				int count = Constants.THREAD_COUNT;
				CountDownLatch doneSignal = new CountDownLatch(count);
				for (File file : files)
				{
					String fileName = MainBatchUnzipper
							.getMJD(file.getName());
					File newDir = new File(Constants.RAW_PATH + fileName);
					if (!newDir.exists()) newDir.mkdir();
					new Thread(new Unzipper(fileName, file, doneSignal))
							.start();
					if (--count == 0)
					{
						doneSignal.await();
						count = Constants.THREAD_COUNT;
						doneSignal = new CountDownLatch(count);
					}
				}
			}

		} catch (Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
}
