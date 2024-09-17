/**
 * 
 */
package org.processmining.explorativeprocessdiscovery.visual;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JViewport;

import org.processmining.explorativeprocessdiscovery.plugins.Result;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.ui.scalableview.ScalableComponent;
import org.processmining.framework.util.ui.widgets.InspectorPanel;
import org.processmining.models.jgraph.ProMJGraph;

import com.fluxicon.slickerbox.factory.SlickerDecorator;
import com.fluxicon.slickerbox.factory.SlickerFactory;

/**
 * @author aadrians Jul 15, 2012
 * 
 */
public abstract class ProjectionVisPanelChanged extends InspectorPanel {
	private static final long serialVersionUID = -6674503536171244970L;
	MainVisualConfig miniembeddedconfig = new MainVisualConfig();

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

	public ProjectionVisPanelChanged(PluginContext context, List<Result> results, int Type) {
		super(context);
		/**
		 * Get some Slickerbox stuff, required by the Look+Feel of some objects.
		 */
		factory = SlickerFactory.instance();
		decorator = SlickerDecorator.instance();

	}

	public JComponent getComponent() {
		return miniembeddedconfig.getZoom();
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
		miniembeddedconfig.getZoom().setScale(b);
	}

	public double getScale() {
		return miniembeddedconfig.getZoom().getScale();
	}

	public Component getVerticalScrollBar() {
		return scroll.getVerticalScrollBar();
	}

	public Component getHorizontalScrollBar() {
		return scroll.getHorizontalScrollBar();
	}

	protected ViewPanel createViewPanel(ProjectionVisPanelChanged mainPanel, int maxZoom) {
		return new ViewPanel(this, maxZoom);
	}

	public void addInteractionViewports(final ViewPanel viewPanel) {
		this.scroll.addComponentListener(new ComponentListener() {

			public void componentHidden(ComponentEvent arg0) {
			}

			public void componentMoved(ComponentEvent arg0) {
			}

			public void componentResized(ComponentEvent arg0) {

				if (arg0.getComponent().isValid()) {

					Dimension size = arg0.getComponent().getSize();

					int width = 250, height = 250;

					if (size.getWidth() > size.getHeight())
						height *= size.getHeight() / size.getWidth();
					else
						width *= size.getWidth() / size.getHeight();

					viewPanel.getPIP().setPreferredSize(new Dimension(width, height));
					viewPanel.getPIP().initializeImage();

					viewPanel.getZoom().computeFitScale();
				}
			}

			public void componentShown(ComponentEvent arg0) {
			}

		});
	}
	
	public void addInteractionViewportsembedd(final ViewPanel viewPanel) {
		this.scroll.addComponentListener(new ComponentListener() {

			public void componentHidden(ComponentEvent arg0) {
			}

			public void componentMoved(ComponentEvent arg0) {
			}

			public void componentResized(ComponentEvent arg0) {

				if (arg0.getComponent().isValid()) {

					Dimension size = arg0.getComponent().getSize();

//					int width = 1000, height = 1000;
//
//					if (size.getWidth() > size.getHeight())
//						height *= size.getHeight() / size.getWidth();
//					else
//						width *= size.getWidth() / size.getHeight();
					
					int width = (int) Math.ceil(size.getWidth()), height = (int) Math.ceil(size.getHeight());

					viewPanel.getPIP().setPreferredSize(new Dimension(width, Math.round(height/2)));
					viewPanel.getPIP().initializeImage();
				}
			}

			public void componentShown(ComponentEvent arg0) {
			}

		});
	}

	protected float getAppropriateStrokeWidth(double value) {
		// update width of incoming and outgoing arcs
		float suggestedArcWidth = 0.5f;

		if (Double.compare(value, 0) > 0) {
			suggestedArcWidth += new Float(Math.log(Math.E) * Math.log10(value));
		}

		return suggestedArcWidth;
	}

}
