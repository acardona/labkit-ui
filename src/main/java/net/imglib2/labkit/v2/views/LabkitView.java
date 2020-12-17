
package net.imglib2.labkit.v2.views;

import bdv.util.Bdv;
import bdv.util.BdvHandlePanel;
import bdv.util.BdvStackSource;
import net.imglib2.labkit.bdv.BdvAutoContrast;
import net.imglib2.labkit.v2.models.ImageModel;
import net.imglib2.labkit.v2.models.LabkitModel;
import net.imglib2.labkit.v2.utils.BdvShowableIoUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LabkitView extends JFrame {

	private final LabkitModel model;

	private final JButton addImageButton = new JButton("add");

	private final ListAdapter listAdapter = new ListAdapter();

	private final JList<String> imageList = new JList<>(listAdapter);

	private final JLabel activeImageLabel = new JLabel("-");

	private BdvHandlePanel activeBdv;

	public LabkitView(LabkitModel model) {
		this.model = model;
		add(activeImageLabel, BorderLayout.PAGE_START);
		add(rightPanel(), BorderLayout.LINE_END);
		setSize(800, 600);
	}

	private JPanel rightPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("", "[grow]", "[][grow][]"));
		panel.add(new JLabel("Images"), "wrap");
		panel.add(new JScrollPane(imageList), "grow, wrap");
		panel.add(addImageButton);
		return panel;
	}

	// Updater

	public void updateImageList() {
		listAdapter.triggerUpdate();
	}

	public void updateActiveImage() {
		ImageModel activeImageModel = model.getActiveImageModel();
		String text = activeImageModel.getName();
		if (activeBdv != null) {
			remove(activeBdv.getViewerPanel());
			activeBdv.close();
		}
		activeBdv = new BdvHandlePanel(this, Bdv.options());
		add(activeBdv.getViewerPanel());
		BdvStackSource<?> source = activeImageModel.getImageForDisplaying().show(activeImageModel
			.getName(), Bdv.options().addTo(activeBdv));
		BdvAutoContrast.autoContrast(source);
		activeImageLabel.setText(text);
	}

	// Getter

	public JButton getAddImageButton() {
		return addImageButton;
	}

	// Listeners

	public void addImageModelSelectionListener(Consumer<ImageModel> listener) {
		imageList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				ImageModel activeImageModel = model.getImageModels().get(imageList.getSelectedIndex());
				listener.accept(activeImageModel);
			}
		});
	}

	// Data visualization

	private class ListAdapter implements ListModel<String> {

		private final List<ListDataListener> listeners = new CopyOnWriteArrayList<>();

		private void triggerUpdate() {
			ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, getSize());
			listeners.forEach(l -> l.contentsChanged(e));
		}

		@Override
		public int getSize() {
			return model.getImageModels().size();
		}

		@Override
		public String getElementAt(int index) {
			return model.getImageModels().get(index).getName();
		}

		@Override
		public void addListDataListener(ListDataListener l) {
			listeners.add(l);
		}

		@Override
		public void removeListDataListener(ListDataListener l) {
			listeners.remove(l);
		}
	};

	// demo

	public static void main(String... args) {
		LabkitModel model = new LabkitModel();
		model.getImageModels().add(ImageModel.createForImageFile("a.tif"));
		model.getImageModels().add(ImageModel.createForImageFile("b.tif"));
		new LabkitView(model).setVisible(true);
	}
}
