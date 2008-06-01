package yaprnn.dvv;

import yaprnn.mlp.ActivationFunction;

public abstract class Data {

	private enum Use {TRAINING, TEST, NOT_USED};

	private Use use;

	public abstract double[] getData();
	public abstract String getName();
	public abstract String getLabel();
	public abstract int getTarget();
	public abstract String getFilename();
	public abstract String getLabelFromTarget(int target);;
	public abstract void subsample(int resolution, double overlap, ActivationFunction function);

	/**
	 * Marks this Data object as a part of the training data set.
	 */
	public void setTraining() {
		use = Use.TRAINING;
	}

	/**
	 * Marks this Data object as a part of the test data set.
	 */
	public void setTest() {
		use = Use.TEST;
	}

	/**
	 * Marks this Data object as neither test nor training data.
	 */
	public void setNotUsed() {
		use = Use.NOT_USED;
	}

	/**
	 * Returns true if this object is part of the training data set, false otherwise.
	 *
	 * @Return true if the object is training data; false otherwise.
	 */
	public boolean isTraining() {
		return use == Use.TRAINING;
	}

	/**
	 * Returns true if this object is part of the test data set, false otherwise.
	 *
	 * @Return true if the object is test data; false otherwise.
	 */
	public boolean isTest() {
		return use == Use.TEST;
	}

	/**
	 * Returns true if this object is part of the not-used data set, false otherwise.
	 *
	 * @Return true if the object is neither test nor training data; false otherwise.
	 */
	public boolean isNotUsed() {
		return use == Use.NOT_USED;
	}

}
