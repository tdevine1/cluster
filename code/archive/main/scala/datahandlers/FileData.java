package datahandlers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import util.ArrayUtils;
import util.Constants;
import util.DoubleArrayComparator;

/**
 * @author zennisarix
 */
/**
 * @author Tom Devine
 *
 */
public class FileData
{
	private final double[]		dmRange;
	/**
	 * 
	 */
	private double[][]			maxSNRs;
	/**
	 * 
	 */
	private final String		name;
	/**
	 * 
	 */
	private final double[][]	pulseCount;
	/**
	 * 
	 */
	private final double[]		pulseCountRange;
	/**
	 * 
	 */
	private int					pulseIndex;
	/**
	 * 
	 */
	private final File			pulseOut;
	/**
	 * 
	 */
	private int					snrIndex;
	/**
	 * 
	 */
	private final File			snrMaxOut;
	/**
	 * 
	 */
	private final File			snrsOut;
	/**
	 * 
	 */
	private final double[]		snrRange;
	/**
	 * The two dimensional storage array containing all SNR values recorded in
	 * the profile. Rather than being represented in an x-array and y-array like
	 * <code>pulseCount</code> and <code>SNRsMax</code>, this array consists of
	 * a variably-sized array element for each DM containing first the DM
	 * itself, followed by the recorded SNR values for that DM.
	 */
	private final double[][]	snrs;
	/**
	 * 
	 */
	private final File			timeOut;
	/**
	 * 
	 */
	private final double[]		timeRange;

	/**
	 * 
	 */
	private final double[][]	times;

	/**
	 * @param path
	 * @param maxDMs
	 * @throws Exception
	 */
	public FileData(File path, int maxDMs, boolean makeDats) throws Exception
	{
		if (Constants.SURVEY.equals("palfa"))
			name = path.getName().substring(0,
					path.getName().lastIndexOf("_singlepulse") - 6);
		else
			name = path.getName().substring(0, path.getName().lastIndexOf("."));
		pulseCount = new double[2][maxDMs];
		times = new double[maxDMs][];
		snrs = new double[maxDMs][];
		dmRange = new double[2];
		snrRange = new double[2];
		pulseCountRange = new double[2];
		timeRange = new double[2];
		dmRange[0] = snrRange[0] = pulseCountRange[0] = timeRange[0] = Double.MAX_VALUE;
		dmRange[1] = snrRange[1] = pulseCountRange[1] = timeRange[1] = Double.MIN_VALUE;
		pulseIndex = snrIndex = 0;
		if (makeDats)
		{
			pulseOut = new File(Constants.DAT_PATH + name + File.separator
					+ "pulseCounts.dat");
			snrsOut = new File(
					Constants.DAT_PATH + name + File.separator + "sigmas.dat");
			snrMaxOut = new File(Constants.DAT_PATH + name + File.separator
					+ "maxSigmas.dat");
			timeOut = new File(
					Constants.DAT_PATH + name + File.separator + "times.dat");
		} else
			pulseOut = snrsOut = snrMaxOut = timeOut = null;
		System.out.println("Processing " + name);
		BufferedReader inBuffer = new BufferedReader(new FileReader(path));
		// eat header
		inBuffer.readLine();
		// get first data line
		String lines = "\n" + inBuffer.readLine() + "\n";

		double lastDM = 0;
		if (lines.contains(","))
			lastDM = Double.parseDouble(lines.substring(0, lines.indexOf(",")));
		else
			lastDM = Double.parseDouble(lines.substring(0, 7).trim());
		pulseCount[0][pulseIndex] = lastDM;
		testRange(dmRange, lastDM, 0);
		while (inBuffer.ready())
		{
			String line = inBuffer.readLine();
			double nextDM = 0;
			if (line.trim().equals(""))
				break;
			else
			{
				if (line.contains(","))
					nextDM = Double
							.parseDouble(line.substring(0, line.indexOf(",")));
				else
					nextDM = Double.parseDouble(line.substring(0, 7).trim());
				if (nextDM == lastDM)
					lines += line + "\n";
				else
				{
					processDMChunk(lastDM, lines.split("\n"));
					testRange(pulseCountRange, pulseCount[1][pulseIndex++], 2);
					snrIndex++;
					lastDM = nextDM;
					pulseCount[0][pulseIndex] = lastDM;
					lines = "\n" + line + "\n";
				}
			}
		}
		processDMChunk(lastDM, lines.split("\n"));
		inBuffer.close();
		pulseCount[0] = Arrays.copyOf(pulseCount[0], pulseIndex + 1);
		pulseCount[1] = Arrays.copyOf(pulseCount[1], pulseIndex + 1);
		sort();
		computeMaxSNRS();
		if (makeDats) write();
	}

	public FileData(String name, double[] dmRange, double[] pulseCountRange,
			double[][] pulseCounts, double[] snrRange, double[][] snrs,
			double[][] maxSNRs, double[] timeRange, double[][] times)
	{
		this.name = name;
		this.dmRange = dmRange;
		this.pulseCountRange = pulseCountRange;
		this.pulseCount = pulseCounts;
		this.snrRange = snrRange;
		this.snrs = snrs;
		this.maxSNRs = maxSNRs;
		this.timeRange = timeRange;
		this.times = times;
		pulseOut = snrsOut = snrMaxOut = timeOut = null;
	}

	/**
	 * @param newFile
	 */
	public void addFile(File newFile)
	{
		try
		{
			String filename = newFile.getName();
			if (!filename.contains(".singlepulse")) return;
			double dm = Double.parseDouble(filename.substring(
					filename.indexOf("DM") + 2, filename.lastIndexOf(".")));
			BufferedReader inBuffer = new BufferedReader(
					new FileReader(newFile));
			pulseCount[0][pulseIndex] = dm;
			testRange(dmRange, dm, 0);
			if (!inBuffer.ready())
			{
				snrs[snrIndex] = new double[2];
				snrs[snrIndex][0] = dm;
				snrs[snrIndex][1] = 0;
				times[snrIndex] = new double[2];
				times[snrIndex][0] = dm;
				times[snrIndex][1] = 0;
				pulseCount[1][pulseIndex] = 0;
			} else
			{
				String lines = "";
				while (inBuffer.ready())
					lines += inBuffer.readLine() + "\n";
				processDMChunk(dm, lines.split("\n"));
			}
			inBuffer.close();
			testRange(pulseCountRange, pulseCount[1][pulseIndex++], 2);
			snrIndex++;
		} catch (Exception e)
		{
			System.out.println("ERROR ADDING " + newFile.getPath());
			e.printStackTrace();
		}
	}

	private void computeMaxSNRS()
	{
		maxSNRs = new double[2][snrs.length];
		maxSNRs[0] = Arrays.copyOf(pulseCount[0], pulseCount[0].length);
		for (int i = 0; i < snrs.length; i++)
		{
			if (snrs[i] != null && snrs[i].length > 1)
				maxSNRs[1][i] = ArrayUtils.maxValue(snrs[i], 1);
			else
				maxSNRs[1][i] = 0.0;
		}
	}

	/**
	 * @return
	 */
	public double[][] getMaxSNRs()
	{
		return maxSNRs;
	}

	/**
	 * @return
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @return
	 */
	public double[][] getPulseCount()
	{
		return pulseCount;
	}

	/**
	 * @return
	 */
	public double[] getPulseCountRange()
	{
		return pulseCountRange;
	}

	/**
	 * @return
	 */
	public int getSize()
	{
		return pulseCount[0].length;
	}

	/**
	 * @return
	 */
	public double[] getSNRRange()
	{
		return snrRange;
	}

	/**
	 * @return
	 */
	public double[][] getSNRs()
	{
		return snrs;
	}

	/**
	 * @return
	 */
	public double[][] getTimes()
	{
		return times;
	}

	public double[] getTimeRange()
	{
		return timeRange;
	}

	private void processDMChunk(double dm, String[] lineAry)
	{
		snrs[snrIndex] = new double[lineAry.length];
		snrs[snrIndex][0] = dm;
		times[snrIndex] = new double[lineAry.length];
		times[snrIndex][0] = dm;
		for (int i = 1; i < lineAry.length; i++)
		{
			String[] tok;
			if (lineAry[1].contains(","))
				tok = lineAry[i].split(",");
			else
				tok = lineAry[i].split("\\s+");
			if (tok.length == 5 || tok.length == 6)
			{
				pulseCount[1][pulseIndex]++;
				double snr, time;
				if (tok[2].trim().contains("inf"))
					snrs[snrIndex][i] = snr = Double.NaN;
				else
				{
					if (tok.length == 6)
						snrs[snrIndex][i] = snr = Double
								.parseDouble(tok[2].trim());
					else
					{
						if (tok[1].trim().contains("inf"))
							snrs[snrIndex][i] = snr = Double.NaN;
						else
							snrs[snrIndex][i] = snr = Double
									.parseDouble(tok[1].trim());
					}
					testRange(snrRange, snr, 1);
				}
				if (tok.length == 6)
					times[snrIndex][i] = time = Double
							.parseDouble(tok[3].trim());
				else
					times[snrIndex][i] = time = Double
							.parseDouble(tok[2].trim());
				testRange(timeRange, time, 3);
			}
		}
	}

	/**
	 * 
	 */
	private void sort()
	{
		testsort();
		DoubleArrayComparator dac = new DoubleArrayComparator();
		Arrays.sort(snrs, dac);
		Arrays.sort(times, dac);
	}

	/**
	 * @param range
	 * @param value
	 * @param which
	 */
	private void testRange(double[] range, double value, int which)
	{
		if (value < range[0])
			switch (which)
			{
				case 0:
					dmRange[0] = value;
					break;
				case 1:
					snrRange[0] = value;
					break;
				case 2:
					pulseCountRange[0] = value;
					break;
				case 3:
					timeRange[0] = value;
					break;
			}
		else
			if (value > range[1]) switch (which)
			{
				case 0:
					dmRange[1] = value;
					break;
				case 1:
					snrRange[1] = value;
					break;
				case 2:
					pulseCountRange[1] = value;
					break;
				case 3:
					timeRange[1] = value;
					break;
			}
	}

	/**
	 * 
	 */
	private void testsort()
	{
		boolean flag = true; // set flag to true to begin first pass

		while (flag)
		{
			flag = false; // set flag to false awaiting a possible swap
			for (int i = 0; i < pulseCount[0].length - 1; i++)
				if (pulseCount[0][i] > pulseCount[0][i + 1])
				{
					double temp = pulseCount[0][i]; // swap elements
					pulseCount[0][i] = pulseCount[0][i + 1];
					pulseCount[0][i + 1] = temp;
					temp = pulseCount[1][i]; // swap elements
					pulseCount[1][i] = pulseCount[1][i + 1];
					pulseCount[1][i + 1] = temp;
					flag = true; // shows a swap occurred
				}
		}
	}

	/**
	 * @throws IOException
	 */
	public void write() throws IOException
	{
		new File(pulseOut.getAbsolutePath().substring(0,
				pulseOut.getAbsolutePath().lastIndexOf(File.separator)))
						.mkdir();
		BufferedWriter outBuffer = new BufferedWriter(new FileWriter(pulseOut));
		for (int i = 0; i < pulseCount[0].length; i++)
			outBuffer.write(pulseCount[0][i] + "\t" + pulseCount[1][i] + "\n");
		outBuffer.close();
		new File(snrMaxOut.getAbsolutePath().substring(0,
				snrMaxOut.getAbsolutePath().lastIndexOf(File.separator)))
						.mkdir();
		outBuffer = new BufferedWriter(new FileWriter(snrMaxOut));
		for (int i = 0; i < maxSNRs[0].length; i++)
			outBuffer.write(maxSNRs[0][i] + "\t" + maxSNRs[1][i] + "\n");
		outBuffer.close();
		new File(snrsOut.getAbsolutePath().substring(0,
				snrsOut.getAbsolutePath().lastIndexOf(File.separator))).mkdir();
		outBuffer = new BufferedWriter(new FileWriter(snrsOut));
		for (double[] snr : snrs)
			if (snr != null)
			{
				for (double element : snr)
					outBuffer.write(element + "\t");
				outBuffer.write("\n");
			}
		outBuffer.close();
		new File(timeOut.getAbsolutePath().substring(0,
				timeOut.getAbsolutePath().lastIndexOf(File.separator))).mkdir();
		outBuffer = new BufferedWriter(new FileWriter(timeOut));
		for (double[] time : times)
			if (time != null)
			{
				for (double element : time)
					outBuffer.write(element + "\t");
				outBuffer.write("\n");
			}
		outBuffer.close();
		// FileUtils.deleteQuietly(new File(Constants.RAW_PATH + name));
	}
}
