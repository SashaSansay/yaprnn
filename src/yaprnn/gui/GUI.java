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
import yaprnn.gui.NetworkTreeModel.AVFNode;
import yaprnn.gui.NetworkTreeModel.BiasNode;
import yaprnn.gui.NetworkTreeModel.DataNode;
import yaprnn.gui.NetworkTreeModel.LayerNode;
import yaprnn.gui.NetworkTreeModel.ModelNode;
import yaprnn.gui.NetworkTreeModel.NetworkNode;
import yaprnn.gui.NetworkTreeModel.NetworkSetsNode;
import yaprnn.gui.NetworkTreeModel.NeuronsNode;
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

	// Informationen ueber den ausgewaehlten Knoten
	private TreePath selectedPath = null;
	private ModelNode selected = null;
	private NeuralNetwork selectedNetwork = null;
	private Data selectedData = null;

	// Einige Preview-Optionen
	private double zoom = 1.0;
	private int resolution = 16;
	private double overlap = 0.4;

	// Standard-Actions
	private NewMLPAction newMLPAction;
	private LoadMLPAction loadMLPAction;
	private SaveMLPAction saveMLPAction;
	private LoadDataSetAction loadDataSetAction;
	private SaveDataSetAction saveDataSetAction;

	// Actions im Popmenu die nur ausgefuehrt werden koennen, wenn der richtige
	// Knoten selektiert ist.
	private MenuSubsamplingAction subsamplingAction;
	private MenuClassifyAction classifyAction;
	private MenuTrainAction trainAction;
	private MenuResetAction resetAction;
	private MenuChooseRandomTrainingTestSetAction chooseRandomTrainingTestSetAction;
	private MenuAddAction addAction;
	private MenuEditAction editAction;
	private MenuRemoveAction removeAction;

	// Andere Listener
	private PreviewPlayAudioListener previewPlayer;

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
		addAction = new MenuAddAction(this);
		editAction = new MenuEditAction(this);
		removeAction = new MenuRemoveAction(this);

		// Preview Handler f�r Audio-Daten
		previewPlayer = new PreviewPlayAudioListener(mainView.getLabelPreview());

		// Andere Handler
		new MenuManualAction(this);
		new MenuWebsiteAction(this);
		new MenuExitAction(this);
		new TreeNeuralNetworkSelection(this);
		new OptionZoomAction(this);
		new OptionResolutionChange(this);
		new OptionOverlapChange(this);

		// Das Anzeigen der View sollte verzoegert geschehen.
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
		updateMenuToolsStates();

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

			// Dem PreviewPlayer-Listener das Data-Objekt geben
			previewPlayer.setData(data);

			// Preview erzeugen
			mainView.getLabelPreview().setImage(
					ImagesMacros.createPreview(data, zoom, false, 0, 0));
			mainView.getLabelPreviewSubsampled().setImage(
					ImagesMacros.createPreview(data, zoom, true, resolution,
							overlap));

		} else {
			// Loeschen der Informationen und previews
			mainView.getLabelFilename().setText("");
			mainView.getLabelSampleLabel().setText("");
			mainView.getLabelUsedSubsamplingOptions().setText("");
			mainView.getLabelPreview().setImage(null);
			mainView.getLabelPreviewSubsampled().setImage(null);
			previewPlayer.setData(null);
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
				double min = weights[0][0], max = weights[0][0];
				Object[][] weights2 = new Object[rows][cols];
				for (int y = 0; y < rows; y++) {
					weights2[y][0] = "to neur: " + (y + 1);
					for (int x = 1; x < cols; x++) {
						double val = weights[y][x - 1];
						weights2[y][x] = Math.round(val * 1000) / 1000d;
						min = (val < min) ? val : min;
						max = (val > max) ? val : max;
					}
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

				// Weights-Image erstellen lassen
				mainView.getLabelWeightsImage().setImage(
						ImagesMacros
								.createWeightsImage(weights, zoom, min, max));

			} else {
				mainView.getTableWeights().setModel(new DefaultTableModel());
				mainView.getLabelWeightsImage().setImage(null);
			}
		} else {
			mainView.getTableWeights().setModel(new DefaultTableModel());
			mainView.getLabelWeightsImage().setImage(null);
		}

	}

	/**
	 * This updates the enabled state of menu items and tool buttons, so the app
	 * can be used in correct order.
	 */
	void updateMenuToolsStates() {
		boolean isNetworkNode = selected instanceof NetworkNode;
		boolean isNeuronsNode = selected instanceof NeuronsNode;
		boolean isAVFNode = selected instanceof AVFNode;
		boolean isBiasNode = selected instanceof BiasNode;
		boolean isNetworkSetsNode = selected instanceof NetworkSetsNode;
		boolean isDataNode = selected instanceof DataNode;

		// Standard Menus
		saveMLPAction.setEnabled(isNetworkNode);
		saveDataSetAction.setEnabled(isNetworkSetsNode);

		// PopupMenus
		subsamplingAction.setEnabled(isDataNode);
		classifyAction.setEnabled(isDataNode);
		trainAction.setEnabled(isNetworkNode || isNetworkSetsNode);
		resetAction.setEnabled(isNetworkNode);
		chooseRandomTrainingTestSetAction.setEnabled(isNetworkSetsNode);
		addAction.setEnabled(isNetworkNode);
		editAction.setEnabled(isNetworkNode || isNeuronsNode || isAVFNode
				|| isBiasNode);
		removeAction.setEnabled(isNetworkNode || isDataNode);

	}

	@Override
	public void setDataSet(Collection<Data> dataset) {
		for (Data d : dataset)
			treeModel.add(d);
	}

	@Override
	public void setTestError(List<Double> errorData) {
		MenuTrainAction.setTestError(errorData);
	}

	@Override
	public void setTrainingError(List<Double> errorData) {
		MenuTrainAction.setTrainingError(errorData);
	}

	TreePath getSelectedPath() {
		return selectedPath;
	}

	void setSelectedPath(TreePath selectedPath) {
		this.selectedPath = selectedPath;
		if (selectedPath != null) {
			Object last = selectedPath.getLastPathComponent();
			selected = (last instanceof ModelNode) ? (ModelNode) last : null;
		}
		if (selected != null) {
			if (selected instanceof DataNode)
				selectedData = ((DataNode) selected).getData();
			else
				selectedData = null;
			if (selected instanceof NetworkNode
					|| selected instanceof LayerNode
					|| selected instanceof NeuronsNode
					|| selected instanceof AVFNode
					|| selected instanceof BiasNode
					|| selected instanceof NetworkSetsNode) {
				if (selected instanceof NetworkNode)
					selectedNetwork = ((NetworkNode) selected).getNetwork();
				if (selected instanceof LayerNode)
					selectedNetwork = ((LayerNode) selected).getNetwork();
				if (selected instanceof NeuronsNode)
					selectedNetwork = ((NeuronsNode) selected).getNetwork();
				if (selected instanceof AVFNode)
					selectedNetwork = ((AVFNode) selected).getNetwork();
				if (selected instanceof BiasNode)
					selectedNetwork = ((BiasNode) selected).getNetwork();
				if (selected instanceof NetworkSetsNode)
					selectedNetwork = ((NetworkSetsNode) selected).getNetwork();
			} else
				selectedNetwork = null;
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

	Data getSelectedData() {
		return selectedData;
	}

	NeuralNetwork getSelectedNetwork() {
		return selectedNetwork;
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
