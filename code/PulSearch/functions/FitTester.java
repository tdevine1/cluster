package functions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import util.Constants;

public class FitTester
{
	private static final double	PEAK_DM			= 67.35;
	private static final String	TEST_FILENAME	= "/AstroData/test/1721-0240.txt";

	/**
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args)
	{
		try
		{
			int obsCount = 0;
			System.out.print("Computing length from file...");
			BufferedReader inBuffer = new BufferedReader(
					new FileReader(new File(TEST_FILENAME)));
			while (inBuffer.ready())
			{
				inBuffer.readLine();
				obsCount++;
			}
			inBuffer.close();
			System.out.println("...COMPLETE");
			System.out.print("Reading data from file...");
			inBuffer = new BufferedReader(
					new FileReader(new File(TEST_FILENAME)));
			double[] obsDM = new double[obsCount];
			double[] obsFlux = new double[obsCount];
			obsCount = 0;
			while (inBuffer.ready())
			{
				String[] line = inBuffer.readLine().split("\t");
				obsDM[obsCount] = Double.parseDouble(line[0]);
				obsFlux[obsCount++] = Double.parseDouble(line[1]);
			}
			System.out.println("...COMPLETE");
			inBuffer.close();
			long startTime = System.currentTimeMillis();
			IdealPulseFitter fitter;
			if (Constants.SURVEY == "gbt350drift")
				fitter = new IdealPulseFitter(new double[] { Constants.NU_GBT,
						Constants.BANDWIDTH_GBT, Constants.NUM_CHANNELS_GBT,
						1.0, 1.0, PEAK_DM });
			else
				if (Constants.SURVEY == "aodrift")
					fitter = new IdealPulseFitter(new double[] {
							Constants.NU_AO, Constants.BANDWIDTH_AO,
							Constants.NUM_CHANNELS_AO, 1.0, 1.0, PEAK_DM });
				else
					throw (new Exception("Survey not set properly."));
			System.out.print("Fitting data...");
			double[] fitted = fitter
					.fit(fitter.getObservations(obsDM, obsFlux));
			System.out.println("Time: "
					+ (0.001 * (System.currentTimeMillis() - startTime))
					+ " seconds\ns = " + fitted[0] + "\nw = " + fitted[1]);
			System.out.println("Chi-Square: " + fitter.chiSquare(fitted));
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
