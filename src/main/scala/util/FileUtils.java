package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Vector;

/**
 * @author Zennisarix
 *
 */
public final class FileUtils
{
	/**
	 * Quickly determines if a directory is empty without listing all of its
	 * files.
	 * 
	 * @param directory
	 * @return true iff the directory is empty
	 * @throws IOException
	 */
	public static boolean isDirEmpty(final Path dir) throws IOException
	{
		try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir))
		{
			return !dirStream.iterator().hasNext();
		}
	}

	public static int countInstances(String arffPath) throws Exception
	{
		BufferedReader in = new BufferedReader(
				new FileReader(new File(arffPath)));
		int count = 0;
		String s = in.readLine();
		while (s.startsWith("@"))
			s = in.readLine();
		while (in.ready())
			count++;
		in.close();
		return count;
	}

	public static Vector<String[]> parsePsrcatKnowns(String filePath)
			throws IOException
	{
		Vector<String[]> psrcatKnowns = new Vector<String[]>();
		BufferedReader inBuffer = new BufferedReader(new FileReader(filePath));
		while (inBuffer.ready())
		{
			String[] radecDM = inBuffer.readLine().split("\t");
			for (int i = 0; i < 3; i += 2)
			{
				if (radecDM[i].length() == 2)
					radecDM[i] += "00";
				else
					if (radecDM[i].length() > 4)
						radecDM[i] = radecDM[i].substring(0, 4);
			}
			psrcatKnowns.add(radecDM);
		}
		inBuffer.close();
		return psrcatKnowns;
	}

	public static Vector<String[]> readArffFile(File arffFile)
			throws FileNotFoundException, IOException
	{
		Vector<String[]> arffData = new Vector<String[]>();
		BufferedReader inBuffer = new BufferedReader(new FileReader(arffFile));
		while (!inBuffer.readLine().toLowerCase().startsWith("@data"))
			;
		while (inBuffer.ready())
		{
			String line = inBuffer.readLine();

			arffData.add(line.split(","));
		}
		inBuffer.close();
		return arffData;
	}

	public static String[] readFileToArray(String filePath) throws Exception
	{
		String output = "";
		BufferedReader in = new BufferedReader(
				new FileReader(new File(filePath)));
		String s = in.readLine();
		while (s.startsWith("@"))
			s = in.readLine();
		output += s + "\n";
		while (in.ready())
			output += in.readLine() + "\n";
		in.close();
		return output.split("\n");
	}

	public static Vector<String> readFileToVector(String filePath)
			throws Exception
	{
		Vector<String> output = new Vector<String>();
		BufferedReader in = new BufferedReader(
				new FileReader(new File(filePath)));
		String s = in.readLine();
		while (s.startsWith("@"))
			s = in.readLine();
		output.add(s);
		while (in.ready())
			output.add(in.readLine());
		in.close();
		return output;
	}

	public static String[] readTxtFile(File file) throws Exception
	{
		String output = "";
		BufferedReader in = new BufferedReader(
				new FileReader(new File(file.getPath())));
		while (in.ready())
			output += in.readLine() + "\n";
		in.close();
		return output.split("\n");
	}

	public static void writeFile(String output, String outPath) throws Exception
	{
		BufferedWriter out = new BufferedWriter(
				new FileWriter(new File(outPath)));
		out.write(output);
		out.close();
	}
}
