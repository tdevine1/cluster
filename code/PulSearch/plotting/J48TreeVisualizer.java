package plotting;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.FileReader;

import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.gui.treevisualizer.PlaceNode1;
import weka.gui.treevisualizer.TreeVisualizer;

public class J48TreeVisualizer
{
	public static void main(String args[]) throws Exception
	{
		// train classifier
		J48 cls = new J48();
		Instances data = new Instances(
				new BufferedReader(
						new FileReader(
								"/AstroData/DataMiningFiles/allgbnccresults/n25m0.5/good fits/n25m0.5_random-25000-noid_oversample.arff")));
		data.setClassIndex(data.numAttributes() - 1);
		cls.buildClassifier(data);

		// display classifier
		final javax.swing.JFrame jf = new javax.swing.JFrame(
				"Weka Classifier Tree Visualizer: J48");
		jf.setSize(2000, 1500);
		jf.getContentPane().setLayout(new BorderLayout());
		TreeVisualizer tv = new TreeVisualizer(null, cls.graph(),
				new PlaceNode1());
		jf.getContentPane().add(tv, BorderLayout.CENTER);
		jf.addWindowListener(new java.awt.event.WindowAdapter()
		{
			@Override
			public void windowClosing(java.awt.event.WindowEvent e)
			{
				jf.dispose();
			}
		});

		jf.setVisible(true);
		tv.fitToScreen();
	}

}