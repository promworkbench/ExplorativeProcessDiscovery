package org.processmining.explorativeprocessdiscovery.plugins;

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.fluxicon.slickerbox.factory.SlickerFactory;

public class ChoiceStepPanel extends JPanel {

	private static final long serialVersionUID = 561834927561834927L;

	protected static JLabel createLabel(String caption, String description) {
		JLabel eventLabel = SlickerFactory.instance().createLabel(
				"<HTML><B>" + caption + "</B><BR/><I>" + description + "</I></HTML>");
		eventLabel.setFont(eventLabel.getFont().deriveFont(Font.PLAIN));
		return eventLabel;
	}


}

