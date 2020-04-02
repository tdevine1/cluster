package plotting;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import javax.imageio.ImageIO;

public class Plotter
{
	private static final int	RECT_HEIGHT		= 750;
	private static final int	SQUARE_HEIGHT	= 500;
	private static final int	SQUARE_WIDTH	= 750;
	private static final int	TITLE_HEIGHT	= 40;
	public static final int		TITLE_FONT_SIZE	= 30;
	public static final int		AXIS_FONT_SIZE	= 20;

	private final double[]		dmRange;
	private double[]			scaledRange		= { 0, 0 };
	private final double		peakDM;
	private ScatterPlot			dmTimePlot;
	private final String		name;
	private Histogram			pcHist;
	private final double[]		pulseCountRange;
	private final double[][]	pulseCounts;
	private double[]			snrRange		= { Double.MAX_VALUE, -1.0 };
	private final double[][]	snrs;
	private ScatterPlot			snrPlot;
	private final double[]		timeRange;
	private final double[][]	times;

	public Plotter(String name, double[] dmRange, double peakDM,
			double[] scaledRange, double[] pulseCountRange,
			double[][] pulseCounts, double[] sigmaRange, double[][] sigmas,
			double[] timeRange, double[][] times)
	{
		this.name = name;
		this.dmRange = dmRange;
		this.peakDM = peakDM;
		this.scaledRange = scaledRange;
		this.pulseCountRange = pulseCountRange;
		this.pulseCounts = pulseCounts;
		this.snrRange = sigmaRange;
		this.snrs = sigmas;
		this.timeRange = timeRange;
		this.times = times;
	}

	public void plot(String outPath)
	{
		try
		{
			DecimalFormat df = new DecimalFormat("00.00");

			BufferedImage title1 = new BufferedImage(SQUARE_WIDTH * 2,
					TITLE_HEIGHT * 2, BufferedImage.TYPE_BYTE_GRAY);
			Graphics2D titleG = title1.createGraphics();
			titleG.setPaint(Color.LIGHT_GRAY);
			titleG.fillRect(0, 0, SQUARE_WIDTH * 2, TITLE_HEIGHT * 2);
			titleG.setColor(Color.BLACK);
			titleG.setFont(new Font(titleG.getFont().getName(), Font.BOLD,
					TITLE_FONT_SIZE));
			int titleWidth = titleG.getFontMetrics().stringWidth(name);
			titleG.drawString(name, SQUARE_WIDTH - titleWidth / 2,
					TITLE_HEIGHT + TITLE_FONT_SIZE / 2);
			titleG.dispose();

			pcHist = new Histogram(null, "Pulse Counts", "DM",
					"Number of Pulses", pulseCounts.length);
			pcHist.setData(pulseCounts);
			pcHist.setRange(new double[] { 0, pulseCountRange[1] });
			pcHist.setDomain(scaledRange);
			BufferedImage pcImg = pcHist.save(SQUARE_WIDTH, SQUARE_HEIGHT);

			snrPlot = new ScatterPlot(null, "SNR vs DM", "DM", "SNR",
					snrs.length);
			snrPlot.setData(snrs, "sigma");
			snrPlot.setRange(snrRange);
			snrPlot.setDomain(scaledRange);
			BufferedImage sigImg = snrPlot.save(SQUARE_WIDTH, SQUARE_HEIGHT);

			dmTimePlot = new ScatterPlot(null, "DM vs Time", "Time", "DM",
					times.length);
			dmTimePlot.setData(times, "time");
			dmTimePlot.setRenderer(new ScatterPlotRenderer(false, true, snrs));
			dmTimePlot.setRange(scaledRange);
			dmTimePlot.setDomain(timeRange);
			BufferedImage dmTimeImg = dmTimePlot.save(2 * SQUARE_WIDTH,
					RECT_HEIGHT);

			BufferedImage composite = new BufferedImage(2 * SQUARE_WIDTH,
					TITLE_HEIGHT * 2 + SQUARE_HEIGHT + RECT_HEIGHT,
					BufferedImage.TYPE_INT_RGB);
			Graphics g = composite.getGraphics();
			g.drawImage(title1, 0, 0, null);
			g.drawImage(pcImg, 0, TITLE_HEIGHT * 2, null);
			g.drawImage(sigImg, SQUARE_WIDTH, TITLE_HEIGHT * 2, null);
			g.drawImage(dmTimeImg, 0, SQUARE_HEIGHT + TITLE_HEIGHT * 2, null);
			g.dispose();
			ImageIO.write(composite, "png",
					new File(outPath + "_" + "DMs" + df.format(dmRange[0]) + "-"
							+ df.format(dmRange[1]) + "-" + df.format(peakDM)
							+ ".png"));
		} catch (IOException e)
		{
			System.out.println("Error writing to file " + outPath + "_" + "DMs"
					+ dmRange[0] + "-" + dmRange[1] + "\n" + e.getMessage());
			e.printStackTrace();
		}
	}
}
