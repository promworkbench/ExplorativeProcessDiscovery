/**
 * 
 */
package org.processmining.explorativeprocessdiscovery.visual;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.jgraph.JGraph;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.explorativeprocessdiscovery.embeddedvisual.PNLogReplayProjectedVisPanelEmbedded;
import org.processmining.explorativeprocessdiscovery.plugins.EvClassLogPetrinetConnection;
import org.processmining.explorativeprocessdiscovery.plugins.MainProjectionPlugin;
import org.processmining.explorativeprocessdiscovery.plugins.Result;
import org.processmining.explorativeprocessdiscovery.plugins.ResultUnit;
import org.processmining.explorativeprocessdiscovery.plugins.ResultUnitList;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.connections.ConnectionManager;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.models.connections.petrinets.PNRepResultAllRequiredParamConnection;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.plugins.IMPetriNet;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayAlgorithm;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParamProvider;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.ui.PNAlgorithmStep;
import org.processmining.plugins.petrinet.replayer.ui.PNReplayerUI;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.pnalignanalysis.conformance.AlignmentPrecGen;
import org.processmining.plugins.pnalignanalysis.conformance.AlignmentPrecGenRes;

import nl.tue.astar.AStarException;

public class MainProjectionVisPanelLite extends ProjectionVisPanelChanged {
	private static final long serialVersionUID = -6674503536171244970L;
	MainVisualConfig miniembeddedconfig = new MainVisualConfig();
	private static final Font BLFont = new Font("TimesRoman", Font.BOLD, 10);
	private static final Font ITALICFont = new Font("TimesRoman", Font.ITALIC, 10);
	private static final Font SITALICFont = new Font("TimesRoman", Font.ITALIC, 4);
	Color limeGreen = new Color(0, 128, 128);
	static int displayType;
	static List<DefaultEdge> alledgelist = new ArrayList<DefaultEdge>();
	static HashMap<String, List<String>> childList = new HashMap<String, List<String>>();
	static HashMap<DefaultEdge, List<String>> mapEdgeSublogPair = new HashMap<DefaultEdge, List<String>>();
	String Name = miniembeddedconfig.getTypeName(miniembeddedconfig.getType());
	int TotalEventCount = miniembeddedconfig.getTotalEventCount();
	int TotalActivityCount = miniembeddedconfig.getTotalActivityCount();
	JPanel parentviewPanel = new JPanel();
	JPanel childviewPanel = new JPanel();
	JPanel TmpFM;
	JPanel TmpSM;
	JPanel EmptyFModel = new JPanel();
	JLabel labelF = new JLabel("Select an edge");
	JPanel EmptySModel = new JPanel();
	JLabel labelS = new JLabel("Select an edge");
	DefaultEdge LastSelectedEdge = null;

	// GUI component
	private ViewPanel viewPanel;

	// for graph visualization
	protected boolean[] placeWithMoveOnLog;

	public static Color LOWFIVESHOW = new Color(0, 0, 255);
	public static Color LOWMEDFIVESHOW = new Color(64, 0, 196);
	public static Color MEDFIVESHOW = new Color(128, 0, 127);
	public static Color MEDHIGHFIVESHOW = new Color(192, 0, 63);
	public static Color HIGHFIVESHOW = new Color(255, 0, 0);

	static int currentStep;
	static int nofSteps = 2;
	static JComponent[] replaySteps;
	Marking tgrmark;
	private static IPNReplayParamProvider paramProvider;

	GraphRepoUnitList GraphRepo = new GraphRepoUnitList();
	GraphRepoUnitList GraphForward = new GraphRepoUnitList();
	int graphCount = 0;
	String TgrStartModel;
	List<String> RootModelLists = new ArrayList<String>();
	List<Double> RootModelValueLists = new ArrayList<Double>();
	List<String> OldRootModelLists4Forward = new ArrayList<String>();
	List<Double> OldRootModelValueLists4Forward = new ArrayList<Double>();
	HashMap<List<Integer>, Color> TargetCells = new HashMap<List<Integer>, Color>();
	HashMap<List<Integer>, Color> OldTargetCells = new HashMap<List<Integer>, Color>();
	List<List<Integer>> IndexOfTargetCells = new ArrayList<List<Integer>>();
	List<List<Integer>> OldIndexOfTargetCells = new ArrayList<List<Integer>>();
	List<Object> previousResult = new ArrayList<Object>();
	List<Object> OldpreviousResult = new ArrayList<Object>();

	int maxNumOfAct;
	int fixedmaxNumOfAct;
	int maxValueCount4bottom = 0;
	List<Object[][]> OldTableRows4Forward = new ArrayList<Object[][]>();

	JButton goDeep = new JButton("Go Deeper");

	JLabel opeNote1 = new JLabel();
	JLabel opeNote2 = new JLabel();

	double resultMinValue = 99.99;
	double resultMaxValue = -99.99;
	double resultMinValue4son = 99.99;
	double resultMaxValue4son = -99.99;

	HashMap<String, DefaultGraphCell> mapSublogNode;
	JTextArea UrlArea = new JTextArea("\"Select an edge\"", 10, 20);

	public MainProjectionVisPanelLite(PluginContext context, List<Result> results, int Type) {
		super(context, results, Type);
		displayType = Type;

		initialize(context, results, Type);
	}

	protected void initialize(PluginContext context, List<Result> result, int Type) {

		EmptyFModel.add(labelF);
		miniembeddedconfig.setFatherModel(EmptyFModel);
		EmptySModel.add(labelS);
		miniembeddedconfig.setSonModel(EmptySModel);

		goDeep.setEnabled(false);
		opeNote1.setText("***It's already the root node***");
		opeNote1.setForeground(Color.RED);
		opeNote2.setText("Select a child node and go deeper");
		maxNumOfAct = result.get(0).subset.size();
		fixedmaxNumOfAct = result.get(0).subset.size();
		boolean isFirstResult = true;
		RootModelLists.add(result.get(0).sublog);
		RootModelValueLists.add(getTypeValue(result.get(0)));
		previousResult.add(result);

		//创建显示数据
		Object[][] Tabledata = new Object[4][maxNumOfAct + 1];
		int LayerElementsCount4Table = 1;
		Tabledata[0][0] = "";
		Tabledata[1][0] = "Removed";
		Tabledata[2][0] = "";
		Tabledata[3][0] = "";

		for (Result r : result) {
			if (isFirstResult) {
				Tabledata[0][1] = r.sublog;
				Tabledata[1][1] = getTypeValue(r);
				for (int lastCount = 2; lastCount < maxNumOfAct + 1; lastCount++) {
					Tabledata[0][lastCount] = "";
					Tabledata[1][lastCount] = "";
				}
				isFirstResult = false;
			} else {
				Tabledata[2][LayerElementsCount4Table] = r.sublog;
				Tabledata[3][LayerElementsCount4Table] = getTypeValue(r);
				LayerElementsCount4Table = LayerElementsCount4Table + 1;
				if (getTypeValue(r) > resultMaxValue4son) {
					resultMaxValue4son = getTypeValue(r);
				}
				if (getTypeValue(r) < resultMinValue4son) {
					resultMinValue4son = getTypeValue(r);
				}
			}
			if (getTypeValue(r) > resultMaxValue) {
				resultMaxValue = getTypeValue(r);
			}
			if (getTypeValue(r) < resultMinValue) {
				resultMinValue = getTypeValue(r);
			}
			List<String> str = Arrays.asList(r.sublog.split(","));
			List<String> TempChild = new ArrayList<String>();
			for (int i = 0; i < str.size(); i++) {
				List<String> TempStrList = new ArrayList<String>();
				for (int j = 0; j < str.size(); j++) {
					if (i == j) {
						continue;
					} else {
						TempStrList.add(str.get(j));
					}
				}
				TempChild.add(String.join(",", TempStrList));
			}
			childList.put(r.sublog, TempChild);
		}
		for (int lastCount = LayerElementsCount4Table; lastCount < maxNumOfAct + 1; lastCount++) {
			Tabledata[2][lastCount] = "";
			Tabledata[3][lastCount] = "";
		}

		int nodesNum = result.size();
		int nodeStepLenX = 100;
		int nodeStepLenY = 250;
		int nodeWidth = 30;
		int infonodeX = 30;
		int infonodeY = 5;
		int initialNodeX = (int) Math.ceil((double) maxNumOfAct / 2) * (nodeStepLenX + nodeWidth) + 20;
		int initialNodeY = 20;
		mapSublogNode = new HashMap<String, DefaultGraphCell>();
		HashMap<List<String>, DefaultGraphCell> EventActivityNode = new HashMap<List<String>, DefaultGraphCell>();

		GraphModel model = new DefaultGraphModel();
		GraphLayoutCache view = new GraphLayoutCache(model, new DefaultCellViewFactory());
		JGraph graph = new JGraph(model, view);

		// test demo
		DefaultGraphCell[] cells = new DefaultGraphCell[nodesNum];
		DefaultGraphCell[] eventcells = new DefaultGraphCell[nodesNum];
		DefaultGraphCell[] activitycells = new DefaultGraphCell[nodesNum];

		int nodeCellCount = 0;
		int startX = initialNodeX, startY = initialNodeY;
		int tempMaxNumOfAct = maxNumOfAct;
		int stepX = startX;
		Color cellColor;
		isFirstResult = true;
		for (Result i : result) {
			int layerNodesNum = i.subset.size();
			cellColor = colorcalc(getTypeValue(i), resultMaxValue, resultMinValue);

			List<String> EventAndActivity = new ArrayList<>();
			double Eventnum = (double) Math.round(((long) i.eventcount * 10000) / TotalEventCount) / 100;
			double Activitynum = (double) Math.round(((long) i.activitycount * 10000) / TotalActivityCount) / 100;
			String Eventstr = Double.toString(Eventnum) + "%";
			String Activitystr = Double.toString(Activitynum) + "%";
			EventAndActivity.add(Eventstr);
			EventAndActivity.add(Activitystr);

			int tempCom = layerNodesNum + 1;
			if (tempCom == 1) {
				startX = initialNodeX;
			} else if (tempCom != 1 && tempCom % 2 == 1) {
				startX = initialNodeX - (int) Math.floor((double) tempCom / 2) * (nodeStepLenX + nodeWidth);
			} else if (tempCom % 2 == 0) {
				startX = initialNodeX - (int) Math.floor((double) tempCom / 2) * (nodeStepLenX + nodeWidth)
						+ nodeStepLenX / 2 + nodeWidth / 2;
			}

			if (layerNodesNum != tempMaxNumOfAct) {
				stepX = startX;
				startY = startY + nodeStepLenY;
				tempMaxNumOfAct = layerNodesNum;
			}

			if (isFirstResult) {
				cells[nodeCellCount] = new DefaultGraphCell(
						new String(Double.toString((double) Math.round(getTypeValue(i) * 100) / 100)));
				GraphConstants.setForeground(cells[nodeCellCount].getAttributes(), Color.WHITE);
				GraphConstants.setFont(cells[nodeCellCount].getAttributes(), ITALICFont);
				GraphConstants.setBounds(cells[nodeCellCount].getAttributes(),
						new Rectangle2D.Double(stepX, startY, nodeWidth, nodeWidth));
				GraphConstants.setBackground(cells[nodeCellCount].getAttributes(), cellColor);
				GraphConstants.setOpaque(cells[nodeCellCount].getAttributes(), true);
				GraphConstants.setMoveable(cells[nodeCellCount].getAttributes(), false); // 不可移动
				GraphConstants.setSizeable(cells[nodeCellCount].getAttributes(), false); // 不可调整大小

				eventcells[nodeCellCount] = new DefaultGraphCell(new String(Eventstr));
				GraphConstants.setForeground(eventcells[nodeCellCount].getAttributes(), Color.WHITE);
				GraphConstants.setFont(eventcells[nodeCellCount].getAttributes(), SITALICFont);
				GraphConstants.setBounds(eventcells[nodeCellCount].getAttributes(),
						new Rectangle2D.Double(stepX, startY + nodeWidth, infonodeX * (Eventnum / 100), infonodeY));
				GraphConstants.setBackground(eventcells[nodeCellCount].getAttributes(), Color.DARK_GRAY);
				GraphConstants.setOpaque(eventcells[nodeCellCount].getAttributes(), true);
				GraphConstants.setMoveable(eventcells[nodeCellCount].getAttributes(), false); // 不可移动
				GraphConstants.setSizeable(eventcells[nodeCellCount].getAttributes(), false); // 不可调整大小
				GraphConstants.setSelectable(eventcells[nodeCellCount].getAttributes(), false); // 不可选中

				activitycells[nodeCellCount] = new DefaultGraphCell(new String(Activitystr));
				GraphConstants.setForeground(activitycells[nodeCellCount].getAttributes(), Color.WHITE);
				GraphConstants.setFont(activitycells[nodeCellCount].getAttributes(), SITALICFont);
				GraphConstants.setBounds(activitycells[nodeCellCount].getAttributes(),
						new Rectangle2D.Double(stepX + nodeWidth, startY, infonodeY, infonodeX * (Activitynum / 100)));
				GraphConstants.setBackground(activitycells[nodeCellCount].getAttributes(), Color.ORANGE);
				GraphConstants.setOpaque(activitycells[nodeCellCount].getAttributes(), true);
				GraphConstants.setMoveable(activitycells[nodeCellCount].getAttributes(), false); // 不可移动
				GraphConstants.setSizeable(activitycells[nodeCellCount].getAttributes(), false); // 不可调整大小
				GraphConstants.setSelectable(activitycells[nodeCellCount].getAttributes(), false); // 不可选中
				isFirstResult = false;
			} else {
				if (getTypeValue(i) == resultMaxValue4son) {
					cells[nodeCellCount] = new DefaultGraphCell(
							new String(Double.toString((double) Math.round(getTypeValue(i) * 100) / 100)));
					GraphConstants.setForeground(cells[nodeCellCount].getAttributes(), Color.WHITE);
					GraphConstants.setFont(cells[nodeCellCount].getAttributes(), ITALICFont);
					GraphConstants.setBounds(cells[nodeCellCount].getAttributes(),
							new Rectangle2D.Double(stepX, startY, nodeWidth, nodeWidth));
					GraphConstants.setBackground(cells[nodeCellCount].getAttributes(), cellColor);
					GraphConstants.setOpaque(cells[nodeCellCount].getAttributes(), true);
					GraphConstants.setBorderColor(cells[nodeCellCount].getAttributes(), Color.GREEN);
					GraphConstants.setLineWidth(cells[nodeCellCount].getAttributes(), (float) 3.0);
					GraphConstants.setMoveable(cells[nodeCellCount].getAttributes(), false); // 不可移动
					GraphConstants.setSizeable(cells[nodeCellCount].getAttributes(), false); // 不可调整大小

					eventcells[nodeCellCount] = new DefaultGraphCell(new String(Eventstr));
					GraphConstants.setForeground(eventcells[nodeCellCount].getAttributes(), Color.WHITE);
					GraphConstants.setFont(eventcells[nodeCellCount].getAttributes(), SITALICFont);
					GraphConstants.setBounds(eventcells[nodeCellCount].getAttributes(),
							new Rectangle2D.Double(stepX, startY + nodeWidth, infonodeX * (Eventnum / 100), infonodeY));
					GraphConstants.setBackground(eventcells[nodeCellCount].getAttributes(), Color.DARK_GRAY);
					GraphConstants.setOpaque(eventcells[nodeCellCount].getAttributes(), true);
					GraphConstants.setMoveable(eventcells[nodeCellCount].getAttributes(), false); // 不可移动
					GraphConstants.setSizeable(eventcells[nodeCellCount].getAttributes(), false); // 不可调整大小
					GraphConstants.setSelectable(eventcells[nodeCellCount].getAttributes(), false); // 不可选中

					activitycells[nodeCellCount] = new DefaultGraphCell(new String(Activitystr));
					GraphConstants.setForeground(activitycells[nodeCellCount].getAttributes(), Color.WHITE);
					GraphConstants.setFont(activitycells[nodeCellCount].getAttributes(), SITALICFont);
					GraphConstants.setBounds(activitycells[nodeCellCount].getAttributes(), new Rectangle2D.Double(
							stepX + nodeWidth, startY, infonodeY, infonodeX * (Activitynum / 100)));
					GraphConstants.setBackground(activitycells[nodeCellCount].getAttributes(), Color.ORANGE);
					GraphConstants.setOpaque(activitycells[nodeCellCount].getAttributes(), true);
					GraphConstants.setMoveable(activitycells[nodeCellCount].getAttributes(), false); // 不可移动
					GraphConstants.setSizeable(activitycells[nodeCellCount].getAttributes(), false); // 不可调整大小
					GraphConstants.setSelectable(activitycells[nodeCellCount].getAttributes(), false); // 不可选中
				} else {
					cells[nodeCellCount] = new DefaultGraphCell(
							new String(Double.toString((double) Math.round(getTypeValue(i) * 100) / 100)));
					GraphConstants.setForeground(cells[nodeCellCount].getAttributes(), Color.WHITE);
					GraphConstants.setFont(cells[nodeCellCount].getAttributes(), ITALICFont);
					GraphConstants.setBounds(cells[nodeCellCount].getAttributes(),
							new Rectangle2D.Double(stepX, startY, nodeWidth, nodeWidth));
					GraphConstants.setBackground(cells[nodeCellCount].getAttributes(), cellColor);
					GraphConstants.setOpaque(cells[nodeCellCount].getAttributes(), true);
					GraphConstants.setMoveable(cells[nodeCellCount].getAttributes(), false); // 不可移动
					GraphConstants.setSizeable(cells[nodeCellCount].getAttributes(), false); // 不可调整大小

					eventcells[nodeCellCount] = new DefaultGraphCell(new String(Eventstr));
					GraphConstants.setForeground(eventcells[nodeCellCount].getAttributes(), Color.WHITE);
					GraphConstants.setFont(eventcells[nodeCellCount].getAttributes(), SITALICFont);
					GraphConstants.setBounds(eventcells[nodeCellCount].getAttributes(),
							new Rectangle2D.Double(stepX, startY + nodeWidth, infonodeX * (Eventnum / 100), infonodeY));
					GraphConstants.setBackground(eventcells[nodeCellCount].getAttributes(), Color.DARK_GRAY);
					GraphConstants.setOpaque(eventcells[nodeCellCount].getAttributes(), true);
					GraphConstants.setMoveable(eventcells[nodeCellCount].getAttributes(), false); // 不可移动
					GraphConstants.setSizeable(eventcells[nodeCellCount].getAttributes(), false); // 不可调整大小
					GraphConstants.setSelectable(eventcells[nodeCellCount].getAttributes(), false); // 不可选中

					activitycells[nodeCellCount] = new DefaultGraphCell(new String(Activitystr));
					GraphConstants.setForeground(activitycells[nodeCellCount].getAttributes(), Color.WHITE);
					GraphConstants.setFont(activitycells[nodeCellCount].getAttributes(), SITALICFont);
					GraphConstants.setBounds(activitycells[nodeCellCount].getAttributes(), new Rectangle2D.Double(
							stepX + nodeWidth, startY, infonodeY, infonodeX * (Activitynum / 100)));
					GraphConstants.setBackground(activitycells[nodeCellCount].getAttributes(), Color.ORANGE);
					GraphConstants.setOpaque(activitycells[nodeCellCount].getAttributes(), true);
					GraphConstants.setMoveable(activitycells[nodeCellCount].getAttributes(), false); // 不可移动
					GraphConstants.setSizeable(activitycells[nodeCellCount].getAttributes(), false); // 不可调整大小
					GraphConstants.setSelectable(activitycells[nodeCellCount].getAttributes(), false); // 不可选中
				}
			}

			mapSublogNode.put(i.sublog, cells[nodeCellCount]);
			EventActivityNode.put(EventAndActivity, cells[nodeCellCount]);

			if (layerNodesNum == tempMaxNumOfAct) {
				stepX = stepX + nodeStepLenX + nodeWidth;
			}

			nodeCellCount++;
		}

		DefaultPort portS = new DefaultPort();
		GraphConstants.setOffset(portS.getAttributes(),
				new Point2D.Double(GraphConstants.PERMILLE / 2, nodeWidth + infonodeY));
		GraphConstants.setAbsoluteY(portS.getAttributes(), true);
		cells[0].add(portS);

		String targetStr = result.get(0).sublog;

		if (!childList.get(targetStr).get(0).equals("")) {
			for (String child : childList.get(targetStr)) {
				String TgrFatherName = targetStr;
				String TgrSonName = child;
				String DeletedEle = "";
				for (String Fele : TgrFatherName.split(",")) {
					int Countdiff = 0;
					for (String Sele : TgrSonName.split(",")) {
						if (Fele.equals(Sele)) {
							Countdiff = 1;
							break;
						}
					}
					if (Countdiff == 0) {
						DeletedEle = Fele;
						break;
					}
				}

				double FatherValue = 0.0, SonValue = 0.0, diffFS = 1.0;
				Color edgeCol = null;
				for (Result resulti : result) {
					if (resulti.sublog.equals(targetStr)) {
						FatherValue = getTypeValue(resulti);
					}
					if (resulti.sublog.equals(child)) {
						SonValue = getTypeValue(resulti);
					}
				}
				if (Double.isNaN(FatherValue) || Double.isNaN(SonValue)) {
					diffFS = 1.0;
					edgeCol = Color.BLACK;
				} else if (FatherValue == SonValue) {
					diffFS = 1.0;
					edgeCol = Color.GREEN;
				} else if (FatherValue > SonValue) {
					diffFS = FatherValue - SonValue;
					edgeCol = Color.BLUE;
				} else if (FatherValue < SonValue) {
					diffFS = SonValue - FatherValue;
					edgeCol = Color.RED;
				}

				DefaultPort portE = new DefaultPort();
				GraphConstants.setOffset(portE.getAttributes(), new Point2D.Double(GraphConstants.PERMILLE / 2, 0));
				mapSublogNode.get(child).add(portE);

				DefaultEdge edge = new DefaultEdge(DeletedEle);
				int arrow = GraphConstants.ARROW_CLASSIC;
				GraphConstants.setLineColor(edge.getAttributes(), edgeCol);
				GraphConstants.setForeground(edge.getAttributes(), Color.BLACK);
				GraphConstants.setFont(edge.getAttributes(), BLFont);
				GraphConstants.setLabelAlongEdge(edge.getAttributes(), true);
				GraphConstants.setLineEnd(edge.getAttributes(), arrow);
				GraphConstants.setEndFill(edge.getAttributes(), true);
				GraphConstants.setLineWidth(edge.getAttributes(), (float) (2.0 * diffFS));
				graph.getGraphLayoutCache().insertEdge(edge, portS, portE);
				alledgelist.add(edge);
				mapSublogNode.get(child).remove(portE);
				List<String> TempSublogPair = new ArrayList<String>();
				TempSublogPair.add(targetStr);
				TempSublogPair.add(child);
				mapEdgeSublogPair.put(edge, TempSublogPair);
			}
			cells[0].remove(portS);
		} else {
			;
		}

		graph.getGraphLayoutCache().insert(cells);
		graph.getGraphLayoutCache().insert(eventcells);
		graph.getGraphLayoutCache().insert(activitycells);

		GraphRepoUnit GraphUni = new GraphRepoUnit();
		GraphUni.cells = cells;
		GraphUni.eventcells = eventcells;
		GraphUni.activitycells = activitycells;
		GraphUni.alledgelist = alledgelist;
		GraphUni.mapSublogNode = mapSublogNode;
		GraphUni.mapEdgeSublogPair = mapEdgeSublogPair;
		GraphUni.literesults = null;
		GraphUni.childList = childList;
		GraphRepo.add(GraphUni);

		JPanel SelectedInfo = new JPanel();
		SelectedInfo.setOpaque(false);
		SelectedInfo.setLayout(new BoxLayout(SelectedInfo, BoxLayout.X_AXIS));
		UrlArea.setLineWrap(true);
		JScrollPane UrlscrollPane = new JScrollPane(UrlArea);
		SelectedInfo.add(UrlscrollPane);

		graph.addGraphSelectionListener(new GraphSelectionListener() {
			public void valueChanged(GraphSelectionEvent e) {
				graph.refresh();

				if (e.getCell() instanceof DefaultEdge) {
					if ((DefaultEdge) e.getCell() != LastSelectedEdge) {
						LastSelectedEdge = (DefaultEdge) e.getCell();
						JDialog runningmessagedialog = new JDialog(new JFrame(), "Running...Please Wait...", true);
						runningmessagedialog.setPreferredSize(new Dimension(300, 0));
						runningmessagedialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
						runningmessagedialog.pack();
						runningmessagedialog.setLocationRelativeTo(null);
						runningmessagedialog.setModal(false);
						runningmessagedialog.setVisible(true);
						List<String> TempPairDrag = mapEdgeSublogPair.get(e.getCell());
						miniembeddedconfig.setSublogPair(TempPairDrag);
						String TgrFatherName = TempPairDrag.get(0);
						String TgrSonName = TempPairDrag.get(1);

						parentviewPanel.remove(miniembeddedconfig.getFatherModel());
						childviewPanel.remove(miniembeddedconfig.getSonModel());

						try {
							TmpFM = GetModelPanel(context, TgrFatherName);
						} catch (ConnectionCannotBeObtained e1) {
							e1.printStackTrace();
						}
						try {
							TmpSM = GetModelPanel(context, TgrSonName);
						} catch (ConnectionCannotBeObtained e1) {
							e1.printStackTrace();
						}

						parentviewPanel.add(TmpFM);
						childviewPanel.add(TmpSM);

						miniembeddedconfig.setFatherModel(TmpFM);
						miniembeddedconfig.setSonModel(TmpSM);

						getInspector().revalidate();
						getInspector().repaint();

						String DeletedEle = "";
						for (String Fele : TgrFatherName.split(",")) {
							int Countdiff = 0;
							for (String Sele : TgrSonName.split(",")) {
								if (Fele.equals(Sele)) {
									Countdiff = 1;
									break;
								}
							}
							if (Countdiff == 0) {
								DeletedEle = Fele;
								break;
							}
						}

						double FValue2 = 0.0, SValue = 0.0;
						String FEvent = null, SEvent = null;
						String FActivity = null, SActivity = null;

						if (GraphRepo.get(graphCount).literesults == null) {
							for (Result resulti : result) {
								if (resulti.sublog.equals(TgrFatherName)) {
									FValue2 = getTypeValue(resulti);
									double Eventnumtemp = (double) Math
											.round((resulti.eventcount * 10000) / TotalEventCount) / 100;
									double Activitynumtemp = (double) Math
											.round((resulti.activitycount * 10000) / TotalActivityCount) / 100;
									FEvent = Double.toString(Eventnumtemp) + "%";
									FActivity = Double.toString(Activitynumtemp) + "%";
								}
								if (resulti.sublog.equals(TgrSonName)) {
									SValue = getTypeValue(resulti);
									double Eventnumtemp = (double) Math
											.round((resulti.eventcount * 10000) / TotalEventCount) / 100;
									double Activitynumtemp = (double) Math
											.round((resulti.activitycount * 10000) / TotalActivityCount) / 100;
									SEvent = Double.toString(Eventnumtemp) + "%";
									SActivity = Double.toString(Activitynumtemp) + "%";
								}
							}

						} else {
							for (ResultUnit resulti : GraphRepo.get(graphCount).literesults) {
								if (resulti.name.equals(TgrFatherName)) {
									FValue2 = getTypeValue2(resulti);
									double Eventnumtemp = (double) Math
											.round((resulti.eventcount * 10000) / TotalEventCount) / 100;
									double Activitynumtemp = (double) Math
											.round((resulti.activitycount * 10000) / TotalActivityCount) / 100;
									FEvent = Double.toString(Eventnumtemp) + "%";
									FActivity = Double.toString(Activitynumtemp) + "%";
								}
								if (resulti.name.equals(TgrSonName)) {
									SValue = getTypeValue2(resulti);
									double Eventnumtemp = (double) Math
											.round((resulti.eventcount * 10000) / TotalEventCount) / 100;
									double Activitynumtemp = (double) Math
											.round((resulti.activitycount * 10000) / TotalActivityCount) / 100;
									SEvent = Double.toString(Eventnumtemp) + "%";
									SActivity = Double.toString(Activitynumtemp) + "%";
								}
							}
						}

						if (Double.isNaN(FValue2) && !Double.isNaN(SValue)) {
							UrlArea.setText("Activities in this subset: \n" + TgrFatherName + "\n" + Name + ": "
									+ Double.toString(FValue2) + "\n" + "Event:" + FEvent + "\n" + "Activity:"
									+ FActivity + "\n\n" + "Delete: " + DeletedEle + "\n" + "Event:" + FEvent + " → "
									+ SEvent + "\n" + "Activity:" + FActivity + " → " + SActivity + "\n" + Name + ": "
									+ Double.toString(FValue2) + " → "
									+ Double.toString((double) Math.round(SValue * 100) / 100) + "\n"
									+ "(Conneted with a failed spot ✕)");
						} else if (!Double.isNaN(FValue2) && Double.isNaN(SValue)) {
							UrlArea.setText("Activities in this subset: \n" + TgrFatherName + "\n" + Name + ": "
									+ Double.toString((double) Math.round(FValue2 * 100) / 100) + "\n" + "Event:"
									+ FEvent + "\n" + "Activity:" + FActivity + "\n\n" + "Delete: " + DeletedEle + "\n"
									+ "Event:" + FEvent + " → " + SEvent + "\n" + "Activity:" + FActivity + " → "
									+ SActivity + "\n" + Name + ": "
									+ Double.toString((double) Math.round(FValue2 * 100) / 100) + " → "
									+ Double.toString(SValue) + "\n" + "(Conneted with a failed spot ✕)");
						} else if (Double.isNaN(FValue2) && Double.isNaN(SValue)) {
							UrlArea.setText("Activities in this subset: \n" + TgrFatherName + "\n" + Name + ": "
									+ Double.toString(FValue2) + "\n" + "Event:" + FEvent + "\n" + "Activity:"
									+ FActivity + "\n\n" + "Delete: " + DeletedEle + "\n" + "Event:" + FEvent + " → "
									+ SEvent + "\n" + "Activity:" + FActivity + " → " + SActivity + "\n" + Name + ": "
									+ Double.toString(FValue2) + " → " + Double.toString(SValue) + "\n"
									+ "(Conneted with a failed spot ✕)");
						} else if (FValue2 == SValue) {
							UrlArea.setText("Activities in this subset: \n" + TgrFatherName + "\n" + Name + ": "
									+ Double.toString((double) Math.round(FValue2 * 100) / 100) + "\n" + "Event:"
									+ FEvent + "\n" + "Activity:" + FActivity + "\n\n" + "Delete: " + DeletedEle + "\n"
									+ "Event:" + FEvent + " → " + SEvent + "\n" + "Activity:" + FActivity + " → "
									+ SActivity + "\n" + Name + ": "
									+ Double.toString((double) Math.round(FValue2 * 100) / 100) + " → "
									+ Double.toString((double) Math.round(SValue * 100) / 100) + "\n"
									+ "(Unchanged -)");
						} else if (FValue2 > SValue) {
							UrlArea.setText("Activities in this subset: \n" + TgrFatherName + "\n" + Name + ": "
									+ Double.toString((double) Math.round(FValue2 * 100) / 100) + "\n" + "Event:"
									+ FEvent + "\n" + "Activity:" + FActivity + "\n\n" + "Delete: " + DeletedEle + "\n"
									+ "Event:" + FEvent + " → " + SEvent + "\n" + "Activity:" + FActivity + " → "
									+ SActivity + "\n" + Name + ": "
									+ Double.toString((double) Math.round(FValue2 * 100) / 100) + " → "
									+ Double.toString((double) Math.round(SValue * 100) / 100) + "\n" + "(Decreased "
									+ Double.toString(
											(double) (Math.round(FValue2 * 100) - Math.round(SValue * 100)) / 100)
									+ " ↓)");
						} else if (FValue2 < SValue) {
							UrlArea.setText("Activities in this subset: \n" + TgrFatherName + "\n" + Name + ": "
									+ Double.toString((double) Math.round(FValue2 * 100) / 100) + "\n" + "Event:"
									+ FEvent + "\n" + "Activity:" + FActivity + "\n\n" + "Delete: " + DeletedEle + "\n"
									+ "Event:" + FEvent + " → " + SEvent + "\n" + "Activity:" + FActivity + " → "
									+ SActivity + "\n" + Name + ": "
									+ Double.toString((double) Math.round(FValue2 * 100) / 100) + " → "
									+ Double.toString((double) Math.round(SValue * 100) / 100) + "\n" + "(Increased "
									+ Double.toString(
											(double) (Math.round(SValue * 100) - Math.round(FValue2 * 100)) / 100)
									+ " ↑)");
						}
						goDeep.setEnabled(false);
						runningmessagedialog.dispose();
					}
				} else if (e.getCell() instanceof DefaultGraphCell) {
					String targetStr = null;
					for (DefaultGraphCell icell : GraphRepo.get(graphCount).mapSublogNode.values()) {
						if (e.getCell() == icell) {
							for (String istr : GraphRepo.get(graphCount).mapSublogNode.keySet()) {
								if (GraphRepo.get(graphCount).mapSublogNode.get(istr) == icell) {
									targetStr = istr;
								}
							}
						}
					}

					TgrStartModel = targetStr;
					if (TgrStartModel.split(",").length == maxNumOfAct) {
						goDeep.setEnabled(false);
					} else {
						goDeep.setEnabled(true);
					}
				}

				graph.refresh();
			}
		});

		miniembeddedconfig.setZoom(graph);

		scroll = new JScrollPane(miniembeddedconfig.getZoom());
		setLayout(new BorderLayout());

		//创建表头
		String[] TablecolumnNames = new String[fixedmaxNumOfAct + 1];
		int numCopies = fixedmaxNumOfAct + 1;
		for (int i = 0; i < numCopies; i++) {
			TablecolumnNames[i] = "";
		}
		DefaultTableModel Tablemodel = new DefaultTableModel(Tabledata, TablecolumnNames);
		JTable resultTable = new JTable(Tablemodel) {
			private static final long serialVersionUID = 1L;

			public String getToolTipText(MouseEvent event) {
				Point p = event.getPoint();
				int row = rowAtPoint(p);
				int col = columnAtPoint(p);

				if (row == -1 || col == -1) {
					return null;
				}
				if (Tablemodel.getValueAt(row, col) != null && Tablemodel.getValueAt(row, col) != "") {
					if (Tablemodel.getValueAt(row, col) instanceof Double) {
						return Tablemodel.getValueAt(row, col).toString();
					} else if (Tablemodel.getValueAt(row, col) instanceof Integer) {
						return Tablemodel.getValueAt(row, col).toString();
					} else {
						return (String) Tablemodel.getValueAt(row, col);
					}
				}
				return null;
			}

			public Point getToolTipLocation(MouseEvent event) {
				Point p = event.getPoint();
				int row = rowAtPoint(p);
				int col = columnAtPoint(p);

				if (row == -1 || col == -1) {
					return null;
				}
				Rectangle cell = getCellRect(row, col, true);
				return new Point(cell.x + cell.width / 2, cell.y + cell.height / 2);
			}
		};
		resultTable.getTableHeader().setReorderingAllowed(false); // disable column reordering
		resultTable.setEnabled(false);
		resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		resultTable.setGridColor(Color.GRAY);
		resultTable.setShowGrid(true);
		for (int col = 0; col < Tablemodel.getColumnCount(); col++) {
			resultTable.getColumnModel().getColumn(col).setPreferredWidth(200);
		}
		TargetCells.put(new ArrayList<>(Arrays.asList(0, 1)), Color.RED);
		TargetCells.put(new ArrayList<>(Arrays.asList(1, 1)), Color.RED);
		IndexOfTargetCells.add(new ArrayList<>(Arrays.asList(0, 1)));
		IndexOfTargetCells.add(new ArrayList<>(Arrays.asList(1, 1)));
		resultTable.setDefaultRenderer(Object.class, createCustomTableCellRenderer(TargetCells));
		JScrollPane scrollTablePane = new JScrollPane(resultTable);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scroll, scrollTablePane);
		splitPane.setOneTouchExpandable(true);

		add(splitPane);

		// add legend panel
		JPanel legendPanel = new LegendPanelConf();

		// add view panel (zoom in/out)
		viewPanel = createViewPanel(this, MAX_ZOOM);
		addInteractionViewports(viewPanel);

		parentviewPanel.add(miniembeddedconfig.getFatherModel());
		childviewPanel.add(miniembeddedconfig.getSonModel());

		JPanel operationPanel = new JPanel(new GridBagLayout());
		operationPanel.setPreferredSize(new Dimension(290, 80));
		JButton goBack = new JButton("  Go Back  ");
		JButton goForward = new JButton("  Forward  ");
		goBack.setEnabled(false);
		goForward.setEnabled(false);
		goBack.addActionListener(new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				Object[] removedRow2 = new Object[fixedmaxNumOfAct + 1];
				Object[] removedRow1 = new Object[fixedmaxNumOfAct + 1];
				Object[][] removedRows = new Object[2][fixedmaxNumOfAct + 1];
				if (graphCount == 0) {
					goBack.setEnabled(false);
					opeNote1.setText("***It's already the root node***");
					opeNote1.setForeground(Color.RED);
					UrlArea.setText("\"Select an edge\"");
				} else {
					for (int columnCount = 0; columnCount < fixedmaxNumOfAct + 1; columnCount++) {
						removedRow2[columnCount] = Tablemodel.getValueAt(Tablemodel.getRowCount() - 1, columnCount);
					}
					Tablemodel.removeRow(Tablemodel.getRowCount() - 1);
					for (int columnCount = 0; columnCount < fixedmaxNumOfAct + 1; columnCount++) {
						removedRow1[columnCount] = Tablemodel.getValueAt(Tablemodel.getRowCount() - 1, columnCount);
					}
					Tablemodel.removeRow(Tablemodel.getRowCount() - 1);
					removedRows[0] = removedRow1;
					removedRows[1] = removedRow2;
					OldTableRows4Forward.add(removedRows);
					Tablemodel.setValueAt("", Tablemodel.getRowCount() - 2, 0);
					Tablemodel.setValueAt("", Tablemodel.getRowCount() - 1, 0);
					OldRootModelLists4Forward.add(RootModelLists.get(RootModelLists.size() - 1));
					OldRootModelValueLists4Forward.add(RootModelValueLists.get(RootModelLists.size() - 1));
					RootModelLists.remove(RootModelLists.size() - 1);
					RootModelValueLists.remove(RootModelValueLists.size() - 1);
					for (int di = 0; di < 4; di++) {
						OldTargetCells.put(IndexOfTargetCells.get(IndexOfTargetCells.size() - 1),
								TargetCells.get(IndexOfTargetCells.get(IndexOfTargetCells.size() - 1)));
						TargetCells.remove(IndexOfTargetCells.get(IndexOfTargetCells.size() - 1));
						OldIndexOfTargetCells.add(IndexOfTargetCells.get(IndexOfTargetCells.size() - 1));
						IndexOfTargetCells.remove(IndexOfTargetCells.size() - 1);
					}
					OldpreviousResult.add(previousResult.get(previousResult.size() - 1));
					previousResult.remove(previousResult.get(previousResult.size() - 1));

					if (graphCount == fixedmaxNumOfAct - 2) {
						for (int bcount = 0; bcount < maxValueCount4bottom * 2; bcount++) {
							OldTargetCells.put(IndexOfTargetCells.get(IndexOfTargetCells.size() - 1),
									TargetCells.get(IndexOfTargetCells.get(IndexOfTargetCells.size() - 1)));
							TargetCells.remove(IndexOfTargetCells.get(IndexOfTargetCells.size() - 1));
							OldIndexOfTargetCells.add(IndexOfTargetCells.get(IndexOfTargetCells.size() - 1));
							IndexOfTargetCells.remove(IndexOfTargetCells.size() - 1);
						}
					}

					resultTable.setDefaultRenderer(Object.class, createCustomTableCellRenderer(TargetCells));

					goForward.setEnabled(true);
					graph.getGraphLayoutCache().remove(GraphRepo.get(graphCount).cells);
					graph.getGraphLayoutCache().remove(GraphRepo.get(graphCount).eventcells);
					graph.getGraphLayoutCache().remove(GraphRepo.get(graphCount).activitycells);
					for (DefaultEdge edgei : GraphRepo.get(graphCount).alledgelist) {
						graph.getGraphLayoutCache().remove(new Object[] { edgei });
					}
					GraphForward.add(GraphRepo.get(graphCount));
					GraphRepo.remove(graphCount);
					graphCount = graphCount - 1;
					graph.getGraphLayoutCache().insert(GraphRepo.get(graphCount).cells);
					graph.getGraphLayoutCache().insert(GraphRepo.get(graphCount).eventcells);
					graph.getGraphLayoutCache().insert(GraphRepo.get(graphCount).activitycells);

					DefaultGraphCell[] tempcells = GraphRepo.get(graphCount).cells;

					DefaultPort portS = new DefaultPort();
					GraphConstants.setOffset(portS.getAttributes(),
							new Point2D.Double(GraphConstants.PERMILLE / 2, nodeWidth + infonodeY));
					GraphConstants.setAbsoluteY(portS.getAttributes(), true);
					tempcells[0].add(portS);

					String targetStr;
					if (GraphRepo.get(graphCount).literesults == null) {
						targetStr = result.get(0).sublog;
					} else {
						targetStr = GraphRepo.get(graphCount).literesults.get(0).name;
					}

					if (!GraphRepo.get(graphCount).childList.get(targetStr).get(0).equals("")) {
						for (String child : GraphRepo.get(graphCount).childList.get(targetStr)) {
							String TgrFatherName = targetStr;
							String TgrSonName = child;
							String DeletedEle = "";
							for (String Fele : TgrFatherName.split(",")) {
								int Countdiff = 0;
								for (String Sele : TgrSonName.split(",")) {
									if (Fele.equals(Sele)) {
										Countdiff = 1;
										break;
									}
								}
								if (Countdiff == 0) {
									DeletedEle = Fele;
									break;
								}
							}

							double FatherValue = 0.0, SonValue = 0.0, diffFS = 1.0;
							Color edgeCol = null;

							if (GraphRepo.get(graphCount).literesults == null) {
								for (Result resulti : result) {
									if (resulti.sublog.equals(targetStr)) {
										FatherValue = getTypeValue(resulti);
									}
									if (resulti.sublog.equals(child)) {
										SonValue = getTypeValue(resulti);
									}
								}

							} else {
								for (ResultUnit resulti : GraphRepo.get(graphCount).literesults) {
									if (resulti.name.equals(targetStr)) {
										FatherValue = getTypeValue2(resulti);
									}
									if (resulti.name.equals(child)) {
										SonValue = getTypeValue2(resulti);
									}
								}
							}
							if (Double.isNaN(FatherValue) || Double.isNaN(SonValue)) {
								diffFS = 1.0;
								edgeCol = Color.BLACK;
							} else if (FatherValue == SonValue) {
								diffFS = 1.0;
								edgeCol = Color.GREEN;
							} else if (FatherValue > SonValue) {
								diffFS = FatherValue - SonValue;
								edgeCol = Color.BLUE;
							} else if (FatherValue < SonValue) {
								diffFS = SonValue - FatherValue;
								edgeCol = Color.RED;
							}

							DefaultPort portE = new DefaultPort();
							GraphConstants.setOffset(portE.getAttributes(),
									new Point2D.Double(GraphConstants.PERMILLE / 2, 0));
							GraphRepo.get(graphCount).mapSublogNode.get(child).add(portE);

							DefaultEdge edge = new DefaultEdge(DeletedEle);
							int arrow = GraphConstants.ARROW_CLASSIC;
							GraphConstants.setLineColor(edge.getAttributes(), edgeCol);
							GraphConstants.setForeground(edge.getAttributes(), Color.BLACK);
							GraphConstants.setFont(edge.getAttributes(), BLFont);
							GraphConstants.setLabelAlongEdge(edge.getAttributes(), true);
							GraphConstants.setLineEnd(edge.getAttributes(), arrow);
							GraphConstants.setEndFill(edge.getAttributes(), true);
							GraphConstants.setLineWidth(edge.getAttributes(), (float) (2.0 * diffFS));
							graph.getGraphLayoutCache().insertEdge(edge, portS, portE);
							GraphRepo.get(graphCount).alledgelist.add(edge);
							GraphRepo.get(graphCount).mapSublogNode.get(child).remove(portE);
							List<String> TempSublogPair = new ArrayList<String>();
							TempSublogPair.add(targetStr);
							TempSublogPair.add(child);
							GraphRepo.get(graphCount).mapEdgeSublogPair.put(edge, TempSublogPair);
						}
					} else {
						;
					}
					tempcells[0].remove(portS);

					parentviewPanel.remove(miniembeddedconfig.getFatherModel());
					childviewPanel.remove(miniembeddedconfig.getSonModel());

					TmpFM = EmptyFModel;
					TmpSM = EmptySModel;

					parentviewPanel.add(TmpFM);
					childviewPanel.add(TmpSM);

					miniembeddedconfig.setFatherModel(TmpFM);
					miniembeddedconfig.setSonModel(TmpSM);

					maxNumOfAct = maxNumOfAct - 1;
					UrlArea.setText("\"Select an edge\"");
				}
				goDeep.setEnabled(false);
				opeNote2.setText("Select a child node and go deeper");
				opeNote2.setForeground(Color.BLACK);
			}
		});
		Object[][] NewRow4Table = new Object[2][fixedmaxNumOfAct + 1];
		goDeep.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			public void actionPerformed(java.awt.event.ActionEvent e) {
				UrlArea.setText("\"Select an edge\"");
				if (TgrStartModel.split(",").length == 1) {
					goDeep.setEnabled(false);
					opeNote2.setText("**It's already the deepest node**");
					opeNote2.setForeground(Color.RED);
				} else {
					JDialog runningmessagedialog = new JDialog(new JFrame(), "Running...Please Wait...", true);
					runningmessagedialog.setPreferredSize(new Dimension(300, 0));
					runningmessagedialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					runningmessagedialog.pack();
					runningmessagedialog.setLocationRelativeTo(null);
					runningmessagedialog.setModal(false);
					runningmessagedialog.setVisible(true);
					OldTableRows4Forward.clear();
					OldRootModelLists4Forward.clear();
					OldRootModelValueLists4Forward.clear();
					OldTargetCells.clear();
					OldIndexOfTargetCells.clear();
					OldpreviousResult.clear();
					goDeep.setEnabled(false);
					goForward.setEnabled(false);
					graph.getGraphLayoutCache().remove(GraphRepo.get(graphCount).cells);
					graph.getGraphLayoutCache().remove(GraphRepo.get(graphCount).eventcells);
					graph.getGraphLayoutCache().remove(GraphRepo.get(graphCount).activitycells);
					for (DefaultEdge edgei : GraphRepo.get(graphCount).alledgelist) {
						graph.getGraphLayoutCache().remove(new Object[] { edgei });
					}
					graphCount = graphCount + 1;
					goBack.setEnabled(true);
					opeNote1.setText("Go back to the last node");
					opeNote1.setForeground(Color.BLACK);

					GraphForward.clear();

					// get subset
					List<HashSet<String>> subsets = GetChildset(TgrStartModel);

					ResultUnitList literesults = new ResultUnitList();
					// for each projection
					for (HashSet<String> subset : subsets) {
						StringBuffer sb = new StringBuffer();
						for (String e1 : subset) {
							if (sb.length() != 0) {
								sb.append(",");
							}
							sb.append(e1);
						}

						int tempActivityCount = subset.size();

						XLog tgr = GetProjection(miniembeddedconfig.getInputLog(), subset).getFirst(); // step1
						int tempEventCount = GetProjection(miniembeddedconfig.getInputLog(), subset).getSecond();

						Object[] tempIMresult = IMPetriNet.minePetriNet(context, tgr,
								miniembeddedconfig.getDialog().getMiningParameters());

						InitialMarkingConnection Conbigstuff1 = new InitialMarkingConnection((Petrinet) tempIMresult[0],
								(Marking) tempIMresult[1]);
						context.addConnection(Conbigstuff1);
						Conbigstuff1 = null;
						FinalMarkingConnection Conbigstuff2 = new FinalMarkingConnection((Petrinet) tempIMresult[0],
								(Marking) tempIMresult[2]);
						context.addConnection(Conbigstuff2);
						Conbigstuff2 = null;

						PetrinetGraph tmpNet = (Petrinet) tempIMresult[0];

						// replayer
						// init local parameter
						EvClassLogPetrinetConnection conn1 = null;

						int retflag = 1;
						PNRepResult ret = null;

						// check existence of initial marking
						try {
							InitialMarkingConnection initCon = context.getConnectionManager()
									.getFirstConnection(InitialMarkingConnection.class, context, tmpNet);

							tgrmark = (Marking) initCon.getObjectWithRole(InitialMarkingConnection.MARKING);
						} catch (ConnectionCannotBeObtained exc) {
							;
						} catch (Exception e1) {
							e1.printStackTrace();
						}

						// check existence of final marking
						try {
							context.getConnectionManager().getFirstConnection(FinalMarkingConnection.class, context,
									tmpNet);
						} catch (ConnectionCannotBeObtained exc) {
							;
						} catch (Exception e1) {
							e1.printStackTrace();
						}

						// check connection in order to determine whether mapping step is needed
						// of not
						try {
							// connection is found, no need for mapping step
							// connection is not found, another plugin to create such connection
							// is automatically
							// executed

							conn1 = context.getConnectionManager()
									.getFirstConnection(EvClassLogPetrinetConnection.class, context, tmpNet, tgr);
						} catch (Exception e1) {
							return;
						}

						TransEvClassMapping mapping1 = null;

						if (retflag == 1) {
							// init gui for each step
							mapping1 = (TransEvClassMapping) conn1
									.getObjectWithRole(EvClassLogPetrinetConnection.TRANS2EVCLASSMAPPING);

							replaySteps = new JComponent[nofSteps];
							replaySteps[0] = new PNAlgorithmStep(context, tmpNet, tgr, mapping1);

							// set current step
							currentStep = 0;

							Object[] resultConfiguration = showConfiguration((UIPluginContext) context, tgr, tmpNet,
									mapping1);

							// This connection MUST exists, as it is constructed by the configuration if necessary
							try {
								context.getConnectionManager().getFirstConnection(EvClassLogPetrinetConnection.class,
										context, tmpNet, tgr);
							} catch (ConnectionCannotBeObtained e2) {
								e2.printStackTrace();
							}

							// get all parameters
							IPNReplayAlgorithm selectedAlg = (IPNReplayAlgorithm) resultConfiguration[PNReplayerUI.ALGORITHM];
							IPNReplayParameter algParameters = (IPNReplayParameter) resultConfiguration[PNReplayerUI.PARAMETERS];

							ret = replayLogPrivate(context, tmpNet, tgr,
									(TransEvClassMapping) resultConfiguration[PNReplayerUI.MAPPING], selectedAlg,
									algParameters);
							selectedAlg = null;
							algParameters = null;
						} else {
							;
						}

						if (ret != null) {
							double fitness = (double) ret.getInfo().get(PNRepResult.TRACEFITNESS);

							// alignment
							ConnectionManager connManager = context.getConnectionManager();
							EvClassLogPetrinetConnection conn = null;
							try {
								conn = connManager.getFirstConnection(EvClassLogPetrinetConnection.class, context,
										tmpNet, tgr);
							} catch (ConnectionCannotBeObtained e2) {
								e2.printStackTrace();
							}
							TransEvClassMapping mapping = (TransEvClassMapping) conn
									.getObjectWithRole(EvClassLogPetrinetConnection.TRANS2EVCLASSMAPPING);

							// get marking
							InitialMarkingConnection initMarkingConn = null;
							try {
								initMarkingConn = connManager.getFirstConnection(InitialMarkingConnection.class,
										context, tmpNet);
							} catch (ConnectionCannotBeObtained e2) {
								e2.printStackTrace();
							}
							Marking initMarking = initMarkingConn.getObjectWithRole(InitialMarkingConnection.MARKING);

							AlignmentPrecGen precGen = new AlignmentPrecGen();
							AlignmentPrecGenRes ret2 = precGen.measureConformanceAssumingCorrectAlignment(context,
									mapping, ret, (Petrinet) tmpNet, initMarking, true);
							precGen = null;

							double precision = ret2.getPrecision();
							ret2 = null;
							double f1 = 2.0 / ((1 / precision) + (1 / fitness));

							ResultUnit r = new ResultUnit();
							r.name = sb.toString();
							r.fitness = fitness;
							r.precision = precision;
							r.f1 = f1;
							r.eventcount = tempEventCount;
							r.activitycount = tempActivityCount;
							literesults.add(r);

							mapping1 = null;
							ret = null;
							r = null;
							tempIMresult = null;
						} else {
							tempIMresult = null;

							double fitness = 0.0 / 0.0;
							double precision = 0.0 / 0.0;
							double f1 = 0.0 / 0.0;

							ResultUnit r = new ResultUnit();
							r.name = sb.toString();
							r.fitness = fitness;
							r.precision = precision;
							r.f1 = f1;
							r.eventcount = tempEventCount;
							r.activitycount = tempActivityCount;
							literesults.add(r);
							r = null;
						}

						sb = null;
						tgr = null;
						tmpNet = null;
					}

					String BestRemoveEle = "";
					for (String Fele : RootModelLists.get(RootModelLists.size() - 1).split(",")) {
						int Countdiff = 0;
						for (String Sele : TgrStartModel.split(",")) {
							if (Fele.equals(Sele)) {
								Countdiff = 1;
								break;
							}
						}
						if (Countdiff == 0) {
							BestRemoveEle = Fele;
							break;
						}
					}

					childList = new HashMap<String, List<String>>();
					//					double resultMinValue = 99.99;
					//					double resultMaxValue = -99.99;
					double resultMinValue4son = 99.99;
					double resultMaxValue4son = -99.99;
					boolean isFirstResult = true;
					for (ResultUnit r : literesults) {
						if (isFirstResult) {
							isFirstResult = false;
						} else {
							if (getTypeValue2(r) > resultMaxValue4son) {
								resultMaxValue4son = getTypeValue2(r);
							}
							if (getTypeValue2(r) < resultMinValue4son) {
								resultMinValue4son = getTypeValue2(r);
							}
						}
						if (getTypeValue2(r) > resultMaxValue) {
							resultMaxValue = getTypeValue2(r);
						}
						if (getTypeValue2(r) < resultMinValue) {
							resultMinValue = getTypeValue2(r);
						}
						List<String> str = Arrays.asList(r.name.split(","));
						List<String> TempChild = new ArrayList<String>();
						for (int i = 0; i < str.size(); i++) {
							List<String> TempStrList = new ArrayList<String>();
							for (int j = 0; j < str.size(); j++) {
								if (i == j) {
									continue;
								} else {
									TempStrList.add(str.get(j));
								}
							}
							TempChild.add(String.join(",", TempStrList));
						}
						childList.put(r.name, TempChild);
					}

					int TablecellColumn = 0;
					if (graphCount == 1) {
						int tempCount = 0;
						for (Result r : (List<Result>) previousResult.get(previousResult.size() - 1)) {
							if (r.sublog == TgrStartModel) {
								TablecellColumn = tempCount;
								break;
							}
							tempCount = tempCount + 1;
						}
					} else {
						int tempCount = 0;
						for (ResultUnit r : (ResultUnitList) previousResult.get(previousResult.size() - 1)) {
							if (r.name == TgrStartModel) {
								TablecellColumn = tempCount;
								break;
							}
							tempCount = tempCount + 1;
						}
					}
					Color TablecellColor = Color.RED;
					if (RootModelValueLists.get(RootModelValueLists.size() - 1) > getTypeValue2(literesults.get(0))) {
						TablecellColor = Color.BLUE;
					} else if (RootModelValueLists
							.get(RootModelValueLists.size() - 1) < getTypeValue2(literesults.get(0))) {
						TablecellColor = Color.RED;
					} else {
						TablecellColor = limeGreen;
					}

					TargetCells.put(new ArrayList<>(Arrays.asList(graphCount * 2, TablecellColumn)), TablecellColor);
					TargetCells.put(new ArrayList<>(Arrays.asList(graphCount * 2 + 1, TablecellColumn)),
							TablecellColor);
					TargetCells.put(new ArrayList<>(Arrays.asList(graphCount * 2, 0)), TablecellColor);
					TargetCells.put(new ArrayList<>(Arrays.asList(graphCount * 2 + 1, 0)), TablecellColor);
					IndexOfTargetCells.add(new ArrayList<>(Arrays.asList(graphCount * 2, TablecellColumn)));
					IndexOfTargetCells.add(new ArrayList<>(Arrays.asList(graphCount * 2 + 1, TablecellColumn)));
					IndexOfTargetCells.add(new ArrayList<>(Arrays.asList(graphCount * 2, 0)));
					IndexOfTargetCells.add(new ArrayList<>(Arrays.asList(graphCount * 2 + 1, 0)));
					previousResult.add(literesults);

					if (graphCount == fixedmaxNumOfAct - 2) {
						boolean isFirstResult4bottom = true;
						int tempCount4bottom = 0;
						Color TablecellColor4bottom = Color.RED;
						for (ResultUnit r : literesults) {
							if (isFirstResult4bottom) {
								isFirstResult4bottom = false;
							} else {
								if (getTypeValue2(r) == resultMaxValue4son) {
									maxValueCount4bottom = maxValueCount4bottom + 1;
									if (getTypeValue2(literesults.get(0)) > getTypeValue2(r)) {
										TablecellColor4bottom = Color.BLUE;
									} else if (getTypeValue2(literesults.get(0)) < getTypeValue2(r)) {
										TablecellColor4bottom = Color.RED;
									} else {
										TablecellColor4bottom = limeGreen;
									}
									TargetCells.put(
											new ArrayList<>(Arrays.asList((graphCount + 1) * 2, tempCount4bottom)),
											TablecellColor4bottom);
									TargetCells.put(
											new ArrayList<>(Arrays.asList((graphCount + 1) * 2 + 1, tempCount4bottom)),
											TablecellColor4bottom);
									IndexOfTargetCells.add(
											new ArrayList<>(Arrays.asList((graphCount + 1) * 2, tempCount4bottom)));
									IndexOfTargetCells.add(
											new ArrayList<>(Arrays.asList((graphCount + 1) * 2 + 1, tempCount4bottom)));
								}
							}
							tempCount4bottom = tempCount4bottom + 1;
						}
					} else {

					}

					resultTable.setDefaultRenderer(Object.class, createCustomTableCellRenderer(TargetCells));

					Tablemodel.setValueAt(graphCount, Tablemodel.getRowCount() - 2, 0);
					Tablemodel.setValueAt(BestRemoveEle, Tablemodel.getRowCount() - 1, 0);
					RootModelLists.add(TgrStartModel);
					RootModelValueLists.add(getTypeValue2(literesults.get(0)));

					maxNumOfAct = literesults.get(0).name.split(",").length;
					int nodesNum = literesults.size();
					int nodeStepLenX = 100;
					int nodeStepLenY = 250;
					int nodeWidth = 30;
					int infonodeX = 30;
					int infonodeY = 5;
					int initialNodeX = (int) Math.ceil((double) maxNumOfAct / 2) * (nodeStepLenX + nodeWidth) + 20;
					int initialNodeY = 20;
					mapSublogNode = new HashMap<String, DefaultGraphCell>();
					HashMap<List<String>, DefaultGraphCell> EventActivityNode = new HashMap<List<String>, DefaultGraphCell>();

					// test demo
					DefaultGraphCell[] cells = new DefaultGraphCell[nodesNum];
					DefaultGraphCell[] eventcells = new DefaultGraphCell[nodesNum];
					DefaultGraphCell[] activitycells = new DefaultGraphCell[nodesNum];

					int nodeCellCount = 0;
					int startX = initialNodeX, startY = initialNodeY;
					int tempMaxNumOfAct = maxNumOfAct;
					int stepX = startX;
					Color cellColor;
					int LayerElementsCount4Table = 1;
					boolean isFirstElementOfRow = true;
					isFirstResult = true;
					for (ResultUnit i : literesults) {
						if (isFirstElementOfRow) {
							isFirstElementOfRow = false;
						} else {
							NewRow4Table[0][LayerElementsCount4Table] = i.name;
							NewRow4Table[1][LayerElementsCount4Table] = getTypeValue2(i);
							LayerElementsCount4Table = LayerElementsCount4Table + 1;
						}
						int layerNodesNum = i.name.split(",").length;
						cellColor = colorcalc(getTypeValue2(i), resultMaxValue, resultMinValue);

						List<String> EventAndActivity = new ArrayList<>();
						double Eventnum = (double) Math.round(((long) i.eventcount * 10000) / TotalEventCount) / 100;
						double Activitynum = (double) Math.round(((long) i.activitycount * 10000) / TotalActivityCount)
								/ 100;
						String Eventstr = Double.toString(Eventnum) + "%";
						String Activitystr = Double.toString(Activitynum) + "%";
						EventAndActivity.add(Eventstr);
						EventAndActivity.add(Activitystr);

						int tempCom = layerNodesNum + 1;
						if (tempCom == 1) {
							startX = initialNodeX;
						} else if (tempCom != 1 && tempCom % 2 == 1) {
							startX = initialNodeX - (int) Math.floor((double) tempCom / 2) * (nodeStepLenX + nodeWidth);
						} else if (tempCom % 2 == 0) {
							startX = initialNodeX - (int) Math.floor((double) tempCom / 2) * (nodeStepLenX + nodeWidth)
									+ nodeStepLenX / 2 + nodeWidth / 2;
						}

						if (layerNodesNum != tempMaxNumOfAct) {
							stepX = startX;
							startY = startY + nodeStepLenY;
							tempMaxNumOfAct = layerNodesNum;
						}

						if (isFirstResult) {
							cells[nodeCellCount] = new DefaultGraphCell(
									new String(Double.toString((double) Math.round(getTypeValue2(i) * 100) / 100)));
							GraphConstants.setForeground(cells[nodeCellCount].getAttributes(), Color.WHITE);
							GraphConstants.setFont(cells[nodeCellCount].getAttributes(), ITALICFont);
							GraphConstants.setBounds(cells[nodeCellCount].getAttributes(),
									new Rectangle2D.Double(stepX, startY, nodeWidth, nodeWidth));
							GraphConstants.setBackground(cells[nodeCellCount].getAttributes(), cellColor);
							GraphConstants.setOpaque(cells[nodeCellCount].getAttributes(), true);
							GraphConstants.setMoveable(cells[nodeCellCount].getAttributes(), false); // 不可移动
							GraphConstants.setSizeable(cells[nodeCellCount].getAttributes(), false); // 不可调整大小

							eventcells[nodeCellCount] = new DefaultGraphCell(new String(Eventstr));
							GraphConstants.setForeground(eventcells[nodeCellCount].getAttributes(), Color.WHITE);
							GraphConstants.setFont(eventcells[nodeCellCount].getAttributes(), SITALICFont);
							GraphConstants.setBounds(eventcells[nodeCellCount].getAttributes(), new Rectangle2D.Double(
									stepX, startY + nodeWidth, infonodeX * (Eventnum / 100), infonodeY));
							GraphConstants.setBackground(eventcells[nodeCellCount].getAttributes(), Color.DARK_GRAY);
							GraphConstants.setOpaque(eventcells[nodeCellCount].getAttributes(), true);
							GraphConstants.setMoveable(eventcells[nodeCellCount].getAttributes(), false); // 不可移动
							GraphConstants.setSizeable(eventcells[nodeCellCount].getAttributes(), false); // 不可调整大小
							GraphConstants.setSelectable(eventcells[nodeCellCount].getAttributes(), false); // 不可选中

							activitycells[nodeCellCount] = new DefaultGraphCell(new String(Activitystr));
							GraphConstants.setForeground(activitycells[nodeCellCount].getAttributes(), Color.WHITE);
							GraphConstants.setFont(activitycells[nodeCellCount].getAttributes(), SITALICFont);
							GraphConstants.setBounds(activitycells[nodeCellCount].getAttributes(),
									new Rectangle2D.Double(stepX + nodeWidth, startY, infonodeY,
											infonodeX * (Activitynum / 100)));
							GraphConstants.setBackground(activitycells[nodeCellCount].getAttributes(), Color.ORANGE);
							GraphConstants.setOpaque(activitycells[nodeCellCount].getAttributes(), true);
							GraphConstants.setMoveable(activitycells[nodeCellCount].getAttributes(), false); // 不可移动
							GraphConstants.setSizeable(activitycells[nodeCellCount].getAttributes(), false); // 不可调整大小
							GraphConstants.setSelectable(activitycells[nodeCellCount].getAttributes(), false); // 不可选中
							isFirstResult = false;
						} else {
							if (getTypeValue2(i) == resultMaxValue4son) {
								cells[nodeCellCount] = new DefaultGraphCell(
										new String(Double.toString((double) Math.round(getTypeValue2(i) * 100) / 100)));
								GraphConstants.setForeground(cells[nodeCellCount].getAttributes(), Color.WHITE);
								GraphConstants.setFont(cells[nodeCellCount].getAttributes(), ITALICFont);
								GraphConstants.setBounds(cells[nodeCellCount].getAttributes(),
										new Rectangle2D.Double(stepX, startY, nodeWidth, nodeWidth));
								GraphConstants.setBackground(cells[nodeCellCount].getAttributes(), cellColor);
								GraphConstants.setOpaque(cells[nodeCellCount].getAttributes(), true);
								GraphConstants.setBorderColor(cells[nodeCellCount].getAttributes(), Color.GREEN);
								GraphConstants.setLineWidth(cells[nodeCellCount].getAttributes(), (float) 3.0);
								GraphConstants.setMoveable(cells[nodeCellCount].getAttributes(), false); // 不可移动
								GraphConstants.setSizeable(cells[nodeCellCount].getAttributes(), false); // 不可调整大小

								eventcells[nodeCellCount] = new DefaultGraphCell(new String(Eventstr));
								GraphConstants.setForeground(eventcells[nodeCellCount].getAttributes(), Color.WHITE);
								GraphConstants.setFont(eventcells[nodeCellCount].getAttributes(), SITALICFont);
								GraphConstants.setBounds(eventcells[nodeCellCount].getAttributes(),
										new Rectangle2D.Double(stepX, startY + nodeWidth, infonodeX * (Eventnum / 100),
												infonodeY));
								GraphConstants.setBackground(eventcells[nodeCellCount].getAttributes(),
										Color.DARK_GRAY);
								GraphConstants.setOpaque(eventcells[nodeCellCount].getAttributes(), true);
								GraphConstants.setMoveable(eventcells[nodeCellCount].getAttributes(), false); // 不可移动
								GraphConstants.setSizeable(eventcells[nodeCellCount].getAttributes(), false); // 不可调整大小
								GraphConstants.setSelectable(eventcells[nodeCellCount].getAttributes(), false); // 不可选中

								activitycells[nodeCellCount] = new DefaultGraphCell(new String(Activitystr));
								GraphConstants.setForeground(activitycells[nodeCellCount].getAttributes(), Color.WHITE);
								GraphConstants.setFont(activitycells[nodeCellCount].getAttributes(), SITALICFont);
								GraphConstants.setBounds(activitycells[nodeCellCount].getAttributes(),
										new Rectangle2D.Double(stepX + nodeWidth, startY, infonodeY,
												infonodeX * (Activitynum / 100)));
								GraphConstants.setBackground(activitycells[nodeCellCount].getAttributes(),
										Color.ORANGE);
								GraphConstants.setOpaque(activitycells[nodeCellCount].getAttributes(), true);
								GraphConstants.setMoveable(activitycells[nodeCellCount].getAttributes(), false); // 不可移动
								GraphConstants.setSizeable(activitycells[nodeCellCount].getAttributes(), false); // 不可调整大小
								GraphConstants.setSelectable(activitycells[nodeCellCount].getAttributes(), false); // 不可选中
							} else {
								cells[nodeCellCount] = new DefaultGraphCell(
										new String(Double.toString((double) Math.round(getTypeValue2(i) * 100) / 100)));
								GraphConstants.setForeground(cells[nodeCellCount].getAttributes(), Color.WHITE);
								GraphConstants.setFont(cells[nodeCellCount].getAttributes(), ITALICFont);
								GraphConstants.setBounds(cells[nodeCellCount].getAttributes(),
										new Rectangle2D.Double(stepX, startY, nodeWidth, nodeWidth));
								GraphConstants.setBackground(cells[nodeCellCount].getAttributes(), cellColor);
								GraphConstants.setOpaque(cells[nodeCellCount].getAttributes(), true);
								GraphConstants.setMoveable(cells[nodeCellCount].getAttributes(), false); // 不可移动
								GraphConstants.setSizeable(cells[nodeCellCount].getAttributes(), false); // 不可调整大小

								eventcells[nodeCellCount] = new DefaultGraphCell(new String(Eventstr));
								GraphConstants.setForeground(eventcells[nodeCellCount].getAttributes(), Color.WHITE);
								GraphConstants.setFont(eventcells[nodeCellCount].getAttributes(), SITALICFont);
								GraphConstants.setBounds(eventcells[nodeCellCount].getAttributes(),
										new Rectangle2D.Double(stepX, startY + nodeWidth, infonodeX * (Eventnum / 100),
												infonodeY));
								GraphConstants.setBackground(eventcells[nodeCellCount].getAttributes(),
										Color.DARK_GRAY);
								GraphConstants.setOpaque(eventcells[nodeCellCount].getAttributes(), true);
								GraphConstants.setMoveable(eventcells[nodeCellCount].getAttributes(), false); // 不可移动
								GraphConstants.setSizeable(eventcells[nodeCellCount].getAttributes(), false); // 不可调整大小
								GraphConstants.setSelectable(eventcells[nodeCellCount].getAttributes(), false); // 不可选中

								activitycells[nodeCellCount] = new DefaultGraphCell(new String(Activitystr));
								GraphConstants.setForeground(activitycells[nodeCellCount].getAttributes(), Color.WHITE);
								GraphConstants.setFont(activitycells[nodeCellCount].getAttributes(), SITALICFont);
								GraphConstants.setBounds(activitycells[nodeCellCount].getAttributes(),
										new Rectangle2D.Double(stepX + nodeWidth, startY, infonodeY,
												infonodeX * (Activitynum / 100)));
								GraphConstants.setBackground(activitycells[nodeCellCount].getAttributes(),
										Color.ORANGE);
								GraphConstants.setOpaque(activitycells[nodeCellCount].getAttributes(), true);
								GraphConstants.setMoveable(activitycells[nodeCellCount].getAttributes(), false); // 不可移动
								GraphConstants.setSizeable(activitycells[nodeCellCount].getAttributes(), false); // 不可调整大小
								GraphConstants.setSelectable(activitycells[nodeCellCount].getAttributes(), false); // 不可选中
							}
						}

						mapSublogNode.put(i.name, cells[nodeCellCount]);
						EventActivityNode.put(EventAndActivity, cells[nodeCellCount]);

						if (layerNodesNum == tempMaxNumOfAct) {
							stepX = stepX + nodeStepLenX + nodeWidth;
						}

						nodeCellCount++;
					}
					for (int lastCount = LayerElementsCount4Table; lastCount < fixedmaxNumOfAct + 1; lastCount++) {
						NewRow4Table[0][LayerElementsCount4Table] = "";
						NewRow4Table[1][LayerElementsCount4Table] = "";
					}
					for (Object[] obj : NewRow4Table) {
						Tablemodel.addRow(obj);
					}

					DefaultPort portS = new DefaultPort();
					GraphConstants.setOffset(portS.getAttributes(),
							new Point2D.Double(GraphConstants.PERMILLE / 2, nodeWidth + infonodeY));
					GraphConstants.setAbsoluteY(portS.getAttributes(), true);
					cells[0].add(portS);

					String targetStr = literesults.get(0).name;

					if (!childList.get(targetStr).get(0).equals("")) {
						for (String child : childList.get(targetStr)) {
							String TgrFatherName = targetStr;
							String TgrSonName = child;
							String DeletedEle = "";
							for (String Fele : TgrFatherName.split(",")) {
								int Countdiff = 0;
								for (String Sele : TgrSonName.split(",")) {
									if (Fele.equals(Sele)) {
										Countdiff = 1;
										break;
									}
								}
								if (Countdiff == 0) {
									DeletedEle = Fele;
									break;
								}
							}

							double FatherValue = 0.0, SonValue = 0.0, diffFS = 1.0;
							Color edgeCol = null;
							for (ResultUnit resulti : literesults) {
								if (resulti.name.equals(targetStr)) {
									FatherValue = getTypeValue2(resulti);
								}
								if (resulti.name.equals(child)) {
									SonValue = getTypeValue2(resulti);
								}
							}
							if (Double.isNaN(FatherValue) || Double.isNaN(SonValue)) {
								diffFS = 1.0;
								edgeCol = Color.BLACK;
							} else if (FatherValue == SonValue) {
								diffFS = 1.0;
								edgeCol = Color.GREEN;
							} else if (FatherValue > SonValue) {
								diffFS = FatherValue - SonValue;
								edgeCol = Color.BLUE;
							} else if (FatherValue < SonValue) {
								diffFS = SonValue - FatherValue;
								edgeCol = Color.RED;
							}

							DefaultPort portE = new DefaultPort();
							GraphConstants.setOffset(portE.getAttributes(),
									new Point2D.Double(GraphConstants.PERMILLE / 2, 0));
							mapSublogNode.get(child).add(portE);

							DefaultEdge edge = new DefaultEdge(DeletedEle);
							int arrow = GraphConstants.ARROW_CLASSIC;
							GraphConstants.setLineColor(edge.getAttributes(), edgeCol);
							GraphConstants.setForeground(edge.getAttributes(), Color.BLACK);
							GraphConstants.setFont(edge.getAttributes(), BLFont);
							GraphConstants.setLabelAlongEdge(edge.getAttributes(), true);
							GraphConstants.setLineEnd(edge.getAttributes(), arrow);
							GraphConstants.setEndFill(edge.getAttributes(), true);
							GraphConstants.setLineWidth(edge.getAttributes(), (float) (2.0 * diffFS));
							graph.getGraphLayoutCache().insertEdge(edge, portS, portE);
							alledgelist.add(edge);
							mapSublogNode.get(child).remove(portE);
							List<String> TempSublogPair = new ArrayList<String>();
							TempSublogPair.add(targetStr);
							TempSublogPair.add(child);
							mapEdgeSublogPair.put(edge, TempSublogPair);
						}
					} else {
						;
					}
					cells[0].remove(portS);

					graph.getGraphLayoutCache().insert(cells);
					graph.getGraphLayoutCache().insert(eventcells);
					graph.getGraphLayoutCache().insert(activitycells);

					GraphRepoUnit GraphUni = new GraphRepoUnit();
					GraphUni.cells = cells;
					GraphUni.eventcells = eventcells;
					GraphUni.activitycells = activitycells;
					GraphUni.alledgelist = alledgelist;
					GraphUni.mapSublogNode = mapSublogNode;
					GraphUni.mapEdgeSublogPair = mapEdgeSublogPair;
					GraphUni.literesults = literesults;
					GraphUni.childList = childList;
					GraphRepo.add(GraphUni);

					parentviewPanel.remove(miniembeddedconfig.getFatherModel());
					childviewPanel.remove(miniembeddedconfig.getSonModel());

					TmpFM = EmptyFModel;
					TmpSM = EmptySModel;

					parentviewPanel.add(TmpFM);
					childviewPanel.add(TmpSM);

					miniembeddedconfig.setFatherModel(TmpFM);
					miniembeddedconfig.setSonModel(TmpSM);
					runningmessagedialog.dispose();
				}
				goDeep.setEnabled(false);
			}
		});
		goForward.addActionListener(new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (GraphForward.size() == 0) {
					goDeep.setEnabled(false);
					goForward.setEnabled(false);
				} else if (GraphForward.size() == 1) {
					goDeep.setEnabled(false);
					goForward.setEnabled(false);
					graph.getGraphLayoutCache().remove(GraphRepo.get(graphCount).cells);
					graph.getGraphLayoutCache().remove(GraphRepo.get(graphCount).eventcells);
					graph.getGraphLayoutCache().remove(GraphRepo.get(graphCount).activitycells);
					for (DefaultEdge edgei : GraphRepo.get(graphCount).alledgelist) {
						graph.getGraphLayoutCache().remove(new Object[] { edgei });
					}
					graphCount = graphCount + 1;
					goBack.setEnabled(true);
					opeNote1.setText("Go back to the last node");
					opeNote1.setForeground(Color.BLACK);

					String BestRemoveEle = "";
					for (String Fele : RootModelLists.get(RootModelLists.size() - 1).split(",")) {
						int Countdiff = 0;
						for (String Sele : OldRootModelLists4Forward.get(OldRootModelLists4Forward.size() - 1)
								.split(",")) {
							if (Fele.equals(Sele)) {
								Countdiff = 1;
								break;
							}
						}
						if (Countdiff == 0) {
							BestRemoveEle = Fele;
							break;
						}
					}

					Tablemodel.setValueAt(graphCount, Tablemodel.getRowCount() - 2, 0);
					Tablemodel.setValueAt(BestRemoveEle, Tablemodel.getRowCount() - 1, 0);
					RootModelLists.add(OldRootModelLists4Forward.get(OldRootModelLists4Forward.size() - 1));
					RootModelValueLists
							.add(OldRootModelValueLists4Forward.get(OldRootModelValueLists4Forward.size() - 1));
					OldRootModelLists4Forward.remove(OldRootModelLists4Forward.size() - 1);
					OldRootModelValueLists4Forward.remove(OldRootModelValueLists4Forward.size() - 1);

					for (Object[] obj : OldTableRows4Forward.get(OldTableRows4Forward.size() - 1)) {
						Tablemodel.addRow(obj);
					}
					;
					OldTableRows4Forward.remove(OldTableRows4Forward.size() - 1);

					for (int di = 0; di < 4; di++) {
						TargetCells.put(OldIndexOfTargetCells.get(OldIndexOfTargetCells.size() - 1),
								OldTargetCells.get(OldIndexOfTargetCells.get(OldIndexOfTargetCells.size() - 1)));
						OldTargetCells.remove(OldIndexOfTargetCells.get(OldIndexOfTargetCells.size() - 1));
						IndexOfTargetCells.add(OldIndexOfTargetCells.get(OldIndexOfTargetCells.size() - 1));
						OldIndexOfTargetCells.remove(OldIndexOfTargetCells.size() - 1);
					}
					previousResult.add(OldpreviousResult.get(OldpreviousResult.size() - 1));
					OldpreviousResult.remove(OldpreviousResult.size() - 1);

					if (graphCount == fixedmaxNumOfAct - 2) {
						for (int bcount = 0; bcount < maxValueCount4bottom * 2; bcount++) {
							TargetCells.put(OldIndexOfTargetCells.get(OldIndexOfTargetCells.size() - 1),
									OldTargetCells.get(OldIndexOfTargetCells.get(OldIndexOfTargetCells.size() - 1)));
							OldTargetCells.remove(OldIndexOfTargetCells.get(OldIndexOfTargetCells.size() - 1));
							IndexOfTargetCells.add(OldIndexOfTargetCells.get(OldIndexOfTargetCells.size() - 1));
							OldIndexOfTargetCells.remove(OldIndexOfTargetCells.size() - 1);
						}
					}

					resultTable.setDefaultRenderer(Object.class, createCustomTableCellRenderer(TargetCells));

					GraphRepo.add(GraphForward.get(GraphForward.size() - 1));

					graph.getGraphLayoutCache().insert(GraphForward.get(GraphForward.size() - 1).cells);
					graph.getGraphLayoutCache().insert(GraphForward.get(GraphForward.size() - 1).eventcells);
					graph.getGraphLayoutCache().insert(GraphForward.get(GraphForward.size() - 1).activitycells);

					DefaultGraphCell[] tempcells = GraphForward.get(GraphForward.size() - 1).cells;

					DefaultPort portS = new DefaultPort();
					GraphConstants.setOffset(portS.getAttributes(),
							new Point2D.Double(GraphConstants.PERMILLE / 2, nodeWidth + infonodeY));
					GraphConstants.setAbsoluteY(portS.getAttributes(), true);
					tempcells[0].add(portS);

					String targetStr;
					if (GraphForward.get(GraphForward.size() - 1).literesults == null) {
						targetStr = result.get(0).sublog;
					} else {
						targetStr = GraphForward.get(GraphForward.size() - 1).literesults.get(0).name;
					}

					if (!GraphForward.get(GraphForward.size() - 1).childList.get(targetStr).get(0).equals("")) {
						for (String child : GraphForward.get(GraphForward.size() - 1).childList.get(targetStr)) {
							String TgrFatherName = targetStr;
							String TgrSonName = child;
							String DeletedEle = "";
							for (String Fele : TgrFatherName.split(",")) {
								int Countdiff = 0;
								for (String Sele : TgrSonName.split(",")) {
									if (Fele.equals(Sele)) {
										Countdiff = 1;
										break;
									}
								}
								if (Countdiff == 0) {
									DeletedEle = Fele;
									break;
								}
							}

							double FatherValue = 0.0, SonValue = 0.0, diffFS = 1.0;
							Color edgeCol = null;

							if (GraphForward.get(GraphForward.size() - 1).literesults == null) {
								for (Result resulti : result) {
									if (resulti.sublog.equals(targetStr)) {
										FatherValue = getTypeValue(resulti);
									}
									if (resulti.sublog.equals(child)) {
										SonValue = getTypeValue(resulti);
									}
								}

							} else {
								for (ResultUnit resulti : GraphForward.get(GraphForward.size() - 1).literesults) {
									if (resulti.name.equals(targetStr)) {
										FatherValue = getTypeValue2(resulti);
									}
									if (resulti.name.equals(child)) {
										SonValue = getTypeValue2(resulti);
									}
								}
							}
							if (Double.isNaN(FatherValue) || Double.isNaN(SonValue)) {
								diffFS = 1.0;
								edgeCol = Color.BLACK;
							} else if (FatherValue == SonValue) {
								diffFS = 1.0;
								edgeCol = Color.GREEN;
							} else if (FatherValue > SonValue) {
								diffFS = FatherValue - SonValue;
								edgeCol = Color.BLUE;
							} else if (FatherValue < SonValue) {
								diffFS = SonValue - FatherValue;
								edgeCol = Color.RED;
							}

							DefaultPort portE = new DefaultPort();
							GraphConstants.setOffset(portE.getAttributes(),
									new Point2D.Double(GraphConstants.PERMILLE / 2, 0));
							GraphForward.get(GraphForward.size() - 1).mapSublogNode.get(child).add(portE);

							DefaultEdge edge = new DefaultEdge(DeletedEle);
							int arrow = GraphConstants.ARROW_CLASSIC;
							GraphConstants.setLineColor(edge.getAttributes(), edgeCol);
							GraphConstants.setForeground(edge.getAttributes(), Color.BLACK);
							GraphConstants.setFont(edge.getAttributes(), BLFont);
							GraphConstants.setLabelAlongEdge(edge.getAttributes(), true);
							GraphConstants.setLineEnd(edge.getAttributes(), arrow);
							GraphConstants.setEndFill(edge.getAttributes(), true);
							GraphConstants.setLineWidth(edge.getAttributes(), (float) (2.0 * diffFS));
							graph.getGraphLayoutCache().insertEdge(edge, portS, portE);
							GraphForward.get(GraphForward.size() - 1).alledgelist.add(edge);
							GraphForward.get(GraphForward.size() - 1).mapSublogNode.get(child).remove(portE);
							List<String> TempSublogPair = new ArrayList<String>();
							TempSublogPair.add(targetStr);
							TempSublogPair.add(child);
							GraphForward.get(GraphForward.size() - 1).mapEdgeSublogPair.put(edge, TempSublogPair);
						}
					} else {
						;
					}
					tempcells[0].remove(portS);

					parentviewPanel.remove(miniembeddedconfig.getFatherModel());
					childviewPanel.remove(miniembeddedconfig.getSonModel());

					TmpFM = EmptyFModel;
					TmpSM = EmptySModel;

					parentviewPanel.add(TmpFM);
					childviewPanel.add(TmpSM);

					miniembeddedconfig.setFatherModel(TmpFM);
					miniembeddedconfig.setSonModel(TmpSM);

					maxNumOfAct = maxNumOfAct - 1;
					UrlArea.setText("\"Select an edge\"");

					GraphForward.remove(GraphForward.size() - 1);
				} else {
					goDeep.setEnabled(false);
					graph.getGraphLayoutCache().remove(GraphRepo.get(graphCount).cells);
					graph.getGraphLayoutCache().remove(GraphRepo.get(graphCount).eventcells);
					graph.getGraphLayoutCache().remove(GraphRepo.get(graphCount).activitycells);
					for (DefaultEdge edgei : GraphRepo.get(graphCount).alledgelist) {
						graph.getGraphLayoutCache().remove(new Object[] { edgei });
					}
					graphCount = graphCount + 1;
					goBack.setEnabled(true);
					opeNote1.setText("Go back to the last node");
					opeNote1.setForeground(Color.BLACK);

					String BestRemoveEle = "";
					for (String Fele : RootModelLists.get(RootModelLists.size() - 1).split(",")) {
						int Countdiff = 0;
						for (String Sele : OldRootModelLists4Forward.get(OldRootModelLists4Forward.size() - 1)
								.split(",")) {
							if (Fele.equals(Sele)) {
								Countdiff = 1;
								break;
							}
						}
						if (Countdiff == 0) {
							BestRemoveEle = Fele;
							break;
						}
					}

					Tablemodel.setValueAt(graphCount, Tablemodel.getRowCount() - 2, 0);
					Tablemodel.setValueAt(BestRemoveEle, Tablemodel.getRowCount() - 1, 0);
					RootModelLists.add(OldRootModelLists4Forward.get(OldRootModelLists4Forward.size() - 1));
					RootModelValueLists
							.add(OldRootModelValueLists4Forward.get(OldRootModelValueLists4Forward.size() - 1));
					OldRootModelLists4Forward.remove(OldRootModelLists4Forward.size() - 1);
					OldRootModelValueLists4Forward.remove(OldRootModelValueLists4Forward.size() - 1);

					for (Object[] obj : OldTableRows4Forward.get(OldTableRows4Forward.size() - 1)) {
						Tablemodel.addRow(obj);
					}
					;
					OldTableRows4Forward.remove(OldTableRows4Forward.size() - 1);

					for (int di = 0; di < 4; di++) {
						TargetCells.put(OldIndexOfTargetCells.get(OldIndexOfTargetCells.size() - 1),
								OldTargetCells.get(OldIndexOfTargetCells.get(OldIndexOfTargetCells.size() - 1)));
						OldTargetCells.remove(OldIndexOfTargetCells.get(OldIndexOfTargetCells.size() - 1));
						IndexOfTargetCells.add(OldIndexOfTargetCells.get(OldIndexOfTargetCells.size() - 1));
						OldIndexOfTargetCells.remove(OldIndexOfTargetCells.size() - 1);
					}
					previousResult.add(OldpreviousResult.get(OldpreviousResult.size() - 1));
					OldpreviousResult.remove(OldpreviousResult.size() - 1);

					if (graphCount == fixedmaxNumOfAct - 2) {
						for (int bcount = 0; bcount < maxValueCount4bottom * 2; bcount++) {
							TargetCells.put(OldIndexOfTargetCells.get(OldIndexOfTargetCells.size() - 1),
									OldTargetCells.get(OldIndexOfTargetCells.get(OldIndexOfTargetCells.size() - 1)));
							OldTargetCells.remove(OldIndexOfTargetCells.get(OldIndexOfTargetCells.size() - 1));
							IndexOfTargetCells.add(OldIndexOfTargetCells.get(OldIndexOfTargetCells.size() - 1));
							OldIndexOfTargetCells.remove(OldIndexOfTargetCells.size() - 1);
						}
					}

					resultTable.setDefaultRenderer(Object.class, createCustomTableCellRenderer(TargetCells));

					GraphRepo.add(GraphForward.get(GraphForward.size() - 1));

					graph.getGraphLayoutCache().insert(GraphForward.get(GraphForward.size() - 1).cells);
					graph.getGraphLayoutCache().insert(GraphForward.get(GraphForward.size() - 1).eventcells);
					graph.getGraphLayoutCache().insert(GraphForward.get(GraphForward.size() - 1).activitycells);

					DefaultGraphCell[] tempcells = GraphForward.get(GraphForward.size() - 1).cells;

					DefaultPort portS = new DefaultPort();
					GraphConstants.setOffset(portS.getAttributes(),
							new Point2D.Double(GraphConstants.PERMILLE / 2, nodeWidth + infonodeY));
					GraphConstants.setAbsoluteY(portS.getAttributes(), true);
					tempcells[0].add(portS);

					String targetStr;
					if (GraphForward.get(GraphForward.size() - 1).literesults == null) {
						targetStr = result.get(0).sublog;
					} else {
						targetStr = GraphForward.get(GraphForward.size() - 1).literesults.get(0).name;
					}

					if (!GraphForward.get(GraphForward.size() - 1).childList.get(targetStr).get(0).equals("")) {
						for (String child : GraphForward.get(GraphForward.size() - 1).childList.get(targetStr)) {
							String TgrFatherName = targetStr;
							String TgrSonName = child;
							String DeletedEle = "";
							for (String Fele : TgrFatherName.split(",")) {
								int Countdiff = 0;
								for (String Sele : TgrSonName.split(",")) {
									if (Fele.equals(Sele)) {
										Countdiff = 1;
										break;
									}
								}
								if (Countdiff == 0) {
									DeletedEle = Fele;
									break;
								}
							}

							double FatherValue = 0.0, SonValue = 0.0, diffFS = 1.0;
							Color edgeCol = null;

							if (GraphForward.get(GraphForward.size() - 1).literesults == null) {
								for (Result resulti : result) {
									if (resulti.sublog.equals(targetStr)) {
										FatherValue = getTypeValue(resulti);
									}
									if (resulti.sublog.equals(child)) {
										SonValue = getTypeValue(resulti);
									}
								}

							} else {
								for (ResultUnit resulti : GraphForward.get(GraphForward.size() - 1).literesults) {
									if (resulti.name.equals(targetStr)) {
										FatherValue = getTypeValue2(resulti);
									}
									if (resulti.name.equals(child)) {
										SonValue = getTypeValue2(resulti);
									}
								}
							}
							if (Double.isNaN(FatherValue) || Double.isNaN(SonValue)) {
								diffFS = 1.0;
								edgeCol = Color.BLACK;
							} else if (FatherValue == SonValue) {
								diffFS = 1.0;
								edgeCol = Color.GREEN;
							} else if (FatherValue > SonValue) {
								diffFS = FatherValue - SonValue;
								edgeCol = Color.BLUE;
							} else if (FatherValue < SonValue) {
								diffFS = SonValue - FatherValue;
								edgeCol = Color.RED;
							}

							DefaultPort portE = new DefaultPort();
							GraphConstants.setOffset(portE.getAttributes(),
									new Point2D.Double(GraphConstants.PERMILLE / 2, 0));
							GraphForward.get(GraphForward.size() - 1).mapSublogNode.get(child).add(portE);

							DefaultEdge edge = new DefaultEdge(DeletedEle);
							int arrow = GraphConstants.ARROW_CLASSIC;
							GraphConstants.setLineColor(edge.getAttributes(), edgeCol);
							GraphConstants.setForeground(edge.getAttributes(), Color.BLACK);
							GraphConstants.setFont(edge.getAttributes(), BLFont);
							GraphConstants.setLabelAlongEdge(edge.getAttributes(), true);
							GraphConstants.setLineEnd(edge.getAttributes(), arrow);
							GraphConstants.setEndFill(edge.getAttributes(), true);
							GraphConstants.setLineWidth(edge.getAttributes(), (float) (2.0 * diffFS));
							graph.getGraphLayoutCache().insertEdge(edge, portS, portE);
							GraphForward.get(GraphForward.size() - 1).alledgelist.add(edge);
							GraphForward.get(GraphForward.size() - 1).mapSublogNode.get(child).remove(portE);
							List<String> TempSublogPair = new ArrayList<String>();
							TempSublogPair.add(targetStr);
							TempSublogPair.add(child);
							GraphForward.get(GraphForward.size() - 1).mapEdgeSublogPair.put(edge, TempSublogPair);
						}
					} else {
						;
					}
					tempcells[0].remove(portS);

					parentviewPanel.remove(miniembeddedconfig.getFatherModel());
					childviewPanel.remove(miniembeddedconfig.getSonModel());

					TmpFM = EmptyFModel;
					TmpSM = EmptySModel;

					parentviewPanel.add(TmpFM);
					childviewPanel.add(TmpSM);

					miniembeddedconfig.setFatherModel(TmpFM);
					miniembeddedconfig.setSonModel(TmpSM);

					maxNumOfAct = maxNumOfAct - 1;
					UrlArea.setText("\"Select an edge\"");

					GraphForward.remove(GraphForward.size() - 1);
				}
			}
		});
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;
		operationPanel.add(goBack, gbc);
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;
		operationPanel.add(opeNote1, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;
		operationPanel.add(goDeep, gbc);
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;
		operationPanel.add(opeNote2, gbc);
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;
		operationPanel.add(goForward, gbc);

		addInfo("Operation", operationPanel);
		addInfo("Legend", legendPanel);
		addInfo("View", viewPanel);
		addInfo("Selected information", SelectedInfo);
		addInfo("Parent Model", parentviewPanel);
		addInfo("Child Model", childviewPanel);

		// attach zoom to 
		scroll.addMouseWheelListener(new MouseWheelListener() {

			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.getWheelRotation() > 0) {
					viewPanel.getZoom().zoomOut();
				} else if (e.getWheelRotation() < 0) {
					viewPanel.getZoom().zoomIn();
				}

			}
		});

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				int totalHeight_Splitter = getHeight();
				int dividerLocation = (int) (totalHeight_Splitter * 0.618);
				splitPane.setDividerLocation(dividerLocation);
			}
		});

		validate();
		repaint();
	}

	public static Color colorcalc(double value, double maxValue, double minValue) {
		Color c = null;
		if (!Double.isNaN(value)) {
			int rvalue, gvalue, bvalue;
			if (maxValue == minValue) {
				rvalue = 0;
				gvalue = 128;
				bvalue = 128;
			} else {
				rvalue = (int) Math.round(255.0 * ((value - minValue) / (maxValue - minValue)));
				gvalue = 0;
				bvalue = 255 - rvalue;
			}
			c = new Color(rvalue, gvalue, bvalue);
		} else {
			c = Color.BLACK;
		}
		return c;
	}

	static double getTypeValue(Result result) {
		double value = 0.0;
		switch (displayType) {
			case 0 :
				value = result.Fitness;
				break;
			case 1 :
				value = result.Percisions;
				break;
			case 2 :
				value = result.Score;
				break;
		}
		return value;
	}

	static double getTypeValue2(ResultUnit result) {
		double value = 0.0;
		switch (displayType) {
			case 0 :
				value = result.fitness;
				break;
			case 1 :
				value = result.precision;
				break;
			case 2 :
				value = result.f1;
				break;
		}
		return value;
	}

	static JPanel GetModelPanel(PluginContext context, String name) throws ConnectionCannotBeObtained {
		MainVisualConfig miniembeddedconfig = new MainVisualConfig();
		XLog input = miniembeddedconfig.getInputLog();
		Marking tgrmark = null;
		EvClassLogPetrinetConnection conn1;

		// inductive
		IMMiningDialog dialog = miniembeddedconfig.getDialog();

		String[] subsettemp = name.split(",");

		HashSet<String> subset = new HashSet<String>();
		for (String str : subsettemp) {
			subset.add(str);
		}

		XLog tgr = MainProjectionPlugin.GetProjection(input, subset).getFirst(); // step1

		Object[] tempIMresult = IMPetriNet.minePetriNet(context, tgr, dialog.getMiningParameters());

		PetrinetGraph tmpNet = (Petrinet) tempIMresult[0];

		try {
			InitialMarkingConnection initCon = context.getConnectionManager()
					.getFirstConnection(InitialMarkingConnection.class, context, tmpNet);

			tgrmark = (Marking) initCon.getObjectWithRole(InitialMarkingConnection.MARKING);
		} catch (ConnectionCannotBeObtained exc) {
			;
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// check existence of final marking
		try {
			context.getConnectionManager().getFirstConnection(FinalMarkingConnection.class, context, tmpNet);
		} catch (ConnectionCannotBeObtained exc) {
			;
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		// check connection in order to determine whether mapping step is needed
		// of not
		try {
			// connection is found, no need for mapping step
			// connection is not found, another plugin to create such connection
			// is automatically
			// executed

			conn1 = context.getConnectionManager().getFirstConnection(EvClassLogPetrinetConnection.class, context,
					tmpNet, tgr);
		} catch (Exception e1) {
			return null;
		}

		TransEvClassMapping mapping1 = (TransEvClassMapping) conn1
				.getObjectWithRole(EvClassLogPetrinetConnection.TRANS2EVCLASSMAPPING);

		replaySteps = new JComponent[nofSteps];
		replaySteps[0] = new PNAlgorithmStep(context, tmpNet, tgr, mapping1);

		// set current step
		currentStep = 0;

		Object[] resultConfiguration = showConfiguration((UIPluginContext) context, tgr, tmpNet, mapping1);

		// This connection MUST exists, as it is constructed by the configuration if necessary
		context.getConnectionManager().getFirstConnection(EvClassLogPetrinetConnection.class, context, tmpNet, tgr);

		// get all parameters
		IPNReplayAlgorithm selectedAlg = (IPNReplayAlgorithm) resultConfiguration[PNReplayerUI.ALGORITHM];
		IPNReplayParameter algParameters = (IPNReplayParameter) resultConfiguration[PNReplayerUI.PARAMETERS];

		PNRepResult ret = replayLogPrivate(context, tmpNet, tgr,
				(TransEvClassMapping) resultConfiguration[PNReplayerUI.MAPPING], selectedAlg, algParameters);

		@SuppressWarnings("unused")
		PNLogReplayProjectedVisPanelEmbedded bigstuff = new PNLogReplayProjectedVisPanelEmbedded(context, tmpNet,
				tgrmark, tgr, mapping1, ret);

		JPanel tempModelJPanel = miniembeddedconfig.getTempModel();

		return tempModelJPanel;
	}

	private static Object[] showConfiguration(UIPluginContext context, XLog log, PetrinetGraph net,
			TransEvClassMapping mapping) {

		go(1, context, net, log, mapping);

		return new Object[] { mapping, ((PNAlgorithmStep) replaySteps[0]).getAlgorithm(),
				paramProvider.constructReplayParameter(replaySteps[1]) };

	}

	private static int go(int direction, UIPluginContext context, PetrinetGraph net, XLog log,
			TransEvClassMapping mapping) {
		currentStep += direction;

		// check which algorithm is selected and adjust parameter as necessary
		if (currentStep == 1) {
			paramProvider = ((PNAlgorithmStep) replaySteps[0]).getAlgorithm().constructParamProvider(context, net, log,
					mapping);
			replaySteps[1] = paramProvider.constructUI();
		}

		if ((currentStep >= 0) && (currentStep < nofSteps)) {
			return currentStep;
		}
		return 0;
	}

	private static PNRepResult replayLogPrivate(PluginContext context, PetrinetGraph net, XLog log,
			TransEvClassMapping mapping, IPNReplayAlgorithm selectedAlg, IPNReplayParameter parameters) {
		if (selectedAlg.isAllReqSatisfied(context, net, log, mapping, parameters)) {
			// for each trace, replay according to the algorithm. Only returns two objects
			PNRepResult replayRes = null;

			if (parameters.isGUIMode()) {

				try {
					replayRes = selectedAlg.replayLog(context, net, log, mapping, parameters);
				} catch (AStarException e) {
					e.printStackTrace();
				}

				NumberFormat nf = NumberFormat.getInstance();
				nf.setMinimumFractionDigits(2);
				nf.setMaximumFractionDigits(2);

			} else {
				try {
					replayRes = selectedAlg.replayLog(context, net, log, mapping, parameters);
				} catch (AStarException e) {
					e.printStackTrace();
				}
			}

			// add connection
			if (replayRes != null) {
				if (parameters.isCreatingConn()) {
					createConnections(context, net, log, mapping, selectedAlg, parameters, replayRes);
				}
			}

			return replayRes;
		} else {
			if (context != null) {
				context.getFutureResult(0).cancel(true);
			}
			return null;
		}
	}

	protected static void createConnections(PluginContext context, PetrinetGraph net, XLog log,
			TransEvClassMapping mapping, IPNReplayAlgorithm selectedAlg, IPNReplayParameter parameters,
			PNRepResult replayRes) {
		context.addConnection(new PNRepResultAllRequiredParamConnection("Connection between replay result, "
				+ XConceptExtension.instance().extractName(log) + ", and " + net.getLabel(), net, log, mapping,
				selectedAlg, parameters, replayRes));
	}

	static List<HashSet<String>> GetChildset(String modelstr) {
		String[] temp = modelstr.split(",");
		HashSet<String> es = new HashSet<String>();
		for (String str : temp) {
			es.add(str);
		}
		List<HashSet<String>> ans = new ArrayList<HashSet<String>>();
		ans.add(es);
		for (String sub : es) {
			HashSet<String> set = new HashSet<String>();
			for (String sub2 : es) {
				if (sub.equals(sub2)) {
					;
				} else {
					set.add(sub2);
				}
			}
			ans.add(set);
		}
		return ans;
	}

	public static Pair<XLog, Integer> GetProjection(XLog input, HashSet<String> subset) {
		int tempEventCount = 0;
		XLog projection = new XLogImpl(new XAttributeMapImpl());
		for (XTrace t : input) {
			// 实例化 XTrance
			XTrace nt = new XTraceImpl(new XAttributeMapImpl());
			for (XEvent e : t) {
				if (subset.contains(GetName(e))) {
					XEvent ne = (XEvent) e.clone();
					nt.add(ne);
					tempEventCount = tempEventCount + 1;
				}
			}
			if (!nt.isEmpty()) {
				projection.add(nt);
			}
			nt = null; // 释放 nt
		}
		return new Pair<>(projection, tempEventCount);
	}

	static String GetName(XEvent e) {
		return e.getAttributes().get("concept:name").toString();
	}

	public static DefaultTableCellRenderer createCustomTableCellRenderer(HashMap<List<Integer>, Color> TargetCells) {
		return new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

				if (TargetCells.containsKey(new ArrayList<>(Arrays.asList(row, column)))) {
					c.setBackground(TargetCells.get(new ArrayList<>(Arrays.asList(row, column))));
					c.setForeground(Color.WHITE);
				} else {
					c.setBackground(table.getBackground());
					c.setForeground(table.getForeground());
				}

				return c;
			}
		};
	}
}
