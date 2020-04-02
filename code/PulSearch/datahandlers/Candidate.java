package datahandlers;

import java.text.DecimalFormat;

import org.apache.commons.math3.util.FastMath;

import functions.IdealPulseFitter;
import functions.IdealPulseFunction;
import util.ArrayUtils;
import util.Constants;

/**
 * @author zennisarix
 */
public class Candidate
{
	/**
	 * The name of the pulse, which consists of the parent file name + the
	 * <code>StartDM</code>and <code>StopDM</code>in the profile.
	 */
	protected String		name;
	/**
	 * The sky survey containing this observation.
	 */
	protected String		survey;
	/**
	 * The mean Julian date of the observation.
	 */
	protected int			mjd;
	/**
	 * The beam in which the signal was detected.
	 */
	protected int			beam;
	/**
	 * The central frequency of the observation in MHz.
	 */
	protected double		centralFrequency;
	/**
	 * The number of channels.
	 */
	protected int			numChannels;
	/**
	 * The bandwidth of a single channel in KHz.
	 */
	protected double		channelBandwidth;
	/**
	 * An array containing:
	 * <ol>
	 * <li>the right ascension of the observed signal, and</li>
	 * <li>the declination of the observed signal.</li>
	 */
	protected String[]		raDec;
	/**
	 * A flag indicating whether the algorithm has found a peak in the pulse
	 * yet.
	 */
	protected boolean		peak;
	/**
	 * The two dimensional storage array containing the array of DM values in
	 * the first element and the corresponding array of pulse counts in the
	 * second element.
	 */
	protected double[][]	pulseCount;
	/**
	 * The mean number of pulses detected per DM increment in the DM range.
	 */
	protected double		pulseCountAvgIntensity;
	/**
	 * The sum of the second element in <code>pulseCount</code>. Represents the
	 * total number of pulses counted in the profile DM range.
	 */
	protected double		pulseCountIntensity;
	/**
	 * <code>pulseCountMax-pulseCountAvgIntensity</code>, which represents the
	 * height of the peak above the local average count of pulses in the
	 * profile.
	 */
	protected double		pulseCountLocalPeakHeight;
	/**
	 * The local maximum of the number of pulses stored in the second array
	 * element of <code>pulseCount</code>.
	 */
	protected double		pulseCountMax;
	/**
	 * The DM location of the maximum number of pulses in the profile.
	 */
	protected double		pulseCountPeakDM;
	/**
	 * The mean SNR value detected per DM increment in the DM range.
	 */
	protected double		snrAvgIntensity;
	/**
	 * The sum of all SNRs recorded in the DM range of the profile.
	 */
	protected double		snrIntensity;
	/**
	 * <code>SNRMaxHeight-SNRAvgIntensity</code>, which represents the height of
	 * the SNR peak above the local SNR average in the profile.
	 */
	protected double		snrLocalPeakHeight;
	/**
	 * The Chi-Square score of the profile stored in the second array element of
	 * <code>SNRsMax</code> when compared to the ideal distribution described in
	 * {@link util.IdealPulseFunction}.
	 */
	protected double		snrMaxFitChiSquare;
	/**
	 * The local maximum of the SNRs stored in the second array element of
	 * <code>SNRsMax</code>.
	 */
	protected double		snrMax;
	/**
	 * The DM location of the maximum SNR value in the profile.
	 */
	protected double		snrPeakDM;
	/**
	 * The two dimensional storage array containing the array of DM values in
	 * the first element and the corresponding array of maximum SNRs for each DM
	 * in the second element.
	 */
	protected double[][]	snrsMax;
	/**
	 * The first DM in the profile range.
	 */
	protected double		startDM;
	/**
	 * The index in {@link DPGPulSearcher} <code>.SNRsMax</code>which marks the
	 * beginning point of the subset of values contained in <code>SNRsMax</code>
	 * .
	 */
	protected int			startIndex;
	/**
	 * 
	 */
	protected double		stopDM;
	/**
	 * <code>stopDM-startDM</code>, or the width in DM of the profile.
	 */
	protected double		width;
	/**
	 * The theoretical full width of the profile in ms at half of the maximum
	 * peak value obtained by fitting the the SNRMax observations to the
	 * {@link IdealPulseFunction} using the {@link IdealPulseFitter}.
	 */
	protected double		fittedWidth;
	/**
	 * The theoretical maximum flux value obtained by fitting the the SNRMax
	 * observations to the {@link IdealPulseFunction} using the
	 * {@link IdealPulseFitter}.
	 */
	protected double		fittedPeakFlux;
	/**
	 * The ratio of the SNR of the first point in the pulse to the maximum SNR.
	 */
	protected double		snrRatio;
	/**
	 * The arrival time of the first recorded signal.
	 */
	protected double		startTime;
	/**
	 * The arrival time of the last recorded signal.
	 */
	protected double		stopTime;

	/**
	 * @param name
	 */
	public Candidate(String name)
	{
		this.name = name;
		initialize();
	}

	private void initialize()
	{
		String[] nameAry = name.split("_");
		mjd = Integer.parseInt(nameAry[1]);
		int stopIndex = (nameAry[2].indexOf("+") > 0) ? nameAry[2].indexOf("+")
				: nameAry[2].indexOf("-");
		raDec = new String[] { nameAry[2].substring(0, stopIndex),
				nameAry[2].substring(stopIndex) };
		beam = Integer.parseInt(nameAry[3]);
		if (Constants.SURVEY.equals("gbt350"))
		{
			centralFrequency = Constants.NU_GBT;
			channelBandwidth = Constants.BANDWIDTH_GBT;
			numChannels = Constants.NUM_CHANNELS_GBT;
		} else
			if (Constants.SURVEY.equals("aodrift")
					|| Constants.SURVEY.equals("palfa"))
			{
				centralFrequency = Constants.NU_AO;
				channelBandwidth = Constants.BANDWIDTH_AO;
				numChannels = Constants.NUM_CHANNELS_AO;
			}
		peak = false;
		startIndex = -1;
		startTime = -1;
		stopTime = -1;
		pulseCountAvgIntensity = -1;
		pulseCountLocalPeakHeight = -1;
		pulseCountMax = -1;
		pulseCountIntensity = -1;
		snrAvgIntensity = -1;
		snrLocalPeakHeight = -1;
		snrMax = -1;
		snrIntensity = -1;
		width = -1;
		fittedWidth = -1;
		fittedPeakFlux = -1;
		snrMaxFitChiSquare = -1;
		snrRatio = -1;
		pulseCount = null;
		snrsMax = null;
	}

	/**
	 * @param name
	 * @param start
	 */
	public Candidate(String name, int start)
	{
		this.name = name;
		initialize();
		startIndex = start;
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
	public int getStartIndex()
	{
		return startIndex;
	}

	/**
	 * Describes the status of this candidate by returning an integer values.
	 * The returned values are:
	 * <ul>
	 * <li>1 - indicates the candidate exists and has a peak</li>
	 * <li>0 - indicates the candidate exists but does not have a peak</li>
	 * <li>-1 - indicates that the candidate does not yet exist</li>
	 * </ul>
	 * 
	 * @return the integer representation of the status
	 */
	public int getStatus()
	{
		if (peak) return 1;
		if (startIndex > 0) return 0;
		return -1;
	}

	/**
	 * @return
	 */
	public double getWidth()
	{
		return width;
	}

	protected String printNaN(double possibleNaN)
	{
		if (("" + possibleNaN).contains("NaN"))
			return "" + possibleNaN;
		else
			return new DecimalFormat("#.##").format(possibleNaN);
	}

	/**
	 * @param pulseCount
	 * @SNR SNRs
	 * @param SNRsMax
	 */
	@SuppressWarnings("unused")
	public void setData(double[][] pulseCount, double[][] snrs,
			double[][] snrsMax, double[][] times)
	{
		try
		{
			startDM = pulseCount[0][0];
			stopDM = pulseCount[0][pulseCount[0].length - 1];
			startTime = ArrayUtils.minValue(times);
			stopTime = ArrayUtils.maxValue(times);
			width = stopDM - startDM;
			name += "_DMs" + startDM + "-" + stopDM;
			this.pulseCount = pulseCount;
			this.snrsMax = snrsMax;
			pulseCountMax = ArrayUtils.maxValue(pulseCount[1], 0);
			pulseCountPeakDM = pulseCount[0][ArrayUtils.maxIndex(pulseCount[1],
					pulseCountMax)];
			pulseCountIntensity = ArrayUtils.sum(pulseCount[1]);
			pulseCountAvgIntensity = ArrayUtils.average(pulseCount[1]);
			pulseCountLocalPeakHeight = pulseCountMax - pulseCountAvgIntensity;
			snrMax = ArrayUtils.maxValue(snrsMax[1], 0);
			snrPeakDM = snrsMax[0][ArrayUtils.maxIndex(snrsMax[1], snrMax)];
			snrIntensity = ArrayUtils.sum(snrs);
			snrAvgIntensity = ArrayUtils.average(snrs);
			snrLocalPeakHeight = snrMax - snrAvgIntensity;
			snrRatio = snrsMax[0][0] / snrMax;
			IdealPulseFitter fitter;
			if (Constants.SURVEY == "gbt350drift")
				fitter = new IdealPulseFitter(new double[] { Constants.NU_GBT,
						Constants.BANDWIDTH_GBT, Constants.NUM_CHANNELS_GBT,
						1.0, 1.0, snrPeakDM });
			else
				if (Constants.SURVEY == "aodrift"
						|| Constants.SURVEY == "palfa")
					fitter = new IdealPulseFitter(new double[] {
							Constants.NU_AO, Constants.BANDWIDTH_AO,
							Constants.NUM_CHANNELS_AO, 1.0, 1.0, snrPeakDM });
				else
					throw (new Exception("Survey not set properly."));
			double[] fitted = fitter
					.fit(fitter.getObservations(snrsMax[0], snrsMax[1]));
			fittedPeakFlux = fitted[0] + 5.0;
			fittedWidth = FastMath.abs(fitted[1]);
			if (fittedPeakFlux >= 6.0 && fittedWidth <= 100)
				snrMaxFitChiSquare = fitter.chiSquare(fitted);
			else
				throw new Exception("Unacceptable fit values");
		} catch (Exception e)
		{
			fittedPeakFlux = Double.NaN;
			fittedWidth = Double.NaN;
			snrMaxFitChiSquare = Double.NaN;
		}
	}

	/**
	 * @param peak
	 */
	public void setPeak(boolean peak)
	{
		this.peak = peak;
	}

	/**
	 * @param startIndex
	 */
	public void setStartIndex(int startIndex)
	{
		this.startIndex = startIndex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		DecimalFormat df = new DecimalFormat("#0.00");
		String output = "";
		try
		{
			output += survey + "," + mjd + "," + raDec[0] + "," + raDec[1] + ","
					+ centralFrequency + "," + channelBandwidth + ","
					+ numChannels + "," + df.format(startDM) + ","
					+ df.format(stopDM) + "," + df.format(pulseCountPeakDM)
					+ "," + pulseCountMax + ","
					+ df.format(pulseCountLocalPeakHeight) + ","
					+ pulseCountIntensity + ","
					+ df.format(pulseCountAvgIntensity) + ","
					+ df.format(snrPeakDM) + "," + df.format(snrMax) + ","
					+ df.format(snrLocalPeakHeight) + ","
					+ df.format(snrIntensity) + "," + df.format(snrAvgIntensity)
					+ "," + df.format(snrRatio) + "," + printNaN(fittedWidth)
					+ "," + printNaN(fittedPeakFlux) + ","
					+ printNaN(snrMaxFitChiSquare) + "\n";
		} catch (Exception e)
		{
			System.out.println(
					"ERROR Writing " + survey + "_" + raDec[0] + raDec[1]);
			e.printStackTrace();
		}
		return output;
	}
}
