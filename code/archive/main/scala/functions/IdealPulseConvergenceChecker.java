package functions;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem.Evaluation;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.util.FastMath;

public class IdealPulseConvergenceChecker<T> implements
		ConvergenceChecker<LeastSquaresProblem.Evaluation>
{
	@Override
	public boolean converged(int iteration, Evaluation prev, Evaluation cur)
	{
		if (FastMath.abs(prev.getRMS() - cur.getRMS()) < 0.0001)
		{
			return true;
		} else
		{
			return false;
		}
	}
}
