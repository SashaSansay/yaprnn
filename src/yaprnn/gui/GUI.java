package yaprnn.gui;

import java.util.Collection;
import java.util.List;
import java.awt.EventQueue;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.TreePath;
import yaprnn.Core;
import yaprnn.GUIInterface;
import yaprnn.dvv.Data;
import yaprnn.gui.NetworkTreeModel.DataNode;
import yaprnn.gui.NetworkTreeModel.LayerNode;
import yaprnn.gui.NetworkTreeModel.ModelNode;
import yaprnn.gui.NetworkTreeModel.NetworkNode;
import yaprnn.gui.view.MainView;
import yaprnn.mlp.NeuralNetwork;

public class GUI implements GUIInterface {

	// Package Globs
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

	// Informationen �ber den ausgew�hlten Knoten
	private TreePath selectedPath = null;
	private ModelNode selected = null;

	// Einige Preview-Optionen
	private double zoom = 1.0;
	private int resolution = 16;
	private double overlap = 0.5;

	// Standard-Actions
	private NewMLPAction newMLPAction;
	private LoadMLPAction loadMLPAction;
	private SaveMLPAction saveMLPAction;
	private LoadDataSetAction loadDataSetAction;
	private SaveDataSetAction saveDataSetAction;

	// Actions im Popmenu die nur ausgef�hrt werden k�nnen, wenn der richtige
	// Knoten selektiert ist.
	private MenuSubsamplingAction subsamplingAction;
	private MenuClassifyAction classifyAction;
	private MenuTrainAction trainAction;
	private MenuResetAction resetAction;
	private MenuChooseRandomTrainingTestSetAction chooseRandomTrainingTestSetAction;
	private MenuRemoveAction removeAction;

	/**
	 * @param core
	 *            the core controller
	 */
	public GUI(Core core) {
		this.core = core;
		this.core.setGUI(this);

		mainView.getTreeNeuralNetwork().setModel(treeModel);
		mainView.getTreeNeuralNetwork().setCellRenderer(
				new NetworkTreeRenderer());

		// EventHandler erstellen
		newMLPAction = new NewMLPAction(this);
		loadMLPAction = new LoadMLPAction(this);
		saveMLPAction = new SaveMLPAction(this);
		loadDataSetAction = new LoadDataSetAction(this);
		saveDataSetAction = new SaveDataSetAction(this);
		new ImportAudioAction(this);
		new ImportImagesAction(this);

		// Popmenu Eventhandler
		subsamplingAction = new MenuSubsamplingAction(this);
		classifyAction = new MenuClassifyAction(this);
		trainAction = new MenuTrainAction(this);
		resetAction = new MenuResetAction(this);
		chooseRandomTrainingTestSetAction = new MenuChooseRandomTrainingTestSetAction(
				this);
		removeAction = new MenuRemoveAction(this);

		// Andere Handler
		new MenuExitAction(this);
		new TreeNeuralNetworkSelection(this);
		new OptionZoomAction(this);
		new OptionResolutionChange(this);
		new OptionOverlapChange(this);

		// Das Anzeigen der View sollte verz�gert geschehen.
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				mainView.setVisible(true);
			}
		});

	}

	/**
	 * This takes the appropriate action due to a change or update on a selected
	 * node.
	 */
	void updateOnSelectedNode() {
		changePopmenuStates();

		// Preview anzeigen, falls eine DataNode selektiert wurde.
		if (selected instanceof DataNode) {
			// Tabinhalt sichtbar machen
			mainView.getTabs().setSelectedIndex(1);

			// Data-Objekt Informationen auslesen
			DataNode dataNode = (DataNode) selected;
			Data data = dataNode.getData();
			mainView.getLabelFilename().setText(data.getFilename());
			mainView.getLabelSampleLabel().setText(data.getLabel());

			// TODO : Subsampling optionen anzeigen
			// mainView.getLabelUsedSubsamplingOptions().setText(data.getSubsamplingOptions());

			// Preview erzeugen
			if (data.isPicture()) {
				mainView.getLabelPreview().setImage(
						ImagesMacros.createImagePreview((byte[][]) data
								.previewRawData(), zoom));
				mainView.getLabelPreviewSubsampled().setImage(
						ImagesMacros.createImagePreview((byte[][]) data
								.previewSubsampledData(resolution, overlap),
								zoom));
			} else if (data.isAudio()) {
				// TODO : Audio preview
				// mainView.getLabelPreview().setImage(
				// ImagesMacros.createAudioPreview(, zoom));
				// mainView.getLabelPreviewSubsampled().setImage(
				// ImagesMacros.createAudioPreview(, zoom));
			}

		} else {
			// L�schen der Informationen und previews
			mainView.getLabelFilename().setText("");
			mainView.getLabelSampleLabel().setText("");
			mainView.getLabelUsedSubsamplingOptions().setText("");
			mainView.getLabelPreview().setImage(null);
			mainView.getLabelPreviewSubsampled().setImage(null);
		}

		// Anzeigen der Gewichte
		if (selected instanceof LayerNode) {
			// Tabinhalt sichtbar machen
			mainView.getTabs().setSelectedIndex(0);

			LayerNode ln = (LayerNode) selected;
			NeuralNetwork net = ln.getNetwork();
			// Wir k�nnen die Eingangsschicht nicht auslesen
			if (ln.getLayerIndex() > 0) {
				double[][] weights = net.getWeights(ln.getLayerIndex());
				int rows = weights.length, cols = weights[0].length + 1;

				// Gewichte auslesen
				Object[][] weights2 = new Object[rows][cols];
				for (int y = 0; y < rows; y++) {
					weights2[y][0] = "to neur: " + (y + 1);
					for (int x = 1; x < cols; x++)
						weights2[y][x] = Math.round(weights[y][x - 1] * 100) / 100;
				}

				// Spaltentitel setzen
				Object[] colNames = new Object[cols];
				colNames[0] = "";
				for (int i = 1; i < cols; i++)
					colNames[i] = "from neur: " + i;

				// Ins model packen
				mainView.getTableWeights().setModel(
						new DefaultTableModel(weights2, colNames) {
							private static final long serialVersionUID = -653408644230117250L;

							@Override
							public boolean isCellEditable(int row, int column) {
								// Wir machen das JTable nicht editierbar.
								return false;
							}
						});
			} else
				mainView.getTableWeights().setModel(new DefaultTableModel());
		} else
			mainView.getTableWeights().setModel(new DefaultTableModel());

	}

	/**
	 * This changes the enabled state of popmenu items, so the items can
	 * correctly be edited.
	 */
	void changePopmenuStates() {
		boolean isNetwork = selected instanceof NetworkNode;
		boolean isData = selected instanceof DataNode;
		subsamplingAction.setEnabled(isData);
		classifyAction.setEnabled(isData);
		trainAction.setEnabled(isNetwork);
		resetAction.setEnabled(isNetwork);
		// chooseRandomTrainingTestSetAction();
		removeAction.setEnabled(isNetwork || isData);
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

	TreePath getSelectedPath() {
		return selectedPath;
	}

	void setSelectedPath(TreePath selectedPath) {
		this.selectedPath = selectedPath;
		if (selectedPath != null) {
			Object last = selectedPath.getLastPathComponent();
			this.selected = (last instanceof ModelNode) ? (ModelNode) last
					: null;
		}
		updateOnSelectedNode();
	}

	double getZoom() {
		return zoom;
	}

	void setZoom(double zoom) {
		this.zoom = zoom;
		updateOnSelectedNode();
	}

	int getResolution() {
		return resolution;
	}

	void setResolution(int resolution) {
		this.resolution = resolution;
		updateOnSelectedNode();
	}

	double getOverlap() {
		return overlap;
	}

	void setOverlap(double overlap) {
		this.overlap = overlap;
		updateOnSelectedNode();
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

	ModelNode getSelected() {
		return selected;
	}

	NewMLPAction getNewMLPAction() {
		return newMLPAction;
	}

	LoadMLPAction getLoadMLPAction() {
		return loadMLPAction;
	}

	SaveMLPAction getSaveMLPAction() {
		return saveMLPAction;
	}

	LoadDataSetAction getLoadDataSetAction() {
		return loadDataSetAction;
	}

	SaveDataSetAction getSaveDataSetAction() {
		return saveDataSetAction;
	}

	MenuSubsamplingAction getSubsamplingAction() {
		return subsamplingAction;
	}

	MenuClassifyAction getClassifyAction() {
		return classifyAction;
	}

	MenuTrainAction getTrainAction() {
		return trainAction;
	}

	MenuResetAction getResetAction() {
		return resetAction;
	}

	MenuChooseRandomTrainingTestSetAction getChooseRandomTrainingTestSetAction() {
		return chooseRandomTrainingTestSetAction;
	}

	MenuRemoveAction getRemoveAction() {
		return removeAction;
	}

	/**
	 * This methods invokes a run of the garbage collector trying to free memory
	 * to avoid heap exceeding.
	 */
	static void tryFreeMemory() {
		Runtime r = Runtime.getRuntime();
		r.runFinalization();
		r.gc();
	}

}
