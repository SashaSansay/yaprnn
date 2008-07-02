package yaprnn.gui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import yaprnn.mlp.BadConfigException;
import yaprnn.mlp.NeuralNetwork;

class NewMLPActionListener implements ActionListener {

	private GUI gui;

	NewMLPActionListener(GUI gui) {
		this.gui = gui;
		gui.getView().getMenuNewMLP().addActionListener(this);
		gui.getView().getToolNewMLP().addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int numLayers = 0;
		int numNeurons = 0;

		// F�r die Eingabe-Schleife.
		boolean notSatisfied;

		// Input Dialog vorbereiten.
		JTextField optionNumLayers = new JTextField("1");
		optionNumLayers.addKeyListener(new OnlyNumbersKeyAdapter());
		JTextField optionNumNeurons = new JTextField("2");
		optionNumLayers.addKeyListener(new OnlyNumbersKeyAdapter());
		JCheckBox optionAutoEncoding = new JCheckBox("Use auto encoding initialization");
		optionAutoEncoding.setSelected(false);
		JPanel panel = new JPanel(new GridLayout(5, 1));
		panel.add(new JLabel("How many layers do you want? (value must be greater then 0)"));
		panel.add(optionNumLayers);
		panel.add(new JLabel("How many neurons per Layer do you want? (value must be greater then 0)"));
		panel.add(optionNumNeurons);
		panel.add(optionAutoEncoding);

		// Parameter anfragen
		notSatisfied = true;
		while (notSatisfied) {
			int ret = JOptionPane.showConfirmDialog(gui.getView(), panel,
					"New MLP", JOptionPane.OK_CANCEL_OPTION);
			if (ret == JOptionPane.CANCEL_OPTION)
				return;
			try {
				numLayers = Integer.parseInt(optionNumLayers.getText());
				numNeurons = Integer.parseInt(optionNumNeurons.getText());
				if (numLayers > 0 && numNeurons > 0)
					notSatisfied = false;
				else {
					// Die Felder mit ungueltigen eingaben werden mit hellem rot
					// unterlegt.
					if (numLayers <= 0)
						optionNumLayers.setBackground(new Color(255, 128, 128));
					else
						optionNumLayers.setBackground(SystemColor.text);
					if (numNeurons <= 0)
						optionNumNeurons.setBackground(new Color(255, 128, 128));
					else
						optionNumNeurons.setBackground(SystemColor.text);
				}
			} catch (Exception ex) {
			}
		}

		// Parameter ausfuellen
		int[] layer = new int[numLayers];
		int[] avf = new int[numLayers + 2];
		double[] bias = new double[numLayers];
		for (int i = 0; i < numLayers; i++) {
			// Annahme von Standardwerten
			layer[i] = numNeurons;
			avf[i] = GUI.DEFAULT_ACTIVATION_FUNCTION;
			bias[i] = GUI.DEFAULT_BIAS;
		}
		avf[numLayers] = 0;
		avf[numLayers + 1] = 0;

		// MLP erstellen
		try {
			NeuralNetwork mlp = gui.getCore().newMLP(layer, avf, bias, 	optionAutoEncoding.isSelected());
			gui.getTreeModel().add(mlp);
		} catch (BadConfigException err) {
			JOptionPane.showMessageDialog(gui.getView(),
					err.getMessage(),
					"NewMLP: Error occured",
					JOptionPane.ERROR_MESSAGE);
		}
	}
}
