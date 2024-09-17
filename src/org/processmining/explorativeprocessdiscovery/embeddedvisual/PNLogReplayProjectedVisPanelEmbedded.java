/**
 * 
 */
package org.processmining.explorativeprocessdiscovery.embeddedvisual;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Collection;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.deckfour.xes.model.XLog;
import org.processmining.explorativeprocessdiscovery.visual.MainVisualConfig;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.visualization.AlignmentConstants;
import org.processmining.plugins.pnalignanalysis.visualization.projection.util.GraphBuilder;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

/**
 * @author aadrians Oct 26, 2011
 * 
 */
public class PNLogReplayProjectedVisPanelEmbedded extends ProjectionVisPanelEmbedded {
	private static final long serialVersionUID = -6674503536171244970L;
	

	MainVisualConfig miniembeddedconfig = new MainVisualConfig();

	// for graph visualization
	protected boolean[] placeWithMoveOnLog;

	protected Color involvedMoveOnLogColor = new Color(255, 0, 0, 200);
	protected Color transparentColor = new Color(255, 255, 255, 0);

	// transition coloring
	public static Color NOOCCURRENCE = new Color(239, 243, 255);
	public static Color LOW = new Color(198, 219, 239);
	public static Color LOWMED = new Color(158, 202, 225);
	public static Color MED = new Color(107, 174, 214);
	public static Color MEDHIGH = new Color(49, 130, 189);
	public static Color HIGH = new Color(8, 81, 156);

	// reference to info
	protected CoreInfoProviderEmbedded provider;

	// utilities
	protected final TransEvClassMapping map;

	public PNLogReplayProjectedVisPanelEmbedded(PluginContext context, PetrinetGraph origNet, Marking origMarking, XLog log,
			TransEvClassMapping map, PNRepResult logReplayResult) {
		super(context, origNet, origMarking, log, map, logReplayResult);
		this.map = map;

		initialize(context);
	}

	protected void initialize(PluginContext context) {

		// calculate info
		provider = createCoreInfoProvider(log, map, logReplayResult);
		this.placeWithMoveOnLog = new boolean[provider.getNumPlaces()];

		/**
		 * Main visualization (has to be after creating provider)
		 */

		scalable = GraphBuilder.buildJGraph(net, oldLayoutConn);
		graph = (ProMJGraph) scalable;

		scroll = new JScrollPane(scalable.getComponent());
		decorator.decorate(scroll, Color.WHITE, Color.GRAY, Color.DARK_GRAY);
		setLayout(new BorderLayout());
		add(scalable.getComponent());

		// initialize decorator for transitions
		Transition[] transArr = provider.getTransArray();
		int pointer = 0;
		while (pointer < provider.getNumTrans()) {
			int[] info = provider.getInfoNode(pointer);
			TransConfDecoratorEmbedded dec = createTransitionDecorator(transArr, pointer, info);
			decoratorMap.put(transArr[pointer], dec);
			graph.getViewSpecificAttributes().putViewSpecific(transArr[pointer], AttributeMap.SHAPEDECORATOR, dec);
			graph.getViewSpecificAttributes().putViewSpecific(transArr[pointer], AttributeMap.SHOWLABEL, false);

			pointer++;
		}

		constructVisualization(graph.getViewSpecificAttributes(), true, true);
		constructPlaceVisualization(graph.getViewSpecificAttributes());

		validate();
		repaint();
		
		JPanel tempJP = new JPanel();
		tempJP.add(graph.getComponent());
		miniembeddedconfig.setTempModel(tempJP);
	}

	protected TransConfDecoratorEmbedded createTransitionDecorator(Transition[] transArr, int pointer, int[] info) {
		return new TransConfDecoratorEmbedded(info[3], info[0], transArr[pointer].getLabel());
	}

	protected CoreInfoProviderEmbedded createCoreInfoProvider(XLog log, TransEvClassMapping map, PNRepResult logReplayResult) {
		return new CoreInfoProviderEmbedded(net, marking, map, log, logReplayResult);
	}

	protected void constructPlaceVisualization(ViewSpecificAttributeMap map) {
		// update place visualization
		Place[] placeArr = provider.getPlaceArray();
		int[] freq = provider.getPlaceFreq();

		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;

		for (int i = 0; i < freq.length; i++) {
			if (freq[i] < min) {
				min = freq[i];
			}
			if (freq[i] > max) {
				max = freq[i];
			}
		}

		double median = ((double) (max - min)) / 2;
		int medianPlaceRadius = 30;
		int flexibility = 10;
		for (int i = 0; i < placeArr.length; i++) {
			int size = medianPlaceRadius + (int) Math.floor((freq[i] - median) * flexibility / median);
			if (freq[i] > 0) {
				map.putViewSpecific(placeArr[i], AttributeMap.FILLCOLOR, AlignmentConstants.MOVELOGCOLOR);
				this.placeWithMoveOnLog[i] = true;
			} else {
				map.putViewSpecific(placeArr[i], AttributeMap.FILLCOLOR, transparentColor);
				this.placeWithMoveOnLog[i] = false;
			}
			map.putViewSpecific(placeArr[i], AttributeMap.SIZE, new Dimension(size, size));
		}
	}

	public void constructVisualization(ViewSpecificAttributeMap map, boolean isShowMoveLogModel, boolean isShowMoveModel) {
		graph.getModel().beginUpdate();

		doUpdateInternal(map, isShowMoveLogModel, isShowMoveModel);

		graph.getModel().endUpdate();
		graph.refresh();
		graph.revalidate();
		graph.repaint();
	}

	protected void doUpdateInternal(ViewSpecificAttributeMap map, boolean isShowMoveLogModel, boolean isShowMoveModel) {
		/**
		 * Update main visualization (add decoration, size)
		 */
		int[] minMaxFreq = provider.getMinMaxFreq(isShowMoveLogModel, isShowMoveModel);

		Transition[] transArr = provider.getTransArray();
		int pointer = 0;

		while (pointer < transArr.length) {
			TransConfDecoratorEmbedded dec = decoratorMap.get(transArr[pointer]);
			int[] info = provider.getInfoNode(pointer);
			int occurrence = 0;
			if (isShowMoveLogModel) {
				dec.setMoveSyncFreq(info[0]);
				occurrence += info[0];
			} else {
				dec.setMoveSyncFreq(0);
			}
			if (isShowMoveModel) {
				if (!transArr[pointer].isInvisible()) {
					if (info[3] > 0) {
						map.putViewSpecific(transArr[pointer], AttributeMap.STROKECOLOR, Color.RED);
					} else {
						map.putViewSpecific(transArr[pointer], AttributeMap.STROKECOLOR, Color.BLACK);
					}
				}
				dec.setMoveOnModelFreq(info[3]);
				occurrence += info[3];
			} else {
				map.putViewSpecific(transArr[pointer], AttributeMap.STROKECOLOR, Color.BLACK);
				dec.setMoveOnModelFreq(0);
			}
			if (occurrence > 0) {
				map.putViewSpecific(transArr[pointer], AttributeMap.STROKECOLOR, Color.BLACK);
			}
			float suggestedArcWidth = getAppropriateStrokeWidth(occurrence);

			int intensity = minMaxFreq[1] > 0 ? 100 - (int) ((50.0 * (occurrence - minMaxFreq[0])) / (minMaxFreq[1] - minMaxFreq[0]))
					: 100;

			Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> edges = net
					.getInEdges(transArr[pointer]);
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges) {
				setPetrinetEdgeAttributes(map, suggestedArcWidth, intensity, edge);
			}
			edges = net.getOutEdges(transArr[pointer]);
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : edges) {
				setPetrinetEdgeAttributes(map, suggestedArcWidth, intensity, edge);
			}
			colorTransition(map, transArr[pointer], occurrence, minMaxFreq[0], minMaxFreq[1]);
			pointer++;
		}
	}

	protected void setPetrinetEdgeAttributes(ViewSpecificAttributeMap map, float suggestedArcWidth, int intensity,
			PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge) {
		map.putViewSpecific(edge, AttributeMap.EDGECOLOR, new Color(intensity, intensity, intensity));
		map.putViewSpecific(edge, AttributeMap.LINEWIDTH, suggestedArcWidth);
	}

	/**
	 * Color transitions based on 5 scaling
	 * 
	 * @param transition
	 * @param occurrence
	 * @param min
	 * @param max
	 */
	private void colorTransition(ViewSpecificAttributeMap map, Transition transition, int occurrence, int min, int max) {
		if (!transition.isInvisible()) {
			map.putViewSpecific(transition, AttributeMap.FILLCOLOR, Color.LIGHT_GRAY);
			// use 5 color scale
			if ((min == max) || (occurrence == max)) {
				map.putViewSpecific(transition, AttributeMap.FILLCOLOR, HIGH);
				decoratorMap.get(transition).setLightColorLabel(true);
			} else {
				if (occurrence == 0) {
					map.putViewSpecific(transition, AttributeMap.FILLCOLOR, NOOCCURRENCE);
					decoratorMap.get(transition).setLightColorLabel(false);
				} else {
					int scale = (occurrence - min) * 5 / (max - min);
					if (scale == 0) {
						map.putViewSpecific(transition, AttributeMap.FILLCOLOR, LOW);
						decoratorMap.get(transition).setLightColorLabel(false);
					} else if (scale == 1) {
						map.putViewSpecific(transition, AttributeMap.FILLCOLOR, LOWMED);
						decoratorMap.get(transition).setLightColorLabel(false);
					} else if (scale == 2) {
						map.putViewSpecific(transition, AttributeMap.FILLCOLOR, MED);
						decoratorMap.get(transition).setLightColorLabel(false);
					} else if (scale == 3) {
						map.putViewSpecific(transition, AttributeMap.FILLCOLOR, MEDHIGH);
						decoratorMap.get(transition).setLightColorLabel(true);
					} else if (scale == 4) {
						map.putViewSpecific(transition, AttributeMap.FILLCOLOR, HIGH);
						decoratorMap.get(transition).setLightColorLabel(true);
					}
				}
			}
		} else {
			decoratorMap.get(transition).setLightColorLabel(true);
		}
	}

	protected void flagTraceIndices(boolean[] caseFilter, SyncReplayResult repResult) {
		for (int idx : repResult.getTraceIndex()) {
			caseFilter[idx] = true;
		}
	}

	public ViewSpecificAttributeMap getViewSpecificAttributeMap() {
		return graph.getViewSpecificAttributes();
	}

	public void filterAlignmentPreserveIndex(Set<Integer> preservedIndex) {
		// TODO Auto-generated method stub
		
	}
	
}
