package functions;

import org.apache.commons.math3.analysis.ParametricUnivariateFunction;
import org.apache.commons.math3.special.Erf;
import org.apache.commons.math3.util.FastMath;

import util.Constants;

public class IdealPulseFunction implements ParametricUnivariateFunction
{
	/**
	 * 
	 */
	private final double	CONSTANT;
	/**
	 * 
	 */
	private final double	PI_FACTOR;
	/**
	 * 
	 */
	private final double	deltaFreq;
	/**
	 * 
	 */
	private final double	freqCube;

	/**
	 * Creates a new instance of {@link IdealPulseFunction} based on Equation 12
	 * from http://iopscience.iop.org/0004-637X/596/2/1142/pdf/58116.web.pdf.
	 * 
	 * @param arg
	 *            Contains an array of the following arguments:
	 *            <ol>
	 *            <li>the cube of the central frequency in MHz
	 *            <li>the product of the bandwidth and number of channels
	 *            </ol>
	 */
	public IdealPulseFunction(double[] arg)
	{
		PI_FACTOR = Constants.PI_FACTOR;
		CONSTANT = Constants.CONSTANT;
		freqCube = arg[0];
		deltaFreq = arg[1];
	}

	/**
	 * @param arg0
	 *            The deltaDM value for a specific point.
	 * @param arg1
	 *            Contains an array of the following arguments:
	 *            <ol>
	 *            <li>the maximum flux,
	 *            <li>full width at half of the maximum flux in ms of the peak.
	 *            </ol>
	 * @return The 2 dimensional gradient array with values for:
	 *         <ol>
	 *         <li>the partial derivative with respect to maximum flux,
	 *         <li>the partial derivative with respect to width.
	 *         </ol>
	 */
	@Override
	public double[] gradient(double arg0, double... arg1)
	{
		double[] grad = new double[2];
		double deltaDM = arg0;
		if (deltaDM == 0) deltaDM = 0.001;
		double peakFlux = arg1[0];
		double width = arg1[1];
		double z = zeta(deltaDM, width);
		grad[0] = PI_FACTOR * (1 / z) * Erf.erf(z);
		grad[1] = peakFlux
				* (((PI_FACTOR * Erf.erf(z)) / (CONSTANT * deltaDM * deltaFreq)) - ((FastMath
						.exp(-(FastMath.pow(CONSTANT, 2)
								* FastMath.pow(deltaDM, 2) * FastMath.pow(
								deltaFreq, 2))
								/ (FastMath.pow(width, 2) * FastMath.pow(
										freqCube, 2)))) / width));
		return grad;
	}

	/**
	 * @param arg0
	 *            the deltaDM value for a specific point
	 * @param arg1
	 *            Contains an array of the following arguments:
	 *            <ol>
	 *            <li>the maximum flux
	 *            <li>full width at half of the maximum flux in ms of the peak
	 *            </ol>
	 * @return peakFlux * (PI_FACTOR * (1 / z) * Erf.erf(z))
	 */
	@Override
	public double value(double arg0, double... arg1)
	{
		double deltaDM = arg0;
		if (deltaDM == 0) deltaDM = 0.001;
		double peakFlux = arg1[0];
		double width = arg1[1];
		double z = zeta(deltaDM, width);
		return peakFlux * (PI_FACTOR * (1 / z) * Erf.erf(z));
	}

	/**
	 * @param deltaDM
	 *            the offset in DM of a point from the central peak DM
	 * @param width
	 *            the full width in ms of the profile at half of the maximum
	 *            flux
	 * @return 0.00691 * deltaDM * (deltaFreq / (width * freqCube))
	 */
	private double zeta(double deltaDM, double width)
	{
		return CONSTANT * deltaDM * (deltaFreq / (width * freqCube));
	}
}
