package datahandlers;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import algorithms.DistributedRAPID;
import util.ArrayUtils;

/**
 * @author zennisarix
 */
public class DistributedPulSearcher extends PulSearcher
		implements Runnable
{
	private String							cluster;
	private String[]						csvData;
	private Vector<SinglePulseCandidate>	candidates;
	private DistributedFileData				data;

	public DistributedPulSearcher(String cluster, String[] data)
	{
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
		return (String[]) candidates.toArray();
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
			String[] clusterAry = cluster.split(",");
			data = new DistributedFileData(csvData, csvData.length,
					Integer.parseInt(clusterAry[4]),
					Double.parseDouble(clusterAry[7]));
			pulseCount = data.getPulseCount();
			snrs = data.getSNRs();
			snrsMax = data.getMaxSNRs();
			times = data.getTimes();
			candidates = new Vector<SinglePulseCandidate>();
			DistributedRAPID rpf = new DistributedRAPID(data.getName(),
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
}
