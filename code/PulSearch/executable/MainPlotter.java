package executable;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.swing.JFileChooser;

import filehandlers.DatFileReader;
import filehandlers.SinglePulseDatFileReader;
import util.Constants;

public class MainPlotter
{
	public static void main(String[] args)
	{
		try
		{
			BufferedReader inBuffer;
			JFileChooser chooser = new JFileChooser(Constants.PLOT_PATH);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setDialogTitle("Select list of file names to plot...");
			chooser.setApproveButtonText("Plot");
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
			{
				String id = chooser.getSelectedFile().getPath();
				id = id.substring(0, id.lastIndexOf("."));
				inBuffer = new BufferedReader(
						new FileReader(chooser.getSelectedFile()));
				while (inBuffer.ready())
				{
					String input = inBuffer.readLine();
					// skip headers
					if (!input.startsWith("filename") && !input.startsWith("@"))
					{
						try
						{
							if (Constants.GRANULARITY == "dpg")
								new DatFileReader(id, input);
							else
								if (Constants.GRANULARITY == "singlepulse")
									new SinglePulseDatFileReader(id, input);
						} catch (FileNotFoundException e)
						{
							System.out.println(e.getMessage());
						}
					}
				}
				inBuffer.close();
			} else
				System.exit(1);
		} catch (Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
}
