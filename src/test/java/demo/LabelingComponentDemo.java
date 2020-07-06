
package demo;

import ij.ImagePlus;
import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.VirtualStackAdapter;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.labkit.LabelingComponent;
import net.imglib2.labkit.inputimage.DatasetInputImage;
import net.imglib2.labkit.labeling.Labeling;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.view.Views;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Random;

public class LabelingComponentDemo {

	public static void main(String... args) {
		JFrame frame = new JFrame();
		frame.setSize(400, 400);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		ImageLabelingModel model = initModel();
		frame.add(initLabelingComponent(frame, model));
		frame.setVisible(true);
	}

	private static ImageLabelingModel initModel() {
		final ImagePlus imp = new ImagePlus(
			"https://imagej.nih.gov/ij/images/FluorescentCells.jpg");
		ImgPlus<?> image = VirtualStackAdapter.wrap(imp);
		return new ImageLabelingModel(new DatasetInputImage(image));
	}

	private static JComponent initLabelingComponent(JFrame frame,
		ImageLabelingModel model)
	{
		LabelingComponent labelingComponent = new LabelingComponent(frame, model);
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				labelingComponent.close();
			}
		});
		return labelingComponent.getComponent();
	}
}
