/**
 * 
 */
package org.processmining.explorativeprocessdiscovery.visual;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

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

public class MainProjectionVisPanelBestRoute extends ProjectionVisPanelChanged {
	private static final long serialVersionUID = -6674503536171244970L;
	MainVisualConfig miniembeddedconfig = new MainVisualConfig();
	private static final Font ITALICFont = new Font("TimesRoman", Font.ITALIC, 10);
	private static final Font SITALICFont = new Font("TimesRoman", Font.ITALIC, 4);
	private static final Font BLFont = new Font("TimesRoman", Font.PLAIN, 4);
	Color limeGreen = new Color(0, 128, 128);
	static int displayType;
	static List<DefaultEdge> alledgelist = new ArrayList<DefaultEdge>();
	static HashMap<String, List<HashSet<String>>> childList = new HashMap<String, List<HashSet<String>>>();
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

	public MainProjectionVisPanelBestRoute(PluginContext context, List<Result> results, int Type) {
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

		int maxNumOfAct = result.get(0).subset.size();
		HashMap<Integer, Integer> layercount = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> layercount4graph = new HashMap<Integer, Integer>();
		for (int i = 0; i < maxNumOfAct; i++) {
			layercount.put(i + 1, 0);
			layercount4graph.put(i + 1, 0);
		}
		List<HashSet<String>> TempChild = new ArrayList<HashSet<String>>();
		for (Result r : result) {
			layercount.put(r.subset.size(), layercount.get(r.subset.size()) + 1);
			layercount4graph.put(r.subset.size(), layercount4graph.get(r.subset.size()) + 1);
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
			TempChild = new ArrayList<HashSet<String>>();
			for (Result r2 : result) {
				HashSet<String> TempsubsetCom = new HashSet<String>(r.subset);
				TempsubsetCom.addAll(r2.subset);
				if (r.subset.size() == TempsubsetCom.size() && r.subset.size() - r2.subset.size() == 1) {
					TempChild.add(r2.subset);
				}
				TempsubsetCom = null;
			}
			childList.put(r.sublog, TempChild);
		}

		int nodesNum = result.size();
		int maxNumOfNodes = 0;
		for (Map.Entry<Integer, Integer> entry : layercount.entrySet()) {
			if (maxNumOfNodes < entry.getValue()) {
				maxNumOfNodes = entry.getValue();
			}
		}
		int nodeStepLenX = 50;
		int nodeStepLenY = 100;
		int nodeWidth = 30;
		int infonodeX = 30;
		int infonodeY = 5;
		int initialNodeX = (int) Math.ceil((double) maxNumOfNodes / 2) * (nodeStepLenX + nodeWidth) + 20;
		int initialNodeY = 20;
		HashMap<String, DefaultGraphCell> mapSublogNode = new HashMap<String, DefaultGraphCell>();
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
			int layerNodesNum = layercount.get(i.subset.size());
			cellColor = colorcalc(getTypeValue(i), resultMaxValue, resultMinValue);

			List<String> EventAndActivity = new ArrayList<>();
			double Eventnum = (double) Math.round(((long) i.eventcount * 10000) / TotalEventCount) / 100;
			double Activitynum = (double) Math.round(((long) i.activitycount * 10000) / TotalActivityCount) / 100;
			String Eventstr = Double.toString(Eventnum) + "%";
			String Activitystr = Double.toString(Activitynum) + "%";
			EventAndActivity.add(Eventstr);
			EventAndActivity.add(Activitystr);

			if (layerNodesNum == 1) {
				startX = initialNodeX;
			} else if (layerNodesNum != 1 && layerNodesNum % 2 == 1) {
				startX = initialNodeX - (int) Math.floor((double) layerNodesNum / 2) * (nodeStepLenX + nodeWidth);
			} else if (layerNodesNum % 2 == 0) {
				startX = initialNodeX - (int) Math.floor((double) layerNodesNum / 2) * (nodeStepLenX + nodeWidth)
						+ nodeStepLenX / 2 + nodeWidth / 2;
			}

			if (i.subset.size() != tempMaxNumOfAct) {
				stepX = startX;
				startY = startY + nodeStepLenY;
				tempMaxNumOfAct = i.subset.size();
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

			if (layercount4graph.get(i.subset.size()) != 1) {
				stepX = stepX + nodeStepLenX + nodeWidth;
				layercount4graph.put(i.subset.size(), layercount4graph.get(i.subset.size()) - 1);
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

					DefaultPort portS = new DefaultPort();
					GraphConstants.setOffset(portS.getAttributes(),
							new Point2D.Double(GraphConstants.PERMILLE / 2, nodeWidth + infonodeY));
					GraphConstants.setAbsoluteY(portS.getAttributes(), true);
					icell.add(portS);

					if (!childList.get(targetStr).isEmpty()) {
						String child = "";
						for (HashSet<String> childSet : childList.get(targetStr)) {
							child = String.join(",", childSet);
							String DeletedEle = "";
							for (String Fele : targetStr.split(",")) {
								int Countdiff = 0;
								for (String Sele : childSet) {
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

							DefaultEdge edge = new DefaultEdge(DeletedEle);
							int arrow = GraphConstants.ARROW_CLASSIC;
							GraphConstants.setLineColor(edge.getAttributes(), edgeCol);
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
						icell.remove(portS);
					} else {
						;
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
						runningmessagedialog.dispose();
					}
				}

				graph.refresh();
			}
		});

		miniembeddedconfig.setZoom(graph);

		scroll = new JScrollPane(miniembeddedconfig.getZoom());
		setLayout(new BorderLayout());
		add(scroll);

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
