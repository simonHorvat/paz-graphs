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
 * An edge of a graph represented by {@link Graph}.
 */
final public class Edge {
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Instance variables
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Graph containing this vertex.
     */
    Graph graph;

    /**
     * The source vertex of the edge.
     */
    final private Vertex source;

    /**
     * The target vertex of the edge.
     */
    final private Vertex target;

    /**
     * Map of properties of the vertex.
     */
    final Map<String, Object> properties = new HashMap<String, Object>();

    /**
     * The weight of the edge.
     */
    double weight = 1;

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
     * Constructs a new edge
     * 
     * @param g
     *            the containing graph
     * @param source
     *            the source vertex
     * @param target
     *            the target vertex
     */
    Edge(Graph g, Vertex source, Vertex target) {
	if (g == null)
	    throw new NullPointerException("Each edge should belong to a graph.");

	if (source == null)
	    throw new NullPointerException("Source of edge is not defined.");

	if (target == null)
	    throw new NullPointerException("Target of edge is not defined.");

	if (source.graph != target.graph)
	    throw new RuntimeException("Source and target must be from the same graph.");

	if (source.graph != g)
	    throw new RuntimeException("Source is a vertex from other graph.");

	if (source == target)
	    throw new RuntimeException("Loops are not allowed.");

	if (g.hasEdge(source, target))
	    throw new RuntimeException("Graph already contains such an edge.");

	graph = g;
	lock = g.lock;
	this.source = source;
	this.target = target;
    }

    /**
     * Returns the graph that contains this edge.
     * 
     * @return the grah containing this edge, or null, if the edge does not
     *         belong to a graph.
     */
    public Graph getGraph() {
	synchronized (lock) {
	    return graph;
	}
    }

    /**
     * Returns the source vertex of the edge.
     */
    public Vertex getSource() {
	synchronized (lock) {
	    checkGraph();
	    return source;
	}
    }

    /**
     * Returns the target vertex of the edge.
     */
    public Vertex getTarget() {
	synchronized (lock) {
	    checkGraph();
	    return target;
	}
    }

    /**
     * Removes the edge from the graph.
     */
    public void remove() {
	synchronized (lock) {
	    checkGraph();
	    graph.removeEdge(this);
	}
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Support for properties.
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Reads properties of the edge from a string. The string should contain the
     * properties encoded as a sequence of semicolon separated key-value pairs.
     * Each pair should use = as a separator of the key part and the value part.<br>
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
	    } else {
		try {
		    weight = Double.parseDouble(propertyPair);
		} catch (NumberFormatException e) {
		    // Chybne retazce ignorujeme
		}
	    }
	}
    }

    /**
     * Gets the value of the property associated to this edge as an integer
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
     * Gets the value of the property associated to this edge as a double value.
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
     * Gets the value of the property associated to this edge as a boolean
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
     * Gets the value of the property associated to this edge as a string
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
     * Gets the value of the property associated to this edge.
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
		graph.fireEdgeChange(this);
	}
    }

    /**
     * Returns the set of property names of this edge.
     * 
     * @return the unmodifiable set of names of all properties defined for this
     *         edge.
     */
    public Set<String> getPropertyNames() {
	synchronized (lock) {
	    return Collections.unmodifiableSet(properties.keySet());
	}
    }

    /**
     * Gets the weight of the edge.
     * 
     * @return the weight of the edge.
     */
    public double getWeight() {
	synchronized (lock) {
	    return weight;
	}
    }

    /**
     * Sets the weight of the edge.
     * 
     * @param weight
     *            the desired weight of the edge.
     */
    public void setWeight(double weight) {
	synchronized (lock) {
	    this.weight = weight;

	    if (graph != null)
		graph.fireEdgeChange(this);
	}
    }

    @Override
    public String toString() {
	checkGraph();
	if (graph.isDirected())
	    return "(" + getSource() + ", " + getTarget() + ")";
	else
	    return "{" + getSource() + ", " + getTarget() + "}";
    }

    /**
     * Checks whether this edge belongs to a graph.
     */
    private void checkGraph() {
	if (graph == null)
	    throw new RuntimeException("Edge does not belong to a graph.");
    }
}
