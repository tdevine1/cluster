package edu.fsuhpc.dsps.util;

/**
 * @author zennisarix
 */
public class QuickSort2D
{
	/**
	 * 
	 */
	private static double[][]	array;

	/**
	 * @param left
	 * @param right
	 * @param pivotIndex
	 * @return
	 */
	private static int partition(int left, int right, int pivotIndex)
	{
		double pivotValue = array[0][pivotIndex];
		swap(pivotIndex, right);
		int storeIndex = left;
		for (int i = left; i < right - 1; i++)
			if (array[0][i] <= pivotValue)
			{
				swap(i, storeIndex);
				storeIndex++;
			}
		swap(storeIndex, right);
		return storeIndex;
	}

	/**
	 * @param left
	 * @param right
	 */
	private static void quicksort(int left, int right)
	{
		if (left < right)
		{
			int pivotIndex = left / 2 + right / 2;
			int pivotNewIndex = partition(left, right, pivotIndex);
			quicksort(left, pivotNewIndex - 1);
			quicksort(pivotNewIndex + 1, right);
		}
	}

	/**
	 * @param values
	 * @return
	 */
	public static double[][] sort(double[][] values)
	{
		array = values;
		quicksort(0, values[0].length - 1);
		return array;
	}

	/**
	 * @param i
	 * @param j
	 */
	private static void swap(int i, int j)
	{
		double temp0 = array[0][i];
		double temp1 = array[1][i];
		array[0][i] = array[0][j];
		array[1][i] = array[1][j];
		array[0][j] = temp0;
		array[1][j] = temp1;
	}

}
