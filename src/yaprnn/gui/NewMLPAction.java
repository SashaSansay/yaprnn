package yaprnn.gui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import yaprnn.mlp.BadConfigException;
import yaprnn.mlp.NeuralNetwork;

class NewMLPAction extends AbstractAction {

	private static final long serialVersionUID = -3290748515408373993L;

	private final static int DEFAULT_NUMLAYERS = 1;
	private final static int DEFAULT_NUMNEURONS = 2;
	private final static double DEFAULT_BIAS = 0.2;
	private final static int DEFAULT_ACTIVATIONFUNCTION = 0;
	private final static boolean DEFAULT_AUTOENCODING = false;

	private GUI gui;

	NewMLPAction(GUI gui) {
		this.gui = gui;
		gui.getView().getMenuNewMLP().addActionListener(this);
		gui.getView().getToolNewMLP().addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int numLayers = 0, numNeurons = 0;
		double bias = 0;

		// F�r die Eingabe-Schleife.
		boolean notSatisfied;

		// Input Dialog vorbereiten.
		JPanel panel = new JPanel(new GridLayout(7, 1));
		JTextField optionNumLayers = new JTextField(Integer
				.toString(DEFAULT_NUMLAYERS));
		JTextField optionNumNeurons = new JTextField(Integer
				.toString(DEFAULT_NUMNEURONS));
		JTextField optionBias = new JTextField(Double.toString(DEFAULT_BIAS));
		JCheckBox optionAutoEncoding = new JCheckBox(
				"Use auto encoding initialization", DEFAULT_AUTOENCODING);
		KeyListener onlyDigits = new OnlyNumbersKeyAdapter();
		optionNumLayers.addKeyListener(onlyDigits);
		optionNumNeurons.addKeyListener(onlyDigits);
		optionBias.addKeyListener(onlyDigits);
		optionAutoEncoding.setSelected(false);
		panel.add(new JLabel(
				"How many layers do you want? (value must be greater then 0)"));
		panel.add(optionNumLayers);
		panel
				.add(new JLabel(
						"How many neurons per Layer do you want? (value must be greater then 0)"));
		panel.add(optionNumNeurons);
		panel.add(new JLabel("Bias (value must be greater then 0)"));
		panel.add(optionBias);
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
				bias = Double.parseDouble(optionBias.getText());
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(gui.getView(),
						"Please enter valid values only.", "Parsing error",
						JOptionPane.ERROR_MESSAGE);
			}
			if (numLayers > 0 && numNeurons > 0 && bias > 0)
				notSatisfied = false;
			else {
				// Die Felder mit ungueltigen Eingaben werden mit hellem rot
				// unterlegt.
				if (numLayers <= 0)
					optionNumLayers.setBackground(new Color(255, 128, 128));
				else
					optionNumLayers.setBackground(SystemColor.text);
				if (numNeurons <= 0)
					optionNumNeurons.setBackground(new Color(255, 128, 128));
				else
					optionNumNeurons.setBackground(SystemColor.text);
				if (bias <= 0)
					optionBias.setBackground(new Color(255, 128, 128));
				else
					optionBias.setBackground(SystemColor.text);
			}
		}

		// Parameter ausfuellen
		int[] layer = new int[numLayers];
		int[] avf = new int[numLayers + 2];
		double[] biases = new double[numLayers];
		for (int i = 0; i < numLayers; i++) {
			// Annahme von Standardwerten
			layer[i] = numNeurons;
			biases[i] = bias;
			avf[i] = DEFAULT_ACTIVATIONFUNCTION;
		}
		avf[numLayers] = 0;
		avf[numLayers + 1] = 0;

		// MLP erstellen
		try {
			NeuralNetwork mlp = gui.getCore().newMLP(layer, avf, biases,
					optionAutoEncoding.isSelected());
			gui.getTreeModel().add(mlp);
		} catch (BadConfigException err) {
			JOptionPane.showMessageDialog(gui.getView(), err.getMessage(),
					"NewMLP: Error occured", JOptionPane.ERROR_MESSAGE);
		}
	}
}
