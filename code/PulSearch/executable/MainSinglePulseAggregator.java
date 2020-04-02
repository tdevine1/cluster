package executable;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFileChooser;

import aggregators.SinglePulseAggregator;
import filehandlers.FileUtils;
import util.Constants;

public class MainSinglePulseAggregator
{
	public static void main(String[] args)
	{
		JFileChooser chooser;

		try
		{
			File[] dirs = null;

			chooser = new JFileChooser(Constants.RAW_PATH);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setMultiSelectionEnabled(true);
			chooser.setDialogTitle("Select directory of files to aggregate...");
			chooser.setApproveButtonText("Aggregate");
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
				dirs = chooser.getSelectedFiles();
			else
				System.exit(1);
			for (File mjdDir : dirs)
			{
				File[] pointingDirs = mjdDir.listFiles();
				for (File dir : pointingDirs)
				{
					if (!FileUtils.isDirEmpty(dir.toPath())) aggregate(dir);
				}
			}
		} catch (Exception e)
		{
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static void aggregate(File dir) throws InterruptedException
	{
		int count = Constants.THREAD_COUNT;
		CountDownLatch doneSignal = new CountDownLatch(count);
		new Thread(new SinglePulseAggregator(dir, doneSignal)).start();
		if (--count == 0)
		{
			doneSignal.await();
			count = Constants.THREAD_COUNT;
			doneSignal = new CountDownLatch(count);
		}
	}
}
