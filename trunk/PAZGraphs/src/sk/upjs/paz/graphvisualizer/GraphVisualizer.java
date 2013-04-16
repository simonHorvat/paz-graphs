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

import java.awt.Color;
import java.awt.EventQueue;
import java.util.*;
import java.util.concurrent.Semaphore;

import javax.swing.UIManager;

import sk.upjs.paz.graph.*;

/**
 * Graph (algorithms) visualizer. This class represents a facade to the
 * visualization and monitoring components.
 */
public class GraphVisualizer {

    /**
     * Graph that is visualized by the visualizer.
     */
    private final Graph graph;

    /**
     * Frame that visualizes the graph.
     */
    private GraphFrame graphFrame;

    /**
     * Constructs a visualizer for a graph.
     * 
     * @param graph
     *            the visualized graph.
     */
    public GraphVisualizer(Graph graph) {
	if (graph == null)
	    throw new NullPointerException("No reference to a graph is provided.");

	this.graph = graph;
	showGraphFrame();
    }

    /**
     * Gets a graph that is visualized by this graph visualizer.
     * 
     * @return a graph visualized by this visualizer.
     */
    public Graph getGraph() {
	return graph;
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Monitoring
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Adds a map to the visual monitor.
     * 
     * @param name
     *            the name of the map in the visual monitor.
     * @param map
     *            the map to be inspected.
     */
    public void addToMonitor(String name, Map<? extends Object, ? extends Object> map) {
	monitorObject(name, map);
    }

    /**
     * Adds a collection to the visual monitor.
     * 
     * @param name
     *            the name of the collection in the visual monitor.
     * @param list
     *            the collection to be inspected.
     */
    public void addToMonitor(String name, Collection<? extends Object> list) {
	monitorObject(name, list);
    }

    /**
     * Adds an array to the visual monitor.
     * 
     * @param name
     *            the name of the array in the visual monitor.
     * @param array
     *            the array to be inspected.
     */
    public void addToMonitor(String name, int[] array) {
	monitorObject(name, array);
    }

    /**
     * Adds an array to the visual monitor.
     * 
     * @param name
     *            the name of the array in the visual monitor.
     * @param array
     *            the array to be inspected.
     */
    public void addToMonitor(String name, short[] array) {
	monitorObject(name, array);
    }

    /**
     * Adds an array to the visual monitor.
     * 
     * @param name
     *            the name of the array in the visual monitor.
     * @param array
     *            the array to be inspected.
     */
    public void addToMonitor(String name, byte[] array) {
	monitorObject(name, array);
    }

    /**
     * Adds an array to the visual monitor.
     * 
     * @param name
     *            the name of the array in the visual monitor.
     * @param array
     *            the array to be inspected.
     */
    public void addToMonitor(String name, boolean[] array) {
	monitorObject(name, array);
    }

    /**
     * Adds an array to the visual monitor.
     * 
     * @param name
     *            the name of the array in the visual monitor.
     * @param array
     *            the array to be inspected.
     */
    public void addToMonitor(String name, char[] array) {
	monitorObject(name, array);
    }

    /**
     * Adds an array to the visual monitor.
     * 
     * @param name
     *            the name of the array in the visual monitor.
     * @param array
     *            the array to be inspected.
     */
    public void addToMonitor(String name, double[] array) {
	monitorObject(name, array);
    }

    /**
     * Adds an array to the visual monitor.
     * 
     * @param name
     *            the name of the array in the visual monitor.
     * @param array
     *            the array to be inspected.
     */
    public void addToMonitor(String name, float[] array) {
	monitorObject(name, array);
    }

    /**
     * Removes an object from the visual monitor.
     * 
     * @param monitoredObject
     *            the monitored object.
     */
    public void removeFromMonitor(final Object monitoredObject) {
	if (monitoredObject == null)
	    return;

	EventQueue.invokeLater(new Runnable() {
	    public void run() {
		try {
		    if (graphFrame != null)
			graphFrame.removeFromMonitor(monitoredObject);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	});
    }

    /**
     * Adds a new object to visual monitor.
     * 
     * @param name
     *            the name of the monitored object in the monitor.
     * @param monitoredObject
     *            the monitored object.
     */
    private void monitorObject(final String name, final Object monitoredObject) {
	if (monitoredObject == null)
	    return;

	EventQueue.invokeLater(new Runnable() {
	    public void run() {
		try {
		    if (graphFrame != null)
			graphFrame.addToMonitor(name, monitoredObject);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	});
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Visual settings
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Sets the color of an edge in the graph visualization.
     * 
     * @param edge
     *            the edge of the visualized graph.
     * @param color
     *            the desired color of the edge.
     */
    public void setColor(Edge edge, Color color) {
	setVisualizationColor(edge, color);
    }

    /**
     * Sets the color of a vertex in the graph visualization.
     * 
     * @param vertex
     *            the vertex of the visualized graph.
     * @param color
     *            the desired color of the vertex.
     */
    public void setColor(Vertex vertex, Color color) {
	setVisualizationColor(vertex, color);
    }

    /**
     * Resets visualization color of an edge.
     * 
     * @param edge
     *            the edge of the visualized graph.
     */
    public void resetColor(Edge edge) {
	setVisualizationColor(edge, null);
    }

    /**
     * Resets visualization color of a vertex.
     * 
     * @param vertex
     *            the vertex of the visualized graph.
     */
    public void resetColor(Vertex vertex) {
	setVisualizationColor(vertex, null);
    }

    /**
     * Resets visualization color of all parts of the graph.
     */
    public void resetAllColors() {
	EventQueue.invokeLater(new Runnable() {
	    public void run() {
		if (graphFrame != null) {
		    graphFrame.resetAllColors();
		}
	    }
	});
    }

    /**
     * Sets visualization color for an object of a graph (vertex or edge).
     * 
     * @param obj
     *            the graph object.
     * @param color
     *            the desired visualization color.
     */
    private void setVisualizationColor(final Object obj, final Color color) {
	if (obj == null)
	    return;

	EventQueue.invokeLater(new Runnable() {
	    public void run() {
		if (graphFrame != null) {
		    graphFrame.setVisualizationColor(obj, color);
		}
	    }
	});
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // Breakpoints
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Pauses execution of the current thread until the continue button is
     * clicked.
     * 
     * @param description
     *            the description of the breakpoint (for instance the
     *            description of the current state of the execution).
     */
    public void pause(final String description) {
	// pausing is not supported from the EDT thread.
	if (EventQueue.isDispatchThread()) {
	    throw new UnsupportedOperationException("Pause cannot be called from the Event Dispatch Thread.");
	}

	final Semaphore semaphore = new Semaphore(0);
	EventQueue.invokeLater(new Runnable() {
	    public void run() {
		if (graphFrame == null) {
		    semaphore.release();
		} else {
		    graphFrame.addBreakpoint(semaphore, description);
		}
	    }
	});

	try {
	    semaphore.acquire();
	} catch (InterruptedException ignore) {

	}
    }

    /**
     * Pauses execution of the current thread until the continue button is
     * clicked.
     */
    public void pause() {
	pause(null);
    }

    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    // GUI initialization
    // -----------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------

    /**
     * Initializes and displays the JFrame that visualizes the content.
     */
    private void showGraphFrame() {
	EventQueue.invokeLater(new Runnable() {
	    public void run() {
		try {
		    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		    e.printStackTrace();
		}

		try {
		    graphFrame = new GraphFrame(graph);
		    graphFrame.setVisible(true);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	});
    }
}
