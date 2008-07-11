package yaprnn.mlp;

import java.util.*;
import yaprnn.mlp.*;
import yaprnn.dvv.*;

public class MLPStub implements NeuralNetwork {

	private String name;
	private int[] layers;
	private ActivationFunction[] activations;
	private double[] biases;
	private boolean autoEncoder;
	private MLP mlp;
	private boolean trained;
	private List<ActivationFunction> allActivations;

	public MLPStub(String name, int numLayers, int numNeurons, int activationFunction,
			double bias, boolean autoEncoder, List<ActivationFunction> allActivations) {
		this.name = name;
		this.allActivations = allActivations;
		this.autoEncoder = autoEncoder;
		layers = new int[numLayers];
		activations = new ActivationFunction[numLayers];
		biases = new double[numLayers];
		for(int i=0; i<numLayers; i++) {
			layers[i] = numNeurons;
			activations[i] = allActivations.get(activationFunction);
			biases[i] = bias;
		}
		layers[0] = 1;
		trained = false;
	}

	public void setNumInputNeurons(int numNeurons) {
		layers[0] = numNeurons;
	}

	public void setNumOutputNeurons(int numNeurons) {
		layers[layers.length-1] = numNeurons;
	}

	public void resetIterations() {
		initMLP();
		mlp.resetIterations();
	}

	/**
	 * This function performs the online calculation with the the Network
	 * 
	 * 
	 * @param dataCollection
	 *            A collection of the type  dvv.Data with input- and targetvalues
	 *            
	 * @param eta
	 *            The learning rate to be used.
	 * @throws BadConfigException
	 */
	public double runOnline(Collection<Data> dataCollection, double eta, double momentum) {
		initMLP();
		return mlp.runOnline(dataCollection, eta, momentum);
	}

	/**This function performs the batch calculation  with the Network
	 * 
	 * @param dataCollection
	 *            A collection of the type  dvv.Data with input- and targetvalues
	 *            
	 * @param eta
	 *            The learning rate to be used.
	 * @return den Testfehler. In case of an  error returns 0.
	 */
	public double runBatch(Collection<Data> dataCollection, int batchSize, double eta, double momentum) {
		initMLP();
		return mlp.runBatch(dataCollection, batchSize, eta, momentum);
	}
		
	/**
	 * This method performs the test using delivered data.
	 * 
	 * @param dataCollection
	 *            The data, tu be used for the test
	 * @return The test error. If an error occurse, returns 0.
	 */
	public double runTest(Collection<Data> dataCollection) {
		return mlp.runTest(dataCollection);	
	}

	/**
	 * This method starts a testrun.
	 * 
	 * @return the output of neurons in  percents.
	 */
	public double[] classify(double[] input) {
		initMLP();
		return mlp.classify(input);
	}

	/**
	 * Returns the number of layers in the neuronal network, including input and output layers.
	 * 
	 * @return Number of layers.
	 */
	public int getNumLayers() {
		return layers.length;
	}

	/**
	 * Returns the size (number of neurons) of a layer.
	 * If layer is 0 and isTrained() returns false, this function will return 1.
	 * 
	 * @param layer the index of the layer
	 * @return number of neurons in the layer
	 */
	public int getLayerSize(int layer) {
		return layers[layer];
	}

	/**
	 * Returns the weight matrix between two layers.
	 * If layer is 1 and isTrained returns false, this function will return null.
	 * If layer is 0, this function will return null.
	 * 
	 * @param layer the index of the second layer (must be >= 1)
	 * @return the weight matrix of type double[][], where the first dimension represents
	 *         the number of neurons in the second layer and the second- in the first one.
	 *         
	 */
	public double[][] getWeights(int layer) {
		if(layer != 0) {
			double[][] result = new double[layers[layer]][layers[layer-1]];
			for(int i=0; i<result.length; i++)
				java.util.Arrays.fill(result[i], 0.0);
			return result;
		} else
			return null;
	}

	/**
	 * Returns the activation function of the layer.
	 * If layer is 0, this function will return null.
	 * 
	 * @param layer the index of the layer
	 * @return the activation function of the layer
	 */
	public ActivationFunction getActivationFunction(int layer) {
		if(layer != 0)
			return activations[layer];
		else
			return null;
	}

	/**
	 * Returns the bias.
	 * If layer is 0, this function will return 0.0 .
	 * 
	 * @param layer the index of the layer
	 * @return the bias
	 */
	public double getBias(int layer) {
		if(layer != 0)
			return biases[layer];
		else
			return 0.0;
	}

	/**
	 * Returns a readable name of NeuralNetwork
	 * 
	 * @return name of the NeuralNetwork
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns true if the network has been trained.
	 * 
	 * @return true if this network has been trained; false otherwise
	 */
	public boolean isTrained() {
		return trained;
	}

	/**
	 * Sets a readable name for the NeuralNetwork
	 * 
	 * @param name new name of the NeuralNetwork
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the number of layers, including input and output layers.
	 * If this.isTrained() returns true, this function does nothing.
	 * If numLayers <= 0, behavior is undefined.
	 *  
	 * @param numLayers the number of layers
	 * @return the same as !this.isTrained()
	 */
	public boolean setnumLayers(int numLayers) {
		if(!trained) {
			int[] newLayers = new int[numLayers];
			double[] newBias = new double[numLayers];
			ActivationFunction[] newActivations = new ActivationFunction[numLayers];
			final int min = numLayers < layers.length ? numLayers : layers.length;
			for(int i=0; i<min; i++) {
				newLayers[i] = layers[i];
				newBias[i] = biases[i];
				newActivations[i] = activations[i];
			}
			for(int i=min; i<newLayers.length; i++) {
				newLayers[i] = newLayers[min-1];
				newBias[i] = newBias[min-1];
				newActivations[i] = newActivations[min-1];
			}
			layers = newLayers;
			biases = newBias;
			activations = newActivations;
		}
		return !trained;	
	}

	/**
	 * Sets the size of a layer.
	 * If this.isTrained() returns true, this function does nothing.
	 * If layer is 0, this function does nothing.
	 * If size <= 0, behavior is undefined.
	 * 
	 * @param layer the index of the layer
	 * @param size the number of neurons
	 * @return the same as !this.isTrained()
	 */
	public boolean setLayerSize(int layer, int size) {
		if(!trained && layer != 0)
			layers[layer] = size;
		return !trained;
	}
	
	/**
	 * Sets the activation function of a layer.
	 * If this.isTrained() returns true, this function does nothing.
	 * If layer is 0, this function does nothing.
	 * If activationFunction == null, behavior is undefined.
	 *
	 * @param layer the index of the layer
	 * @param activationFunction the activation function
	 * @return the same as !this.isTrained()
	 */
	public boolean setActivationFunction(int layer, ActivationFunction activationFunction) {
		if(!trained && layer != 0)
			activations[layer] = activationFunction;
		return !trained;
	}

	/**
	 * Sets the bias of a layer.
	 * If this.isTrained() returns true, this function does nothing.
	 * If layer is 0, this function does nothing.
	 *
	 * @param layer the index of the layer
	 * @param bias the bias
	 * @return the same as !this.isTrained()
	 */
	public boolean setBias(int layer, double bias) {
		if(!trained && layer != 0)
			biases[layer] = bias;
		return !trained;
	}

	private void initMLP() {
		if(mlp == null) {
			trained = true;
			int[] newLayers = new int[layers.length-2];
			double[] newBias = new double[layers.length-2];
			for(int i=0; i<newLayers.length; i++) {
				newLayers[i] = layers[i+1];
				newBias[i] = biases[i+1];
			}
			try {
				mlp = new MLP(layers[0], layers[layers.length-1], newLayers, activations,
					newBias, autoEncoder);
			} catch(BadConfigException e) {
				e.printStackTrace();
			}
		}
	}

}
