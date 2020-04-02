package datahandlers;

import java.io.File;
import java.io.FileWriter;
import java.util.Vector;

import filehandlers.DatFileReader;
import util.ArrayUtils;
import util.Constants;

/**
 * @author zennisarix
 */
public class DPGPulSearcher extends PulSearcher implements Runnable
{

	/**
	 * @param target
	 */
	public DPGPulSearcher(File target) throws Exception
	{
		if (target.isDirectory())
			data = new DatFileReader(target).getData();
		else
			data = new FileData(target, 20000, true);
		pulseCount = data.getPulseCount();
		snrs = data.getSNRs();
		snrsMax = data.getMaxSNRs();
		candidates = new Vector<Candidate>();
		outFile = new FileWriter(new File(
				Constants.ARFF_PATH + "signals_ " + data.getName() + ".arff"));
	}

	/**
	 * @param data
	 */
	public DPGPulSearcher(FileData data) throws Exception
	{
		this.data = data;
		pulseCount = data.getPulseCount();
		snrs = data.getSNRs();
		snrsMax = data.getMaxSNRs();
		candidates = new Vector<Candidate>();
		outFile = new FileWriter(new File(
				Constants.ARFF_PATH + "signals_ " + data.getName() + ".arff"));
	}

	/**
	 * @param stop
	 */
	public void addCandidate(Candidate current, int stop)
	{
		int start = current.getStartIndex();
		current.setData(ArrayUtils.copyWide(pulseCount, start, stop),
				ArrayUtils.copyLong(snrs, start, stop),
				ArrayUtils.copyWide(snrsMax, start, stop),
				ArrayUtils.copyLong(snrs, start, stop));
		candidates.add(current);
	}

	@Override
	public void run()
	{
		try
		{
			// RAPID rpf = new RAPID(data.getName(), data.getSize(), this);
			// rpf.search(0, 0);
			// save();
		} catch (Exception e)
		{
			System.out.println("ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
