package edu.fsuhpc.dsps.util;

import java.text.DecimalFormat;
import java.util.Arrays;

/**
 * @author zennisarix
 */
public final class ArrayUtils
{
	/**
	 * @param neighborhood
	 * @return
	 */
	public static double average(double[] neighborhood)
	{
		double average = 0;
		for (double element : neighborhood)
			average += element / neighborhood.length;
		return average;
	}

	/**
	 * @param neighborhood
	 * @return
	 */
	public static double average(double[][] neighborhood)
	{
		int total = 0;
		double sum = 0.0;
		for (double[] element : neighborhood)
			if (element.length > 1) for (int j = 1; j < element.length; j++)
			{
				sum += element[j];
				total++;
			}
		return sum / total;
	}

	/**
	 * @param doubles
	 * @return
	 */
	public static long[] convertD2L(double[] doubles)
	{
		long[] longs = new long[doubles.length];
		for (int i = 0; i < doubles.length; i++)
			longs[i] = (long) doubles[i];
		return longs;
	}

	/**
	 * @param array
	 * @param start
	 * @param stop
	 * @return
	 */
	public static double[][] copyLong(double[][] array, int start, int stop)
	{
		double[][] neighborhood = new double[stop - start][];
		for (int i = 0; i < stop - start; i++)
			neighborhood[i] = array[i + start];
		return neighborhood;
	}

	/**
	 * @param array
	 * @param start
	 * @param stop
	 * @return
	 */
	public static double[][] copyWide(double[][] array, int start, int stop)
	{
		double[][] neighborhood = new double[2][stop - start];
		neighborhood[0] = Arrays.copyOfRange(array[0], start, stop);
		neighborhood[1] = Arrays.copyOfRange(array[1], start, stop);
		return neighborhood;
	}

	/**
	 * @param data
	 * @return
	 */
	public static int getDataSize(double[][] data)
	{
		int dataIndex = 0;
		for (double[] element : data)
			if (element != null && element.length > 1)
				for (int j = 1; j < element.length; j++)
				dataIndex++;
		return dataIndex;
	}

	public static double[] getRange(double[][] values)
	{
		double[] range = { Double.MAX_VALUE, -1 };
		for (double test : values[1])
		{
			if (test < range[0]) range[0] = test;
			if (test > range[1]) range[1] = test;
		}
		return range;
	}

	public static double[][] make2DWideArray(double[][] data)
	{
		double[][] newData = new double[2][ArrayUtils.getDataSize(data)];
		int dataIndex = 0;
		for (double[] element : data)
			if (element != null && data[0].length > 1)
				for (int j = 1; j < element.length; j++)
			{
				newData[0][dataIndex] = element[0];
				newData[1][dataIndex++] = element[j];
			}
		return newData;
	}

	/**
	 * @param neighborhood
	 * @return
	 */
	public static int maxIndex(double[] neighborhood, double maxValue)
	{
		int index = 0;
		for (double element : neighborhood)
			if (element != maxValue)
				index++;
			else
				break;
		return index;
	}

	/**
	 * @param neighborhood
	 * @return
	 */
	public static double maxValue(double[] neighborhood, int startingIndex)
	{
		double max = 0.0;
		for (int i = startingIndex; i < neighborhood.length; i++)
			if (neighborhood[i] > max) max = neighborhood[i];
		return max;
	}

	/**
	 * @param neighborhood
	 */
	public static void print(double[] array)
	{
		DecimalFormat df = new DecimalFormat("#.##");
		for (double value : array)
			System.out.print(df.format(value) + " ");
		System.out.println();
	}

	/**
	 * @param neighborhood
	 */
	public static void print(double[][] neighborhood)
	{
		System.out.println("Array is " + neighborhood.length + "x"
				+ neighborhood[0].length);
		for (double[] element : neighborhood)
			if (element != null)
			{
				System.out.print("[\t");
				for (double element2 : element)
					System.out.print(element2 + "\t");
				System.out.println("]");
			}
	}

	/**
	 * 
	 */
	public static void print(String[] array)
	{
		for (String value : array)
			System.out.print(value + " ");
		System.out.println();
	}

	/**
	 * @param neighborhood
	 * @return
	 */
	public static double sum(double[] neighborhood)
	{
		double sum = 0.0;
		for (double element : neighborhood)
			sum += element;
		return sum;
	}

	/**
	 * @param neighborhood
	 * @return
	 */
	public static double sum(double[][] neighborhood)
	{
		double sum = 0.0;
		for (double[] element : neighborhood)
			if (element.length > 1) for (int j = 1; j < element.length; j++)
				sum += element[j];
		return sum;
	}

	/**
	 * @param A
	 *            2 dimensional array.
	 * @return The maximum value in the array, ignoring the first indexed double
	 *         (the DM).
	 */
	public static double maxValue(double[][] array2D)
	{
		double max = 0.0;
		for (double[] array : array2D)
		{
			for (int i = 1; i < array.length; i++)
			{
				if (array[i] > max) max = array[i];
			}
		}
		return max;
	}

	/**
	 * @param A
	 *            2 dimensional array.
	 * @return The minimum value in the array, ignoring the first indexed double
	 *         (the DM) and 0s.
	 */
	public static double minValue(double[][] array2D)
	{
		double min = Double.MAX_VALUE;
		for (double[] array : array2D)
		{
			for (int i = 1; i < array.length; i++)
			{
				if (array[i] < min && array[i] > 0.0) min = array[i];
			}
		}
		return min;
	}
}
