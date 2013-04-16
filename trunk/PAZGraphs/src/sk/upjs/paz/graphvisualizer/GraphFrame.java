/*
 * Copyright 2013 Frantisek Galcik
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package sk.upjs.paz.graphvisualizer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

import javax.swing.DefaultComboBoxModel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import sk.upjs.paz.graph.Edge;
import sk.upjs.paz.graph.Graph;
import sk.upjs.paz.graph.Vertex;

import java.awt.Dimension;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JLabel;

/**
 * The frame displaying all visualization components.
 */
@SuppressWarnings("serial")
class GraphFrame extends JFrame {

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // MonitoredObject
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Represents a monitored object.
     */
    private static class MonitoredObject {
	/**
	 * Name of the monitored object
	 */
	String name;

	/**
	 * Monitored object.
	 */
	Object monitoredObject;

	@Override
	public String toString() {
	    if (name != null)
		return name;
	    else
		return monitoredObject.toString();
	}
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // KeyValueTableModel
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Table model for displaying key-value collection of monitored object.
     */
    private static class KeyValueTableModel extends AbstractTableModel {
	/**
	 * Key value pairs.
	 */
	private final Map<Object, Object> valuesMap = new HashMap<Object, Object>();

	/**
	 * List containing ordered keys.
	 */
	private final List<Object> keysOrder = new ArrayList<Object>();

	/**
	 * Keys for which the value was changed.
	 */
	private final Set<Object> changedKeys = new HashSet<Object>();

	/**
	 * Clear the table.
	 */
	public void clear() {
	    valuesMap.clear();
	    keysOrder.clear();
	    fireTableDataChanged();
	}

	/**
	 * Set new content of the table.
	 */
	public void updateContent(Map<Object, Object> rows, boolean displayChanges) {
	    Map<Object, Object> compareMap = null;
	    boolean noChangeFound = true;
	    if (displayChanges)
		compareMap = new HashMap<Object, Object>(valuesMap);
	    else
		changedKeys.clear();

	    valuesMap.clear();
	    keysOrder.clear();
	    for (Map.Entry<Object, Object> entry : rows.entrySet()) {

		// if check for changes is enabled
		if (displayChanges) {
		    Object oldValue = compareMap.get(entry.getKey());
		    boolean changed;
		    if (oldValue == null) {
			changed = (oldValue != entry.getValue());
		    } else {
			changed = !(oldValue.equals(entry.getValue()));
		    }

		    if (changed) {
			if (noChangeFound) {
			    noChangeFound = false;
			    changedKeys.clear();
			}

			changedKeys.add(entry.getKey());
		    }
		}

		valuesMap.put(entry.getKey(), entry.getValue());
		keysOrder.add(entry.getKey());
	    }

	    // if display of changes is enabled and there was no change
	    // detected, we end without firing of data changes.
	    if (displayChanges && noChangeFound)
		return;

	    fireTableDataChanged();
	}

	@Override
	public int getColumnCount() {
	    return 2;
	}

	@Override
	public String getColumnName(int column) {
	    if (column == 0)
		return "Key";
	    else
		return "Value";
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
	    if (columnIndex == 0)
		return String.class;
	    else
		return ColoredText.class;
	}

	@Override
	public int getRowCount() {
	    return valuesMap.size();
	}

	@Override
	public Object getValueAt(int row, int collumn) {
	    if ((row >= 0) && (row < valuesMap.size())) {
		if (collumn == 0)
		    return keysOrder.get(row);
		else {
		    Object key = keysOrder.get(row);
		    return new ColoredText(valuesMap.get(key), changedKeys.contains(key) ? Color.yellow : Color.white);
		}
	    }

	    return null;
	}

	/**
	 * Clean changes.
	 */
	public void clearChanges() {
	    changedKeys.clear();
	    fireTableDataChanged();
	}
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Label
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Named label.
     */
    private static class Label {
	String name;

	/**
	 * Constructs a new label with given name.
	 * 
	 * @param name
	 *            the name of label.
	 */
	public Label(String name) {
	    this.name = name;
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((name == null) ? 0 : name.hashCode());
	    return result;
	}

	@Override
	public boolean equals(Object obj) {
	    if (this == obj)
		return true;
	    if (obj == null)
		return false;
	    if (getClass() != obj.getClass())
		return false;
	    Label other = (Label) obj;
	    if (name == null) {
		if (other.name != null)
		    return false;
	    } else if (!name.equals(other.name))
		return false;
	    return true;
	}

	@Override
	public String toString() {
	    return name.toString();
	}
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Cell rendering
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    private static class ColoredTextRenderer extends DefaultTableCellRenderer {
	public ColoredTextRenderer() {
	    super();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
		boolean hasFocus, int row, int column) {
	    Component result = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	    if (!isSelected) {
		if ((value != null) && (value instanceof ColoredText)) {
		    result.setBackground(((ColoredText) value).color);
		}
	    }
	    return result;
	}
    }

    /**
     * Special class for highlighted values.
     */
    private static class ColoredText {
	private Object obj;
	private Color color;

	public ColoredText(Object obj, Color color) {
	    this.obj = obj;
	    this.color = color;
	}

	@Override
	public String toString() {
	    if (obj != null)
		return obj.toString();
	    else
		return "";
	}
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Breakpoint (pause)
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * The breakpoint that stops execution of an thread executing an algorithm.
     */
    private static class Breakpoint {
	/**
	 * Semaphore that block the thread executing an algorithm.
	 */
	Semaphore semaphore;

	/**
	 * Description of the breakpoint.
	 */
	String description;
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Instance variables
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Graph that is visualized in this frame.
     */
    private Graph graph;

    /**
     * List of monitored objects
     */
    private final List<MonitoredObject> monitoredObjects = new ArrayList<MonitoredObject>();

    /**
     * Special record for monitored object that is always monitoring selected
     * graph object.
     */
    private final MonitoredObject graphObjectSelection = new MonitoredObject();

    /**
     * Currently actively monitored object.
     */
    private MonitoredObject monitorSelection;

    /**
     * Table model for displaying key-value table of actively monitored object.
     */
    private final KeyValueTableModel mapTableModel = new KeyValueTableModel();

    /**
     * Queue of unconfirmed semaphores.
     */
    private final Queue<Breakpoint> breakpoints = new LinkedList<Breakpoint>();

    /**
     * The graph panel displaying the visualized graph.
     */
    private GraphPanel graphPanel;

    /**
     * Combobox with list of objects that are monitored.
     */
    private JComboBox<MonitoredObject> mapComboBox;

    /**
     * Checkbox for enabling/disabling display of weights of graph edges.
     */
    private JCheckBox showWeightsCheckbox;

    /**
     * Table displaying key-value pairs of the monitored object.
     */
    private JTable mapTable;

    /**
     * Button to control visualization breakpoints.
     */
    private JButton continueButton;

    /**
     * Label for displaying the title of the current breakpoint (for instance
     * the title can be description of the current state of the algorithm
     * execution).
     */
    private JLabel breakpointTitleLabel;

    /**
     * Timer that control refresh of the monitored object.
     */
    private javax.swing.Timer refreshTimer;

    /**
     * Period of refresh of the monitored object.
     */
    private static final int MONITOR_REFRESH_PERIOD = 1000;

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Constructs the frame.
     */
    public GraphFrame() {
	this(null);
    }

    /**
     * Constructs a graph visualization frame.
     * 
     * @param graph
     *            the graph that is visualized.
     */
    public GraphFrame(Graph graph) {
	this.graph = graph;
	initializeComponents();

	// create default monitored object - properties of a selected graph
	// object
	graphObjectSelection.name = "Selected object";
	monitoredObjects.add(graphObjectSelection);
	updateListOfMonitoredObjects();
	onMonitorChanged();

	installRefreshTimer();
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Custom visualization colors
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Sets visualization color for an object of a graph (vertex or edge).
     * 
     * @param obj
     *            the graph object.
     * @param color
     *            the desired visualization color.
     */
    public void setVisualizationColor(Object obj, Color color) {
	graphPanel.setVisualizationColor(obj, color);
    }

    /**
     * Resets visualization color of all parts of the graph.
     */
    public void resetAllColors() {
	graphPanel.resetAllColors();
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Monitoring
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Adds a new object to the set of monitored objects.
     * 
     * @param name
     *            the displayed name of the monitored object.
     * @param monitoredObject
     *            the monitored object.
     */
    public void addToMonitor(String name, Object monitoredObject) {
	if (monitoredObject == null)
	    return;

	if (name != null)
	    name = name.trim();

	boolean found = false;
	for (MonitoredObject mo : monitoredObjects) {
	    if (mo.monitoredObject == monitoredObject) {
		mo.name = name;
		found = true;
		break;
	    }
	}

	if (!found) {
	    MonitoredObject mo = new MonitoredObject();
	    mo.name = name;
	    mo.monitoredObject = monitoredObject;
	    monitoredObjects.add(mo);
	}

	updateListOfMonitoredObjects();
    }

    /**
     * Removes a monitored object from the set of monitored objects.
     * 
     * @param monitoredObject
     *            the monitored object to be removed from monitoring.
     */
    public void removeFromMonitor(Object monitoredObject) {
	boolean wasChange = false;
	Iterator<MonitoredObject> it = monitoredObjects.iterator();
	while (it.hasNext()) {
	    MonitoredObject mo = it.next();
	    if (mo.monitoredObject == monitoredObject) {
		it.remove();
		wasChange = true;
		break;
	    }
	}

	if (wasChange)
	    updateListOfMonitoredObjects();
    }

    /**
     * Updates combobox for choosing actively monitored object.
     */
    private void updateListOfMonitoredObjects() {
	MonitoredObject oldSelection = monitorSelection;
	mapComboBox
		.setModel(new DefaultComboBoxModel<MonitoredObject>(monitoredObjects.toArray(new MonitoredObject[0])));

	monitorSelection = (MonitoredObject) mapComboBox.getSelectedItem();
	if (monitorSelection != oldSelection)
	    onMonitorChanged();
    }

    /**
     * Realizes all actions after changing which object is monitored.
     */
    private void onMonitorChanged() {
	monitorSelection = (MonitoredObject) mapComboBox.getSelectedItem();
	if (monitorSelection != null) {
	    mapTableModel.updateContent(transformToMap(monitorSelection.monitoredObject), false);
	} else {
	    mapTableModel.clear();
	}
    }

    /**
     * Transforms the state of a object to a map, i.e., a set of key-value
     * pairs.
     * 
     * @param obj
     *            the object to be transformed to a map.
     * @return the map describing the object or empty map, if the object cannot
     *         be transformed to the map.
     */
    @SuppressWarnings("unchecked")
    private Map<Object, Object> transformToMap(Object obj) {
	if (obj == null)
	    return Collections.emptyMap();

	// Map
	if (obj instanceof Map) {
	    return (Map<Object, Object>) obj;
	}

	// Edge
	if (obj instanceof Edge) {
	    Edge e = (Edge) obj;
	    LinkedHashMap<Object, Object> result = new LinkedHashMap<Object, Object>();
	    result.put(new Label("weight"), e.getWeight());
	    for (String propertyName : e.getPropertyNames())
		result.put(propertyName, e.getValue(propertyName));

	    return result;
	}

	// Vertex
	if (obj instanceof Vertex) {
	    Vertex v = (Vertex) obj;
	    LinkedHashMap<Object, Object> result = new LinkedHashMap<Object, Object>();
	    result.put(new Label("label"), v.getLabel());
	    for (String propertyName : v.getPropertyNames())
		result.put(propertyName, v.getValue(propertyName));

	    return result;
	}

	// Collection (Set or List)
	if (obj instanceof Collection) {
	    Collection<Object> collection = (Collection<Object>) obj;
	    LinkedHashMap<Object, Object> result = new LinkedHashMap<Object, Object>();
	    int idx = 0;
	    for (Object item : collection) {
		result.put(idx, item);
		idx++;
	    }

	    return result;
	}

	// Array
	if (obj.getClass().isArray()) {
	    int length = Array.getLength(obj);
	    LinkedHashMap<Object, Object> result = new LinkedHashMap<Object, Object>();
	    for (int i = 0; i < length; i++) {
		result.put(i, Array.get(obj, i));
	    }

	    return result;
	}

	return Collections.emptyMap();
    }

    /**
     * Install refresh timer that controls refresh of data of the monitored
     * object.
     */
    private void installRefreshTimer() {
	refreshTimer = new Timer(MONITOR_REFRESH_PERIOD, new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		if (monitorSelection != null)
		    mapTableModel.updateContent(transformToMap(monitorSelection.monitoredObject), true);
	    }
	});

	refreshTimer.setRepeats(true);
	refreshTimer.start();
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Breakpoints
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Adds a semaphore to the list of unconfirmed semaphores that are released
     * after clicking the Continue button.
     * 
     * @param semaphore
     *            the semaphore to be released by clicking the Continue button.
     */
    public void addBreakpoint(Semaphore semaphore, String description) {
	if (semaphore == null)
	    return;

	Breakpoint breakpoint = new Breakpoint();
	breakpoint.semaphore = semaphore;
	breakpoint.description = description;
	breakpoints.offer(breakpoint);
	setupContinueButton();
    }

    /**
     * Setups the continue button and description according to the oldest
     * breakpoint (if exists).
     */
    private void setupContinueButton() {
	continueButton.setEnabled(!breakpoints.isEmpty());

	if (!breakpoints.isEmpty()) {
	    String description = breakpoints.peek().description;
	    if (description == null)
		breakpointTitleLabel.setText("");
	    else
		breakpointTitleLabel.setText(description);
	} else {
	    breakpointTitleLabel.setText("");
	}

	// Update values
	if (monitorSelection != null) {
	    mapTableModel.updateContent(transformToMap(monitorSelection.monitoredObject), true);
	} else {
	    mapTableModel.clear();
	}
    }

    /**
     * Invoked when the continue button is clicked.
     */
    private void continueButtonClicked() {
	if (!breakpoints.isEmpty()) {
	    Breakpoint current = breakpoints.poll();
	    current.semaphore.release();
	}

	mapTableModel.clearChanges();
	setupContinueButton();
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // GUI initialization
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Creates and initializes components of the frame.
     */
    private void initializeComponents() {
	setTitle("GraphVisualizer");
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	setBounds(100, 100, 731, 520);
	JPanel contentPane = new JPanel();
	contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
	setContentPane(contentPane);

	JPanel headPanel = new JPanel();
	headPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

	JPanel mainPanel = new JPanel();
	GroupLayout gl_contentPane = new GroupLayout(contentPane);
	gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
		.addComponent(headPanel, GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE)
		.addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, 540, Short.MAX_VALUE));
	gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addGroup(
		gl_contentPane
			.createSequentialGroup()
			.addComponent(headPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
				GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED)
			.addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, 386, Short.MAX_VALUE)));
	headPanel.setLayout(new BorderLayout(0, 0));

	JPanel breakpointPanel = new JPanel();
	headPanel.add(breakpointPanel, BorderLayout.EAST);

	breakpointTitleLabel = new JLabel("");
	breakpointPanel.add(breakpointTitleLabel);

	continueButton = new JButton("Continue");
	continueButton.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		continueButtonClicked();
	    }
	});
	continueButton.setEnabled(false);
	breakpointPanel.add(continueButton);

	JPanel settingsPanel = new JPanel();
	headPanel.add(settingsPanel, BorderLayout.WEST);

	showWeightsCheckbox = new JCheckBox("Show weights of edges");
	showWeightsCheckbox.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		graphPanel.setShowWeights(showWeightsCheckbox.isSelected());
	    }
	});
	settingsPanel.add(showWeightsCheckbox);
	mainPanel.setLayout(new BorderLayout(0, 0));

	JSplitPane mainSplitPane = new JSplitPane();
	mainSplitPane.setResizeWeight(1.0);
	mainPanel.add(mainSplitPane);

	JPanel mapViewPanel = new JPanel();
	mapViewPanel.setPreferredSize(new Dimension(150, 10));
	mainSplitPane.setRightComponent(mapViewPanel);
	mapViewPanel.setLayout(new BorderLayout(0, 0));

	mapComboBox = new JComboBox<MonitoredObject>();
	mapComboBox.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		onMonitorChanged();
	    }
	});
	mapViewPanel.add(mapComboBox, BorderLayout.NORTH);

	JScrollPane mapTableScrollPane = new JScrollPane();
	mapViewPanel.add(mapTableScrollPane, BorderLayout.CENTER);

	mapTable = new JTable();
	mapTable.setModel(mapTableModel);
	mapTable.setDefaultRenderer(ColoredText.class, new ColoredTextRenderer());
	mapTableScrollPane.setViewportView(mapTable);

	JScrollPane graphScrollPane = new JScrollPane();
	graphScrollPane.setAutoscrolls(true);
	graphScrollPane.setPreferredSize(new Dimension(640, 480));
	mainSplitPane.setLeftComponent(graphScrollPane);

	graphPanel = new GraphPanel(graph);
	graphPanel.setSelectionListener(new GraphPanel.SelectionListener() {
	    @Override
	    public void selectionChanged(Object selection) {
		if (graphObjectSelection.monitoredObject == selection)
		    return;

		graphObjectSelection.monitoredObject = selection;
		if (graphObjectSelection == monitorSelection) {
		    mapTableModel.updateContent(transformToMap(monitorSelection.monitoredObject), false);
		}
	    }
	});
	graphPanel.setPreferredSize(new Dimension(640, 480));
	graphPanel.setShowWeights(showWeightsCheckbox.isSelected());
	graphScrollPane.setViewportView(graphPanel);
	mainSplitPane.setDividerLocation(500);
	contentPane.setLayout(gl_contentPane);
    }
}
