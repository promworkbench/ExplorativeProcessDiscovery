package org.processmining.explorativeprocessdiscovery.visual;

import java.util.HashMap;
import java.util.List;

import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.processmining.explorativeprocessdiscovery.plugins.ResultUnitList;

public class GraphRepoUnit {
	DefaultGraphCell[] cells;
	DefaultGraphCell[] eventcells;
	DefaultGraphCell[] activitycells;
	List<DefaultEdge> alledgelist;
	HashMap<String, DefaultGraphCell> mapSublogNode;
	HashMap<DefaultEdge, List<String>> mapEdgeSublogPair;
	ResultUnitList literesults;
	HashMap<String, List<String>> childList;
}
