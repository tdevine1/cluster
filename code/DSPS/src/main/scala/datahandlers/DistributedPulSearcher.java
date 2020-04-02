package datahandlers;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import algorithms.DRAPID;
import util.ArrayUtils;

/**
 * @author zennisarix
 */
public class DistributedPulSearcher implements Runnable
{
	private String							cluster;
	private String[]						csvData;
	private Vector<SinglePulseCandidate>	candidates;
	private DistributedFileData				data;
	protected double[][]					pulseCount;
	protected double[][]					snrs;
	protected double[][]					snrsMax;
	protected double[][]					times;

	public DistributedPulSearcher(String cluster, String[] data)
	{
		candidates = new Vector<SinglePulseCandidate>();
		this.cluster = cluster;
		this.csvData = data;
	}

	public void addCandidate(SinglePulseCandidate current, int stop)
	{
		int start = current.getStartIndex();
		current.setData(ArrayUtils.copyWide(pulseCount, start, stop),
				ArrayUtils.copyLong(snrs, start, stop),
				ArrayUtils.copyWide(snrsMax, start, stop),
				ArrayUtils.copyLong(times, start, stop));
		candidates.add(current);
	}

	public String[] getCandidates()
	{
		if (candidates != null && !candidates.isEmpty())
		{
			int count = 0;
			String[] candArray = new String[candidates.size()];
			Enumeration<SinglePulseCandidate> e = candidates.elements();
			while (e.hasMoreElements())
				candArray[count++] = e.nextElement().toString();
			return candArray;
		} else
			return new String[] { "" };
	}

	private void rankCandidates()
	{
		if (candidates != null && !candidates.isEmpty())
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
	}

	@Override
	public void run()
	{
		try
		{
			data = new DistributedFileData(cluster, csvData);
			pulseCount = data.getPulseCount();
			snrs = data.getSNRs();
			snrsMax = data.getMaxSNRs();
			times = data.getTimes();
			candidates = new Vector<SinglePulseCandidate>();
			DRAPID rpf = new DRAPID(data.getName(),
					data.getSize(), this, data.getClusterRank(),
					data.getDMSpacing());
			rpf.search(0, 0);
			rankCandidates();
		} catch (Exception e)
		{
			System.err.println("ERROR: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public double[][] getSNRs(int start, int next)
	{
		return ArrayUtils.copyWide(snrsMax, start, next);
	}

	public void print()
	{
		if (candidates != null && !candidates.isEmpty())
		{
			System.out.println("[PROCESSING]\t" + cluster + ": Printing candidates...");
			Enumeration<SinglePulseCandidate> e = candidates.elements();
			while (e.hasMoreElements())
				System.out.println(e.nextElement().toString());
		}
	}
}
