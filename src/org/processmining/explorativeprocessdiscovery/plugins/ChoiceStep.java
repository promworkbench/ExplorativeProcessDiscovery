package org.processmining.explorativeprocessdiscovery.plugins;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.processmining.explorativeprocessdiscovery.visual.MainVisualConfig;

public final class ChoiceStep extends ChoiceStepPanel {

	private static final long serialVersionUID = 5L;
	private MainVisualConfig miniprojectVisualconfig = new MainVisualConfig();
	
	public ChoiceStep() {
		super();

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(false);
		
		JPanel FullPanel = new JPanel();
		FullPanel.setOpaque(false);
		FullPanel.setLayout(new BoxLayout(FullPanel, BoxLayout.X_AXIS));
		JButton fullbutton = new JButton("<html><body><p align=\"center\">Full Version<br/>"
				+ "<font size=3>* Show all child nodes at the same time</font><br/>"
				+ "<font size=3>(With Pareto Optimality)</font><br/>"
				+ "<font color=\"red\"><font size=3>Note: This version is very time consuming. Please be patient.</font></font></p></body></html>");
		fullbutton.setFont(new Font("Times New Roman",Font.BOLD,22));
		fullbutton.setForeground(Color.BLACK);
		FullPanel.add(Box.createHorizontalStrut(200));
		FullPanel.add(fullbutton);
		FullPanel.add(Box.createHorizontalStrut(200));
		add(FullPanel);

		JPanel LitePanel = new JPanel();
		LitePanel.setOpaque(false);
		LitePanel.setLayout(new BoxLayout(LitePanel, BoxLayout.X_AXIS));
		JButton litebutton = new JButton("<html><body><p align=\"center\">Lite Version<br/>"
				+ "<font size=3>* Show child nodes step by step by clicking</font><br/>"
				+ "<font size=3>(Without Pareto Optimality)</font></p></body></html>");
		litebutton.setFont(new Font("Times New Roman",Font.BOLD,22));
		LitePanel.add(Box.createHorizontalStrut(200));
		LitePanel.add(litebutton);
		LitePanel.add(Box.createHorizontalStrut(200));
		add(LitePanel);

		JPanel BestRoutePanel = new JPanel();
		BestRoutePanel.setOpaque(false);
		BestRoutePanel.setLayout(new BoxLayout(BestRoutePanel, BoxLayout.X_AXIS));
		JButton BestRoutebutton = new JButton("<html><body><p align=\"center\">Best Route<br/>"
				+ "<font size=3>* Show the best remove route directly</font></p></body></html>");
		BestRoutebutton.setFont(new Font("Times New Roman",Font.BOLD,22));
		BestRoutePanel.add(Box.createHorizontalStrut(200));
		BestRoutePanel.add(BestRoutebutton);
		BestRoutePanel.add(Box.createHorizontalStrut(200));
		add(BestRoutePanel);
		
		fullbutton.addActionListener(new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				miniprojectVisualconfig.setVersionFlag(0);
				miniprojectVisualconfig.setFirstGoBackFlag(1);
				fullbutton.setEnabled(false);
				litebutton.setEnabled(true);
				BestRoutebutton.setEnabled(true);
			}
		});

		litebutton.addActionListener(new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				miniprojectVisualconfig.setVersionFlag(1);
				miniprojectVisualconfig.setFirstGoBackFlag(1);
				litebutton.setEnabled(false);
				fullbutton.setEnabled(true);
				BestRoutebutton.setEnabled(true);
			}
		});

		BestRoutebutton.addActionListener(new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				miniprojectVisualconfig.setVersionFlag(2);
				miniprojectVisualconfig.setFirstGoBackFlag(1);
				BestRoutebutton.setEnabled(false);
				fullbutton.setEnabled(true);
				litebutton.setEnabled(true);
			}
		});

	}

}