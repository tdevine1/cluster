package plotting;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.GradientXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYBarDataset;

/**
 * @author zennisarix
 */
public class Histogram
{
	/**
	 * 
	 */
	private JFreeChart				chart;
	/**
	 * 
	 */
	private double[][]				data;
	/**
	 * 
	 */
	private final DefaultXYDataset	dataset;
	/**
	 * 
	 */
	private final File				path;
	/**
	 * 
	 */
	private final XYPlot			plot;
	/**
	 * 
	 */
	private final String			title;
	/**
	 * 
	 */
	private final String			xTitle;

	/**
	 * @param path
	 * @param title
	 * @param xTitle
	 * @param yTitle
	 * @param maxSize
	 */
	public Histogram(File path, String title, String xTitle, String yTitle,
			int maxSize)
	{
		this.path = path;
		this.title = title;
		this.xTitle = xTitle;
		data = null;
		dataset = new DefaultXYDataset();
		XYBarRenderer renderer = new XYBarRenderer();
		renderer.setShadowVisible(false);
		renderer.setBarPainter(new GradientXYBarPainter(0, 0, 0));
		NumberAxis xAxis = new NumberAxis(xTitle);
		NumberAxis yAxis = new NumberAxis(yTitle);
		renderer.setSeriesPaint(0, Color.black);
		plot = new XYPlot(null, xAxis, yAxis, renderer);
		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setBackgroundPaint(Color.white);
		Font tickFont = new Font(Font.SANS_SERIF, Font.PLAIN,
				Plotter.AXIS_FONT_SIZE);
		Font labelFont = new Font(Font.SANS_SERIF, Font.PLAIN,
				Plotter.AXIS_FONT_SIZE + 5);
		plot.getDomainAxis().setTickLabelFont(tickFont);
		plot.getRangeAxis().setTickLabelFont(tickFont);
		plot.getDomainAxis().setLabelFont(labelFont);
		plot.getRangeAxis().setLabelFont(labelFont);
	}

	/**
	 * 
	 */
	public void printData()
	{
		for (int i = 0; i < data.length; i++)
		{
			System.out.print(data[i] + "\t");
			if ((i + 1) % 10 == 0) System.out.println();
		}
	}

	/**
	 * @throws IOException
	 */
	public void save() throws IOException
	{
		dataset.addSeries(xTitle, data);
		plot.setDataset(new XYBarDataset(dataset, 0.1));
		chart = new JFreeChart(title, null, plot, false);
		chart.getTitle().setFont(
				new Font(Font.SANS_SERIF, Font.PLAIN, Plotter.TITLE_FONT_SIZE));
		ChartUtilities.saveChartAsPNG(path, chart, 1000, 600);
	}

	public BufferedImage save(int width, int height)
	{
		dataset.addSeries(xTitle, data);
		plot.setDataset(new XYBarDataset(dataset, 0.1));
		chart = new JFreeChart(title, null, plot, false);
		chart.getTitle().setFont(
				new Font(Font.SANS_SERIF, Font.PLAIN, Plotter.TITLE_FONT_SIZE));
		return chart.createBufferedImage(width, height);
	}

	/**
	 * @param data
	 */
	public void setData(double[][] data)
	{
		this.data = data;
	}

	/**
	 * @param range
	 */
	public void setDomain(double[] range)
	{
		NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
		xAxis.setAutoRange(false);
		xAxis.setLowerBound(range[0]);
		xAxis.setUpperBound(range[1] + 1);
	}

	/**
	 * @param range
	 */
	public void setRange(double[] range)
	{
		NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
		yAxis.setAutoRange(false);
		yAxis.setLowerBound(range[0]);
		yAxis.setUpperBound(range[1] + 1);
	}
}
