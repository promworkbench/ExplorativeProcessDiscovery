/**
 * 
 */
package org.processmining.explorativeprocessdiscovery.embeddedvisual;

import java.awt.Component;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.ui.scalableview.ScalableComponent;
import org.processmining.framework.util.ui.widgets.InspectorPanel;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

import com.fluxicon.slickerbox.factory.SlickerDecorator;
import com.fluxicon.slickerbox.factory.SlickerFactory;

/**
 * @author aadrians Jul 15, 2012
 * 
 */
public abstract class ProjectionVisPanelEmbedded extends InspectorPanel {
	private static final long serialVersionUID = -6674503536171244970L;

	// slicker factory
	protected SlickerFactory factory;
	protected SlickerDecorator decorator;

	// for graph visualization
	protected ProMJGraph graph;

	// GUI component
	protected ScalableComponent scalable;
	protected JScrollPane scroll;

	// zoom-related properties
	// The maximal zoom factor for the primary view on the transition system.
	public static final int MAX_ZOOM = 1200;

	// reference to log replay result
	//	private TransEvClassMapping mapping;
	protected XLog log;
	protected PNRepResult logReplayResult;

	// mapping elements to index
	protected Map<Transition, Integer> mapTrans2Idx;
	protected Map<XEventClass, Integer> mapEc2Int;

	protected GraphLayoutConnection oldLayoutConn;

	protected PetrinetGraph net;

	protected Marking marking;

	protected Map<Transition, TransConfDecoratorEmbedded> decoratorMap = new HashMap<Transition, TransConfDecoratorEmbedded>();

	public ProjectionVisPanelEmbedded(PluginContext context, PetrinetGraph origNet, Marking origMarking, XLog log,
			TransEvClassMapping map, PNRepResult logReplayResult) {
		super(context);
		this.net = origNet;
		this.marking = origMarking;
		/**
		 * Get some Slickerbox stuff, required by the Look+Feel of some objects.
		 */
		factory = SlickerFactory.instance();
		decorator = SlickerDecorator.instance();

		/**
		 * Shared stuffs
		 */
		this.log = log;
		this.logReplayResult = logReplayResult;

		// net and marking to be modified

		try {
			/*
			 * Try to get an existing layout.
			 */
			Collection<GraphLayoutConnection> layouts = context.getConnectionManager().getConnections(
					GraphLayoutConnection.class, context, origNet);
			if (layouts != null) {
				/*
				 * Found an existing layout. 
				 */
				oldLayoutConn = layouts.iterator().next();

				// copy the layoutconnection to avoid communication between two Petrinet visualizations
				oldLayoutConn = new GraphLayoutConnection(oldLayoutConn);
			}
		} catch (ConnectionCannotBeObtained ex) {
			/*
			 * No existing layout. No worries, one will be created later.
			 */
		}

	}

	public JComponent getComponent() {
		return scalable.getComponent();
	}

	/**
	 * @return the logReplayResult
	 */
	public PNRepResult getLogReplayResult() {
		return logReplayResult;
	}

	/**
	 * @return the scalable
	 */
	public ScalableComponent getScalable() {
		return scalable;
	}

	public JViewport getViewport() {
		return scroll.getViewport();
	}

	public void setScale(double d) {
		double b = Math.max(d, 0.01);
		b = Math.min(b, MAX_ZOOM / 100.);
		scalable.setScale(b);
	}

	public double getScale() {
		return scalable.getScale();
	}

	public Component getVerticalScrollBar() {
		return scroll.getVerticalScrollBar();
	}

	public Component getHorizontalScrollBar() {
		return scroll.getHorizontalScrollBar();
	}

	protected float getAppropriateStrokeWidth(double value) {
		// update width of incoming and outgoing arcs
		float suggestedArcWidth = 0.5f;

		if (Double.compare(value, 0) > 0) {
			suggestedArcWidth += new Float(Math.log(Math.E) * Math.log10(value));
		}

		return suggestedArcWidth;
	}

	public abstract void filterAlignmentPreserveIndex(Set<Integer> preservedIndex);

}
