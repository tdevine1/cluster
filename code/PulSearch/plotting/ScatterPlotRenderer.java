package plotting;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

import util.ArrayUtils;

@SuppressWarnings("serial")
public class ScatterPlotRenderer extends XYLineAndShapeRenderer
{
	private double[][]	sigmas;

	public ScatterPlotRenderer(boolean f, boolean t, double[][] sigmas)
	{
		super(f, t);
		this.sigmas = ArrayUtils.make2DWideArray(sigmas);
		setSeriesPaint(0, Color.BLACK);
		setBaseShapesFilled(false);
	}

	@Override
	public Shape getItemShape(int row, int col)
	{
		return new Ellipse2D.Double(sigmas[1][col] * -0.5,
				sigmas[1][col] * 0.5, sigmas[1][col], sigmas[1][col]);
	}
}
