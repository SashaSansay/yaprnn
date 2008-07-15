package yaprnn.dvv;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;

import yaprnn.mlp.ActivationFunction;

/** DVV is the main data managment and preprocessing class.
 *  It provides methods for reading picture and sound files and for preprocessing loaded files.
 */
public class DVV {

	private final Collection<Data> allData;
	private Collection<Data> trainingData;
	private Collection<Data> testData;
	private int numInputNeurons;
	private final int numOutputNeurons;
	private final Data.Type type;

	/** Constructs a DVV for holding {@link IdxPicture} data with the specified filenames.
	 *
	 *  @param dataFilename  the name of the file containing the image data
	 *  @param labelFilename the name of the file containing the labels/targets
	 *  @throws InvalidFileException if one of the files does not have the required format
	 *  @throws FileMismatchException if the files do not appear to belong to the same dataset
	 *  @throws NoSuchFileException if one of the files does not exist
	 */
	public DVV(String dataFilename, String labelFilename)
				throws InvalidFileException, FileMismatchException, NoSuchFileException, IOException {
		allData = IdxPicture.readFromFile(dataFilename, labelFilename);
		numOutputNeurons = 10;
		type = Data.Type.PICTURE;
	}
	
	/** Constructs a DVV for holding {@link AiffSound} data with the specified filenames.
	 *
	 *  @param filenames the names of the file containing the sound data
	 *  @throws InvalideFileException if one of the files does not have the required format
	 *  @throws NoSuchFileException if one of the files does not exist
	 */
	public DVV(Collection<String> filenames) throws InvalidFileException, NoSuchFileException {
		allData = AiffSound.readFromFile(filenames);
		numOutputNeurons = 5;
		type = Data.Type.AUDIO;
	}

	/** Returns the whole data set.
	 *
	 *  @return the data set
	 */
	public Collection<Data> getDataSet() {
		return allData;
	}

	/** Returns the training data set.
	 *
	 *  @return the training data
	 */
	public Collection<Data> getTrainingData() {
		if(trainingData == null) selectTrainingData();
		shuffle(trainingData);
		return trainingData;
	}

	/** Returns the test data set.
	 *
	 *  @return the test data
	 */
	public Collection<Data> getTestData() {
		if(testData == null) selectTrainingData();
		shuffle(testData);
		return testData;
	}

	/** Randomly selects data for the training or test data set, according to the specified percentages.
	 *
	 *  @param trainingDataPercentage the percentage of data to be used for training
	 *  @param testDataPercentage the percentage of data to be used for testing
	 */ 
	public void chooseRandomTrainingData(double trainingDataPercentage, double testDataPercentage) {
		shuffle(allData);
		int numTraining = (int)(allData.size() * trainingDataPercentage);
		int numTest = (int)(allData.size() * testDataPercentage);
		for(Data data : allData) {
			if(numTraining > 0) {
				data.setTraining();
				--numTraining;
			} else if(numTest > 0) {
				data.setTest();
				--numTest;
			} else
				data.setNotUsed();
		}
		trainingData = null;
		testData = null;
	}

	/** Preprocesses the whole data set.
	 *
	 *  @param resolution      the data is to be sampled to
	 *  @param overlap         the overlap used when determining the window sizes
	 *  @param scalingFunction the function used to scale the subsampled data
	 */
	public void preprocess(int resolution, double overlap, ActivationFunction scalingFunction) throws NoSuchFileException{
		if (allData.size() > 0) {
			try {
			for(Data data : allData)
					data.subsample(resolution, overlap, scalingFunction);
				} catch (NoSuchFileException ex) {
					throw new NoSuchFileException(ex.getFilename());
				}
			if (allData.iterator().next().isAudio())
				numInputNeurons = resolution;
			else 
				numInputNeurons = resolution * resolution;
		}
	}

	/** Returns the size of the input vector.
	 *
	 *  @return the size of the input vector
	 */
	public int getNumInputNeurons() {
		return numInputNeurons;
	}

	/** Retuns the size of the output vector.
	 *
	 *  @return the size of the output vector
	 */
	public int getNumOutputNeurons() {
		return numOutputNeurons;
	}

	/** Returns the type of the data.
	 *
	 *  @return the type of the data
	 */
	public Data.Type getDataType() {
		return type;
	}

	/** Selects training and test data and stores them in the appropriate collections. */
	private void selectTrainingData() {
		trainingData = new LinkedList<Data>();
		testData = new LinkedList<Data>();
		for(Data data : allData) {
			if(data.isTraining()) 
				trainingData.add(data);
			else if(data.isTest())
				testData.add(data);
		}
	}

	/** Shuffles the specified data set. */
	private void shuffle(Collection<Data> input) {
		Data[] dataset = input.toArray(new Data[0]);
		input.clear();
		Random random = new Random();
		int n = dataset.length;
		while(n > 1) {
			final int k = random.nextInt(n);
			--n;
			final Data tmp = dataset[n];
			dataset[n] = dataset[k];
			dataset[k] = tmp;
		}
		for(int i=0; i<dataset.length; i++)
			input.add(dataset[i]);
	}

}
