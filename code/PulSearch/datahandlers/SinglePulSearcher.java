package datahandlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

import algorithms.RAPIDSinglePulse;
import filehandlers.SinglePulseDatFileReader;
import util.ArrayUtils;
import util.Constants;

/**
 * @author zennisarix
 */
public class SinglePulSearcher extends PulSearcher implements Runnable
{
	/**
	 * @param data
	 */
	private File							singlePulseFile;

	/**
	 * 
	 */
	private Vector<SinglePulseCandidate>	candidates;
	/**
	 * 
	 */
	private SinglePulseFileData				data;
	private final CountDownLatch			doneSignal;

	public SinglePulSearcher(File target, CountDownLatch doneSignal)
	{
		singlePulseFile = target;
		this.doneSignal = doneSignal;
	}

	/**
	 * @param stop
	 */
	public void addCandidate(SinglePulseCandidate current, int stop)
	{
		int start = current.getStartIndex();
		current.setData(ArrayUtils.copyWide(pulseCount, start, stop),
				ArrayUtils.copyLong(snrs, start, stop),
				ArrayUtils.copyWide(snrsMax, start, stop),
				ArrayUtils.copyLong(times, start, stop));
		candidates.add(current);
	}

	private void rankCandidates()
	{
		Collections.sort(candidates);
		int rank = candidates.size();
		Enumeration<SinglePulseCandidate> e = candidates.elements();
		while (e.hasMoreElements())
		{
			SinglePulseCandidate spc = e.nextElement();
			spc.setPulseRank(rank--);
		}
	}

	@Override
	public void run()
	{
		try
		{
			String curName = "";
			// read cluster file line by line
			BufferedReader inBuffer = new BufferedReader(
					new FileReader(singlePulseFile));
			// skip the header
			inBuffer.readLine();
			while (inBuffer.ready())
			{
				// for each line, make parameterized FileData
				try
				{
					String cluster = inBuffer.readLine();
					data = new SinglePulseDatFileReader(cluster.split(","))
							.getData();
					pulseCount = data.getPulseCount();
					snrs = data.getSNRs();
					snrsMax = data.getMaxSNRs();
					times = data.getTimes();
					candidates = new Vector<SinglePulseCandidate>();
					if (curName.isEmpty())
					{
						curName = data.getName();
						outFile = new FileWriter(new File(Constants.ARFF_PATH
								+ "signals_" + curName + ".arff"));
					}
					if (!data.getName().equals(curName))
					{
						outFile.close();
						curName = data.getName();
						outFile = new FileWriter(new File(Constants.ARFF_PATH
								+ "signals_" + curName + ".arff"));
					}
					RAPIDSinglePulse rpf = new RAPIDSinglePulse(data.getName(),
							data.getSize(), this, data.getClusterRank(),
							data.getDMSpacing());
					rpf.search(0, 0);
					rankCandidates();
					save(outFile);
				} catch (Exception e)
				{
					System.out.println("ERROR: " + e.getMessage());
				}
			}
			outFile.close();
			inBuffer.close();
			doneSignal.countDown();
		} catch (Exception e)
		{
			System.out.println("ERROR: " + e.getMessage());
			doneSignal.countDown();
		}
	}

	public void save(FileWriter outFile) throws IOException
	{
		Enumeration<SinglePulseCandidate> e = candidates.elements();
		while (e.hasMoreElements())
			outFile.write(e.nextElement().toString());
	}
}
