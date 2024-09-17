/**
 * 
 */
package org.processmining.explorativeprocessdiscovery.visual;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.processmining.plugins.pnalignanalysis.visualization.projection.util.FiveColorsLegendPanel;

/**
 * @author aadrians Nov 21, 2012
 * 
 */
public class LegendPanelConf extends JPanel {
	private static final long serialVersionUID = 2194428804018314613L;

	public LegendPanelConf() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		FiveColorsLegendPanel fiveColorLegend = new FiveColorsLegendPanel(new Color[] {
				MainProjectionVisPanelFull.LOWFIVESHOW, MainProjectionVisPanelFull.LOWMEDFIVESHOW,
				MainProjectionVisPanelFull.MEDFIVESHOW, MainProjectionVisPanelFull.MEDHIGHFIVESHOW,
				MainProjectionVisPanelFull.HIGHFIVESHOW }, 30);
		Dimension dFiveColorLegend = new Dimension(220, 35);
		fiveColorLegend.setPreferredSize(dFiveColorLegend);

		LegendConfSpecific legendConfSpecific = new LegendConfSpecific();
		legendConfSpecific.setPreferredSize(new Dimension(265,450));

		add(fiveColorLegend);
		add(legendConfSpecific);
	}
}