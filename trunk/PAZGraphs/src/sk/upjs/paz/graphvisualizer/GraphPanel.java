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

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.util.*;

import javax.swing.JPanel;

import sk.upjs.paz.graph.*;

/**
 * The panel for visualizing the associated graph.
 */
@SuppressWarnings("serial")
class GraphPanel extends JPanel {

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // SelectionListener
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * The listener interface for receiving notification when selected graph
     * object is changed.
     */
    public interface SelectionListener {
	/**
	 * Invoked when selected graph object was changed.
	 * 
	 * @param selection
	 *            currently selected object.
	 */
	void selectionChanged(Object selection);
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Visual representation of graph parts.
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Visual representation of a graph vertex.
     */
    private static class VisualVertex {
	Vertex vertex;
	int x;
	int y;
	String label;
	Shape shape;
    }

    /**
     * Visual representation of a graph edge.
     * 
     */
    private static class VisualEdge {
	Edge edge;
	VisualVertex source;
	VisualVertex target;

	Shape shape;
	String label;
	boolean isStraight;
	double anchorX;
	double anchorY;
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // GraphListener
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Listener for receiving notifications about changes in the associated
     * graph.
     */
    private class GraphListener implements Graph.GraphListener {

	@Override
	public void graphChanged(Graph graph) {
	    EventQueue.invokeLater(new Runnable() {
		@Override
		public void run() {
		    dirtyGraphStructure = true;
		    repaint();
		}
	    });
	}

	@Override
	public void edgeChanged(final Edge edge) {
	    EventQueue.invokeLater(new Runnable() {
		@Override
		public void run() {
		    VisualEdge ve = edges.get(edge);
		    if (ve != null) {
			ve.label = createEdgeLabel(edge);
		    }
		    repaint();
		}
	    });
	}

	@Override
	public void vertexChanged(final Vertex vertex) {
	    EventQueue.invokeLater(new Runnable() {
		@Override
		public void run() {
		    VisualVertex vv = vertices.get(vertex);
		    if (vv != null) {
			vv.label = createVertexLabel(vertex);
		    }
		    repaint();
		}
	    });
	}
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Configuration constants
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Initial dimensions of the panel.
     */
    private static Dimension DEFAULT_DIMENSION = new Dimension(640, 480);

    /**
     * Padding around vertex label.
     */
    private static final int LABEL_PADDING = 3;

    /**
     * Rounding of vertex boxes.
     */
    private static final int LABEL_ROUNDING = 10;

    /**
     * Diameter of filled circle used for visualizing a vertex without a label.
     */
    private static final int NO_LABEL_DIAMETER = 25;

    /**
     * Distance of midpoint of a curved edge from the line segment joining the
     * vertices.
     */
    private static final int CURVED_EDGE_CP_DISTANCE = 10;

    /**
     * Distance of edge weight from the midpoint of the edge.
     */
    private static final int EDGE_WEIGHT_DISTANCE = 15;

    /**
     * Maximal distance from the edge (or its midpoint) that is accepted as
     * click zone for selection this edge.
     */
    private static final int SELECTION_DISTANCE = 5;

    /**
     * Padding of the pane.
     */
    private static final int PANE_BORDER = 15;

    /**
     * Font for printing label of vertices.
     */
    private static final Font VERTEX_LABEL_FONT = new Font(null, Font.PLAIN, 13);

    /**
     * Color of the vertex label.
     */
    private static final Color VERTEX_LABEL_COLOR = Color.black;

    /**
     * Font for printing weights of edges.
     */
    private static final Font EDGE_WEIGHT_FONT = new Font(null, Font.PLAIN, 13);

    /**
     * Color of the selected vertex.
     */
    private static final Color SELECTED_VERTEX_COLOR = new Color(255, 243, 147);

    /**
     * Color of the selected edge.
     */
    private static final Color SELECTED_EDGE_COLOR = new Color(255, 170, 10);

    /**
     * Default color of a vertex.
     */
    private static final Color DEFAULT_VERTEX_COLOR = new Color(234, 234, 234);

    /**
     * Default color of an edge.
     */
    private static final Color DEFAULT_EDGE_COLOR = Color.black;

    /**
     * Stroke used to draw border of a vertex or line representing an edge.
     */
    private static final Stroke DEFAULT_STROKE = new BasicStroke();

    /**
     * Stroke used to draw border of a selected vertex or line representing a
     * selected edge.
     */
    private static final Stroke SELECTED_STROKE = new BasicStroke(2);

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Instance variables
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * The visualized graph.
     */
    final private Graph graph;

    /**
     * Indicates whether the graph is directed or undirected.
     */
    private boolean isDirectedGraph;

    /**
     * Indicates whether weights of edges are displayed.
     */
    private boolean showWeights = true;

    /**
     * Mapping of vertices to their visual representations.
     */
    final private Map<Vertex, VisualVertex> vertices = new HashMap<Vertex, VisualVertex>();

    /**
     * Mapping of edges to their visual representations.
     */
    final private Map<Edge, VisualEdge> edges = new HashMap<Edge, VisualEdge>();

    /**
     * Currently dragged vertex.
     */
    private VisualVertex draggedVertex;

    /**
     * Relative position of the drag point to the center of the dragged vertex.
     */
    private Point dragAnchor;

    /**
     * Currently selected vertex.
     */
    private VisualVertex selectedVertex;

    /**
     * Currently selected edge.
     */
    private VisualEdge selectedEdge;

    /**
     * Preferences for visualization colors for parts of the object.
     */
    private final Map<Object, Color> visualizationColors = new WeakHashMap<Object, Color>();

    /**
     * Currently selected part of the graph (vertex or edge).
     */
    private Object selectedObject;

    /**
     * Indicates that the visual representation of the graph does not correspond
     * to real structure of the graph.
     */
    private boolean dirtyGraphStructure = false;

    /**
     * Registered listener for receiving notifications about change of the
     * selected part of the graph.
     */
    private SelectionListener selectionListener;

    /**
     * Polygon utilized to draw arrows of the directed edges.
     */
    private final Polygon arrowHead = new Polygon();

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Constructor and initialization
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    public GraphPanel(Graph graph) {
	this.graph = graph;
	setSize(DEFAULT_DIMENSION.width, DEFAULT_DIMENSION.height);

	// create polygon of the midpoint arrow
	arrowHead.addPoint(0, 5);
	arrowHead.addPoint(-5, -5);
	arrowHead.addPoint(5, -5);

	// install basic handlers for GUI events
	installHandlers();

	// graph initialization
	if (graph != null) {
	    updateGraphStructure();
	    graph.addGraphListener(new GraphListener());
	}
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Simple settings
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Returns whether weights of edges are displayed.
     */
    public boolean isShowWeights() {
	return showWeights;
    }

    /**
     * Sets whether weights of edges are displayed.
     */
    public void setShowWeights(boolean showWeights) {
	this.showWeights = showWeights;
	repaint();
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Visualization settings
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
	if (obj == null)
	    return;

	if (color != null)
	    visualizationColors.put(obj, color);
	else
	    visualizationColors.remove(obj);

	repaint();
    }

    /**
     * Resets visualization color of all parts of the graph.
     */
    public void resetAllColors() {
	visualizationColors.clear();
	repaint();
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Selection
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Returns the registered selection listener.
     */
    public SelectionListener getSelectionListener() {
	return selectionListener;
    }

    /**
     * Sets the registered selection listener.
     */
    public void setSelectionListener(SelectionListener selectionListener) {
	this.selectionListener = selectionListener;
    }

    /**
     * Changes the selected objects and fires the selection event if necessary.
     */
    private void updateSelectedObject() {
	Object oldSelectedObject = selectedObject;

	selectedObject = null;
	if (selectedEdge != null)
	    selectedObject = selectedEdge.edge;

	if (selectedVertex != null)
	    selectedObject = selectedVertex.vertex;

	if ((selectionListener != null) && (selectedObject != oldSelectedObject))
	    selectionListener.selectionChanged(selectedObject);
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // GUI Handlers and Drag&Drop support
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Install handlers for mouse events.
     */
    private void installHandlers() {
	addMouseListener(new MouseAdapter() {

	    @Override
	    public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
		    Point clickPoint = e.getPoint();
		    draggedVertex = getVertexAt(clickPoint);
		    if (draggedVertex != null) {
			dragAnchor = new Point(clickPoint.x - draggedVertex.x, clickPoint.y - draggedVertex.y);
			selectedVertex = draggedVertex;
			selectedEdge = null;
		    } else {
			selectedEdge = getEdgeAt(clickPoint);
			selectedVertex = null;
		    }
		    updateSelectedObject();
		    repaint();
		}
	    }

	    @Override
	    public void mouseReleased(MouseEvent e) {
		if ((e.getButton() == MouseEvent.BUTTON1) && (draggedVertex != null)) {
		    draggedVertex.x = e.getPoint().x - dragAnchor.x;
		    draggedVertex.y = e.getPoint().y - dragAnchor.y;
		    updateVertexLocation(draggedVertex);
		    updatePreferredSize();
		    draggedVertex = null;

		    repaint();
		}
	    }
	});

	addMouseMotionListener(new MouseAdapter() {
	    @Override
	    public void mouseDragged(MouseEvent e) {
		if (draggedVertex != null) {
		    draggedVertex.x = e.getPoint().x - dragAnchor.x;
		    draggedVertex.y = e.getPoint().y - dragAnchor.y;
		    updateVertexLocation(draggedVertex);
		    repaint();
		}
	    }
	});
    }

    /**
     * Restricts location of visual vertex during dragging.
     * 
     * @param vv
     *            visual vertex whose position is restricted
     */
    private void updateVertexLocation(VisualVertex vv) {
	Rectangle2D bounds = (vv.shape != null) ? vv.shape.getBounds() : null;
	if (bounds != null) {
	    vv.x = Math.max(vv.x, (int) (bounds.getWidth() / 2));
	    vv.y = Math.max(vv.y, (int) (bounds.getHeight() / 2));
	    vv.x = Math.min(vv.x, (int) (getWidth() - bounds.getWidth() / 2));
	    vv.y = Math.min(vv.y, (int) (getHeight() - bounds.getHeight() / 2));
	}
    }

    /**
     * Updates preferred size of the panel after drag&drop in order to reflect
     * current locations of vertices.
     */
    public void updatePreferredSize() {
	int maxWidth = 0;
	int maxHeight = 0;
	for (VisualVertex vv : vertices.values())
	    if (vv.shape != null) {
		Rectangle2D bounds = vv.shape.getBounds();
		maxWidth = Math.max(maxWidth, (int) bounds.getMaxX());
		maxHeight = Math.max(maxHeight, (int) bounds.getMaxY());
	    }

	setPreferredSize(new Dimension(maxWidth + PANE_BORDER, maxHeight + PANE_BORDER));
    }

    /**
     * Returns the visual vertex that is located at given position.
     * 
     * @param point
     *            the position
     * @return the visual vertex at given position or null, if no visual vertex
     *         is located at that position.
     */
    private VisualVertex getVertexAt(Point point) {
	VisualVertex result = null;
	for (VisualVertex vv : vertices.values())
	    if ((vv.shape != null) && (vv.shape.contains(point)))
		result = vv;

	return result;
    }

    /**
     * Returns the visual edge that is at given position (for curved edges the
     * position is considered with respect to the position of the midpoint
     * arrow).
     * 
     * @param point
     *            the position
     * @return the visual edge that is at given position or null, if no edge is
     *         at given position.
     */
    private VisualEdge getEdgeAt(Point point) {
	double px = point.getX();
	double py = point.getY();

	VisualEdge closestEdge = null;
	double bestDist = java.lang.Double.POSITIVE_INFINITY;
	double currentDist;
	for (VisualEdge ve : edges.values())
	    if (ve.shape != null) {
		if (ve.shape instanceof Line2D) {
		    currentDist = ((Line2D) ve.shape).ptSegDist(px, py);
		} else {
		    currentDist = point.distance(ve.anchorX, ve.anchorY);
		}

		if (currentDist < bestDist) {
		    bestDist = currentDist;
		    closestEdge = ve;
		}
	    }

	return (bestDist < SELECTION_DISTANCE) ? closestEdge : null;
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Changes of graph structure.
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Updates the graph structure.
     */
    private void updateGraphStructure() {
	isDirectedGraph = graph.isDirected();

	// check for removed or new vertices
	Set<Vertex> removedVertices = new HashSet<Vertex>(vertices.keySet());
	removedVertices.removeAll(graph.getVertices());
	Set<Vertex> newVertices = new HashSet<Vertex>(graph.getVertices());
	newVertices.removeAll(vertices.keySet());
	vertices.keySet().removeAll(removedVertices);

	boolean isFirstLayout = (vertices.size() == 0);
	for (Vertex v : newVertices) {
	    VisualVertex vv = new VisualVertex();
	    vv.vertex = v;
	    if (!isFirstLayout) {
		Point newLocation = getBestNewVertexLocation();
		if (newLocation != null) {
		    vv.x = newLocation.x;
		    vv.y = newLocation.y;
		} else {
		    vv.x = (int) (Math.random() * getWidth());
		    vv.y = (int) (Math.random() * getHeight());
		}
	    }
	    vv.label = createVertexLabel(v);
	    vertices.put(v, vv);
	}

	if (isFirstLayout)
	    arrangeVerticesInCircle();

	// check for removed or new edges
	Set<Edge> removedEdges = new HashSet<Edge>(edges.keySet());
	removedEdges.removeAll(graph.getEdges());
	Set<Edge> newEdges = new HashSet<Edge>(graph.getEdges());
	newEdges.removeAll(edges.keySet());
	edges.keySet().removeAll(removedEdges);

	for (Edge e : newEdges) {
	    VisualEdge ve = new VisualEdge();
	    ve.edge = e;
	    ve.source = vertices.get(e.getSource());
	    ve.target = vertices.get(e.getTarget());
	    ve.label = createEdgeLabel(e);
	    edges.put(e, ve);
	}

	// structural change can influence drawing of edges
	if (isDirectedGraph) {
	    for (VisualEdge ve : edges.values())
		ve.isStraight = !graph.hasEdge(ve.edge.getTarget(), ve.edge.getSource());
	} else {
	    for (VisualEdge ve : edges.values())
		ve.isStraight = true;
	}

	// check whether structural change caused removal of the selected object
	if (selectedObject != null) {
	    boolean removeSelection = false;
	    if (selectedObject instanceof Edge) {
		if (((Edge) selectedObject).getGraph() == null)
		    removeSelection = true;
	    }

	    if (selectedObject instanceof Vertex) {
		if (((Vertex) selectedObject).getGraph() == null)
		    removeSelection = true;
	    }

	    if (removeSelection) {
		selectedEdge = null;
		selectedVertex = null;
		updateSelectedObject();
	    }
	}

	dirtyGraphStructure = false;
	updatePreferredSize();
    }

    /**
     * Computes the location for a new vertex.
     * 
     * @return
     */
    private Point getBestNewVertexLocation() {
	int gridSize = Math.max(40 - vertices.size(), 5);
	int xSize = getWidth() / gridSize;
	int ySize = getHeight() / gridSize;

	Point bestLocation = null;
	double bestLocationDistance = 0;
	for (int x = 1; x < xSize - 1; x++) {
	    for (int y = 1; y < ySize - 1; y++) {
		Point testPoint = new Point(x * gridSize, y * gridSize);
		double bestDist = java.lang.Double.POSITIVE_INFINITY;
		for (VisualVertex vv : vertices.values())
		    bestDist = Math.min(bestDist, testPoint.distance(vv.x, vv.y));

		for (VisualEdge ve : edges.values())
		    bestDist = Math.min(bestDist, testPoint.distance(ve.anchorX, ve.anchorY));

		bestDist = Math.min(bestDist, testPoint.x);
		bestDist = Math.min(bestDist, testPoint.y);

		if (bestDist >= bestLocationDistance) {
		    bestLocationDistance = bestDist;
		    bestLocation = testPoint;
		}
	    }
	}

	return bestLocation;
    }

    /**
     * Arranges the vertices of the graph to a circle.
     */
    private void arrangeVerticesInCircle() {
	if (vertices.size() == 0)
	    return;

	double centerX = getWidth() / 2;
	double centerY = getHeight() / 2;
	double radius = 0.8 * Math.min(centerX, centerY);
	double step = 2 * Math.PI / vertices.size();
	double angle = 0;

	for (VisualVertex vv : vertices.values()) {
	    vv.x = (int) Math.round(centerX + Math.sin(angle) * radius);
	    vv.y = (int) Math.round(centerY + Math.cos(angle) * radius);
	    angle += step;
	}
    }

    /**
     * Constructs the label for a given vertex.
     */
    private String createVertexLabel(Vertex v) {
	if (v == null)
	    return null;

	String vertexLabel = v.getLabel();
	if (vertexLabel == null)
	    return null;

	vertexLabel = vertexLabel.trim();
	if (vertexLabel.length() == 0)
	    return null;
	else
	    return vertexLabel;
    }

    /**
     * Constructs the label for a given edge (usually the label is weight of the
     * edge).
     */
    private String createEdgeLabel(Edge e) {
	if (e == null)
	    return null;

	return showWeights ? java.lang.Double.toString(e.getWeight()) : null;
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Painting
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
	if (dirtyGraphStructure)
	    updateGraphStructure();

	Graphics2D g2 = (Graphics2D) g;
	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	Rectangle clip = g.getClipBounds();
	if (clip == null)
	    clip = new Rectangle(getWidth(), getHeight());

	g2.setPaint(Color.white);
	g2.fill(clip);

	// draw edges
	for (VisualEdge ve : edges.values())
	    drawEdge(ve, g2);

	// draw vertices
	for (VisualVertex vv : vertices.values())
	    drawVertex(vv, g2);
    }

    /**
     * Draws a vertex.
     */
    private void drawVertex(VisualVertex vv, Graphics2D g2) {
	String label = vv.label;

	// draw vertex with label
	if ((label != null) && (label.length() != 0)) {
	    g2.setFont(VERTEX_LABEL_FONT);
	    FontMetrics fm = g2.getFontMetrics();
	    Rectangle2D bounds = fm.getStringBounds(label, g2);
	    Rectangle labelRect = new Rectangle((int) (vv.x - bounds.getWidth() / 2),
		    (int) (vv.y - bounds.getHeight() / 2), (int) bounds.getWidth(), (int) bounds.getHeight());

	    double labelCaseWidth = Math.max(labelRect.getWidth(), labelRect.getHeight()) + 2 * LABEL_PADDING;
	    double labelCaseHeight = labelRect.getHeight() + 2 * LABEL_PADDING;

	    vv.shape = new RoundRectangle2D.Double(vv.x - labelCaseWidth / 2, vv.y - labelCaseHeight / 2,
		    labelCaseWidth, labelCaseHeight, LABEL_ROUNDING, LABEL_ROUNDING);

	    if (vv == selectedVertex) {
		g2.setPaint(SELECTED_VERTEX_COLOR);
	    } else {
		Color preferredColor = visualizationColors.get(vv.vertex);
		if (preferredColor != null)
		    g2.setPaint(preferredColor);
		else
		    g2.setPaint(DEFAULT_VERTEX_COLOR);
	    }

	    g2.fill(vv.shape);

	    if (vv == selectedVertex) {
		g2.setStroke(SELECTED_STROKE);
	    } else {
		g2.setStroke(DEFAULT_STROKE);
	    }

	    g2.setPaint(Color.black);
	    g2.draw(vv.shape);

	    g2.setColor(VERTEX_LABEL_COLOR);
	    g2.drawString(label, labelRect.x, vv.y + (int) (bounds.getHeight() / 2) - fm.getDescent());
	} else {
	    vv.shape = new Ellipse2D.Double(vv.x - NO_LABEL_DIAMETER / 2, vv.y - NO_LABEL_DIAMETER / 2,
		    NO_LABEL_DIAMETER, NO_LABEL_DIAMETER);

	    if (vv == selectedVertex) {
		g2.setPaint(SELECTED_VERTEX_COLOR);
	    } else {
		g2.setPaint(DEFAULT_VERTEX_COLOR);
	    }

	    g2.fill(vv.shape);

	    if (vv == selectedVertex) {
		g2.setStroke(SELECTED_STROKE);
	    } else {
		g2.setStroke(DEFAULT_STROKE);
	    }

	    g2.setPaint(Color.black);
	    g2.draw(vv.shape);
	}
    }

    /**
     * Draws an edge.
     */
    private void drawEdge(VisualEdge ve, Graphics2D g2) {
	double centerX = (ve.source.x + ve.target.x) / 2;
	double centerY = (ve.source.y + ve.target.y) / 2;
	double dx = ve.target.x - ve.source.x;
	double dy = ve.target.y - ve.source.y;

	if ((dx == 0) && (dy == 0)) {
	    ve.shape = null;
	    return;
	}

	double norm = Math.sqrt(dx * dx + dy * dy);
	double dxNormed = dx / norm;
	double dyNormed = dy / norm;
	double labelX, labelY;

	if (ve.isStraight) {
	    ve.anchorX = centerX;
	    ve.anchorY = centerY;
	    labelX = centerX - EDGE_WEIGHT_DISTANCE * dyNormed;
	    labelY = centerY + EDGE_WEIGHT_DISTANCE * dxNormed;

	    ve.shape = new Line2D.Double(ve.source.x, ve.source.y, ve.target.x, ve.target.y);
	} else {
	    ve.anchorX = centerX - (CURVED_EDGE_CP_DISTANCE * dyNormed);
	    ve.anchorY = centerY + (CURVED_EDGE_CP_DISTANCE * dxNormed);
	    labelX = centerX - (CURVED_EDGE_CP_DISTANCE + EDGE_WEIGHT_DISTANCE) * dyNormed;
	    labelY = centerY + (CURVED_EDGE_CP_DISTANCE + EDGE_WEIGHT_DISTANCE) * dxNormed;

	    ve.shape = new QuadCurve2D.Double(ve.source.x, ve.source.y, centerX - 2 * dyNormed
		    * CURVED_EDGE_CP_DISTANCE, centerY + 2 * dxNormed * CURVED_EDGE_CP_DISTANCE, ve.target.x,
		    ve.target.y);
	}

	Color edgeColor = visualizationColors.get(ve.edge);
	if (edgeColor == null)
	    edgeColor = DEFAULT_EDGE_COLOR;

	if (ve == selectedEdge) {
	    edgeColor = SELECTED_EDGE_COLOR;
	    g2.setStroke(SELECTED_STROKE);
	} else {
	    g2.setStroke(DEFAULT_STROKE);
	}

	g2.setPaint(edgeColor);
	g2.draw(ve.shape);

	if (isDirectedGraph)
	    drawArrow(ve.anchorX, ve.anchorY, dx, dy, g2);

	if ((ve.label != null) && (showWeights))
	    printWeight(labelX, labelY, ve.label, edgeColor, g2);
    }

    /**
     * Draws a midpoint arrow.
     */
    private void drawArrow(double x, double y, double dx, double dy, Graphics2D g2) {
	double angle = Math.atan2(dy, dx);
	Graphics2D g = (Graphics2D) g2.create();
	g.translate(x, y);
	g.rotate(angle - Math.PI / 2d);
	g.fill(arrowHead);
	g.dispose();
    }

    /**
     * Prints weight of a edge.
     */
    private void printWeight(double x, double y, String label, Color edgeColor, Graphics2D g2) {
	g2.setFont(EDGE_WEIGHT_FONT);
	g2.setColor(edgeColor);
	FontMetrics fm = g2.getFontMetrics();
	Rectangle2D bounds = fm.getStringBounds(label, g2);
	g2.drawString(label, (float) (x - bounds.getWidth() / 2),
		(float) (y + bounds.getHeight() / 2 - fm.getDescent()));
    }
}
