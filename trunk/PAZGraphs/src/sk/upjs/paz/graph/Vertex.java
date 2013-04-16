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

import java.util.*;

/**
 * Vertex of a graph represented by {@link Graph}.
 */
final public class Vertex {

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Instance variables
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Label of the vertex.
     */
    private String label;

    /**
     * Graph containing this vertex.
     */
    Graph graph;

    /**
     * Map of properties of the vertex.
     */
    final Map<String, Object> properties = new HashMap<String, Object>();

    /**
     * Synchronization lock.
     */
    final Object lock;

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Construct the vertex.
     * 
     * @param g
     *            the graph
     */
    Vertex(Graph g) {
	this.graph = g;
	lock = g.lock;
    }

    /**
     * Returns the graph that contains this vertex.
     * 
     * @return the graph that contains this vertex.
     */
    public Graph getGraph() {
	synchronized (lock) {
	    return graph;
	}
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Support for vertex properties.
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Reads properties of the vertex from a string. The string should contain
     * the properties encoded as a sequence of semicolon separated key-value
     * pairs. Each pair should use = as a separator of the key part and the
     * value part.<br>
     * Example:
     * 
     * <pre>
     * min=4;max=8;description=connection
     * </pre>
     * 
     * @param properties
     *            the string encoding the properties.
     */
    public void readProperties(String properties) {
	if (properties == null)
	    return;

	synchronized (lock) {
	    // Rozdelime vstup na jednotlive pary
	    String[] propertyPairs = properties.split(";");

	    // Spracujeme kazdy par
	    for (String propertyPair : propertyPairs) {
		// Zistime poziciu znaku =
		int separatorPos = propertyPair.indexOf("=");

		// Ak par obsahuje znak =, je to korektny par, inak sa hodnotu
		// pokusime
		// interpretovat ako nastavenie vahy hrany
		if (separatorPos >= 0) {
		    String key = propertyPair.substring(0, separatorPos).trim();
		    String value = propertyPair.substring(separatorPos + 1).trim();

		    if (!key.equals(""))
			this.properties.put(key, value);
		}
	    }

	    if (graph != null)
		graph.fireVertexChange(this);
	}
    }

    /**
     * Gets the value of the property associated to this vertex as an integer
     * value.
     * 
     * @param propertyName
     *            the name of the property.
     * @return the current value of the property. If the property value is not
     *         defined, the NullPointerException is thrown.
     */
    public int getIntValue(String propertyName) {
	return Integer.parseInt(getStringValue(propertyName));
    }

    /**
     * Gets the value of the property associated to this vertex as a double
     * value.
     * 
     * @param propertyName
     *            the name of the property.
     * @return the current value of the property. If the property value is not
     *         defined, the NullPointerException is thrown.
     */
    public double getDoubleValue(String propertyName) {
	return Double.parseDouble(getStringValue(propertyName));
    }

    /**
     * Gets the value of the property associated to this vertex as a boolean
     * value.
     * 
     * @param propertyName
     *            the name of the property.
     * @return the current value of the property. If the property value is not
     *         defined, the NullPointerException is thrown.
     */
    public boolean getBooleanValue(String propertyName) {
	return Boolean.parseBoolean(getStringValue(propertyName));
    }

    /**
     * Gets the value of the property associated to this vertex as a string
     * 
     * @param propertyName
     *            the name of the property.
     * @return the current value of the property. If the property value is not
     *         defined, the NullPointerException is thrown.
     */
    public String getStringValue(String propertyName) {
	synchronized (lock) {
	    return properties.get(propertyName).toString();
	}
    }

    /**
     * Gets the value of the property associated to this vertex.
     * 
     * @param propertyName
     *            the name of the property.
     * @return the current value of the property or null, if the property value
     *         is not defined.
     */
    public Object getValue(String propertyName) {
	synchronized (lock) {
	    return properties.get(propertyName);
	}
    }

    /**
     * Sets the value of a property.
     * 
     * @param propertyName
     *            the name of the property
     * @param value
     *            the new value of the property
     */
    public void setValue(String propertyName, Object value) {
	synchronized (lock) {
	    if (value == null)
		properties.remove(propertyName);
	    else
		properties.put(propertyName, value);

	    if (graph != null)
		graph.fireVertexChange(this);
	}
    }

    /**
     * Returns the set of property names of this vertex.
     * 
     * @return the unmodifiable set of names of all properties defined for this
     *         vertex.
     */
    public Set<String> getPropertyNames() {
	synchronized (lock) {
	    return Collections.unmodifiableSet(properties.keySet());
	}
    }

    /**
     * Gets the label of the vertex.
     * 
     * @return the label of the vertex
     */
    public String getLabel() {
	synchronized (lock) {
	    return label;
	}
    }

    /**
     * Sets the label of the vertex.
     * 
     * @param label
     *            the desired label of the vertex.
     */
    public void setLabel(String label) {
	synchronized (lock) {
	    this.label = label;

	    if (graph != null)
		graph.fireVertexChange(this);
	}
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Getters and setters for graph properties.
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Removes the vertex from the graph.
     */
    public void remove() {
	synchronized (lock) {
	    checkGraph();
	    graph.removeVertex(this);
	}
    }

    /**
     * Returns the set of all outcoming edges incident to this vertex.
     * 
     * @return the unmodifiable set of all outcoming edges.
     */
    public Set<Edge> getOutEdges() {
	synchronized (lock) {
	    checkGraph();
	    return graph.getOutEdges(this);
	}
    }

    /**
     * Returns the set of all incoming edges incident to this vertex.
     * 
     * @return the unmodifiable set of all incoming edges.
     */
    public Set<Edge> getInEdges() {
	synchronized (lock) {
	    checkGraph();
	    return graph.getInEdges(this);
	}
    }

    /**
     * Returns the set of all edges incident to this vertex.
     * 
     * @return the unmodifiable set of all edges.
     */
    public Set<Edge> getEdges() {
	synchronized (lock) {
	    checkGraph();
	    return graph.getEdges(this);
	}
    }

    /**
     * Returns the set of all neighbors of this vertex.
     * 
     * @return the unmodifiable set of all neighbors.
     */
    public Set<Vertex> getNeighbours() {
	synchronized (lock) {
	    Set<Vertex> result = new HashSet<Vertex>();
	    for (Edge e : getEdges()) {
		if (e.getTarget() != this)
		    result.add(e.getTarget());

		if (e.getSource() != this)
		    result.add(e.getSource());
	    }

	    return Collections.unmodifiableSet(result);
	}
    }

    /**
     * Returns the set of all out-neighbors of this vertex.
     * 
     * @return the unmodifiable set of all out-neighbors.
     */
    public Set<Vertex> getOutNeighbours() {
	synchronized (lock) {
	    Set<Vertex> result = new HashSet<Vertex>();
	    for (Edge e : getOutEdges()) {
		if (e.getTarget() != this)
		    result.add(e.getTarget());

		if (e.getSource() != this)
		    result.add(e.getSource());
	    }

	    return Collections.unmodifiableSet(result);
	}
    }

    /**
     * Returns the set of all in-neighbors of this vertex.
     * 
     * @return the unmodifiable set of all in-neighbors.
     */
    public Set<Vertex> getInNeighbours() {
	synchronized (lock) {
	    Set<Vertex> result = new HashSet<Vertex>();
	    for (Edge e : getInEdges()) {
		if (e.getTarget() != this)
		    result.add(e.getTarget());

		if (e.getSource() != this)
		    result.add(e.getSource());
	    }

	    return Collections.unmodifiableSet(result);
	}
    }

    @Override
    public String toString() {
	synchronized (lock) {
	    if (label != null)
		return label;
	    else
		return super.toString();
	}
    }

    /**
     * Checks whether this vertex belongs to a graph.
     */
    private void checkGraph() {
	if (graph == null)
	    throw new RuntimeException("Vertex does not belong to a graph.");
    }
}
