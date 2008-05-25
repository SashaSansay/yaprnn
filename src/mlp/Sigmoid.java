package mlp;

public class Sigmoid implements ActivationFunction{

	public double compute(double x) {
		return (1.0 / (1.0 + Math.exp(-x)));
	}

	public double derivation(double x) {
		double e = Math.exp(x);
		return e / ((1.0 + e) * (1.0 + e));
	}

	public double getMinimumValue() {
		return 0;
	}
}
