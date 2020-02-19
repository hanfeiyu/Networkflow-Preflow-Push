//
// @Author: Hanfei Yu
//

package algorithm;

import java.io.*;
import java.util.*;

import util.*;


public class Preflow_Push {
	
	//
	// Test interface of this class
	//
	public static void PP(SimpleGraph graph, Hashtable table) {
		// Start recording running time in ms
	    long start = System.currentTimeMillis(); 
	    
		Double maxFlow = PreflowPush(graph, table);
		
		// End recording running time in ms
		long end = System.currentTimeMillis();
		
		System.out.println("Preflow Push: \nruntime is " + (end - start) + "ms, maximum flow is " + maxFlow + "\n");
	}
	
	//
    // Search all the possible adjacent vertices lower than a given vertex,
	// then create an adjacency list of searched vertices for the given vertex
	//
    public static void updateAdjacencyList(SimpleGraph g, Vertex v) {
    	// Clear the adjacency list and set current to the start 	
    	v.adjacencyList.clear();
    	v.current = 0;
    	
    	// Update the adjacency list of v
    	for (int i=0; i<v.incidentEdgeList.size(); i++) {
    		Edge e = (Edge) v.incidentEdgeList.get(i);
    		Vertex w = g.opposite(v, e);
    		
    		// If height of w is 1 less than v
    		if ((int) v.getHeight() == (int) (w.getHeight() + 1) ) {
    			// If e is a forward edge
    			if (g.direction(v, w)
    					// For forward edge, if there is room for flow to increase
    					&& (double) e.getFlow() < (double) e.getData()) {
    				v.adjacencyList.add(w);
    			}
    			// If e is a backward edge
    			else if (!g.direction(v, w) 
    					// For backward edge, if there is room for flow to decrease
    					&& (double) e.getFlow() > 0.0) {
    				v.adjacencyList.add(w);
    			}
    		}
    	}
    }
    	
    //
    // Push operation
    //
	public static boolean push(SimpleGraph g, Vertex v, Vertex w) {
		Edge e = g.findEdge(v, w);
		Double delta = 0.0;
		boolean isSaturating = false;
		
		// Forward edge
		if (g.direction(v, w)) {			
			// Update the smaller one
			if ((double) v.getExcess() < (double) ((Double)e.getData()-e.getFlow())) {
				delta = v.getExcess();
			}else {
				delta = (Double)e.getData() - e.getFlow();
				isSaturating = true;
			}
		}
		// Backward edge
		else {
			// Update the smaller one
			if ((double) v.getExcess() < (double) e.getFlow()) {
				delta = -v.getExcess();
			}else {
				delta = -e.getFlow();
				isSaturating = true;
			}
		}
			
		e.setFlow(e.getFlow() + delta);
		
		//Update excess of v and w after changing a flow
		v.updateExcess();
		w.updateExcess();
		
		// False for a nonsaturating push, true for a saturating push
		return isSaturating;
	}

	//
	// Relabel operation
	//
	public static void relabel(SimpleGraph g, Vertex v) {
		v.relabel();
		
		// Update adjacency list of v, once v is relabeled
		updateAdjacencyList(g, v);
		
		// Update adjacency list of related vertices concerning v, once v is relabeled
		for (int i=0; i<v.incidentEdgeList.size(); i++) {
			Edge e = (Edge) v.incidentEdgeList.get(i);
			Vertex w = g.opposite(v, e);
			
			// Only update vertices that have v in their adjacency lists
			if (w.adjacencyList.contains(v)) {
				updateAdjacencyList(g, w);
			}
		}
	}
	
	//
	// Preflow-Push algorithm main process
	//
	public static Double PreflowPush(SimpleGraph graph, Hashtable table) {
		LinkedList eList = graph.edgeList;
		LinkedList vList = graph.vertexList;
		
		// Use a max heap to store vertices which have positive excess
		PriorityQueue<Vertex> excessMaxHeap = new PriorityQueue<Vertex>(
				graph.numVertices(), new Comparator<Vertex>() { 
		    @Override
		    public int compare(Vertex v, Vertex w) {
		    	return w.getHeight() - v.getHeight();
		    }
		});
		
		// Initialize height for all the vertices		
		for (int i=0; i<vList.size(); i++) {
			Vertex v = (Vertex) vList.get(i);
			
			if (v.isSource()) {
				v.setHeight(graph.numVertices());
			}else {
				v.setHeight(0);
			}
		}

		// Initialize flow for all the edges
		for (int i=0; i<eList.size(); i++) {
			Edge e = (Edge) eList.get(i);
			
			if (e.getFirstEndpoint().getName().equals("s") 
					|| e.getSecondEndpoint().getName().equals("s")) {
				e.setFlow((Double) e.getData());
			}else {
				e.setFlow(0.0);
			}
		}
	
		// Update excess for all the vertices
		// Initially add positive-excess vertices to max heap 
		for (int i=0; i<vList.size(); i++) {
			Vertex v = (Vertex) vList.get(i);
			v.updateExcess();
			
			if ((double) v.getExcess() > 0.0) {
				excessMaxHeap.add(v);
			}
		}

		// Start algorithm
		while (!excessMaxHeap.isEmpty()) {
			
			Vertex v = excessMaxHeap.poll();
			
			// If v hasn't set up adjacency list yet, set it up
			if (v.adjacencyList.isEmpty()) {
				updateAdjacencyList(graph, v);
			}
			
			// Case 1: Relabel
			if ((int) v.current == v.adjacencyList.size()) {
				relabel(graph, v); 
				
				// Add v to max heap again if excess of v is positive
				if ((double) v.getExcess() > 0.0 
						&& !v.getName().equals("t") 
						&& !excessMaxHeap.contains(v)) {
					excessMaxHeap.add(v);
				}
			}
			// Case 2: Push
			else {
				Vertex w = (Vertex) v.adjacencyList.get(v.current);
				Edge e = graph.findEdge(v, w);
				boolean isSaturatingPush;
				
				isSaturatingPush = push(graph, v, w);	
				
				// Add v to max heap again if excess of v is positive
				if ((double) v.getExcess() > 0.0 
						&& !v.getName().equals("t") 
						&& !excessMaxHeap.contains(v)) {
					excessMaxHeap.add(v);
				}
				
				// Add v to max heap again if excess of v is positive
				if ((double) w.getExcess() > 0.0 
						&& !w.getName().equals("t") 
						&& !excessMaxHeap.contains(w)) {
					excessMaxHeap.add(w);
				}
				
				// If the push is a saturating push, move to next edge
				if (isSaturatingPush) {
					v.current++;
				}
			}
		}
		
		// Calculate the maximum flow
		double maxFlow = 0.0;
		Vertex s = (Vertex) table.get("s");
		
		for (int i=0; i<s.incidentEdgeList.size(); i++) {
			Edge e = (Edge) s.incidentEdgeList.get(i);
			maxFlow = maxFlow + e.getFlow();
		}
		
		return (Double) maxFlow;
	}
}
