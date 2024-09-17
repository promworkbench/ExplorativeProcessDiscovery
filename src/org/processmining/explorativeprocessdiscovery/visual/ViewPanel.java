/**
 * 
 */
package org.processmining.explorativeprocessdiscovery.visual;

import javax.swing.JPanel;

import org.processmining.plugins.pnalignanalysis.visualization.projection.PIPPanel;
import org.processmining.plugins.pnalignanalysis.visualization.projection.ProjectionVisPanel;

import info.clearthought.layout.TableLayout;


/**
 * @author aadrians
 * Nov 1, 2011
 *
 */
public class ViewPanel extends JPanel {

	private static final long serialVersionUID = 7931015104099746628L;
	private PIPPanelChanged pip;
	private PIPPanel pipembedded;
	private ZoomPanel zoom;
	public ViewPanel(ProjectionVisPanelChanged mainPanel, int maxZoom){
		double[][] size = new double[][]{ {TableLayout.FILL}, {TableLayout.FILL, TableLayout.PREFERRED}} ;
		setLayout(new TableLayout(size));
		
		pip = new PIPPanelChanged(mainPanel);
		zoom = new ZoomPanel(mainPanel, pip, maxZoom);

		add(pip, "0,0");
		add(zoom, "0,1");
	}
	
	public ViewPanel(ProjectionVisPanel mainPanel){
		double[][] size = new double[][]{ {TableLayout.FILL}, {TableLayout.FILL, TableLayout.PREFERRED}} ;
		setLayout(new TableLayout(size));
		
		pipembedded = new PIPPanel(mainPanel);

		add(pipembedded, "0,0");
	}
	
	public PIPPanelChanged getPIP() {
		return pip;
	}
	
	public ZoomPanel getZoom(){
		return zoom;
	}
}
