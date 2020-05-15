
package net.imglib2.labkit;

import net.imglib2.labkit.actions.AddLabelingIoAction;
import net.imglib2.labkit.actions.BatchSegmentAction;
import net.imglib2.labkit.actions.BitmapImportExportAction;
import net.imglib2.labkit.actions.ClassifierIoAction;
import net.imglib2.labkit.actions.ClassifierSettingsAction;
import net.imglib2.labkit.actions.LabelEditAction;
import net.imglib2.labkit.actions.LabelingIoAction;
import net.imglib2.labkit.actions.ResetViewAction;
import net.imglib2.labkit.actions.SegmentationAsLabelAction;
import net.imglib2.labkit.actions.SegmentationExportAction;
import net.imglib2.labkit.menu.MenuKey;
import net.imglib2.labkit.models.ColoredLabelsModel;
import net.imglib2.labkit.models.DefaultSegmentationModel;
import net.imglib2.labkit.models.Holder;
import net.imglib2.labkit.models.ImageLabelingModel;
import net.imglib2.labkit.models.SegmentationItem;
import net.imglib2.labkit.panel.ImageInfoPanel;
import net.imglib2.labkit.panel.LabelPanel;
import net.imglib2.labkit.panel.SegmenterPanel;
import net.imglib2.labkit.plugin.MeasureConnectedComponents;
import net.imglib2.labkit.segmentation.PredictionLayer;
import net.imglib2.labkit.segmentation.TrainClassifier;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class SegmentationComponent implements AutoCloseable {

	private final JComponent panel;

	private final boolean fixedLabels;

	private final DefaultExtensible extensible;

	private BasicLabelingComponent labelingComponent;

	private DefaultSegmentationModel segmentationModel;

	public SegmentationComponent(JFrame dialogBoxOwner,
		DefaultSegmentationModel segmentationModel, boolean fixedLabels)
	{
		this.extensible = new DefaultExtensible(segmentationModel.context(),
			dialogBoxOwner);
		this.fixedLabels = fixedLabels;
		this.segmentationModel = segmentationModel;
		labelingComponent = new BasicLabelingComponent(dialogBoxOwner,
			segmentationModel.imageLabelingModel());
		labelingComponent.addBdvLayer(new PredictionLayer(
			segmentationModel.selectedSegmenter(),
			segmentationModel.segmentationVisibility(),
			segmentationModel.labelTransformation(),
			segmentationModel.image()));
		initActions();
		this.panel = initPanel();
	}

	private void initActions() {
		final Holder<SegmentationItem> selectedSegmenter = segmentationModel
			.selectedSegmenter();
		final ImageLabelingModel labelingModel = segmentationModel
			.imageLabelingModel();
		new TrainClassifier(extensible, segmentationModel);
		new ClassifierSettingsAction(extensible, selectedSegmenter);
		new ClassifierIoAction(extensible, selectedSegmenter);
		new LabelingIoAction(extensible, labelingModel);
		new AddLabelingIoAction(extensible, labelingModel.labeling());
		new SegmentationExportAction(extensible);
		new ResetViewAction(extensible, labelingModel);
		new BatchSegmentAction(extensible, selectedSegmenter);
		new SegmentationAsLabelAction(extensible, segmentationModel);
		new BitmapImportExportAction(extensible, labelingModel);
		new LabelEditAction(extensible, fixedLabels, new ColoredLabelsModel(
			labelingModel));
		MeasureConnectedComponents.addAction(extensible, labelingModel);
		labelingComponent.addShortcuts(extensible.getShortCuts());
	}

	private JPanel initLeftPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("", "[grow]", "[][grow][grow]"));
		panel.add(ImageInfoPanel.newFramedImageInfoPanel(segmentationModel
			.imageLabelingModel(), labelingComponent), "grow, wrap");
		panel.add(LabelPanel.newFramedLabelPanel(segmentationModel
			.imageLabelingModel(), extensible, fixedLabels), "grow, wrap");
		panel.add(SegmenterPanel.newFramedSegmeterPanel(segmentationModel,
			extensible), "grow");
		panel.invalidate();
		panel.repaint();
		return panel;
	}

	private JComponent initPanel() {
		JSplitPane panel = new JSplitPane();
		panel.setOneTouchExpandable(true);
		panel.setLeftComponent(initLeftPanel());
		panel.setRightComponent(labelingComponent.getComponent());
		panel.setBorder(BorderFactory.createEmptyBorder());
		return panel;
	}

	public JComponent getComponent() {
		return panel;
	}

	public JMenu createMenu(MenuKey<Void> key) {
		if (key == MenuBar.SEGMENTER_MENU) return extensible.createMenu(
			SegmentationItem.SEGMENTER_MENU, segmentationModel
				.selectedSegmenter()::get);
		return extensible.createMenu(key, () -> null);
	}

	@Override
	public void close() {
		labelingComponent.close();
	}

}
