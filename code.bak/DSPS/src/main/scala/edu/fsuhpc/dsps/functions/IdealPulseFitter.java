package edu.fsuhpc.dsps.functions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.fitting.AbstractCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoint;
import org.apache.commons.math3.fitting.leastsquares.GaussNewtonOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.linear.DiagonalMatrix;

/**
 * @author zennisarix
 */
public class IdealPulseFitter extends AbstractCurveFitter
{
	/**
	 * 
	 */
	private final double				deltaFreq;
	/**
	 * 
	 */
	private final double				freqCube;
	/**
	 * 
	 */
	private final double[]				estimates;
	/**
	 * 
	 */
	private double						peakDM;
	/**
	 * 
	 */
	public List<WeightedObservedPoint>	observations;

	/**
	 * Creates a new instance of {@link IdealPulseFitter} to fit observed data
	 * to Equation 12 from
	 * http://iopscience.iop.org/0004-637X/596/2/1142/pdf/58116.web.pdf.
	 * 
	 * @param arg
	 *            Contains an array of the following arguments:
	 *            <ol>
	 *            <li>central frequency in MHz
	 *            <li>channel bandwidth in KHz
	 *            <li>number of channels
	 *            <li>a 2 element array with the initial estimate for the
	 *            peakFlux [0] and width [1]
	 *            <li>the DM of the maximum flux seen in the observation
	 *            </ol>
	 */
	public IdealPulseFitter(double[] arg)
	{
		double freqGHz = arg[0] * 0.001;
		freqCube = freqGHz * freqGHz * freqGHz;
		deltaFreq = arg[1] * arg[2] * 0.001;
		estimates = new double[] { arg[3], arg[4] };
		peakDM = arg[5];
		observations = new ArrayList<WeightedObservedPoint>();
	}

	public double chiSquare(double[] fittedParameters)
	{
		double[] deltaDMs = new double[observations.size()];
		double[] observed = new double[observations.size()];
		int count = 0;
		for (WeightedObservedPoint obs : observations)
		{
			deltaDMs[count] = obs.getX();
			observed[count++] = obs.getY();
		}
		double[] expected = new double[observations.size()];
		IdealPulseFunction fun = new IdealPulseFunction(new double[] {
				freqCube, deltaFreq });
		for (int i = 0; i < expected.length; i++)
			expected[i] = fun.value(deltaDMs[i], fittedParameters);
		double value = 0.0;
		for (int i = 0; i < observed.length; i++)
			value += ((observed[i] - expected[i]) * (observed[i] - expected[i]))
					/ expected[i];
		return value;
	}

	public List<WeightedObservedPoint> getObservations(double[] obsDM,
			double[] obsFlux)
	{
		observations = new ArrayList<WeightedObservedPoint>();
		for (int i = 0; i < obsDM.length; i++)
		{
			double sigma = obsFlux[i] - 5.0;
			if (sigma <= 0.0) sigma = 0.001;
			observations.add(new WeightedObservedPoint(1.0, obsDM[i] - peakDM,
					sigma));
		}
		return observations;
	}

	@Override
	protected LeastSquaresOptimizer getOptimizer()
	{
		return new GaussNewtonOptimizer();
	}

	@Override
	protected LeastSquaresProblem getProblem(
			Collection<WeightedObservedPoint> observations)
	{
		final int length = observations.size();
		final double[] target = new double[length];
		final double[] weights = new double[length];

		int i = 0;
		for (WeightedObservedPoint obs : observations)
		{
			target[i] = obs.getY();
			weights[i++] = obs.getWeight();
		}

		final AbstractCurveFitter.TheoreticalValuesFunction model = new AbstractCurveFitter.TheoreticalValuesFunction(
				new IdealPulseFunction(new double[] { freqCube, deltaFreq }),
				observations);
		// Return a new least squares problem set up to fit curve
		// to the observed points.
		return new LeastSquaresBuilder()
				.checker(
						new IdealPulseConvergenceChecker<LeastSquaresProblem.Evaluation>())
				.maxEvaluations(Integer.MAX_VALUE)
				.maxIterations(10000)
				.start(estimates)
				.target(target)
				.weight(new DiagonalMatrix(weights))
				.model(model.getModelFunction(),
						model.getModelFunctionJacobian()).build();
	}
}
