package com.fsuhpc.dsps.util;

import java.io.File;

import org.apache.commons.math3.util.FastMath;

public class Constants
{
	public static final String	GRANULARITY			= "singlepulse";
	public static final String	PLOT				= "peak";
//	public static final String SURVEY = "gbt350drift";
//	public static final String	SURVEY				= "aodrift";
	 public static final String SURVEY 				= "palfa";
	public static final String MAIN_PATH 			= "hdfs://master00.local:8020/";
	public static final String	CSV_PATH			= Constants.MAIN_PATH
			+ "data" + File.separator + SURVEY + File.separator + "csv"
			+ File.separator;
	public static final String	ARFF_PATH			= MAIN_PATH + "arff"
			+ File.separator;
	public static final String	DMF_PATH			= MAIN_PATH
			+ "DataMiningFiles" + File.separator + GRANULARITY + File.separator
			+ SURVEY + File.separator;
	public static final String	RAW_PATH			= MAIN_PATH + "raw"
			+ File.separator + SURVEY + File.separator;
	public static final String	PLOT_PATH			= MAIN_PATH + "plots"
			+ File.separator;
	public static final String	ZIP_PATH			= "A:\\AstroData\\zip_files"
			+ File.separator + SURVEY + File.separator;
	public static final String	LABEL_PATH			= MAIN_PATH + "labeled"
			+ File.separator;
	public static final String	PREDICTION_PATH		= MAIN_PATH + "predictions"
			+ File.separator;
	public static final String	MOVE_PATH			= "A:\\AstroData\\raw\\palfa\\";
	public static final String	CSV_HEADER			= "DM,Sigma,Time,Sample,Downfact\n";

	public static final int		THREAD_COUNT		= 1;

	public static final int		CLASS_COUNT			= 4;
	public static final int		BIN_SIZE			= 25;
	public static final double	BIN_WEIGHT			= 0.75;
	// public static final double SLOPE_THRESHOLD = 0.05;
	// public static final double SLOPE_THRESHOLD = 0.1;
	// public static final double SLOPE_THRESHOLD = 0.25;
	public static final double	SLOPE_THRESHOLD		= 0.5;

	public static final int		DEGREE_RANGE		= 50;
	public static final float	DM_RANGE			= 0.5f;
	public static final double	DM_THRESHOLD_MIN	= 1.5;
	public static final double	DM_THRESHOLD_MAX	= 2.0;
	public static final int		PEAK_INDEX			= 19;
	public static final int		DM_START_INDEX		= 7;
	public static final int		DM_STOP_INDEX		= 8;
	public static final int		MATCH_COUNT			= 2;

	public static double		NU_GBT				= 350;
	public static double		BANDWIDTH_GBT		= 24.4140625;
	public static int			NUM_CHANNELS_GBT	= 2048;
	// public static double NU_AO = 1214.28955078;
	public static double		NU_AO				= 1375.59814453;
	public static double		BANDWIDTH_AO		= 336.059570312;
	public static int			NUM_CHANNELS_AO		= 960;

	public static final double	CONSTANT			= 0.00691;
	public static final double	PI_FACTOR			= FastMath.sqrt(FastMath.PI)
			/ 2.0;
}
