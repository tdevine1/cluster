package executable;

import util.Constants;

public class test
{

	private final static String OUT_PATH = Constants.CSV_PATH + "palfa";

	public static void main(String[] args)
	{
		int count = 0;
		System.out.println(OUT_PATH + "_" + count + ".csv");
		for (int i = 1; i < 465; i++)
		{
			if (++count % 25 == 0)
			{
				System.out.println(
						count + "\t" + OUT_PATH + "_" + count + ".csv");
			}
		}
	}
}
