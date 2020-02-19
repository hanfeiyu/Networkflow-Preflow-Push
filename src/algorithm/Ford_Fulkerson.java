//
// @Author: Feifei Zhang
//

package algorithm;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import util.*;


public class Ford_Fulkerson {

	//static final String getPath = "F:\\JAVA\\JDK11\\AA_final\\src\\input.txt";
	
	public static void FF(SimpleGraph graph, Hashtable table) {
		Maxflow(toFlowGf(graph));
	}
	
	@SuppressWarnings("unchecked")
	public static Double Maxflow(SimpleGraph flowGf) {
		Long startTime = System.currentTimeMillis();
		Double maxFlow = 0.0;
		Double flow = 0.0, t = 0.0;
		ArrayList<String> path;
		HashMap<String, Vertex> vertices = getNode(flowGf);
		for (Iterator<Edge> iterator = flowGf.incidentEdges(vertices.get("s")); iterator.hasNext();) {
			if ((t = ((Double[]) iterator.next().getData())[0]) > flow) {
				flow = t;
			} 
			  
		}
		SimpleGraph residualGf = toResidualGf(flowGf);
		SimpleGraph produceGf = produceGf(residualGf);
		while (null != (path = augmentPath(produceGf))) {
			Double bottleneck = getBottleneck(produceGf, path); 
			maxFlow += bottleneck;
			updateflowGf(flowGf, path, bottleneck);
			residualGf = toResidualGf(flowGf);
			produceGf = produceGf(residualGf);
		}
		System.out.println("Ford Fulkerson: \nruntime is " + (System.currentTimeMillis() - startTime) + "ms, maximum flow is " + maxFlow + "\n");
		return maxFlow;
	}
	
	
	@SuppressWarnings("unchecked")
	private static HashMap<String, Vertex> getNode(SimpleGraph flowGraph) {
		HashMap<String, Vertex> map = new HashMap<>();
		for (Iterator<Vertex> iterator = flowGraph.vertices(); iterator.hasNext();) {
			Vertex v = iterator.next();
			map.put((String) v.getName(), v);
		}
		return map;
	}
	
	@SuppressWarnings("unchecked")
	private static ArrayList<String> augmentPath(SimpleGraph residualGf) {
		ArrayList<String> list = new ArrayList<>();
		HashMap<String, Vertex> vertices = getNode(residualGf);
		if (null == vertices.get("s") || null == vertices.get("t")) {
			return list;
		}

		Queue<Vertex> queue = new LinkedList<Vertex>();
		HashMap<String, String> hm = new HashMap<>();
		queue.add(vertices.get("s"));
		hm.put("s", null);
		while (!queue.isEmpty()) {
			Vertex v = queue.poll();
			for (Iterator<Edge> iterator = residualGf.incidentEdges(v); iterator.hasNext();) {
				Edge e = iterator.next();
				if (e.getSecondEndpoint().getName() == "t") {
					hm.put("t", (String) v.getName());
					break;
				} else {
					if (!hm.containsKey((String) e.getSecondEndpoint().getName())) {
						hm.put((String) e.getSecondEndpoint().getName(), (String) v.getName());
						queue.add(e.getSecondEndpoint());
					}
				}
			}
		}
		if (!hm.containsKey("t")) {
			return null;
		}
		String currentV = "t";
		while (hm.get(currentV) != null) {
			list.add(currentV);
			currentV = hm.get(currentV);
		}
		list.add("s");
		Collections.reverse(list);
		return list;
	}
	
	@SuppressWarnings("unchecked")
	private static Double getBottleneck(SimpleGraph residualGf, List<String> path) {
		HashMap<String, Vertex> residualGraphVertices = getNode(residualGf);
		Double bottleneck = Double.MAX_VALUE;
		for (int i = 0; i < path.size() - 1; i++) {
			Vertex v = residualGraphVertices.get(path.get(i));
			for (Iterator<Edge> iterator = residualGf.incidentEdges(v); iterator.hasNext();) {
				Edge e;
				if ((e = iterator.next()).getSecondEndpoint().getName().equals(path.get(i + 1))) {
					bottleneck = bottleneck > ((Double[]) e.getData())[0] ? ((Double[]) e.getData())[0] : bottleneck;
				}
			}
		}
		return bottleneck;
	}
	
	
	@SuppressWarnings("unchecked")
	private static SimpleGraph toResidualGf(SimpleGraph flowGf) {
		SimpleGraph residualGf = new SimpleGraph();
		for (Iterator<Vertex> iterator = flowGf.vertices(); iterator.hasNext();) {
			Vertex ver = iterator.next();
			residualGf.insertVertex(ver.getData(), ver.getName());
		}
		HashMap<String, Vertex> vertices = getNode(residualGf);
		for (Iterator<Edge> iterator = flowGf.edges(); iterator.hasNext();) {
			Edge e = iterator.next();
			Double[] data = (Double[]) e.getData();
			if (data[1] == 0) {
				residualGf.insertEdge(vertices.get(e.getFirstEndpoint().getName()),
						vertices.get(e.getSecondEndpoint().getName()), new Double[] { data[0], 0.0 }, e.getName());
			} else if (data[0] == data[1]) {
				residualGf.insertEdge(vertices.get(e.getSecondEndpoint().getName()),
						vertices.get(e.getFirstEndpoint().getName()), new Double[] { data[1], 0.0 }, e.getName());
			} else {
				residualGf.insertEdge(vertices.get(e.getFirstEndpoint().getName()),
						vertices.get(e.getSecondEndpoint().getName()), new Double[] { data[0] - data[1], 0.0 },
						e.getName());
				residualGf.insertEdge(vertices.get(e.getSecondEndpoint().getName()),
						vertices.get(e.getFirstEndpoint().getName()), new Double[] { data[1], 0.0 }, e.getName());
			}
		}
		return residualGf;
	}

	
	@SuppressWarnings("unchecked")
	private static void updateflowGf(SimpleGraph flowGf, List<String> path, Double bottleneck) {
		HashMap<String, Vertex> map = getNode(flowGf);
		for (int i = 0; i < path.size() - 1; i++) {
			Vertex v = map.get(path.get(i));
			boolean foundForwardEdge = false;
			for (Iterator<Edge> iterator = flowGf.incidentEdges(v); iterator.hasNext();) {
				Edge e;
				if ((e = iterator.next()).getSecondEndpoint().getName().equals(path.get(i + 1))) {
					Double[] data = (Double[]) e.getData();
					data[1] += bottleneck;
					e.setData(data);
					foundForwardEdge = true;
				}
			}
			if (!foundForwardEdge) {
				v = map.get(path.get(i + 1));
				for (Iterator<Edge> iterator = flowGf.incidentEdges(v); iterator.hasNext();) {
					Edge e;
					if ((e = iterator.next()).getSecondEndpoint().getName().equals(path.get(i))) {
						Double[] data = (Double[]) e.getData();
						data[1] -= bottleneck;
						e.setData(data);
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private static SimpleGraph produceGf(SimpleGraph residualGf) {
		SimpleGraph produceGf = new SimpleGraph();
		for (Iterator<Vertex> iterator = residualGf.vertices(); iterator.hasNext();) {
			Vertex v = iterator.next();
			produceGf.insertVertex(v.getData(), v.getName());
		}
		HashMap<String, Vertex> vertices = getNode(produceGf);
		for (Iterator<Edge> iterator = residualGf.edges(); iterator.hasNext();) {
			Edge e = iterator.next();
			Double[] data = (Double[]) e.getData();
			if (data[0] >0) {
				produceGf.insertEdge(vertices.get(e.getFirstEndpoint().getName()),
						vertices.get(e.getSecondEndpoint().getName()), new Double[] { data[0], 0.0 }, e.getName());
			}
		}
		return produceGf;
	}
	@SuppressWarnings("unchecked")
	private static SimpleGraph toFlowGf(SimpleGraph graph) {
		SimpleGraph flowGf = new SimpleGraph();
		for (Iterator<Vertex> iterator = graph.vertices(); iterator.hasNext();) {
			Vertex v = iterator.next();
			flowGf.insertVertex(v.getData(), v.getName());
		}
		HashMap<String, Vertex> vertices = getNode(flowGf);
		for (Iterator<Edge> iterator = graph.edges(); iterator.hasNext();) {
			Edge e = iterator.next();
			Double data = (Double) e.getData();
			flowGf.insertEdge(vertices.get(e.getFirstEndpoint().getName()),
					vertices.get(e.getSecondEndpoint().getName()), new Double[] { data, 0.0 }, e.getName());
		}
		return flowGf;
	}	
}