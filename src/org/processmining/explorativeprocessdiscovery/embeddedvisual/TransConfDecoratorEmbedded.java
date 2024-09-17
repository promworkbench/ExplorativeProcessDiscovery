/**
 * 
 */
package org.processmining.explorativeprocessdiscovery.embeddedvisual;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import javax.swing.JLabel;

import org.processmining.models.shapes.Decorated;
import org.processmining.plugins.petrinet.visualization.AlignmentConstants;

/**
 * @author aadrians Nov 4, 2011
 * 
 */
public class TransConfDecoratorEmbedded implements Decorated {
	private int moveOnModelFreq = 0;
	private int moveSyncFreq = 0;
	private boolean lightColorLabel = false;
	private String label;
	
	public static Color UNUSED_TRANSITION = new Color(127, 127, 127, 200); // grey 

	private static Font defFont = new Font(Font.SANS_SERIF, Font.PLAIN, 7);

	private static int MARGIN = 1;

	@SuppressWarnings("unused")
	private TransConfDecoratorEmbedded() {
	};

	public TransConfDecoratorEmbedded(int moveOnModelFreq, int moveSyncFreq, String label) {
		this.moveOnModelFreq = moveOnModelFreq;
		this.moveSyncFreq = moveSyncFreq;
		this.label = label;
	}

	public boolean isLightColorLabel() {
		return lightColorLabel;
	}

	/**
	 * @return the moveOnModelFreq
	 */
	public int getMoveOnModelFreq() {
		return moveOnModelFreq;
	}

	/**
	 * @param moveOnModelFreq
	 *            the moveOnModelFreq to set
	 */
	public void setMoveOnModelFreq(int moveOnModelFreq) {
		this.moveOnModelFreq = moveOnModelFreq;
	}

	/**
	 * @return the moveSyncFreq
	 */
	public int getMoveSyncFreq() {
		return moveSyncFreq;
	}

	/**
	 * @param moveSyncFreq
	 *            the moveSyncFreq to set
	 */
	public void setMoveSyncFreq(int moveSyncFreq) {
		this.moveSyncFreq = moveSyncFreq;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label
	 *            the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	public void decorate(Graphics2D g2d, double x, double y, double width, double height) {
		int pad = (int) (0.05 * width);
		int totalFrequency = moveSyncFreq + moveOnModelFreq;
		
		if (totalFrequency > 0) {
			int moveModelWidth = (int) ((moveOnModelFreq * width) / totalFrequency);
			if (moveOnModelFreq > 0) {
				g2d.setColor(AlignmentConstants.MOVESYNCCOLOR);
				g2d.fillRect((int) x, (int) (y + (0.85 * height)), (int) (width - moveModelWidth), (int) (0.1 * height));
				g2d.setColor(Color.MAGENTA);
				g2d.fillRect((int) ((int) x + width - moveModelWidth), (int) (y + (0.85 * height)), moveModelWidth,
						(int) (0.1 * height));
	
				g2d.setColor(Color.RED);
				g2d.fillRect((int) x, (int) y, pad, (int) height);
				g2d.fillRect((int) x, (int) y, (int) width, pad);
				g2d.fillRect((int) x, (int) ((int) y + height - pad), (int) width, pad);
				g2d.fillRect((int) ((int) x + width - pad), (int) y, pad, (int) height);
			} else {
				// HV: Add the color bar and color border as usual, but make the border green indicating everything is fine.
				g2d.setColor(AlignmentConstants.MOVESYNCCOLOR);
				g2d.fillRect((int) x, (int) (y + (0.85 * height)), (int) (width - moveModelWidth), (int) (0.1 * height));
				g2d.setColor(Color.MAGENTA);
				g2d.fillRect((int) ((int) x + width - moveModelWidth), (int) (y + (0.85 * height)), moveModelWidth,
						(int) (0.1 * height));
	
				g2d.setColor(AlignmentConstants.MOVESYNCCOLOR);
				g2d.fillRect((int) x, (int) y, pad, (int) height);
				g2d.fillRect((int) x, (int) y, (int) width, pad);
				g2d.fillRect((int) x, (int) ((int) y + height - pad), (int) width, pad);
				g2d.fillRect((int) ((int) x + width - pad), (int) y, pad, (int) height);
			}
		} else {
			// DF: transition was never used, make everything grey
			g2d.setColor(UNUSED_TRANSITION);
			g2d.fillRect((int) x, (int) (y + (0.85 * height)), (int) (width), (int) (0.1 * height));

			g2d.setColor(UNUSED_TRANSITION);
			g2d.fillRect((int) x, (int) y, pad, (int) height);
			g2d.fillRect((int) x, (int) y, (int) width, pad);
			g2d.fillRect((int) x, (int) ((int) y + height - pad), (int) width, pad);
			g2d.fillRect((int) ((int) x + width - pad), (int) y, pad, (int) height);
		}

		JLabel nodeName;

		StringBuilder sb = new StringBuilder();
		sb.append("<html><div style=\"align:center;width:" + (width - (2 * MARGIN)) + "px;\">");
		sb.append(label);
		sb.append("<br><b>(");
		
		if (totalFrequency > 0) {
			sb.append(moveSyncFreq);
			sb.append("/");
			sb.append(moveOnModelFreq);
		} else {
			sb.append(0);
		}
		sb.append(")</b>");
		sb.append("</div></html>");

		nodeName = new JLabel(sb.toString());
		sb.setLength(0);

		// draw transition label
		// get metrics from the graphics
		FontMetrics metrics = g2d.getFontMetrics(defFont);
		// get the height of a line of text in this
		// font and render context
		int hgt = (int) height - (1 * MARGIN);
		// get the advance of my text in this font
		// and render context
		int adv = metrics.stringWidth(nodeName.getText());

		final int labelX = (int) x + MARGIN;
		final int labelY = (int) y + 1;
		final int labelW = adv;
		final int labelH = hgt;

		nodeName.setPreferredSize(new Dimension(labelW, labelH));
		nodeName.setSize(new Dimension(labelW, labelH));

		nodeName.setFont(defFont);
		nodeName.validate();
		if (lightColorLabel) {
			nodeName.setForeground(Color.WHITE);
		} else {
			nodeName.setForeground(Color.BLACK);
		}
		nodeName.paint(g2d.create(labelX, labelY, labelW, labelH));
	}

	public void setLightColorLabel(boolean isLight) {
		this.lightColorLabel = isLight;
	}

}
