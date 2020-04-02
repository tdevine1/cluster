package executable;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFileChooser;

import datahandlers.DPGPulSearcher;
import datahandlers.SinglePulSearcher;
import util.Constants;

public class MainPulSearcher
{
	public static void main(String[] args)
	{
		try
		{

			if (Constants.GRANULARITY.equals("dpg"))
			{
				File[] mainDir = null;
				JFileChooser chooser = new JFileChooser(Constants.CSV_PATH);
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setMultiSelectionEnabled(true);
				chooser.setDialogTitle("Select directory to search...");
				chooser.setApproveButtonText("Search");
				if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
					mainDir = chooser.getSelectedFiles();
				else
					System.exit(1);
				if (Constants.SURVEY.equals("palfa"))
				{
					for (File dir : mainDir)
					{
						for (File singlePulseFile : dir.listFiles())
							new Thread(new DPGPulSearcher(singlePulseFile))
									.start();
					}
				} else
				{
					for (File dir : mainDir)
					{
						if (dir.listFiles().length != 0)
							new Thread(new DPGPulSearcher(dir)).start();
					}
				}
			} else
				if (Constants.GRANULARITY.equals("singlepulse"))
				{

					File[] clusterFiles = null;

					JFileChooser chooser = new JFileChooser(
							Constants.MAIN_PATH + "data" + File.separator
									+ Constants.SURVEY + File.separator);
					chooser.setDialogTitle("Select cluster files to search...");
					chooser.setApproveButtonText("Search");
					chooser.setMultiSelectionEnabled(true);
					if (chooser.showOpenDialog(
							null) == JFileChooser.APPROVE_OPTION)
						clusterFiles = chooser.getSelectedFiles();
					else
						System.exit(1);
					int count = Constants.THREAD_COUNT;
					for (File cf : clusterFiles)
					{
						CountDownLatch doneSignal = new CountDownLatch(count);
						System.out.println("Processing " + cf.getName() + "\t"
								+ doneSignal.getCount());
						new Thread(new SinglePulSearcher(cf, doneSignal))
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
			System.out.println("ERROR: " + e.getMessage());
		}
	}
}
