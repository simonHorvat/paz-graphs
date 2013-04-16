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
package sk.upjs.paz.graph;

import java.io.File;
import java.util.*;

/**
 * The directed or undirected graph without loops and multiple edges.
 */
final public class Graph implements Cloneable {

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // GraphListener
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * The listener interface for receiving notification about changes in the
     * graph.
     */
    public interface GraphListener {
	/**
	 * Invoked when the structure of the graph was changed, e.g. a new
	 * vertex or edges is added or removed.
	 * 
	 * @param graph
	 *            the graph that was changed.
	 */
	void graphChanged(Graph graph);

	/**
	 * Invoked when an edge of a graph has changed, e.g. its weight or a
	 * property was changed.
	 * 
	 * @param edge
	 *            the edge that was changed.
	 */
	void edgeChanged(Edge edge);

	/**
	 * Invoked when a vertex of a graph has changed, e.g. its label or a
	 * property was changed.
	 * 
	 * @param vertex
	 *            the vertex that was changed.
	 */
	void vertexChanged(Vertex vertex);
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Instance variables.
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Set of edges of the graph.
     */
    private final Set<Edge> edges = new HashSet<Edge>();

    /**
     * Set of vertices of the graph.
     */
    private final Set<Vertex> vertices = new HashSet<Vertex>();

    /**
     * Incidency map that assigns to each vertex a map mapping a neighbor and
     * incident edge.
     */
    private final Map<Vertex, Map<Vertex, Edge>> incidencyMap = new HashMap<Vertex, Map<Vertex, Edge>>();

    /**
     * Indicated whether the graph is directed.
     */
    private boolean directed = false;

    /**
     * List of registered graph listeners.
     */
    private final List<GraphListener> listeners = new ArrayList<GraphListener>();

    /**
     * Synchronization lock.
     */
    final Object lock = new Object();

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Constructs a new graph.
     * 
     * @param directed
     *            true, if the created graph is a directed graph.
     */
    public Graph(boolean directed) {
	this.directed = directed;
    }

    /**
     * Constructs an empty undirected graph.
     */
    public Graph() {
	this(false);
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Listener support
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Adds the specified graph listener to receive graph change events from
     * this graph.
     * 
     * @param listener
     *            the graph listener.
     */
    public void addGraphListener(GraphListener listener) {
	if (listener == null)
	    return;

	synchronized (lock) {
	    if (!listeners.contains(listener)) {
		listeners.add(listener);
	    }
	}
    }

    /**
     * Removes the specified graph listener so that it no longer receives graph
     * change events from this graph.
     * 
     * @param listener
     *            the graph listener.
     */
    public void removeGraphListener(GraphListener listener) {
	if (listener == null)
	    return;

	synchronized (lock) {
	    listeners.remove(listener);
	}
    }

    /**
     * Fires the structural change event.
     */
    private void fireStructuralChange() {
	synchronized (lock) {
	    for (GraphListener listener : listeners)
		listener.graphChanged(this);

	}
    }

    /**
     * Fires the edge change event.
     */
    void fireEdgeChange(Edge e) {
	synchronized (lock) {
	    for (GraphListener listener : listeners)
		listener.edgeChanged(e);
	}
    }

    /**
     * Fires the vertex change event.
     */
    void fireVertexChange(Vertex v) {
	synchronized (lock) {
	    for (GraphListener listener : listeners)
		listener.vertexChanged(v);
	}
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Getters and setters
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Returns the order of the graph, i.e., the number of vertices of the
     * graph.
     * 
     * @return the order of the graph.
     */
    public int getOrder() {
	synchronized (lock) {
	    return vertices.size();
	}
    }

    /**
     * Returns the size of the graph, i.e., the number of edges of the graph.
     * 
     * @return the size of the graph.
     */
    public int getSize() {
	synchronized (lock) {
	    return edges.size();
	}
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Structural change of the graph.
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Creates a new vertex in the graph.
     * 
     * @param label
     *            the label of the newly created vertex.
     * @return the newly created vertex
     */
    public Vertex addVertex(String label) {
	synchronized (lock) {
	    Vertex result = new Vertex(this);
	    result.setLabel(label);

	    vertices.add(result);
	    incidencyMap.put(result, new HashMap<Vertex, Edge>());

	    fireStructuralChange();
	    return result;
	}
    }

    /**
     * Creates a new vertex in the graph.
     * 
     * @return the newly created vertex
     */
    public Vertex addVertex() {
	return addVertex(null);
    }

    /**
     * Gets the set of all vertices of the graph.
     * 
     * @return the unmodifiable set of graph vertices
     */
    public Set<Vertex> getVertices() {
	synchronized (lock) {
	    return Collections.unmodifiableSet(vertices);
	}
    }

    /**
     * Gets vertex of the graph with given label.
     * 
     * @param label
     *            the label
     * @return the vertex with given label or null, if a vertex with given label
     *         does not exist in the graph.
     */
    public Vertex getVertex(String label) {
	synchronized (lock) {
	    for (Vertex v : vertices) {
		if (v.getLabel() == label)
		    return v;

		if (label != null)
		    if (label.equals(v.getLabel()))
			return v;
	    }

	    return null;
	}
    }

    /**
     * Gets the set of outcoming edges from the vertex.
     * 
     * @param v
     *            the vertex
     * @return unmodifiable set of outcoming edges of the vertex
     */
    Set<Edge> getOutEdges(Vertex v) {
	synchronized (lock) {
	    if ((v == null) || (v.graph != this))
		return null;

	    return Collections.unmodifiableSet(new HashSet<Edge>(incidencyMap.get(v).values()));

	}
    }

    /**
     * Gets the set of incoming edges from the vertex.
     * 
     * @param v
     *            the vertex
     * @return unmodifiable set of incoming edges of the vertex
     */
    Set<Edge> getInEdges(Vertex v) {
	synchronized (lock) {
	    if ((v == null) || (v.graph != this))
		return null;

	    if (!directed)
		return getOutEdges(v);
	    else {
		Set<Edge> inEdges = new HashSet<Edge>();
		for (Edge e : edges)
		    if (e.getTarget() == v)
			inEdges.add(e);

		return Collections.unmodifiableSet(inEdges);
	    }
	}
    }

    /**
     * Gets the set of edges incident to a vertex.
     * 
     * @param v
     *            the vertex
     * @return unmodifiable set of edges incident to the vertex.
     */
    Set<Edge> getEdges(Vertex v) {
	synchronized (lock) {
	    if ((v == null) || (v.graph != this))
		return null;

	    if (!directed)
		return getOutEdges(v);
	    else {
		Set<Edge> result = new HashSet<Edge>();
		result.addAll(getInEdges(v));
		result.addAll(getOutEdges(v));
		return Collections.unmodifiableSet(result);
	    }
	}
    }

    /**
     * Returns whether there is an edge between two vertices.
     * 
     * @param source
     *            the source (starting point) of an edge
     * @param target
     *            the target (endpoint) of an edge
     * @return true, if the graph contains an edge connecting the source to the
     *         target.
     */
    public boolean hasEdge(Vertex source, Vertex target) {
	return (getEdge(source, target) != null);
    }

    /**
     * Gets an edge between two vertices.
     * 
     * @param source
     *            the source (starting point) of an edge
     * @param target
     *            the target (endpoint) of an edge
     * 
     * @return the edge from the source to the target, or null, if such an edge
     *         does not exist in the graph.
     */
    public Edge getEdge(Vertex source, Vertex target) {
	synchronized (lock) {
	    if ((source == null) || (target == null))
		return null;

	    if ((source.graph != this) || (target.graph != this))
		return null;

	    return incidencyMap.get(source).get(target);
	}
    }

    /**
     * Returns the set of all edges of the graph.
     * 
     * @return the unmodifiable set of all graph edges.
     */
    public Set<Edge> getEdges() {
	synchronized (lock) {
	    return Collections.unmodifiableSet(edges);
	}
    }

    /**
     * Creates an edge between two vertices. If the edge already exists, no new
     * edge is created and existing edge is returned.
     * 
     * @param source
     *            the source (starting point) of the edge.
     * @param target
     *            the target (endpoint) of the edge
     * @return the edge from the source to the target.
     */
    public Edge addEdge(Vertex source, Vertex target) {
	synchronized (lock) {
	    Edge result = getEdge(source, target);
	    if (result != null)
		return result;

	    if (source == target) {
		throw new UnsupportedOperationException("Loops are not allowed.");
	    }

	    result = new Edge(this, source, target);
	    incidencyMap.get(result.getSource()).put(result.getTarget(), result);

	    if (!directed)
		incidencyMap.get(result.getTarget()).put(result.getSource(), result);

	    edges.add(result);
	    fireStructuralChange();
	    return result;
	}
    }

    /**
     * Removes a vertex from the graph.
     * 
     * @param v
     *            the vertex to be removed.
     */
    public void removeVertex(Vertex v) {
	synchronized (lock) {
	    if ((v == null) || (v.graph != this))
		return;

	    List<Edge> toRemoveEdges = new ArrayList<Edge>();
	    for (Edge e : edges)
		if ((e.getSource() == v) || (e.getTarget() == v))
		    toRemoveEdges.add(e);

	    for (Edge e : toRemoveEdges)
		removeEdge(e);

	    vertices.remove(v);
	    v.graph = null;

	    fireStructuralChange();
	}
    }

    /**
     * Removes an edge from the graph.
     * 
     * @param e
     *            the edge to be removed.
     */
    public void removeEdge(Edge e) {
	synchronized (lock) {
	    if ((e == null) || (e.graph != this))
		return;

	    edges.remove(e);

	    incidencyMap.get(e.getSource()).remove(e.getTarget());
	    if (!directed)
		incidencyMap.get(e.getTarget()).remove(e.getSource());

	    e.graph = null;
	    fireStructuralChange();
	}
    }

    /**
     * Returns whether the graph is directed.
     */
    public boolean isDirected() {
	synchronized (lock) {
	    return directed;
	}
    }

    /**
     * Sets whether the graph is directed.
     * 
     * @param directed
     *            true, if the graph should be changed to the directed graph, or
     *            false, if the graph should be changed to undirected graph.
     */
    public void setDirected(boolean directed) {
	synchronized (lock) {
	    if (this.directed == directed)
		return;

	    if (directed) {
		List<Edge> undirectedEdges = new ArrayList<Edge>(edges);
		this.directed = true;
		for (Edge e : undirectedEdges) {
		    incidencyMap.get(e.getTarget()).remove(e.getSource());
		    Edge newEdge = addEdge(e.getTarget(), e.getSource());
		    newEdge.weight = e.weight;
		    newEdge.properties.putAll(e.properties);
		}
	    } else {
		List<Edge> oldEdgesList = new ArrayList<Edge>(edges);

		for (Edge e : oldEdgesList)
		    if (hasEdge(e.getTarget(), e.getSource()))
			e.remove();

		for (Edge e : edges)
		    incidencyMap.get(e.getTarget()).put(e.getSource(), e);

		this.directed = false;
	    }

	    fireStructuralChange();
	}
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Loading from files
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Načíta opis grafu zo súboru. Metóda postupne číta textový súbor a na
     * základe neho vytvára hrany.<br>
     * <b>Formát súboru:</b>
     * <ul>
     * <li>každý riadok obsahuje informáciu o jednej hrane alebo vrchole</li>
     * <li>záznam hrany: <i>od do : zoznam parametrov</i>
     * <ul>
     * <li><b>od</b> je označenie vrcholu, z ktorého hrana vychádza</li>
     * <li><b>do</b> je označenie vrcholu, do ktorého hrana vchádza</li>
     * <li>uvedenie dvojbodky a zoznamu parametrov je voliteľné</li>
     * <li>zoznam parametrov je bodkočiarkami oddelený zoznam párov kľúč=hodnota
     * </li>
     * <li>ak vrcholy z uvedenými označeniami neexistujú, tak sa vytvoria
     * </ul>
     * </li>
     * <li>záznam pre vrchol: <i>label : zoznam parametrov</i>
     * <ul>
     * <li><b>label</b> je označenie vrcholu</li>
     * <li>uvedenie dvojbodky a zoznamu parametrov je voliteľné</li>
     * <li>zoznam parametrov je bodkočiarkami oddelený zoznam párov kľúč=hodnota
     * </li>
     * <li>ak vrchol ešte neexistuje, tak sa vytvorí
     * </ul>
     * </li>
     * </ul>
     * 
     * <b>Príklad súboru:</b><br>
     * <i>KE PO : 35;typ=dialnica;kapacita=100<br>
     * KE RV : 26;typ=prva trieda;kapacita=50<br>
     * PO SB : 15;typ=druha trieda;kapacita=40<br>
     * </i>
     * 
     * @param filename
     *            názov súboru, z ktorého sa načítava
     * @return true, ak načítanie prebehlo korektne
     */
    private void loadFromFile(String filename) {
	Scanner s = null;
	try {
	    s = new Scanner(new File(filename));

	    // Postupne precitame jednotlive riadky
	    while (s.hasNextLine()) {
		String line = s.nextLine();

		String definition = line;
		String info = null;

		// Overime, ci je tam doplnujuca informacia
		int infoSeparatorPos = line.indexOf(":");
		if (infoSeparatorPos >= 0) {
		    definition = line.substring(0, infoSeparatorPos).trim();
		    info = line.substring(infoSeparatorPos + 1).trim();
		} else
		    definition = definition.trim();

		// Analyzujeme definiciu v riadku
		Scanner lineScanner = new Scanner(definition);
		String startVertexLabel = null;
		String targetVertexLabel = null;

		if (lineScanner.hasNext())
		    startVertexLabel = lineScanner.next();

		if (lineScanner.hasNext())
		    targetVertexLabel = lineScanner.next();

		lineScanner.close();

		// Ak nemame ziadnu definiciu, tak nerobime nic
		if ((startVertexLabel == null) && (targetVertexLabel == null))
		    continue;

		// Vytvorime uzly, ak neexistuju
		Vertex start = null;
		if (startVertexLabel != null) {
		    startVertexLabel = startVertexLabel.trim();
		    start = getVertex(startVertexLabel);
		    if (start == null)
			start = addVertex(startVertexLabel);
		}

		Vertex target = null;
		if (targetVertexLabel != null) {
		    targetVertexLabel = targetVertexLabel.trim();
		    target = getVertex(targetVertexLabel);
		    if (target == null)
			target = addVertex(targetVertexLabel);
		}

		// Ak obe su null, tak vkladame hranu
		if ((start != null) && (target != null)) {
		    Edge e = addEdge(start, target);
		    e.readProperties(info);
		} else
		    start.readProperties(info);
	    }
	} catch (Exception e) {
	    throw new RuntimeException("Graph cannot be loaded from the file.", e);
	} finally {
	    if (s != null)
		s.close();
	}
    }

    /**
     * Creates a new graph according to graph definition from a text file.
     * 
     * @param filename
     *            the filename of a text file with definition of a graph.
     * @param directed
     *            true, if the resulting graph is a directed graph.
     * @return the created graph.
     */
    public static Graph createFromFile(String filename, boolean directed) {
	Graph g = new Graph(directed);
	g.loadFromFile(filename);
	return g;
    }

    /**
     * Creates a new undirected graph according to graph definition from a text
     * file.
     * 
     * @param filename
     *            the filename of a text file with definition of a graph.
     * @return the created undirected graph.
     */
    public static Graph createFromFile(String filename) {
	return createFromFile(filename, false);
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Cloning
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    @Override
    public Graph clone() {
	synchronized (lock) {
	    Graph result = new Graph();
	    result.setDirected(isDirected());

	    Map<Vertex, Vertex> binding = new HashMap<Vertex, Vertex>();

	    for (Vertex v : vertices) {
		Vertex vertexCopy = result.addVertex(v.getLabel());
		vertexCopy.properties.putAll(v.properties);
		binding.put(v, vertexCopy);
	    }

	    for (Edge e : edges) {
		Edge edgeCopy = result.addEdge(binding.get(e.getSource()), binding.get(e.getTarget()));
		edgeCopy.properties.putAll(e.properties);
		edgeCopy.setWeight(e.getWeight());
	    }

	    return result;
	}
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // ToString
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    @Override
    public String toString() {
	synchronized (lock) {
	    return "[Vertices: " + vertices + " Edges: " + edges + "]";
	}
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Useful helper methods.
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Creates a map with the vertices of the graph as keys. All keys are mapped
     * to the given value.
     * 
     * @param value
     *            the value
     * @return the map
     */
    public <T> Map<Vertex, T> createVertexMap(T value) {
	synchronized (lock) {
	    Map<Vertex, T> result = new HashMap<Vertex, T>();
	    for (Vertex v : getVertices()) {
		result.put(v, value);
	    }

	    return result;
	}
    }

    /**
     * Creates a map with the edges of the graph as keys. All keys are mapped to
     * the given value.
     * 
     * @param value
     *            the value
     * @return the map
     */
    public <T> Map<Edge, T> createEdgeMap(T value) {
	synchronized (lock) {
	    Map<Edge, T> result = new HashMap<Edge, T>();
	    for (Edge e : getEdges()) {
		result.put(e, value);
	    }

	    return result;
	}
    }

    /**
     * Creates an array containing all vertices of the graph.
     * 
     * @return the array with all vertices of the graph.
     */
    public Vertex[] createVertexArray() {
	synchronized (lock) {
	    Vertex[] result = new Vertex[vertices.size()];
	    int idx = 0;
	    for (Vertex v : vertices) {
		result[idx] = v;
		idx++;
	    }

	    return result;
	}
    }

    /**
     * Creates an adjacency matrix for the induced subgraph formed by vertices
     * given in the array.
     * 
     * @param vertices
     *            the array of vertices that specify the induced subgraph.
     * @return the adjacency matrix.
     * 
     */
    public boolean[][] createAdjacencyMatrix(Vertex[] vertices) {
	if (vertices == null)
	    return null;

	synchronized (lock) {
	    Set<Vertex> subset = new HashSet<Vertex>();
	    for (Vertex v : vertices) {
		if (v == null)
		    throw new RuntimeException("The array of vertices cannot contain the null value.");

		if (v.getGraph() != this)
		    throw new RuntimeException(
			    "The array of vertices cannot contain a vertex that does not belong to this graph.");

		if (!subset.add(v))
		    throw new RuntimeException("The array of vertices contains duplicated vertices.");
	    }

	    boolean[][] result = new boolean[vertices.length][vertices.length];
	    for (int i = 0; i < vertices.length; i++)
		for (int j = 0; j < vertices.length; j++)
		    result[i][j] = hasEdge(vertices[i], vertices[j]);

	    return result;
	}
    }

    /**
     * Creates an adjacency matrix with weights for the induced subgraph formed
     * by vertices given in the array.
     * 
     * @param vertices
     *            the array of vertices that specify the induced subgraph.
     * 
     * @param noEdgeValue
     *            the weight value that encodes that an edge is not present.
     * @return the adjacency matrix with weights.
     * 
     */
    public double[][] createWeightedAdjacencyMatrix(Vertex[] vertices, double noEdgeValue) {
	if (vertices == null)
	    return null;

	synchronized (lock) {
	    Set<Vertex> subset = new HashSet<Vertex>();
	    for (Vertex v : vertices) {
		if (v == null)
		    throw new RuntimeException("The array of vertices cannot contain the null value.");

		if (v.getGraph() != this)
		    throw new RuntimeException(
			    "The array of vertices cannot contain a vertex that does not belong to this graph.");

		if (!subset.add(v))
		    throw new RuntimeException("The array of vertices contains duplicated vertices.");
	    }

	    double[][] result = new double[vertices.length][vertices.length];
	    for (int i = 0; i < vertices.length; i++)
		for (int j = 0; j < vertices.length; j++) {
		    Edge e = getEdge(vertices[i], vertices[j]);
		    result[i][j] = (e != null) ? e.getWeight() : noEdgeValue;
		}

	    return result;
	}
    }

    /**
     * Creates an adjacency matrix with weights for the induced subgraph formed
     * by vertices given in the array. The value Double.POSITIVE_INFINITY
     * encodes that an edge is not present.
     * 
     * @param vertices
     *            the array of vertices that specify the induced subgraph.
     * 
     * @return the adjacency matrix with weights.
     * 
     */
    public double[][] createWeightedAdjacencyMatrix(Vertex[] vertices) {
	return createWeightedAdjacencyMatrix(vertices, Double.POSITIVE_INFINITY);
    }

    /**
     * Sort the list of edges according to their weights.
     * 
     * @param edgeList
     *            the list of graph edges.
     */
    public static void sortEdgesByWeight(List<Edge> edgeList) {
	Collections.sort(edgeList, new Comparator<Edge>() {
	    public int compare(Edge e1, Edge e2) {
		return Double.compare(e1.getWeight(), e2.getWeight());
	    }
	});
    }

    /**
     * Creates an undirected random graph with given number of vertices.
     * 
     * @param order
     *            the desired number of vertices.
     * @param size
     *            the maximum number of edges.
     * @return the random graph.
     */
    public static Graph createRandomGraph(int order, int size) {
	Vertex[] vertices = new Vertex[order];
	Graph g = new Graph();
	for (int i = 0; i < order; i++)
	    vertices[i] = g.addVertex(Integer.toString(i));

	int maxEdges = (order * (order - 1)) / 2;
	if (size > maxEdges)
	    size = maxEdges;

	for (int i = 0; i < size; i++) {
	    int source = (int) (Math.random() * order);
	    int target = (int) (Math.random() * order);
	    if (source != target) {
		g.addEdge(vertices[source], vertices[target]);
	    }
	}

	return g;
    }
}
