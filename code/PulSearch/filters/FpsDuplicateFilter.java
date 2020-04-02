package filters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JFileChooser;

import filehandlers.FileUtils;
import util.Constants;

public class FpsDuplicateFilter
{
	public static int			CLASS_VALUE	= 2;
	public static final String	FPS_PATH	= Constants.PLOT_PATH
			+ Constants.SURVEY + File.separator + "fp_lists" + File.separator
			+ "multiclass" + File.separator;
	public static final String	OUT_PATH	= Constants.PLOT_PATH
			+ Constants.SURVEY + File.separator + "fp_lists" + File.separator
			+ "multiclass" + File.separator + "unique-" + CLASS_VALUE + "s.txt";

	public static void main(String[] args)
	{
		try
		{
			JFileChooser chooser;

			File inFile = null;

			chooser = new JFileChooser(FPS_PATH);
			chooser.setMultiSelectionEnabled(false);
			chooser.setDialogTitle("Select duplicate file...");
			chooser.setApproveButtonText("Filter");
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
				inFile = chooser.getSelectedFile();
			else
				System.exit(1);
			String[] fps = FileUtils.readTxtFile(inFile);
			Vector<String> unique = new Vector<String>();
			BufferedWriter out = new BufferedWriter(
					new FileWriter(new File(OUT_PATH)));
			for (String fp : fps)
			{
				boolean duplicate = false;
				Enumeration<String> e = unique.elements();
				while (e.hasMoreElements() && !duplicate)
				{
					if (e.nextElement()
							.contains(fp.substring(0, fp.indexOf("\t"))))
						duplicate = true;
				}
				if (!duplicate) unique.add(fp);
			}
			Enumeration<String> e = unique.elements();
			while (e.hasMoreElements())
				out.write(e.nextElement() + "\n");
			out.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
