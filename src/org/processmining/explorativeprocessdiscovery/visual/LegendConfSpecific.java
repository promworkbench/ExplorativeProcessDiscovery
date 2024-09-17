package org.processmining.explorativeprocessdiscovery.visual;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

public class LegendConfSpecific extends JPanel {
	private static final long serialVersionUID = -1668066830588872184L;
	MainVisualConfig miniprojectVisualconfig = new MainVisualConfig();
	Color limeGreen = new Color(0, 128, 128);

	private static final Font boldFont = new Font("Sans", Font.BOLD, 12);
	private static final Font normalFont = new Font("Sans", Font.PLAIN, 12);
	private static final Font bigFont = new Font("Sans", Font.PLAIN, 24);

	public LegendConfSpecific() {
		super();
	}

	public void paint(Graphics graphics) {
		Graphics2D g = (Graphics2D) graphics;
		String Name = miniprojectVisualconfig.getTypeName(miniprojectVisualconfig.getType());

		// draw legend
		int padding = 8;
		int currY = padding;
		FontMetrics fm = graphics.getFontMetrics(boldFont);
		FontMetrics fmBig = graphics.getFontMetrics(bigFont);
		int fontHeight = fm.getHeight();

		g.setPaint(MainProjectionVisPanelFull.MEDFIVESHOW);
		g.fillRect(80, currY, 80, 80);
		g.setPaint(Color.darkGray);
		g.fillRect(80, currY+80, 80, 15);
		g.setPaint(Color.orange);
		g.fillRect(160, currY, 15, 80);

		currY += fmBig.getHeight();
		g.setColor(Color.BLACK);
		g.setFont(new Font("Sans", Font.PLAIN, 22));
		g.drawString("Subset", 84, currY+16);

		currY += 70 + fontHeight;
		g.setFont(normalFont);
		g.drawString("- Subset color shows the degree of " + Name,
				padding, currY);
		currY += fontHeight;
		g.drawString("- The amount of activities was reduced",
				padding, currY);
		currY += fontHeight;
		g.drawString("from top to bottom", padding + 13, currY);
		currY += fontHeight;

		// place index		
		g.setPaint(Color.DARK_GRAY);
		g.fillRect(padding, currY, 20, 10);
		g.setPaint(Color.BLACK);
		currY += 10;
		g.drawString("Event rate", padding + 25, currY-2);
		currY += 10;
		g.drawString("(The longer the length, the higher the rate)", padding + 25, currY+2);
		currY += 10;
		g.setPaint(Color.orange);
		g.fillRect(padding, currY, 10, 20);
		g.setPaint(Color.BLACK);
		currY += 10;
		g.drawString("Activity rate", padding + 25, currY-2);
		currY += 10;
		g.drawString("(The longer the length, the higher the rate)", padding + 25, currY+2);
		currY += 10;
		g.setPaint(Color.RED);
		g.fillRect(padding, currY, 10, 10);
		g.setPaint(Color.BLACK);
		currY += 10;
		g.drawString("Best spot with highest " + Name, padding + 15, currY);
		currY += 10;
		g.setPaint(Color.BLUE);
		g.fillRect(padding, currY, 10, 10);
		g.setPaint(Color.BLACK);
		currY += 10;
		g.drawString("Worst spot with lowest " + Name, padding + 15, currY);
		currY += 10;
		g.setPaint(Color.BLACK);
		g.fillRect(padding, currY, 10, 10);
		g.setPaint(Color.BLACK);
		currY += 10;
		g.drawString("Failed spot", padding + 15, currY);
		currY += 10;
		g.setPaint(limeGreen);
		g.fillRect(padding, currY, 10, 10);
		g.setPaint(Color.BLACK);
		currY += 10;
		g.drawString("All spots have the same " + Name, padding + 15, currY);
		currY += 10;
		g.setPaint(Color.GREEN);
		g.drawRect(padding, currY, 10, 10);
		g.setPaint(Color.BLACK);
		currY += 10;
		g.drawString("Spots have the highest " + Name, padding + 15, currY);
		currY += fontHeight;

		currY += 10;
		g.setPaint(Color.RED);
		g.drawLine(padding, currY-5, 18, currY-5);
		g.setPaint(Color.BLACK);
		g.drawString(Name + " increased", padding + 15, currY);
		currY += fontHeight;
		g.setPaint(Color.BLUE);
		g.drawLine(padding, currY-5, 18, currY-5);
		g.setPaint(Color.BLACK);
		g.drawString(Name + " decreased", padding + 15, currY);
		currY += fontHeight;
		g.setPaint(Color.GREEN);
		g.drawLine(padding, currY-5, 18, currY-5);
		g.setPaint(Color.BLACK);
		g.drawString(Name + " unchanged", padding + 15, currY);
		currY += fontHeight;
		g.setPaint(Color.BLACK);
		g.drawLine(padding, currY-5, 18, currY-5);
		g.setPaint(Color.BLACK);
		g.drawString("Connected with a failed spot", padding + 15, currY);
		currY += fontHeight;
		g.setPaint(Color.BLACK);
		g.drawString("The width of line indicates the degree of", padding, currY);
		currY += fontHeight;
		g.drawString(Name + " changed", padding, currY);
	}
}
