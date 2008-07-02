package yaprnn.gui;

import java.util.Collection;
import java.util.List;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import javax.swing.filechooser.FileNameExtensionFilter;
import yaprnn.Core;
import yaprnn.GUIInterface;
import yaprnn.dvv.Data;
import yaprnn.gui.view.MainView;

public class GUI implements GUIInterface {

	// Package Globs
	final static double DEFAULT_BIAS = 0.2;
	final static int DEFAULT_ACTIVATION_FUNCTION = 0;
	final static FileNameExtensionFilter FILEFILTER_YDS = new FileNameExtensionFilter(
			"YAPRNN DataSet", "yds");
	final static FileNameExtensionFilter FILEFILTER_MLP = new FileNameExtensionFilter(
			"Neural Network", "mlp");
	final static FileNameExtensionFilter FILEFILTER_AIFF = new FileNameExtensionFilter(
			"Audio files", "aiff");
	final static FileNameExtensionFilter FILEFILTER_IMGPKG = new FileNameExtensionFilter(
			"Image package", "idx3-ubyte");
	final static FileNameExtensionFilter FILEFILTER_LBLPKG = new FileNameExtensionFilter(
			"Label package", "idx1-ubyte");

	private Core core;
	private MainView mainView = new MainView();
	private NetworkTreeModel treeModel = new NetworkTreeModel();

	/**
	 * @param mv
	 *            the main view to be used
	 */
	public GUI(Core core) {
		this.core = core;
		this.core.setGUI(this);

		// Look and Feel anpassen
		// try { UIManager.setLookAndFeel(...); } catch (Exception e) {}

		// TreeModel einsetzen
		mainView.getTreeNeuralNetwork().setModel(treeModel);
		mainView.getTreeNeuralNetwork()
				.setCellRenderer(treeModel.getRenderer());

		// EventHandler hinzuf�gen
		new NewMLPActionListener(this);
		new LoadMLPActionListener(this);
		new SaveMLPActionListener(this);
		new LoadDataSetActionListener(this);
		new SaveDataSetActionListener(this);
		new ImportAudioActionListener(this);
		new ImportImagesActionListener(this);
		new MenuExitActionListener(this);
		new ToolImportActionListener(this);

		// Das Anzeigen der View sollte verz�gert geschehen.
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				mainView.setVisible(true);
			}
		});

	}

	MainView getView() {
		return mainView;
	}

	NetworkTreeModel getTreeModel() {
		return treeModel;
	}

	Core getCore() {
		return core;
	}

	@Override
	public void setDataSet(Collection<Data> dataset) {
		for (Data d : dataset)
			treeModel.add(d);
	}

	@Override
	public void setTestError(List<Double> errorData) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setTrainingError(List<Double> errorData) {
		// TODO Auto-generated method stub
	}

	/**
	 * Resizes an image.
	 * 
	 * @param image
	 * @param width
	 * @param height
	 * @return the resized image
	 */
	static Image resizeImage(Image image, int width, int height) {
		if (image == null)
			return null;
		int ow = image.getWidth(null);
		int oh = image.getHeight(null);
		// Transform wird zur skalierung ben�tigt.
		BufferedImage bi = new BufferedImage(ow, oh,
				BufferedImage.TYPE_INT_ARGB);
		bi.getGraphics().drawImage(image, 0, 0, ow, oh, null);
		AffineTransform tx = new AffineTransform();
		tx.scale(width / (double) ow, height / (double) oh);
		return new AffineTransformOp(tx, AffineTransformOp.TYPE_BICUBIC)
				.filter(bi, null);
	}

}
