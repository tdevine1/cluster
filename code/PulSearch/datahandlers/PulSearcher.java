package datahandlers;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import util.ArrayUtils;

public class PulSearcher
{
	/**
	 * 
	 */
	protected Vector<Candidate>	candidates;
	/**
	 * 
	 */
	protected FileData			data;
	/**
	 * 
	 */
	protected double[][]		pulseCount;
	/**
	 * 
	 */
	protected double[][]		snrs;
	/**
	 * 
	 */
	protected double[][]		snrsMax;
	/**
	 * 
	 */
	protected double[][]		times;
	/**
	 * 
	 */
	protected FileWriter		outFile;

	public double[][] getSNRs(int start, int next)
	{
		return ArrayUtils.copyWide(snrsMax, start, next);
	}

	public void print()
	{
		Enumeration<Candidate> e = candidates.elements();
		while (e.hasMoreElements())
			System.out.println(e.nextElement().toString());
	}

	/**
	 * @param outFile
	 * @throws IOException
	 */
	public void save() throws IOException
	{
		Enumeration<Candidate> e = candidates.elements();
		while (e.hasMoreElements())
			outFile.write(e.nextElement().toString());
		outFile.close();
	}

}
