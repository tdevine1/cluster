package aggregators;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import executable.MainBatchUnzipper;
import util.CSVStringComparator;
import util.Constants;

public class SinglePulseAggregator implements Runnable
{
	private String					dirName;
	private String					mjd;
	private final File				dir;
	private List<String>			data;
	private final CountDownLatch	doneSignal;

	public SinglePulseAggregator(File dirPath) throws IOException
	{
		dirName = dirPath.getName();
		dir = dirPath;
		mjd = dir.toString().substring(
				dir.toString().lastIndexOf(File.separator) - 4,
				dir.toString().lastIndexOf(File.separator));
		data = new Vector<String>();
		doneSignal = null;
	}

	public SinglePulseAggregator(File dir, CountDownLatch doneSignal)
	{
		dirName = dir.getName();
		this.dir = dir;
		mjd = dir.toString().substring(
				dir.toString().lastIndexOf(File.separator) - 4,
				dir.toString().lastIndexOf(File.separator));
		data = new Vector<String>();
		this.doneSignal = doneSignal;
	}

	private void read(File file) throws Exception
	{
		String filename = file.getName();
		if (filename.contains("DM") && filename.contains(".singlepulse"))
		{
			BufferedReader inBuffer = new BufferedReader(new FileReader(file));
			String dm = filename.substring(filename.indexOf("DM") + 2,
					filename.indexOf("single") - 1);
			inBuffer.readLine();
			if (!inBuffer.ready()) data.add(dm + ",0.00,0.000000,0,0");
			while (inBuffer.ready())
			{
				String csv = "";
				String[] line = inBuffer.readLine().split("\\s+");
				if (line.length < 5 || line.length > 6)
					data.add(dm + ",0.00,0.000000,0,0");
				else
				{
					int index = (line.length == 6) ? 1 : 0;
					while (index < line.length - 1)
					{
						try
						{
							Double.parseDouble(line[index]);
							csv += line[index++] + ",";
						} catch (NumberFormatException e)
						{
							csv += "NaN,";
						}
					}
					try
					{
						Double.parseDouble(line[line.length - 1]);
						data.add(csv + line[line.length - 1]);
					} catch (NumberFormatException e)
					{
						csv += "NaN";
					}
				}
			}
			inBuffer.close();
		}
	}

	@Override
	public void run()
	{
		try
		{
			Iterator<Path> walker = Files.newDirectoryStream(dir.toPath())
					.iterator();
			File firstFile = walker.next().toFile();
			dirName = firstFile.getName();
			read(firstFile);
			while (walker.hasNext())
				read(walker.next().toFile());
		} catch (Exception e)
		{
			System.out.println("\n\nERROR: " + e.getMessage()
					+ "\nWHILE READING " + dirName);
			e.printStackTrace();
		}
		Collections.sort(data, new CSVStringComparator());
		try
		{
			write();
		} catch (Exception e)
		{
			System.out.println("\n\nERROR: " + e.getMessage()
					+ "\nWHILE WRITING " + dirName);
			e.printStackTrace();
		}
		if (doneSignal != null) doneSignal.countDown();
	}

	private void write() throws Exception
	{
		File out = null;
		if (Constants.SURVEY.equals("palfa"))
		{
			File dir = new File(
					Constants.CSV_PATH + MainBatchUnzipper.getMJD(dirName));
			if (!dir.exists()) dir.mkdirs();
			out = new File(Constants.CSV_PATH
					+ MainBatchUnzipper.getMJD(dirName) + File.separator
					+ dirName.substring(0, dirName.lastIndexOf("_") - 6)
					+ "_singlepulse.csv");
		} else
			if (Constants.SURVEY == "aodrift")
			{
				File dir = new File(Constants.CSV_PATH + mjd);
				if (!dir.exists()) dir.mkdirs();
				System.out.println("Writing " + Constants.CSV_PATH + mjd
						+ dirName.substring(0, dirName.indexOf("_"))
						+ "_singlepulse.csv");
				out = new File(Constants.CSV_PATH + mjd + File.separator
						+ dirName.substring(0, dirName.indexOf("_"))
						+ "_singlepulse.csv");
			}
		BufferedWriter outBuffer = new BufferedWriter(new FileWriter(out));
		outBuffer.write(Constants.CSV_HEADER);
		Iterator<String> e = data.iterator();
		e.next();
		while (e.hasNext())
			outBuffer.write(e.next() + "\n");
		outBuffer.close();
	}
}
