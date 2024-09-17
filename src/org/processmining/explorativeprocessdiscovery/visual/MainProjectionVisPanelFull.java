/**
 * 
 */
package org.processmining.explorativeprocessdiscovery.visual;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
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
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
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

import nl.tue.astar.AStarException;

public class MainProjectionVisPanelFull extends ProjectionVisPanelChanged {
	private static final long serialVersionUID = -6674503536171244970L;
	MainVisualConfig miniembeddedconfig = new MainVisualConfig();
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
	JPanel OptmalviewPanel = new JPanel();
	HashMap<String, List<Double>> optimalModels = new HashMap<String, List<Double>>();
	JPanel JPOptimal = new JPanel();
	JPanel TmpFM;
	JPanel TmpSM;
	JPanel TmpOPM;
	JPanel EmptyFModel = new JPanel();
	JLabel labelF = new JLabel("Select an edge");
	JPanel EmptySModel = new JPanel();
	JLabel labelS = new JLabel("Select an edge");
	DefaultEdge LastSelectedEdge = null;
	JPanel EmptyOPTModel = new JPanel();
	JLabel labelOPT = new JLabel(
			"<html><body><p align=\"center\">Select a node.<br/>There may have some overlapped nodes.<br/> You can switch between them by clicking on the same node.</p></body></html>");

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

	int valueAC;
	int valueEC;

	public MainProjectionVisPanelFull(PluginContext context, List<Result> results, int Type) {
		super(context, results, Type);
		displayType = Type;

		initialize(context, results, Type);
	}

	protected void initialize(PluginContext context, List<Result> result, int Type) {
		double resultMinValue = 99.99;
		double resultMaxValue = -99.99;

		EmptyFModel.add(labelF);
		miniembeddedconfig.setFatherModel(EmptyFModel);
		EmptySModel.add(labelS);
		miniembeddedconfig.setSonModel(EmptySModel);
		EmptyOPTModel.add(labelOPT);
		miniembeddedconfig.setOptimalModel(EmptyOPTModel);
		List<Result> results4bestremove = new ArrayList<Result>();
		List<String> results4bestremoveStr = new ArrayList<String>();
		int maxNumOfAct = result.get(0).subset.size();
		int tempLayerIndexMax = maxNumOfAct;
		int maxNumOfNodes = (int) CombinationNum(maxNumOfAct, Math.round((double) maxNumOfAct / 2));
		List<Result> tempresults4bestremove = new ArrayList<Result>();
		List<String> tempchildlist4bestremove = new ArrayList<String>();
		tempchildlist4bestremove.add(result.get(0).sublog);
		int LayerIndex = 0;

		//创建显示数据
//		Object[][] Tabledata = new Object[maxNumOfAct*2][maxNumOfNodes];
//		int LayerCount4Table = 0;
//		int LayerElementsCount4Table = 0;

		for (Result r : result) {
			LayerIndex = r.subset.size();
			if (LayerIndex == tempLayerIndexMax) {
//				Tabledata[LayerCount4Table*2][LayerElementsCount4Table] = r.sublog;
//				Tabledata[LayerCount4Table*2 + 1][LayerElementsCount4Table] = getTypeValue(r);
//				LayerElementsCount4Table = LayerElementsCount4Table + 1;
				if (tempchildlist4bestremove.contains(r.sublog)) {
					if (tempresults4bestremove.isEmpty()) {
						tempresults4bestremove.add(r);
					} else if (getTypeValue(r) > getTypeValue(tempresults4bestremove.get(0))) {
						tempresults4bestremove = new ArrayList<Result>();
						tempresults4bestremove.add(r);
					} else if (getTypeValue(r) == getTypeValue(tempresults4bestremove.get(0))) {
						tempresults4bestremove.add(r);
					}					
				}
			} else {
//				for (int lastCount = LayerElementsCount4Table; lastCount < maxNumOfNodes; lastCount++) {
//					Tabledata[LayerCount4Table*2][LayerElementsCount4Table] = "";
//					Tabledata[LayerCount4Table*2 + 1][LayerElementsCount4Table] = "";
//				}
//				LayerElementsCount4Table = 0;
//				LayerCount4Table = LayerCount4Table + 1;
//				Tabledata[LayerCount4Table*2][LayerElementsCount4Table] = r.sublog;
//				Tabledata[LayerCount4Table*2 + 1][LayerElementsCount4Table] = getTypeValue(r);
//				LayerElementsCount4Table = LayerElementsCount4Table + 1;				
				tempchildlist4bestremove = new ArrayList<String>();
				for (Result rr : tempresults4bestremove) {
					tempchildlist4bestremove.addAll(childList.get(rr.sublog));
				}
				results4bestremove.addAll(tempresults4bestremove);
				tempresults4bestremove = new ArrayList<Result>();
				if (tempchildlist4bestremove.contains(r.sublog)) {
					tempresults4bestremove.add(r);					
				}
				tempLayerIndexMax = LayerIndex;
			}
			int catchflag = 1;
			if (Double.isNaN(r.Fitness) || Double.isNaN(r.Percisions)) {
				catchflag = 0;
			} else {
				for (Result c : result) {
					if (Double.isNaN(c.Fitness) || Double.isNaN(c.Percisions)) {
						;
					} else {
						if (r.Fitness < c.Fitness && r.Percisions < c.Percisions) {
							catchflag = 0;
							break;
						}
					}
				}
			}

			if (catchflag == 1) {
				List<Double> Tempcatch = new ArrayList<Double>();
				Tempcatch.add(r.Fitness);
				Tempcatch.add(r.Percisions);
				Tempcatch.add((double) r.eventcount);
				Tempcatch.add((double) r.activitycount);
				optimalModels.put(r.sublog, Tempcatch);
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
		results4bestremove.addAll(tempresults4bestremove);	
		tempresults4bestremove = null;
		for (Result r : results4bestremove) {
			results4bestremoveStr.add(r.sublog);
		}
		;
		results4bestremove = null;

		int nodesNum = result.size();
		int nodeStepLenX = 50;
		int nodeStepLenY = 100;
		int nodeWidth = 30;
		int infonodeX = 30;
		int infonodeY = 5;
		int initialNodeX = (int) Math.ceil((double) maxNumOfNodes / 2) * (nodeStepLenX + nodeWidth) + 20;
		int initialNodeY = 20;
		HashMap<String, DefaultGraphCell> mapSublogNode = new HashMap<String, DefaultGraphCell>();
		HashMap<String, DefaultGraphCell> mapSublogOptNode = new HashMap<String, DefaultGraphCell>();
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

			int tempCom = (int) CombinationNum(maxNumOfAct, layerNodesNum);
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

			if (getTypeValue(i) == resultMaxValue) {
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
				GraphConstants.setBounds(activitycells[nodeCellCount].getAttributes(),
						new Rectangle2D.Double(stepX + nodeWidth, startY, infonodeY, infonodeX * (Activitynum / 100)));
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
				GraphConstants.setBounds(activitycells[nodeCellCount].getAttributes(),
						new Rectangle2D.Double(stepX + nodeWidth, startY, infonodeY, infonodeX * (Activitynum / 100)));
				GraphConstants.setBackground(activitycells[nodeCellCount].getAttributes(), Color.ORANGE);
				GraphConstants.setOpaque(activitycells[nodeCellCount].getAttributes(), true);
				GraphConstants.setMoveable(activitycells[nodeCellCount].getAttributes(), false); // 不可移动
				GraphConstants.setSizeable(activitycells[nodeCellCount].getAttributes(), false); // 不可调整大小
				GraphConstants.setSelectable(activitycells[nodeCellCount].getAttributes(), false); // 不可选中
			}

			mapSublogNode.put(i.sublog, cells[nodeCellCount]);
			EventActivityNode.put(EventAndActivity, cells[nodeCellCount]);

			if (layerNodesNum == tempMaxNumOfAct) {
				stepX = stepX + nodeStepLenX + nodeWidth;
			}

			nodeCellCount++;
		}

		graph.getGraphLayoutCache().insert(cells);
		graph.getGraphLayoutCache().insert(eventcells);
		graph.getGraphLayoutCache().insert(activitycells);

		String targetStr = null;
		for (DefaultGraphCell icell : mapSublogNode.values()) {
			for (String istr : mapSublogNode.keySet()) {
				if (mapSublogNode.get(istr) == icell) {
					targetStr = istr;
					if (results4bestremoveStr.contains(targetStr)) {
						DefaultPort portS = new DefaultPort();
						GraphConstants.setOffset(portS.getAttributes(),
								new Point2D.Double(GraphConstants.PERMILLE / 2, nodeWidth + infonodeY));
						GraphConstants.setAbsoluteY(portS.getAttributes(), true);
						icell.add(portS);

						if (!childList.get(targetStr).get(0).equals("")) {
							for (String child : childList.get(targetStr)) {
								if (results4bestremoveStr.contains(child)) {
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
										diffFS = 0.1;
										edgeCol = Color.GRAY;
									} else if (FatherValue == SonValue) {
										diffFS = 0.1;
										edgeCol = limeGreen;
									} else if (FatherValue > SonValue) {
										diffFS = FatherValue - SonValue;
										edgeCol = Color.CYAN;
									} else if (FatherValue < SonValue) {
										diffFS = SonValue - FatherValue;
										edgeCol = Color.PINK;
									}

									DefaultPort portE = new DefaultPort();
									GraphConstants.setOffset(portE.getAttributes(),
											new Point2D.Double(GraphConstants.PERMILLE / 2, 0));
									mapSublogNode.get(child).add(portE);

									DefaultEdge edge = new DefaultEdge();
									int arrow = GraphConstants.ARROW_CLASSIC;
									GraphConstants.setLineColor(edge.getAttributes(), edgeCol);
									GraphConstants.setLineEnd(edge.getAttributes(), arrow);
									GraphConstants.setEndFill(edge.getAttributes(), true);
									GraphConstants.setLineWidth(edge.getAttributes(), (float) (2.0 * diffFS));
									graph.getGraphLayoutCache().insertEdge(edge, portS, portE);
									mapSublogNode.get(child).remove(portE);
									List<String> TempSublogPair = new ArrayList<String>();
									TempSublogPair.add(targetStr);
									TempSublogPair.add(child);
									mapEdgeSublogPair.put(edge, TempSublogPair);
								}
							}
							icell.remove(portS);
						} else {
							;
						}

					}
				}
			}
		}

		JPanel SelectedInfo = new JPanel();
		SelectedInfo.setOpaque(false);
		SelectedInfo.setLayout(new BoxLayout(SelectedInfo, BoxLayout.X_AXIS));
		JTextArea UrlArea = new JTextArea("\"Select an edge\"", 10, 20);
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
						for (Result resulti : result) {
							if (resulti.sublog.equals(TgrFatherName)) {
								FValue2 = getTypeValue(resulti);
								double Eventnumtemp = (double) Math.round((resulti.eventcount * 10000) / TotalEventCount)
										/ 100;
								double Activitynumtemp = (double) Math
										.round((resulti.activitycount * 10000) / TotalActivityCount) / 100;
								FEvent = Double.toString(Eventnumtemp) + "%";
								FActivity = Double.toString(Activitynumtemp) + "%";
							}
							if (resulti.sublog.equals(TgrSonName)) {
								SValue = getTypeValue(resulti);
								double Eventnumtemp = (double) Math.round((resulti.eventcount * 10000) / TotalEventCount)
										/ 100;
								double Activitynumtemp = (double) Math
										.round((resulti.activitycount * 10000) / TotalActivityCount) / 100;
								SEvent = Double.toString(Eventnumtemp) + "%";
								SActivity = Double.toString(Activitynumtemp) + "%";
							}
						}
						if (Double.isNaN(FValue2) && !Double.isNaN(SValue)) {
							UrlArea.setText("Activities in this subset: \n" + TgrFatherName + "\n" + Name + ": "
									+ Double.toString(FValue2) + "\n" + "Event:" + FEvent + "\n" + "Activity:" + FActivity
									+ "\n\n" + "Delete: " + DeletedEle + "\n" + "Event:" + FEvent + " → " + SEvent + "\n"
									+ "Activity:" + FActivity + " → " + SActivity + "\n" + Name + ": "
									+ Double.toString(FValue2) + " → "
									+ Double.toString((double) Math.round(SValue * 100) / 100) + "\n"
									+ "(Conneted with a failed spot ✕)");
						} else if (!Double.isNaN(FValue2) && Double.isNaN(SValue)) {
							UrlArea.setText("Activities in this subset: \n" + TgrFatherName + "\n" + Name + ": "
									+ Double.toString((double) Math.round(FValue2 * 100) / 100) + "\n" + "Event:" + FEvent
									+ "\n" + "Activity:" + FActivity + "\n\n" + "Delete: " + DeletedEle + "\n" + "Event:"
									+ FEvent + " → " + SEvent + "\n" + "Activity:" + FActivity + " → " + SActivity + "\n"
									+ Name + ": " + Double.toString((double) Math.round(FValue2 * 100) / 100) + " → "
									+ Double.toString(SValue) + "\n" + "(Conneted with a failed spot ✕)");
						} else if (Double.isNaN(FValue2) && Double.isNaN(SValue)) {
							UrlArea.setText("Activities in this subset: \n" + TgrFatherName + "\n" + Name + ": "
									+ Double.toString(FValue2) + "\n" + "Event:" + FEvent + "\n" + "Activity:" + FActivity
									+ "\n\n" + "Delete: " + DeletedEle + "\n" + "Event:" + FEvent + " → " + SEvent + "\n"
									+ "Activity:" + FActivity + " → " + SActivity + "\n" + Name + ": "
									+ Double.toString(FValue2) + " → " + Double.toString(SValue) + "\n"
									+ "(Conneted with a failed spot ✕)");
						} else if (FValue2 == SValue) {
							UrlArea.setText("Activities in this subset: \n" + TgrFatherName + "\n" + Name + ": "
									+ Double.toString((double) Math.round(FValue2 * 100) / 100) + "\n" + "Event:" + FEvent
									+ "\n" + "Activity:" + FActivity + "\n\n" + "Delete: " + DeletedEle + "\n" + "Event:"
									+ FEvent + " → " + SEvent + "\n" + "Activity:" + FActivity + " → " + SActivity + "\n"
									+ Name + ": " + Double.toString((double) Math.round(FValue2 * 100) / 100) + " → "
									+ Double.toString((double) Math.round(SValue * 100) / 100) + "\n" + "(Unchanged -)");
						} else if (FValue2 > SValue) {
							UrlArea.setText("Activities in this subset: \n" + TgrFatherName + "\n" + Name + ": "
									+ Double.toString((double) Math.round(FValue2 * 100) / 100) + "\n" + "Event:" + FEvent
									+ "\n" + "Activity:" + FActivity + "\n\n" + "Delete: " + DeletedEle + "\n" + "Event:"
									+ FEvent + " → " + SEvent + "\n" + "Activity:" + FActivity + " → " + SActivity + "\n"
									+ Name + ": " + Double.toString((double) Math.round(FValue2 * 100) / 100) + " → "
									+ Double.toString((double) Math.round(SValue * 100) / 100) + "\n" + "(Decreased "
									+ Double.toString((double) (Math.round(FValue2 * 100) - Math.round(SValue * 100)) / 100)
									+ " ↓)");
						} else if (FValue2 < SValue) {
							UrlArea.setText("Activities in this subset: \n" + TgrFatherName + "\n" + Name + ": "
									+ Double.toString((double) Math.round(FValue2 * 100) / 100) + "\n" + "Event:" + FEvent
									+ "\n" + "Activity:" + FActivity + "\n\n" + "Delete: " + DeletedEle + "\n" + "Event:"
									+ FEvent + " → " + SEvent + "\n" + "Activity:" + FActivity + " → " + SActivity + "\n"
									+ Name + ": " + Double.toString((double) Math.round(FValue2 * 100) / 100) + " → "
									+ Double.toString((double) Math.round(SValue * 100) / 100) + "\n" + "(Increased "
									+ Double.toString((double) (Math.round(SValue * 100) - Math.round(FValue2 * 100)) / 100)
									+ " ↑)");
						}
						runningmessagedialog.dispose();
					}
				} else if (e.getCell() instanceof DefaultGraphCell) {
					String targetStr = null;
					for (DefaultGraphCell icell : mapSublogNode.values()) {
						if (e.getCell() == icell) {
							for (String istr : mapSublogNode.keySet()) {
								if (mapSublogNode.get(istr) == icell) {
									targetStr = istr;
								}
							}
						}
					}

					UrlArea.setText("\"Select an edge\"");

					for (DefaultEdge edgei : alledgelist) {
						graph.getGraphLayoutCache().remove(new Object[] { edgei });
					}

					DefaultPort portS = new DefaultPort();
					GraphConstants.setOffset(portS.getAttributes(),
							new Point2D.Double(GraphConstants.PERMILLE / 2, nodeWidth + infonodeY));
					GraphConstants.setAbsoluteY(portS.getAttributes(), true);
					((DefaultGraphCell) e.getCell()).add(portS);

					if (!childList.get(targetStr).get(0).equals("")) {
						for (String child : childList.get(targetStr)) {
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
							GraphConstants.setOffset(portE.getAttributes(),
									new Point2D.Double(GraphConstants.PERMILLE / 2, 0));
							mapSublogNode.get(child).add(portE);

							DefaultEdge edge = new DefaultEdge();
							int arrow = GraphConstants.ARROW_CLASSIC;
							GraphConstants.setLineColor(edge.getAttributes(), edgeCol);
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
						((DefaultGraphCell) e.getCell()).remove(portS);
					} else {
						;
					}
				}

				graph.refresh();
			}
		});

		graph.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Object cell = graph.getFirstCellForLocation(e.getX(), e.getY());
				if (cell == null) {
					UrlArea.setText("\"Select an edge\"");

					for (DefaultEdge edgei : alledgelist) {
						graph.getGraphLayoutCache().remove(new Object[] { edgei });
					}
				} else {
					;
				}
			}
		});

		miniembeddedconfig.setZoom(graph);

		scroll = new JScrollPane(miniembeddedconfig.getZoom());
		setLayout(new BorderLayout());
		add(scroll);
//
////		//创建表头
////		String[] TablecolumnNames = new String[maxNumOfNodes];
////		int numCopies = maxNumOfNodes;
////		for (int i = 0; i < numCopies; i++) {
////			TablecolumnNames[i] = "";
////		}
////		DefaultTableModel Tablemodel = new DefaultTableModel(Tabledata, TablecolumnNames);
////		JTable resultTable = new JTable(Tablemodel){
////            private static final long serialVersionUID = 1L;
////
////			public String getToolTipText(MouseEvent event) {
////                Point p = event.getPoint();
////                int row = rowAtPoint(p);
////                int col = columnAtPoint(p);
////
////                if (row == -1 || col == -1) {
////                    return null;
////                }
////                if (Tablemodel.getValueAt(row, col) != null && Tablemodel.getValueAt(row, col) != "") {
////                	if (Tablemodel.getValueAt(row, col) instanceof Double) {
////                		return Tablemodel.getValueAt(row, col).toString();
////                	} else if (Tablemodel.getValueAt(row, col) instanceof Integer) {
////						return Tablemodel.getValueAt(row, col).toString();
////					} else {
////                        return (String) Tablemodel.getValueAt(row, col);
////                	}             	
////                }
////                return null;
////            }
////
////            public Point getToolTipLocation(MouseEvent event) {
////                Point p = event.getPoint();
////                int row = rowAtPoint(p);
////                int col = columnAtPoint(p);
////
////                if (row == -1 || col == -1) {
////                    return null;
////                }
////                Rectangle cell = getCellRect(row, col, true);
////                return new Point(
////                        cell.x + cell.width / 2,
////                        cell.y + cell.height / 2);
////            }
////        };
////		resultTable.getTableHeader().setReorderingAllowed(false); // disable column reordering
////		resultTable.setEnabled(false);
////		resultTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
////		for (int col = 0; col < Tablemodel.getColumnCount(); col++) {
////			resultTable.getColumnModel().getColumn(col).setPreferredWidth(200);
////		}
////		JScrollPane scrollTablePane = new JScrollPane(resultTable);
////
////		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scroll, scrollTablePane);
////		splitPane.setOneTouchExpandable(true);
////
////		add(splitPane);

		// add legend panel
		JPanel legendPanel = new LegendPanelConf();

		// add view panel (zoom in/out)
		viewPanel = createViewPanel(this, MAX_ZOOM);
		addInteractionViewports(viewPanel);

		parentviewPanel.add(miniembeddedconfig.getFatherModel());
		childviewPanel.add(miniembeddedconfig.getSonModel());

		addInfo("Legend", legendPanel);
		addInfo("View", viewPanel);
		addInfo("Selected information", SelectedInfo);
		addInfo("Parent Model", parentviewPanel);
		addInfo("Child Model", childviewPanel);

		JPanel ParetO = getInspector().addTab("Pareto Optimality");

		GraphModel model2 = new DefaultGraphModel();
		GraphLayoutCache view2 = new GraphLayoutCache(model2, new DefaultCellViewFactory());
		JGraph jpAxis = new JGraph(model2, view2);
		Font fnAxis = new Font("Times New Roman", Font.BOLD, 22);
		DefaultGraphCell axisY = new DefaultGraphCell();
		GraphConstants.setForeground(axisY.getAttributes(), Color.WHITE);
		GraphConstants.setFont(axisY.getAttributes(), fnAxis);
		GraphConstants.setBounds(axisY.getAttributes(), new Rectangle2D.Double(69, 10, 2, 550));
		GraphConstants.setBackground(axisY.getAttributes(), Color.DARK_GRAY);
		GraphConstants.setOpaque(axisY.getAttributes(), true);
		GraphConstants.setMoveable(axisY.getAttributes(), false); // 不可移动
		GraphConstants.setSizeable(axisY.getAttributes(), false); // 不可调整大小
		GraphConstants.setSelectable(axisY.getAttributes(), false); // 不可选中

		DefaultGraphCell axisX = new DefaultGraphCell();
		GraphConstants.setForeground(axisX.getAttributes(), Color.WHITE);
		GraphConstants.setFont(axisX.getAttributes(), fnAxis);
		GraphConstants.setBounds(axisX.getAttributes(), new Rectangle2D.Double(69, 560, 550, 2));
		GraphConstants.setBackground(axisX.getAttributes(), Color.DARK_GRAY);
		GraphConstants.setOpaque(axisX.getAttributes(), true);
		GraphConstants.setMoveable(axisX.getAttributes(), false); // 不可移动
		GraphConstants.setSizeable(axisX.getAttributes(), false); // 不可调整大小
		GraphConstants.setSelectable(axisX.getAttributes(), false); // 不可选中

		DefaultGraphCell discriptX = new DefaultGraphCell(new String("Fitness"));
		GraphConstants.setFont(discriptX.getAttributes(), fnAxis);
		GraphConstants.setBounds(discriptX.getAttributes(), new Rectangle2D.Double(269, 575, 100, 50));
		GraphConstants.setMoveable(discriptX.getAttributes(), false); // 不可移动
		GraphConstants.setSizeable(discriptX.getAttributes(), false); // 不可调整大小
		GraphConstants.setSelectable(discriptX.getAttributes(), false); // 不可选中		

		DefaultGraphCell discriptY1 = new DefaultGraphCell(new String("Y1"));
		GraphConstants.setFont(discriptY1.getAttributes(), fnAxis);
		GraphConstants.setBounds(discriptY1.getAttributes(), new Rectangle2D.Double(39, 555, 10, 10));
		GraphConstants.setMoveable(discriptY1.getAttributes(), false); // 不可移动
		GraphConstants.setSizeable(discriptY1.getAttributes(), false); // 不可调整大小
		GraphConstants.setSelectable(discriptY1.getAttributes(), false); // 不可选中
		DefaultGraphCell discriptY2 = new DefaultGraphCell(new String("Y2"));
		GraphConstants.setFont(discriptY2.getAttributes(), fnAxis);
		GraphConstants.setBounds(discriptY2.getAttributes(), new Rectangle2D.Double(39.0001, 65, 10, 10));
		GraphConstants.setMoveable(discriptY2.getAttributes(), false); // 不可移动
		GraphConstants.setSizeable(discriptY2.getAttributes(), false); // 不可调整大小
		GraphConstants.setSelectable(discriptY2.getAttributes(), false); // 不可选中
		DefaultEdge discriptY = new DefaultEdge(new String("Precision"));
		GraphConstants.setLabelAlongEdge(discriptY.getAttributes(), true);
		GraphConstants.setFont(discriptY.getAttributes(), fnAxis);
		GraphConstants.setLineColor(discriptY.getAttributes(), Color.WHITE);
		GraphConstants.setForeground(discriptY.getAttributes(), Color.BLACK);
		GraphConstants.setEndFill(discriptY.getAttributes(), true);
		GraphConstants.setLineWidth(discriptY.getAttributes(), (float) (2.0));
		GraphConstants.setSelectable(discriptY.getAttributes(), false); // 不可选中

		DefaultGraphCell[] axisXText = new DefaultGraphCell[10];
		DefaultGraphCell[] axisYText = new DefaultGraphCell[10];
		DefaultGraphCell axis0Text = new DefaultGraphCell("0");
		GraphConstants.setBounds(axis0Text.getAttributes(), new Rectangle2D.Double(49, 570, 10, 10));
		GraphConstants.setMoveable(axis0Text.getAttributes(), false); // 不可移动
		GraphConstants.setSizeable(axis0Text.getAttributes(), false); // 不可调整大小
		GraphConstants.setSelectable(axis0Text.getAttributes(), false); // 不可选中
		for (int i = 0; i < 21; i++) {
			if (i % 2 == 0 && i / 2 != 0) {
				axisXText[i / 2 - 1] = new DefaultGraphCell(Double.toString((double) i / 20));
				GraphConstants.setBounds(axisXText[i / 2 - 1].getAttributes(),
						new Rectangle2D.Double(44, 550 - i / 2 * 50, 20, 20));
				GraphConstants.setMoveable(axisXText[i / 2 - 1].getAttributes(), false); // 不可移动
				GraphConstants.setSizeable(axisXText[i / 2 - 1].getAttributes(), false); // 不可调整大小
				GraphConstants.setSelectable(axisXText[i / 2 - 1].getAttributes(), false); // 不可选中
				axisYText[i / 2 - 1] = new DefaultGraphCell(Double.toString((double) i / 20));
				GraphConstants.setBounds(axisYText[i / 2 - 1].getAttributes(),
						new Rectangle2D.Double(59 + i / 2 * 50, 565, 20, 20));
				GraphConstants.setMoveable(axisYText[i / 2 - 1].getAttributes(), false); // 不可移动
				GraphConstants.setSizeable(axisYText[i / 2 - 1].getAttributes(), false); // 不可调整大小
				GraphConstants.setSelectable(axisYText[i / 2 - 1].getAttributes(), false); // 不可选中
			}
		}

		DefaultGraphCell[] OptModelCells = new DefaultGraphCell[optimalModels.size()];
		DefaultGraphCell[] LineMainCells = new DefaultGraphCell[optimalModels.size()];
		DefaultGraphCell[] LineXCells = new DefaultGraphCell[optimalModels.size()];
		DefaultGraphCell[] LineYCells = new DefaultGraphCell[optimalModels.size()];
		int OptMCcount = 0;
		for (String key : optimalModels.keySet()) {
			OptModelCells[OptMCcount] = new DefaultGraphCell();
			GraphConstants.setBounds(OptModelCells[OptMCcount].getAttributes(), new Rectangle2D.Double(
					67 + 500 * optimalModels.get(key).get(0), 558 - 500 * optimalModels.get(key).get(1), 4, 4));
			GraphConstants.setBackground(OptModelCells[OptMCcount].getAttributes(), Color.RED);
			GraphConstants.setOpaque(OptModelCells[OptMCcount].getAttributes(), true);
			GraphConstants.setMoveable(OptModelCells[OptMCcount].getAttributes(), false); // 不可移动
			GraphConstants.setSizeable(OptModelCells[OptMCcount].getAttributes(), false); // 不可调整大小
			GraphConstants.setEditable(OptModelCells[OptMCcount].getAttributes(), false);

			LineMainCells[OptMCcount] = new DefaultGraphCell();
			GraphConstants.setBounds(LineMainCells[OptMCcount].getAttributes(),
					new Rectangle2D.Double(68.99995 + 500 * optimalModels.get(key).get(0),
							559.99995 - 500 * optimalModels.get(key).get(1), 0.0001, 0.0001));
			LineXCells[OptMCcount] = new DefaultGraphCell();
			GraphConstants.setBounds(LineXCells[OptMCcount].getAttributes(),
					new Rectangle2D.Double(68.99995 + 500 * optimalModels.get(key).get(0), 559.99995, 0.0001, 0.0001));
			LineYCells[OptMCcount] = new DefaultGraphCell();
			GraphConstants.setBounds(LineYCells[OptMCcount].getAttributes(),
					new Rectangle2D.Double(68.99995, 559.99995 - 500 * optimalModels.get(key).get(1), 0.0001, 0.0001));

			DefaultEdge LineXEdge = new DefaultEdge();
			GraphConstants.setLineColor(LineXEdge.getAttributes(), Color.BLACK);
			GraphConstants.setLineWidth(LineXEdge.getAttributes(), (float) (1.0));
			GraphConstants.setSelectable(LineXEdge.getAttributes(), false); // 不可选中
			GraphConstants.setDashPattern(LineXEdge.getAttributes(), new float[] { 10, 10 });
			GraphConstants.setDashOffset(LineXEdge.getAttributes(), 5);
			jpAxis.getGraphLayoutCache().insertEdge(LineXEdge, LineMainCells[OptMCcount], LineXCells[OptMCcount]);

			DefaultEdge LineYEdge = new DefaultEdge();
			GraphConstants.setLineColor(LineYEdge.getAttributes(), Color.BLACK);
			GraphConstants.setLineWidth(LineYEdge.getAttributes(), (float) (1.0));
			GraphConstants.setSelectable(LineYEdge.getAttributes(), false); // 不可选中
			GraphConstants.setDashPattern(LineYEdge.getAttributes(), new float[] { 10, 10 });
			GraphConstants.setDashOffset(LineYEdge.getAttributes(), 5);
			jpAxis.getGraphLayoutCache().insertEdge(LineYEdge, LineMainCells[OptMCcount], LineYCells[OptMCcount]);

			mapSublogOptNode.put(key, OptModelCells[OptMCcount]);

			OptMCcount = OptMCcount + 1;
		}

		jpAxis.getGraphLayoutCache().insert(axisY);
		jpAxis.getGraphLayoutCache().insert(axisX);
		jpAxis.getGraphLayoutCache().insert(discriptX);
		jpAxis.getGraphLayoutCache().insert(discriptY1);
		jpAxis.getGraphLayoutCache().insert(discriptY2);
		jpAxis.getGraphLayoutCache().insert(axis0Text);
		jpAxis.getGraphLayoutCache().insert(axisXText);
		jpAxis.getGraphLayoutCache().insert(axisYText);
		jpAxis.getGraphLayoutCache().insertEdge(discriptY, discriptY1, discriptY2);
		jpAxis.getGraphLayoutCache().insert(OptModelCells);

		jpAxis.setPreferredSize(new Dimension(650, 630));

		JPanel SelectedInfo2 = new JPanel();
		SelectedInfo2.setOpaque(false);
		SelectedInfo2.setLayout(new BoxLayout(SelectedInfo2, BoxLayout.X_AXIS));
		JTextArea UrlArea2 = new JTextArea(
				"\"Select a node. (There may have some overlapped nodes. You can switch between them by clicking on the same node.)\"",
				6, 20);
		UrlArea2.setLineWrap(true);
		JScrollPane UrlscrollPane2 = new JScrollPane(UrlArea2);
		SelectedInfo2.add(UrlscrollPane2);

		jpAxis.addGraphSelectionListener(new GraphSelectionListener() {
			public void valueChanged(GraphSelectionEvent e) {
				String targetStr2 = null;
				jpAxis.refresh();

				for (DefaultGraphCell icell : mapSublogOptNode.values()) {
					if (e.getCell() == icell) {
						for (String istr : mapSublogOptNode.keySet()) {
							if (mapSublogOptNode.get(istr) == icell) {
								targetStr2 = istr;
							}
						}
					}
				}

				OptmalviewPanel.remove(miniembeddedconfig.getOptimalModel());

				try {
					TmpOPM = GetModelPanel(context, targetStr2);
				} catch (ConnectionCannotBeObtained e1) {
					e1.printStackTrace();
				}

				OptmalviewPanel.add(TmpOPM);

				miniembeddedconfig.setOptimalModel(TmpOPM);

				getInspector().revalidate();
				getInspector().repaint();

				double FitnessValue = optimalModels.get(targetStr2).get(0);
				double PrecisionValue = optimalModels.get(targetStr2).get(1);
				double EventValue = Math.round(optimalModels.get(targetStr2).get(2) / TotalEventCount * 10000) / 100;
				double ActivityValue = optimalModels.get(targetStr2).get(3) / TotalActivityCount * 100;

				if (Double.isNaN(FitnessValue) && Double.isNaN(PrecisionValue)) {
					UrlArea2.setText("Activities in this subset: \n" + targetStr2 + "\n" + "Fitness: "
							+ Double.toString(FitnessValue) + "\n" + "Precision: " + Double.toString(PrecisionValue)
							+ "\n" + "Event: " + Double.toString(EventValue) + "%" + "\n" + "Activity: "
							+ Double.toString(ActivityValue) + "%");
				} else if (Double.isNaN(FitnessValue) && !Double.isNaN(PrecisionValue)) {
					UrlArea2.setText("Activities in this subset: \n" + targetStr2 + "\n" + "Fitness: "
							+ Double.toString(FitnessValue) + "\n" + "Precision" + ": "
							+ Double.toString((double) Math.round(PrecisionValue * 100) / 100) + "\n" + "Event: "
							+ Double.toString(EventValue) + "%" + "\n" + "Activity: " + Double.toString(ActivityValue)
							+ "%");
				} else if (!Double.isNaN(FitnessValue) && Double.isNaN(PrecisionValue)) {
					UrlArea2.setText("Activities in this subset: \n" + targetStr2 + "\n" + "Fitness: "
							+ Double.toString((double) Math.round(FitnessValue * 100) / 100) + "\n" + "Precision" + ": "
							+ Double.toString(PrecisionValue) + "\n" + "Event: " + Double.toString(EventValue) + "%"
							+ "\n" + "Activity: " + Double.toString(ActivityValue) + "%");
				} else {
					UrlArea2.setText("Activities in this subset: \n" + targetStr2 + "\n" + "Fitness: "
							+ Double.toString((double) Math.round(FitnessValue * 100) / 100) + "\n" + "Precision" + ": "
							+ Double.toString((double) Math.round(PrecisionValue * 100) / 100) + "\n" + "Event: "
							+ Double.toString(EventValue) + "%" + "\n" + "Activity: " + Double.toString(ActivityValue)
							+ "%");
				}

				jpAxis.refresh();
			}
		});

		OptmalviewPanel.add(miniembeddedconfig.getOptimalModel());

		int OPTS_MIN = 0;
		int OPTS_MAX = 100;
		int OPTS_INIT = 1; //initial frames per second

		JSlider OPTSliderA = new JSlider(JSlider.VERTICAL, OPTS_MIN, OPTS_MAX, OPTS_INIT);

		//Turn on labels at major tick marks.
		OPTSliderA.setMajorTickSpacing(10);
		OPTSliderA.setMinorTickSpacing(1);
		OPTSliderA.setPaintTicks(true);
		OPTSliderA.setPaintLabels(true);
		OPTSliderA.setPreferredSize(new Dimension(60, 600));
		//Create the label table
		Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
		labelTable.put(0, new JLabel("0%"));
		labelTable.put(10, new JLabel("10%"));
		labelTable.put(20, new JLabel("20%"));
		labelTable.put(30, new JLabel("30%"));
		labelTable.put(40, new JLabel("40%"));
		labelTable.put(50, new JLabel("50%"));
		labelTable.put(60, new JLabel("60%"));
		labelTable.put(70, new JLabel("70%"));
		labelTable.put(80, new JLabel("80%"));
		labelTable.put(90, new JLabel("90%"));
		labelTable.put(100, new JLabel("100%"));
		OPTSliderA.setLabelTable(labelTable);
		OPTSliderA.setPaintLabels(true);

		OPTSliderA.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				valueAC = OPTSliderA.getValue();
				HashMap<String, List<Double>> filteredModels = new HashMap<String, List<Double>>();
				HashMap<String, List<Double>> filteredoptimalModels = new HashMap<String, List<Double>>();
				for (Result r : result) {
					if (r.activitycount >= (maxNumOfAct * ((float) valueAC / 100))
							&& r.eventcount >= (TotalEventCount * ((float) valueEC / 100))) {
						List<Double> Tempfiltercatch = new ArrayList<Double>();
						Tempfiltercatch.add(r.Fitness);
						Tempfiltercatch.add(r.Percisions);
						Tempfiltercatch.add((double) r.eventcount);
						Tempfiltercatch.add((double) r.activitycount);
						filteredModels.put(r.sublog, Tempfiltercatch);
					}
				}

				for (Map.Entry<String, List<Double>> entryr : filteredModels.entrySet()) {
					int catchflag = 1;
					if (Double.isNaN(entryr.getValue().get(0)) || Double.isNaN(entryr.getValue().get(1))) {
						catchflag = 0;
					} else {
						for (Map.Entry<String, List<Double>> entryc : filteredModels.entrySet()) {
							if (Double.isNaN(entryc.getValue().get(0)) || Double.isNaN(entryc.getValue().get(1))) {
								;
							} else {
								if (entryr.getValue().get(0) < entryc.getValue().get(0)
										&& entryr.getValue().get(1) < entryc.getValue().get(1)) {
									catchflag = 0;
									break;
								}
							}
						}
					}

					if (catchflag == 1) {
						List<Double> Tempcatch = new ArrayList<Double>();
						Tempcatch.add(entryr.getValue().get(0));
						Tempcatch.add(entryr.getValue().get(1));
						Tempcatch.add(entryr.getValue().get(2));
						Tempcatch.add(entryr.getValue().get(3));
						filteredoptimalModels.put(entryr.getKey(), Tempcatch);
					}
				}

				JPOptimal.remove(miniembeddedconfig.getOptimalGraph());

				GraphModel model2 = new DefaultGraphModel();
				GraphLayoutCache view2 = new GraphLayoutCache(model2, new DefaultCellViewFactory());
				JGraph jpAxis = new JGraph(model2, view2);
				Font fnAxis = new Font("Times New Roman", Font.BOLD, 22);
				DefaultGraphCell axisY = new DefaultGraphCell();
				GraphConstants.setForeground(axisY.getAttributes(), Color.WHITE);
				GraphConstants.setFont(axisY.getAttributes(), fnAxis);
				GraphConstants.setBounds(axisY.getAttributes(), new Rectangle2D.Double(69, 10, 2, 550));
				GraphConstants.setBackground(axisY.getAttributes(), Color.DARK_GRAY);
				GraphConstants.setOpaque(axisY.getAttributes(), true);
				GraphConstants.setMoveable(axisY.getAttributes(), false); // 不可移动
				GraphConstants.setSizeable(axisY.getAttributes(), false); // 不可调整大小
				GraphConstants.setSelectable(axisY.getAttributes(), false); // 不可选中

				DefaultGraphCell axisX = new DefaultGraphCell();
				GraphConstants.setForeground(axisX.getAttributes(), Color.WHITE);
				GraphConstants.setFont(axisX.getAttributes(), fnAxis);
				GraphConstants.setBounds(axisX.getAttributes(), new Rectangle2D.Double(69, 560, 550, 2));
				GraphConstants.setBackground(axisX.getAttributes(), Color.DARK_GRAY);
				GraphConstants.setOpaque(axisX.getAttributes(), true);
				GraphConstants.setMoveable(axisX.getAttributes(), false); // 不可移动
				GraphConstants.setSizeable(axisX.getAttributes(), false); // 不可调整大小
				GraphConstants.setSelectable(axisX.getAttributes(), false); // 不可选中

				DefaultGraphCell discriptX = new DefaultGraphCell(new String("Fitness"));
				GraphConstants.setFont(discriptX.getAttributes(), fnAxis);
				GraphConstants.setBounds(discriptX.getAttributes(), new Rectangle2D.Double(269, 575, 100, 50));
				GraphConstants.setMoveable(discriptX.getAttributes(), false); // 不可移动
				GraphConstants.setSizeable(discriptX.getAttributes(), false); // 不可调整大小
				GraphConstants.setSelectable(discriptX.getAttributes(), false); // 不可选中		

				DefaultGraphCell discriptY1 = new DefaultGraphCell(new String("Y1"));
				GraphConstants.setFont(discriptY1.getAttributes(), fnAxis);
				GraphConstants.setBounds(discriptY1.getAttributes(), new Rectangle2D.Double(39, 555, 10, 10));
				GraphConstants.setMoveable(discriptY1.getAttributes(), false); // 不可移动
				GraphConstants.setSizeable(discriptY1.getAttributes(), false); // 不可调整大小
				GraphConstants.setSelectable(discriptY1.getAttributes(), false); // 不可选中
				DefaultGraphCell discriptY2 = new DefaultGraphCell(new String("Y2"));
				GraphConstants.setFont(discriptY2.getAttributes(), fnAxis);
				GraphConstants.setBounds(discriptY2.getAttributes(), new Rectangle2D.Double(39.0001, 65, 10, 10));
				GraphConstants.setMoveable(discriptY2.getAttributes(), false); // 不可移动
				GraphConstants.setSizeable(discriptY2.getAttributes(), false); // 不可调整大小
				GraphConstants.setSelectable(discriptY2.getAttributes(), false); // 不可选中
				DefaultEdge discriptY = new DefaultEdge(new String("Precision"));
				GraphConstants.setLabelAlongEdge(discriptY.getAttributes(), true);
				GraphConstants.setFont(discriptY.getAttributes(), fnAxis);
				GraphConstants.setLineColor(discriptY.getAttributes(), Color.WHITE);
				GraphConstants.setForeground(discriptY.getAttributes(), Color.BLACK);
				GraphConstants.setEndFill(discriptY.getAttributes(), true);
				GraphConstants.setLineWidth(discriptY.getAttributes(), (float) (2.0));
				GraphConstants.setSelectable(discriptY.getAttributes(), false); // 不可选中

				DefaultGraphCell[] axisXText = new DefaultGraphCell[10];
				DefaultGraphCell[] axisYText = new DefaultGraphCell[10];
				DefaultGraphCell axis0Text = new DefaultGraphCell("0");
				GraphConstants.setBounds(axis0Text.getAttributes(), new Rectangle2D.Double(49, 570, 10, 10));
				GraphConstants.setMoveable(axis0Text.getAttributes(), false); // 不可移动
				GraphConstants.setSizeable(axis0Text.getAttributes(), false); // 不可调整大小
				GraphConstants.setSelectable(axis0Text.getAttributes(), false); // 不可选中
				for (int i = 0; i < 21; i++) {
					if (i % 2 == 0 && i / 2 != 0) {
						axisXText[i / 2 - 1] = new DefaultGraphCell(Double.toString((double) i / 20));
						GraphConstants.setBounds(axisXText[i / 2 - 1].getAttributes(),
								new Rectangle2D.Double(44, 550 - i / 2 * 50, 20, 20));
						GraphConstants.setMoveable(axisXText[i / 2 - 1].getAttributes(), false); // 不可移动
						GraphConstants.setSizeable(axisXText[i / 2 - 1].getAttributes(), false); // 不可调整大小
						GraphConstants.setSelectable(axisXText[i / 2 - 1].getAttributes(), false); // 不可选中
						axisYText[i / 2 - 1] = new DefaultGraphCell(Double.toString((double) i / 20));
						GraphConstants.setBounds(axisYText[i / 2 - 1].getAttributes(),
								new Rectangle2D.Double(59 + i / 2 * 50, 565, 20, 20));
						GraphConstants.setMoveable(axisYText[i / 2 - 1].getAttributes(), false); // 不可移动
						GraphConstants.setSizeable(axisYText[i / 2 - 1].getAttributes(), false); // 不可调整大小
						GraphConstants.setSelectable(axisYText[i / 2 - 1].getAttributes(), false); // 不可选中
					}
				}

				DefaultGraphCell[] OptModelCells = new DefaultGraphCell[filteredoptimalModels.size()];
				DefaultGraphCell[] LineMainCells = new DefaultGraphCell[filteredoptimalModels.size()];
				DefaultGraphCell[] LineXCells = new DefaultGraphCell[filteredoptimalModels.size()];
				DefaultGraphCell[] LineYCells = new DefaultGraphCell[filteredoptimalModels.size()];
				int OptMCcount = 0;
				for (String key : filteredoptimalModels.keySet()) {
					OptModelCells[OptMCcount] = new DefaultGraphCell();
					GraphConstants.setBounds(OptModelCells[OptMCcount].getAttributes(),
							new Rectangle2D.Double(67 + 500 * filteredoptimalModels.get(key).get(0),
									558 - 500 * filteredoptimalModels.get(key).get(1), 4, 4));
					GraphConstants.setBackground(OptModelCells[OptMCcount].getAttributes(), Color.RED);
					GraphConstants.setOpaque(OptModelCells[OptMCcount].getAttributes(), true);
					GraphConstants.setMoveable(OptModelCells[OptMCcount].getAttributes(), false); // 不可移动
					GraphConstants.setSizeable(OptModelCells[OptMCcount].getAttributes(), false); // 不可调整大小
					GraphConstants.setEditable(OptModelCells[OptMCcount].getAttributes(), false);

					LineMainCells[OptMCcount] = new DefaultGraphCell();
					GraphConstants.setBounds(LineMainCells[OptMCcount].getAttributes(),
							new Rectangle2D.Double(68.99995 + 500 * filteredoptimalModels.get(key).get(0),
									559.99995 - 500 * filteredoptimalModels.get(key).get(1), 0.0001, 0.0001));
					LineXCells[OptMCcount] = new DefaultGraphCell();
					GraphConstants.setBounds(LineXCells[OptMCcount].getAttributes(), new Rectangle2D.Double(
							68.99995 + 500 * filteredoptimalModels.get(key).get(0), 559.99995, 0.0001, 0.0001));
					LineYCells[OptMCcount] = new DefaultGraphCell();
					GraphConstants.setBounds(LineYCells[OptMCcount].getAttributes(), new Rectangle2D.Double(68.99995,
							559.99995 - 500 * filteredoptimalModels.get(key).get(1), 0.0001, 0.0001));

					DefaultEdge LineXEdge = new DefaultEdge();
					GraphConstants.setLineColor(LineXEdge.getAttributes(), Color.BLACK);
					GraphConstants.setLineWidth(LineXEdge.getAttributes(), (float) (1.0));
					GraphConstants.setSelectable(LineXEdge.getAttributes(), false); // 不可选中
					GraphConstants.setDashPattern(LineXEdge.getAttributes(), new float[] { 10, 10 });
					GraphConstants.setDashOffset(LineXEdge.getAttributes(), 5);
					jpAxis.getGraphLayoutCache().insertEdge(LineXEdge, LineMainCells[OptMCcount],
							LineXCells[OptMCcount]);

					DefaultEdge LineYEdge = new DefaultEdge();
					GraphConstants.setLineColor(LineYEdge.getAttributes(), Color.BLACK);
					GraphConstants.setLineWidth(LineYEdge.getAttributes(), (float) (1.0));
					GraphConstants.setSelectable(LineYEdge.getAttributes(), false); // 不可选中
					GraphConstants.setDashPattern(LineYEdge.getAttributes(), new float[] { 10, 10 });
					GraphConstants.setDashOffset(LineYEdge.getAttributes(), 5);
					jpAxis.getGraphLayoutCache().insertEdge(LineYEdge, LineMainCells[OptMCcount],
							LineYCells[OptMCcount]);

					mapSublogOptNode.put(key, OptModelCells[OptMCcount]);

					OptMCcount = OptMCcount + 1;
				}

				jpAxis.getGraphLayoutCache().insert(axisY);
				jpAxis.getGraphLayoutCache().insert(axisX);
				jpAxis.getGraphLayoutCache().insert(discriptX);
				jpAxis.getGraphLayoutCache().insert(discriptY1);
				jpAxis.getGraphLayoutCache().insert(discriptY2);
				jpAxis.getGraphLayoutCache().insert(axis0Text);
				jpAxis.getGraphLayoutCache().insert(axisXText);
				jpAxis.getGraphLayoutCache().insert(axisYText);
				jpAxis.getGraphLayoutCache().insertEdge(discriptY, discriptY1, discriptY2);
				jpAxis.getGraphLayoutCache().insert(OptModelCells);

				jpAxis.setPreferredSize(new Dimension(650, 630));

				jpAxis.addGraphSelectionListener(new GraphSelectionListener() {
					public void valueChanged(GraphSelectionEvent e) {
						String targetStr2 = null;
						jpAxis.refresh();

						for (DefaultGraphCell icell : mapSublogOptNode.values()) {
							if (e.getCell() == icell) {
								for (String istr : mapSublogOptNode.keySet()) {
									if (mapSublogOptNode.get(istr) == icell) {
										targetStr2 = istr;
									}
								}
							}
						}

						OptmalviewPanel.remove(miniembeddedconfig.getOptimalModel());

						try {
							TmpOPM = GetModelPanel(context, targetStr2);
						} catch (ConnectionCannotBeObtained e1) {
							e1.printStackTrace();
						}

						OptmalviewPanel.add(TmpOPM);

						miniembeddedconfig.setOptimalModel(TmpOPM);

						getInspector().revalidate();
						getInspector().repaint();

						double FitnessValue = filteredoptimalModels.get(targetStr2).get(0);
						double PrecisionValue = filteredoptimalModels.get(targetStr2).get(1);
						double EventValue = Math
								.round(filteredoptimalModels.get(targetStr2).get(2) / TotalEventCount * 10000) / 100;
						double ActivityValue = filteredoptimalModels.get(targetStr2).get(3) / TotalActivityCount * 100;

						if (Double.isNaN(FitnessValue) && Double.isNaN(PrecisionValue)) {
							UrlArea2.setText("Activities in this subset: \n" + targetStr2 + "\n" + "Fitness: "
									+ Double.toString(FitnessValue) + "\n" + "Precision: "
									+ Double.toString(PrecisionValue) + "\n" + "Event: " + Double.toString(EventValue)
									+ "%" + "\n" + "Activity: " + Double.toString(ActivityValue) + "%");
						} else if (Double.isNaN(FitnessValue) && !Double.isNaN(PrecisionValue)) {
							UrlArea2.setText("Activities in this subset: \n" + targetStr2 + "\n" + "Fitness: "
									+ Double.toString(FitnessValue) + "\n" + "Precision" + ": "
									+ Double.toString((double) Math.round(PrecisionValue * 100) / 100) + "\n"
									+ "Event: " + Double.toString(EventValue) + "%" + "\n" + "Activity: "
									+ Double.toString(ActivityValue) + "%");
						} else if (!Double.isNaN(FitnessValue) && Double.isNaN(PrecisionValue)) {
							UrlArea2.setText("Activities in this subset: \n" + targetStr2 + "\n" + "Fitness: "
									+ Double.toString((double) Math.round(FitnessValue * 100) / 100) + "\n"
									+ "Precision" + ": " + Double.toString(PrecisionValue) + "\n" + "Event: "
									+ Double.toString(EventValue) + "%" + "\n" + "Activity: "
									+ Double.toString(ActivityValue) + "%");
						} else {
							UrlArea2.setText("Activities in this subset: \n" + targetStr2 + "\n" + "Fitness: "
									+ Double.toString((double) Math.round(FitnessValue * 100) / 100) + "\n"
									+ "Precision" + ": "
									+ Double.toString((double) Math.round(PrecisionValue * 100) / 100) + "\n"
									+ "Event: " + Double.toString(EventValue) + "%" + "\n" + "Activity: "
									+ Double.toString(ActivityValue) + "%");
						}

						jpAxis.refresh();
					}
				});

				JPOptimal.add(jpAxis);
				miniembeddedconfig.setOptimalGraph(jpAxis);

				getInspector().revalidate();
				getInspector().repaint();
			}
		});

		JSlider OPTSliderE = new JSlider(JSlider.VERTICAL, OPTS_MIN, OPTS_MAX, OPTS_INIT);

		//Turn on labels at major tick marks.
		OPTSliderE.setMajorTickSpacing(10);
		OPTSliderE.setMinorTickSpacing(1);
		OPTSliderE.setPaintTicks(true);
		OPTSliderE.setPaintLabels(true);
		OPTSliderE.setPreferredSize(new Dimension(60, 600));
		OPTSliderE.setLabelTable(labelTable);
		OPTSliderE.setPaintLabels(true);

		OPTSliderE.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				valueEC = OPTSliderE.getValue();
				HashMap<String, List<Double>> filteredModels = new HashMap<String, List<Double>>();
				HashMap<String, List<Double>> filteredoptimalModels = new HashMap<String, List<Double>>();
				for (Result r : result) {
					if (r.eventcount >= (TotalEventCount * ((float) valueEC / 100))
							&& r.activitycount >= (maxNumOfAct * ((float) valueAC / 100))) {
						List<Double> Tempfiltercatch = new ArrayList<Double>();
						Tempfiltercatch.add(r.Fitness);
						Tempfiltercatch.add(r.Percisions);
						Tempfiltercatch.add((double) r.eventcount);
						Tempfiltercatch.add((double) r.activitycount);
						filteredModels.put(r.sublog, Tempfiltercatch);
					}
				}

				for (Map.Entry<String, List<Double>> entryr : filteredModels.entrySet()) {
					int catchflag = 1;
					if (Double.isNaN(entryr.getValue().get(0)) || Double.isNaN(entryr.getValue().get(1))) {
						catchflag = 0;
					} else {
						for (Map.Entry<String, List<Double>> entryc : filteredModels.entrySet()) {
							if (Double.isNaN(entryc.getValue().get(0)) || Double.isNaN(entryc.getValue().get(1))) {
								;
							} else {
								if (entryr.getValue().get(0) < entryc.getValue().get(0)
										&& entryr.getValue().get(1) < entryc.getValue().get(1)) {
									catchflag = 0;
									break;
								}
							}
						}
					}

					if (catchflag == 1) {
						List<Double> Tempcatch = new ArrayList<Double>();
						Tempcatch.add(entryr.getValue().get(0));
						Tempcatch.add(entryr.getValue().get(1));
						Tempcatch.add(entryr.getValue().get(2));
						Tempcatch.add(entryr.getValue().get(3));
						filteredoptimalModels.put(entryr.getKey(), Tempcatch);
					}
				}

				JPOptimal.remove(miniembeddedconfig.getOptimalGraph());

				GraphModel model2 = new DefaultGraphModel();
				GraphLayoutCache view2 = new GraphLayoutCache(model2, new DefaultCellViewFactory());
				JGraph jpAxis = new JGraph(model2, view2);
				Font fnAxis = new Font("Times New Roman", Font.BOLD, 22);
				DefaultGraphCell axisY = new DefaultGraphCell();
				GraphConstants.setForeground(axisY.getAttributes(), Color.WHITE);
				GraphConstants.setFont(axisY.getAttributes(), fnAxis);
				GraphConstants.setBounds(axisY.getAttributes(), new Rectangle2D.Double(69, 10, 2, 550));
				GraphConstants.setBackground(axisY.getAttributes(), Color.DARK_GRAY);
				GraphConstants.setOpaque(axisY.getAttributes(), true);
				GraphConstants.setMoveable(axisY.getAttributes(), false); // 不可移动
				GraphConstants.setSizeable(axisY.getAttributes(), false); // 不可调整大小
				GraphConstants.setSelectable(axisY.getAttributes(), false); // 不可选中

				DefaultGraphCell axisX = new DefaultGraphCell();
				GraphConstants.setForeground(axisX.getAttributes(), Color.WHITE);
				GraphConstants.setFont(axisX.getAttributes(), fnAxis);
				GraphConstants.setBounds(axisX.getAttributes(), new Rectangle2D.Double(69, 560, 550, 2));
				GraphConstants.setBackground(axisX.getAttributes(), Color.DARK_GRAY);
				GraphConstants.setOpaque(axisX.getAttributes(), true);
				GraphConstants.setMoveable(axisX.getAttributes(), false); // 不可移动
				GraphConstants.setSizeable(axisX.getAttributes(), false); // 不可调整大小
				GraphConstants.setSelectable(axisX.getAttributes(), false); // 不可选中

				DefaultGraphCell discriptX = new DefaultGraphCell(new String("Fitness"));
				GraphConstants.setFont(discriptX.getAttributes(), fnAxis);
				GraphConstants.setBounds(discriptX.getAttributes(), new Rectangle2D.Double(269, 575, 100, 50));
				GraphConstants.setMoveable(discriptX.getAttributes(), false); // 不可移动
				GraphConstants.setSizeable(discriptX.getAttributes(), false); // 不可调整大小
				GraphConstants.setSelectable(discriptX.getAttributes(), false); // 不可选中		

				DefaultGraphCell discriptY1 = new DefaultGraphCell(new String("Y1"));
				GraphConstants.setFont(discriptY1.getAttributes(), fnAxis);
				GraphConstants.setBounds(discriptY1.getAttributes(), new Rectangle2D.Double(39, 555, 10, 10));
				GraphConstants.setMoveable(discriptY1.getAttributes(), false); // 不可移动
				GraphConstants.setSizeable(discriptY1.getAttributes(), false); // 不可调整大小
				GraphConstants.setSelectable(discriptY1.getAttributes(), false); // 不可选中
				DefaultGraphCell discriptY2 = new DefaultGraphCell(new String("Y2"));
				GraphConstants.setFont(discriptY2.getAttributes(), fnAxis);
				GraphConstants.setBounds(discriptY2.getAttributes(), new Rectangle2D.Double(39.0001, 65, 10, 10));
				GraphConstants.setMoveable(discriptY2.getAttributes(), false); // 不可移动
				GraphConstants.setSizeable(discriptY2.getAttributes(), false); // 不可调整大小
				GraphConstants.setSelectable(discriptY2.getAttributes(), false); // 不可选中
				DefaultEdge discriptY = new DefaultEdge(new String("Precision"));
				GraphConstants.setLabelAlongEdge(discriptY.getAttributes(), true);
				GraphConstants.setFont(discriptY.getAttributes(), fnAxis);
				GraphConstants.setLineColor(discriptY.getAttributes(), Color.WHITE);
				GraphConstants.setForeground(discriptY.getAttributes(), Color.BLACK);
				GraphConstants.setEndFill(discriptY.getAttributes(), true);
				GraphConstants.setLineWidth(discriptY.getAttributes(), (float) (2.0));
				GraphConstants.setSelectable(discriptY.getAttributes(), false); // 不可选中

				DefaultGraphCell[] axisXText = new DefaultGraphCell[10];
				DefaultGraphCell[] axisYText = new DefaultGraphCell[10];
				DefaultGraphCell axis0Text = new DefaultGraphCell("0");
				GraphConstants.setBounds(axis0Text.getAttributes(), new Rectangle2D.Double(49, 570, 10, 10));
				GraphConstants.setMoveable(axis0Text.getAttributes(), false); // 不可移动
				GraphConstants.setSizeable(axis0Text.getAttributes(), false); // 不可调整大小
				GraphConstants.setSelectable(axis0Text.getAttributes(), false); // 不可选中
				for (int i = 0; i < 21; i++) {
					if (i % 2 == 0 && i / 2 != 0) {
						axisXText[i / 2 - 1] = new DefaultGraphCell(Double.toString((double) i / 20));
						GraphConstants.setBounds(axisXText[i / 2 - 1].getAttributes(),
								new Rectangle2D.Double(44, 550 - i / 2 * 50, 20, 20));
						GraphConstants.setMoveable(axisXText[i / 2 - 1].getAttributes(), false); // 不可移动
						GraphConstants.setSizeable(axisXText[i / 2 - 1].getAttributes(), false); // 不可调整大小
						GraphConstants.setSelectable(axisXText[i / 2 - 1].getAttributes(), false); // 不可选中
						axisYText[i / 2 - 1] = new DefaultGraphCell(Double.toString((double) i / 20));
						GraphConstants.setBounds(axisYText[i / 2 - 1].getAttributes(),
								new Rectangle2D.Double(59 + i / 2 * 50, 565, 20, 20));
						GraphConstants.setMoveable(axisYText[i / 2 - 1].getAttributes(), false); // 不可移动
						GraphConstants.setSizeable(axisYText[i / 2 - 1].getAttributes(), false); // 不可调整大小
						GraphConstants.setSelectable(axisYText[i / 2 - 1].getAttributes(), false); // 不可选中
					}
				}

				DefaultGraphCell[] OptModelCells = new DefaultGraphCell[filteredoptimalModels.size()];
				DefaultGraphCell[] LineMainCells = new DefaultGraphCell[filteredoptimalModels.size()];
				DefaultGraphCell[] LineXCells = new DefaultGraphCell[filteredoptimalModels.size()];
				DefaultGraphCell[] LineYCells = new DefaultGraphCell[filteredoptimalModels.size()];
				int OptMCcount = 0;
				for (String key : filteredoptimalModels.keySet()) {
					OptModelCells[OptMCcount] = new DefaultGraphCell();
					GraphConstants.setBounds(OptModelCells[OptMCcount].getAttributes(),
							new Rectangle2D.Double(67 + 500 * filteredoptimalModels.get(key).get(0),
									558 - 500 * filteredoptimalModels.get(key).get(1), 4, 4));
					GraphConstants.setBackground(OptModelCells[OptMCcount].getAttributes(), Color.RED);
					GraphConstants.setOpaque(OptModelCells[OptMCcount].getAttributes(), true);
					GraphConstants.setMoveable(OptModelCells[OptMCcount].getAttributes(), false); // 不可移动
					GraphConstants.setSizeable(OptModelCells[OptMCcount].getAttributes(), false); // 不可调整大小
					GraphConstants.setEditable(OptModelCells[OptMCcount].getAttributes(), false);

					LineMainCells[OptMCcount] = new DefaultGraphCell();
					GraphConstants.setBounds(LineMainCells[OptMCcount].getAttributes(),
							new Rectangle2D.Double(68.99995 + 500 * filteredoptimalModels.get(key).get(0),
									559.99995 - 500 * filteredoptimalModels.get(key).get(1), 0.0001, 0.0001));
					LineXCells[OptMCcount] = new DefaultGraphCell();
					GraphConstants.setBounds(LineXCells[OptMCcount].getAttributes(), new Rectangle2D.Double(
							68.99995 + 500 * filteredoptimalModels.get(key).get(0), 559.99995, 0.0001, 0.0001));
					LineYCells[OptMCcount] = new DefaultGraphCell();
					GraphConstants.setBounds(LineYCells[OptMCcount].getAttributes(), new Rectangle2D.Double(68.99995,
							559.99995 - 500 * filteredoptimalModels.get(key).get(1), 0.0001, 0.0001));

					DefaultEdge LineXEdge = new DefaultEdge();
					GraphConstants.setLineColor(LineXEdge.getAttributes(), Color.BLACK);
					GraphConstants.setLineWidth(LineXEdge.getAttributes(), (float) (1.0));
					GraphConstants.setSelectable(LineXEdge.getAttributes(), false); // 不可选中
					GraphConstants.setDashPattern(LineXEdge.getAttributes(), new float[] { 10, 10 });
					GraphConstants.setDashOffset(LineXEdge.getAttributes(), 5);
					jpAxis.getGraphLayoutCache().insertEdge(LineXEdge, LineMainCells[OptMCcount],
							LineXCells[OptMCcount]);

					DefaultEdge LineYEdge = new DefaultEdge();
					GraphConstants.setLineColor(LineYEdge.getAttributes(), Color.BLACK);
					GraphConstants.setLineWidth(LineYEdge.getAttributes(), (float) (1.0));
					GraphConstants.setSelectable(LineYEdge.getAttributes(), false); // 不可选中
					GraphConstants.setDashPattern(LineYEdge.getAttributes(), new float[] { 10, 10 });
					GraphConstants.setDashOffset(LineYEdge.getAttributes(), 5);
					jpAxis.getGraphLayoutCache().insertEdge(LineYEdge, LineMainCells[OptMCcount],
							LineYCells[OptMCcount]);

					mapSublogOptNode.put(key, OptModelCells[OptMCcount]);

					OptMCcount = OptMCcount + 1;
				}

				jpAxis.getGraphLayoutCache().insert(axisY);
				jpAxis.getGraphLayoutCache().insert(axisX);
				jpAxis.getGraphLayoutCache().insert(discriptX);
				jpAxis.getGraphLayoutCache().insert(discriptY1);
				jpAxis.getGraphLayoutCache().insert(discriptY2);
				jpAxis.getGraphLayoutCache().insert(axis0Text);
				jpAxis.getGraphLayoutCache().insert(axisXText);
				jpAxis.getGraphLayoutCache().insert(axisYText);
				jpAxis.getGraphLayoutCache().insertEdge(discriptY, discriptY1, discriptY2);
				jpAxis.getGraphLayoutCache().insert(OptModelCells);

				jpAxis.setPreferredSize(new Dimension(650, 630));

				jpAxis.addGraphSelectionListener(new GraphSelectionListener() {
					public void valueChanged(GraphSelectionEvent e) {
						String targetStr2 = null;
						jpAxis.refresh();

						for (DefaultGraphCell icell : mapSublogOptNode.values()) {
							if (e.getCell() == icell) {
								for (String istr : mapSublogOptNode.keySet()) {
									if (mapSublogOptNode.get(istr) == icell) {
										targetStr2 = istr;
									}
								}
							}
						}

						OptmalviewPanel.remove(miniembeddedconfig.getOptimalModel());

						try {
							TmpOPM = GetModelPanel(context, targetStr2);
						} catch (ConnectionCannotBeObtained e1) {
							e1.printStackTrace();
						}

						OptmalviewPanel.add(TmpOPM);

						miniembeddedconfig.setOptimalModel(TmpOPM);

						getInspector().revalidate();
						getInspector().repaint();

						double FitnessValue = filteredoptimalModels.get(targetStr2).get(0);
						double PrecisionValue = filteredoptimalModels.get(targetStr2).get(1);
						double EventValue = Math
								.round(filteredoptimalModels.get(targetStr2).get(2) / TotalEventCount * 10000) / 100;
						double ActivityValue = filteredoptimalModels.get(targetStr2).get(3) / TotalActivityCount * 100;

						if (Double.isNaN(FitnessValue) && Double.isNaN(PrecisionValue)) {
							UrlArea2.setText("Activities in this subset: \n" + targetStr2 + "\n" + "Fitness: "
									+ Double.toString(FitnessValue) + "\n" + "Precision: "
									+ Double.toString(PrecisionValue) + "\n" + "Event: " + Double.toString(EventValue)
									+ "%" + "\n" + "Activity: " + Double.toString(ActivityValue) + "%");
						} else if (Double.isNaN(FitnessValue) && !Double.isNaN(PrecisionValue)) {
							UrlArea2.setText("Activities in this subset: \n" + targetStr2 + "\n" + "Fitness: "
									+ Double.toString(FitnessValue) + "\n" + "Precision" + ": "
									+ Double.toString((double) Math.round(PrecisionValue * 100) / 100) + "\n"
									+ "Event: " + Double.toString(EventValue) + "%" + "\n" + "Activity: "
									+ Double.toString(ActivityValue) + "%");
						} else if (!Double.isNaN(FitnessValue) && Double.isNaN(PrecisionValue)) {
							UrlArea2.setText("Activities in this subset: \n" + targetStr2 + "\n" + "Fitness: "
									+ Double.toString((double) Math.round(FitnessValue * 100) / 100) + "\n"
									+ "Precision" + ": " + Double.toString(PrecisionValue) + "\n" + "Event: "
									+ Double.toString(EventValue) + "%" + "\n" + "Activity: "
									+ Double.toString(ActivityValue) + "%");
						} else {
							UrlArea2.setText("Activities in this subset: \n" + targetStr2 + "\n" + "Fitness: "
									+ Double.toString((double) Math.round(FitnessValue * 100) / 100) + "\n"
									+ "Precision" + ": "
									+ Double.toString((double) Math.round(PrecisionValue * 100) / 100) + "\n"
									+ "Event: " + Double.toString(EventValue) + "%" + "\n" + "Activity: "
									+ Double.toString(ActivityValue) + "%");
						}

						jpAxis.refresh();
					}
				});

				JPOptimal.add(jpAxis);
				miniembeddedconfig.setOptimalGraph(jpAxis);

				getInspector().revalidate();
				getInspector().repaint();
			}
		});

		miniembeddedconfig.setOptimalGraph(jpAxis);

		JLabel OAL = new JLabel("Activity") {
			private static final long serialVersionUID = 1L;

			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				AffineTransform aT = g2.getTransform();
				Shape oldshape = g2.getClip();
				double x = getWidth() / 2.0;
				double y = getHeight() / 2.0;
				aT.rotate(Math.toRadians(-90), x, y);
				g2.setTransform(aT);
				g2.setClip(oldshape);
				super.paintComponent(g);
			}
		};
		JLabel OAE = new JLabel("Event") {
			private static final long serialVersionUID = 1L;

			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				AffineTransform aT = g2.getTransform();
				Shape oldshape = g2.getClip();
				double x = getWidth() / 2.0;
				double y = getHeight() / 2.0;
				aT.rotate(Math.toRadians(-90), x, y);
				g2.setTransform(aT);
				g2.setClip(oldshape);
				super.paintComponent(g);
			}
		};
		JPOptimal.add(OAL);
		JPOptimal.add(OPTSliderA);
		JPOptimal.add(OAE);
		JPOptimal.add(OPTSliderE);
		JPOptimal.add(jpAxis);

		getInspector().addGroup(ParetO, "Pareto Optimal Model", JPOptimal);
		getInspector().addGroup(ParetO, "Selected information", SelectedInfo2);
		getInspector().addGroup(ParetO, "Selected model", OptmalviewPanel);

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
//
////		addComponentListener(new ComponentAdapter() {
////			@Override
////			public void componentResized(ComponentEvent e) {
////				int totalHeight_Splitter = getHeight();
////				int dividerLocation = (int) (totalHeight_Splitter * 0.618);
////				splitPane.setDividerLocation(dividerLocation);
////			}
////		});

		validate();
		repaint();
	}

	public static long factorial(long number) {
		if (number <= 1)
			return 1;
		else
			return number * factorial(number - 1);
	}

	public static long CombinationNum(long numberN, long numberM) {
		return (factorial(numberN) / (factorial(numberM) * factorial(numberN - numberM)));
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

	//	static JPanel GetModelPanel(PluginContext context, String name) {
	//		MainVisualConfig miniembeddedconfig = new MainVisualConfig();
	//		XLog input = miniembeddedconfig.getInputLog();
	//
	//		// inductive
	//		IMMiningDialog dialog = miniembeddedconfig.getDialog();
	//
	//		String[] subsettemp = name.split(",");
	//
	//		HashSet<String> subset = new HashSet<String>();
	//		for (String str : subsettemp) {
	//			subset.add(str);
	//		}
	//
	//		XLog tgr = MainProjectionPlugin.GetProjection(input, subset).getFirst(); // step1
	//
	//		Object[] tempIMresult = IMPetriNet.minePetriNet(context, tgr, dialog.getMiningParameters());
	//
	//		PetriNetVisualization tempModelJPanel = new PetriNetVisualization(context,
	//				(PetrinetGraph) tempIMresult[0]);
	//
	//		return tempModelJPanel.MJP;
	//	}

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
}
