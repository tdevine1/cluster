package filehandlers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import util.Constants;

public class ArffFileWriter
{
	BufferedWriter	outBuffer;
	File			outFile;

	public ArffFileWriter(File outFile) throws IOException
	{
		outBuffer = new BufferedWriter(new FileWriter(outFile));
		outBuffer.write("@RELATION " + Constants.SURVEY + "_"
				+ Constants.GRANULARITY
				+ "\n@ATTRIBUTE survey STRING\n@ATTRIBUTE mjd NUMERIC\n@ATTRIBUTE ra STRING\n@ATTRIBUTE dec STRING\n"
				+ "@ATTRIBUTE beam NUMERIC\n@ATTRIBUTE centralFrequency NUMERIC\n@ATTRIBUTE channelBandwidth NUMERIC\n"
				+ "@ATTRIBUTE numChannels NUMERIC\n@ATTRIBUTE startDM NUMERIC\n@ATTRIBUTE stopDM  NUMERIC\n");
		if (Constants.GRANULARITY.equals("singlepulse"))
			outBuffer.write("@ATTRIBUTE dmSpacing NUMERIC\n"
					+ "@ATTRIBUTE startTime NUMERIC\n@ATTRIBUTE stopTime NUMERIC\n"
					+ "@ATTRIBUTE clusterRank NUMERIC\n@ATTRIBUTE pulseRank NUMERIC\n");
		outBuffer
				.write("@ATTRIBUTE pulsePeakDM NUMERIC\n@ATTRIBUTE pulseMaxCount NUMERIC\n"
						+ "@ATTRIBUTE pulseLocalPeakHeight NUMERIC\n@ATTRIBUTE pulseTotalCount NUMERIC\n"
						+ "@ATTRIBUTE pulseAvgCount NUMERIC\n@ATTRIBUTE snrPeakDM NUMERIC\n"
						+ "@ATTRIBUTE snrMax NUMERIC\n@ATTRIBUTE snrLocalPeakHeight NUMERIC\n"
						+ "@ATTRIBUTE snrIntensity NUMERIC\n@ATTRIBUTE snrAvgIntensity NUMERIC\n@ATTRIBUTE snrRatio NUMERIC\n"
						+ "@ATTRIBUTE fittedWidth NUMERIC\n@ATTRIBUTE fittedPeakFlux NUMERIC\n"
						+ "@ATTRIBUTE snrMaxFitChiSquare NUMERIC\n@ATTRIBUTE pulsar {");
		for (int i = 0; i < Constants.CLASS_COUNT - 1; i++)
			outBuffer.write(i + ",");
		outBuffer.write(Constants.CLASS_COUNT - 1 + "}\n@DATA\n");
	}

	public void close() throws IOException
	{
		outBuffer.close();
	}

	public void write(String s) throws IOException
	{
		outBuffer.write(s);
	}
}
