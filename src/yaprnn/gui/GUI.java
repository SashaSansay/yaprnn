package yaprnn.gui;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.awt.EventQueue;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.TreePath;
import yaprnn.Core;
import yaprnn.GUIInterface;
import yaprnn.dvv.Data;
import yaprnn.gui.NetworkTreeModel.DataNode;
import yaprnn.gui.NetworkTreeModel.LayerNode;
import yaprnn.gui.NetworkTreeModel.ModelNode;
import yaprnn.gui.NetworkTreeModel.NetworkNode;
import yaprnn.gui.view.MainView;
import yaprnn.gui.view.TrainingView;
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

	// Trainingfenster
	private Dictionary<NeuralNetwork, TrainingView> trainingViews = new Hashtable<NeuralNetwork, TrainingView>();

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

	/**
	 * This takes the appropriate action due to a change or update on a selected
	 * node.
	 */
	void updateOnSelectedNode() {
		changePopmenuStates();
		if (selected instanceof DataNode) {
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
		if (selected instanceof LayerNode) {
			// Anzeigen der Gewichte
		} else {
			// mainView.getTableWeights().setModel(null);
		}
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

	TrainingView getTrainingView(NeuralNetwork network) {
		TrainingView v = trainingViews.get(network);
		if (v == null) {
			v = new TrainingView();
			trainingViews.put(network, v);
			// TODO : Trainingview initialisieren
		}
		return v;
	}

	void removeTrainingView(NeuralNetwork network) {
		trainingViews.remove(network);
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

}
