package yaprnn.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

class LoadDataSetActionListener implements ActionListener {

	private GUI gui;

	LoadDataSetActionListener(GUI gui) {
		this.gui = gui;
		gui.getView().getMenuLoadDataSet().addActionListener(this);
		gui.getView().getToolLoadDataSet().addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser chooser = new JFileChooser();
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileFilter(GUI.FILEFILTER_YDS);
		if (chooser.showOpenDialog(gui.getView()) == JFileChooser.APPROVE_OPTION)
			// TODO : Laderoutine f�r DataSets
			JOptionPane.showMessageDialog(null, chooser.getSelectedFile()
					.getPath());
	}

}
