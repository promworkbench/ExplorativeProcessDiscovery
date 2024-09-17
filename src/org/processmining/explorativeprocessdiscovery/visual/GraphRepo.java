package org.processmining.explorativeprocessdiscovery.visual;

import java.util.HashMap;
import java.util.List;

import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.processmining.explorativeprocessdiscovery.plugins.ResultUnitList;

public class GraphRepo {
	public DefaultGraphCell[] cells;
	public DefaultGraphCell[] eventcells;
	public DefaultGraphCell[] activitycells;
	public List<DefaultEdge> alledgelist;
	public HashMap<String, DefaultGraphCell> mapSublogNode;
	public HashMap<DefaultEdge, List<String>> mapEdgeSublogPair;
	public ResultUnitList literesults;
	public HashMap<String, List<String>> childList;

	public GraphRepo(DefaultGraphCell[] cells, DefaultGraphCell[] eventcells, 
			DefaultGraphCell[] activitycells, List<DefaultEdge> alledgelist, 
			HashMap<String, DefaultGraphCell> mapSublogNode, HashMap<DefaultEdge, List<String>> mapEdgeSublogPair, 
			ResultUnitList literesults, HashMap<String, List<String>> childList) {
		this.cells = cells;
		this.eventcells = eventcells;
		this.activitycells = activitycells;
		this.alledgelist = alledgelist;
		this.mapSublogNode = mapSublogNode;
		this.mapEdgeSublogPair = mapEdgeSublogPair;
		this.literesults = literesults;
		this.childList = childList;
	}
}