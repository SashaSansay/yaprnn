package yaprnn.gui;

import java.awt.Component;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import yaprnn.dvv.Data;
import yaprnn.mlp.NeuralNetwork;

// The structure is organized according to the following:
// root
// |-Networks
// | |-Network 1
// | |...
// | |-Network n
// |-Datasets
// _ |-Loaded
// _ | |-Data 1
// _ | |...
// _ | |-Data m
// _ |-for Network' 1
// _ | |-Training set
// _ | | |-Data' 1
// _ | | |...
// _ | | |-Data' x_1
// _ | |-Test set
// _ | _ |-Data' 1
// _ | _ |...
// _ | _ |-Data' y_1
// _ |...
// _ |-for Network' n
// _ _ |-Training set
// _ _ | |-Data' 1
// _ _ | |...
// _ _ | |-Data' x_n
// _ _ |-Test set
// _ _ _ |-Data' 1
// _ _ _ |...
// _ _ _ |-Data' y_n
/**
 * NetworkTreeModel is used to store and reflect NeuralNetworks and Data-sets in
 * a JTree.
 */
class NetworkTreeModel implements TreeModel {

	/**
	 * LabeledNode is used internally for giving a container of data a readable
	 * label.
	 * 
	 * @param <D>
	 *            Type of the value contained in this node.
	 */
	private class LabeledNode<D> {

		private String label;
		private D value;

		LabeledNode(String label, D value) {
			this.label = label;
			this.value = value;
		}

		D getValue() {
			return value;
		}

		@Override
		public String toString() {
			return label;
		}

	}

	/**
	 * NetworkTreeRenderer is used to display the correct icons for the nodes in
	 * treeNeuralNetwork.
	 */
	private class NetworkTreeRenderer extends DefaultTreeCellRenderer {

		/**
		 * Autogenerated serialization ID
		 */
		private static final long serialVersionUID = -4438149908755280690L;

		private ImageIcon iconMLP = new ImageIcon(getClass().getResource(
				"/yaprnn/gui/view/iconMLP.png"));
		private ImageIcon iconNeuron = new ImageIcon(getClass().getResource(
				"/yaprnn/gui/view/iconNeuron.png"));
		private ImageIcon iconLayer = new ImageIcon(getClass().getResource(
				"/yaprnn/gui/view/iconLayer.png"));
		private ImageIcon iconAVF = new ImageIcon(getClass().getResource(
				"/yaprnn/gui/view/iconAVF.png"));
		private ImageIcon iconProcessed = new ImageIcon(getClass().getResource(
				"/yaprnn/gui/view/iconProcessed.png"));
		private ImageIcon iconUnProcessed = new ImageIcon(getClass()
				.getResource("/yaprnn/gui/view/iconUnProcessed.png"));
		private ImageIcon iconTrainingData = new ImageIcon(getClass()
				.getResource("/yaprnn/gui/view/iconFolderTraining.png"));
		private ImageIcon iconTestData = new ImageIcon(getClass().getResource(
				"/yaprnn/gui/view/iconFolderTest.png"));
		private ImageIcon iconDataSet = new ImageIcon(getClass().getResource(
				"/yaprnn/gui/view/iconFolderDataSet.png"));
		private ImageIcon iconNotUsed = new ImageIcon(getClass().getResource(
				"/yaprnn/gui/view/iconFolderNotUsed.png"));

		NetworkTreeRenderer() {
			setClosedIcon(iconLayer);
			setOpenIcon(iconLayer);
		}

		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {

			// This is needed to get the node text to be displayed.
			super.getTreeCellRendererComponent(tree, value, sel, expanded,
					leaf, row, hasFocus);

			// TODO : Selecting the right icon

			setIcon(iconNotUsed);

			return this;
		}

	}

	// Listeners attached to this model.
	private Collection<TreeModelListener> listeners = new Vector<TreeModelListener>();

	// Storage for our networks and datasets
	private List<NeuralNetwork> nets = new Vector<NeuralNetwork>();
	private List<Data> loadedData = new Vector<Data>();
	private Dictionary<NeuralNetwork, List<Data>> trainingSets = new Hashtable<NeuralNetwork, List<Data>>();
	private Dictionary<NeuralNetwork, List<Data>> testSets = new Hashtable<NeuralNetwork, List<Data>>();

	// The static nodes of the tree
	@SuppressWarnings("unchecked")
	// A raw type is wanted here!
	private LabeledNode<List<LabeledNode>> rootNode = new LabeledNode<List<LabeledNode>>(
			"root", new Vector<LabeledNode>());
	private LabeledNode<List<NeuralNetwork>> netsNode = new LabeledNode<List<NeuralNetwork>>(
			"Networks", nets);
	@SuppressWarnings("unchecked")
	// A raw type is wanted here!
	private LabeledNode<List<LabeledNode>> datasetsNode = new LabeledNode<List<LabeledNode>>(
			"Datasets", new Vector<LabeledNode>());
	private LabeledNode<List<Data>> loadedDataNode = new LabeledNode<List<Data>>(
			"Loaded", loadedData);

	NetworkTreeModel() {
		rootNode.getValue().add(netsNode);
		rootNode.getValue().add(datasetsNode);
		datasetsNode.getValue().add(loadedDataNode);
	}

	@Override
	public int getChildCount(Object parent) {
		if (parent == null)
			return 0;
		if (parent == rootNode)
			return rootNode.getValue().size();
		if (parent == netsNode)
			return netsNode.getValue().size();
		if (parent == datasetsNode)
			return datasetsNode.getValue().size();
		if (parent == loadedDataNode)
			return loadedDataNode.getValue().size();
		// ...
		return 0;
	}

	@Override
	public Object getChild(Object parent, int index) {
		if (parent == null)
			return null;
		if (parent == rootNode)
			return rootNode.getValue().get(index);
		if (parent == netsNode)
			return netsNode.getValue().get(index);
		if (parent == datasetsNode)
			return datasetsNode.getValue().get(index);
		if (parent == loadedDataNode)
			return loadedDataNode.getValue().get(index);
		// ...
		return null;
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		if (parent == null)
			return -1;
		if (parent == rootNode)
			return rootNode.getValue().indexOf(child);
		if (parent == netsNode)
			return netsNode.getValue().indexOf(child);
		if (parent == datasetsNode)
			return datasetsNode.getValue().indexOf(child);
		if (parent == loadedDataNode)
			return loadedDataNode.getValue().indexOf(child);
		// ...
		return -1;
	}

	@Override
	public Object getRoot() {
		return rootNode;
	}

	@Override
	public boolean isLeaf(Object node) {
		if (node == null)
			return false;
		if (node == rootNode || node == netsNode || node == datasetsNode
				|| node == loadedDataNode)
			return false;
		// ...
		return false;
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		// TODO Auto-generated method stub
	}

	@Override
	public void addTreeModelListener(TreeModelListener l) {
		listeners.add(l);
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		if (listeners.contains(l))
			listeners.remove(l);
	}

	/**
	 * Returns a new instance of the renderer that will handle the correct
	 * displaying of the icons of the nodes.
	 */
	public DefaultTreeCellRenderer getRenderer() {
		return new NetworkTreeRenderer();
	}

}
