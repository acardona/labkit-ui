
package net.imglib2.labkit.panel;

import net.imglib2.labkit.utils.Listeners;
import net.miginfocom.swing.MigLayout;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Similar two JList. But doesn't require the complicated renderer. And the
 * components in the ComponentList can normally react to user input.
 */
public class ComponentList<K, C extends JComponent> {

	private final Color SELECTED_BACKGROUND = UIManager.getColor(
		"List.selectionBackground");
	private final Color BACKGROUND = UIManager.getColor("List.background");

	private JPanel background = new JPanel();
	private JComponent scrollPane = new JScrollPane(background,
		ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
		ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	private K selected;
	private Map<K, C> items = new HashMap<>();
	private Listeners listeners = new Listeners();

	public ComponentList() {
		background.setLayout(new MigLayout("insets 4pt, gap 4pt", "[grow]"));
		background.setBackground(BACKGROUND);
	}

	public JComponent getComponent() {
		return scrollPane;
	}

	public void add(K key, C component) {
		component.setBackground(BACKGROUND);
		component.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				setSelected(key);
			}
		});
		component.setBackground(key == selected ? SELECTED_BACKGROUND : BACKGROUND);
		items.put(key, component);
		background.add(component, "grow, wrap");
		background.revalidate();
		background.repaint();
	}

	public void setSelected(K key) {
		if (this.selected == key) return;
		setItemBackground(selected, BACKGROUND);
		this.selected = key;
		setItemBackground(key, SELECTED_BACKGROUND);
		focusItem(key);
		listeners.notifyListeners();
	}

	private void setItemBackground(K key, Color selected_background) {
		C component = getComponent(key);
		if (component != null)
			component.setBackground(selected_background);
	}

	private void focusItem(K key) {
		C component = getComponent(key);
		if (component != null)
			component.scrollRectToVisible(new Rectangle(component.getWidth(), component.getHeight()));
	}

	private C getComponent(K key) {
		if (key == null)
			return null;
		return items.get(key);
	}

	public K getSelected() {
		return selected;
	}

	public void clear() {
		items.clear();
		background.removeAll();
		background.revalidate();
		background.repaint();
	}

	public Listeners listeners() {
		return listeners;
	}

	// -- demo --

	public static void main(String... args) {
		JFrame frame = new JFrame();
		frame.setSize(300, 600);
		frame.setLayout(new MigLayout("", "[grow]", "[grow][]"));
		ComponentList<Integer, JPanel> panelList = new ComponentList<>();
		AtomicInteger counter = new AtomicInteger();
		for (int i = 0; i < 1000; i++)
			addItem(panelList, counter);
		Random random = new Random();
		frame.add(panelList.getComponent(), "grow, wrap");
		frame.add(new JButton(new RunnableAction("add", () -> addItem(panelList, counter))), "split");
		frame.add(new JButton(new RunnableAction("clear", () -> panelList.clear())));
		frame.add(new JButton(new RunnableAction("jump", () -> panelList.setSelected(random.nextInt(
			counter.get())))));
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	private static void addItem(ComponentList<Integer, JPanel> panelList, AtomicInteger counter) {
		int key = counter.getAndIncrement();
		panelList.add(key, newExamplePanel(key));
	}

	private static JPanel newExamplePanel(int i) {
		JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("", "[][][grow]"));
		panel.add(new JCheckBox());
		JButton button = new JButton();
		button.setBackground(Color.RED);
		panel.add(button);
		panel.add(new JLabel("Hello " + i));
		return panel;
	}
}
