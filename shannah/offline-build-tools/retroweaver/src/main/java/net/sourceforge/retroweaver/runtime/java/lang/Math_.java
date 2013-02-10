package net.sourceforge.retroweaver.runtime.java.lang;

public class Math_ {

	private static final double LOG10 = Math.log(10);

	public static double log10(double a) {
		return Math.log(a)/LOG10;
	}

	public static double signum(double d) {
		if (Double.isNaN(d)) {
			return Double.NaN;
		} else if (d > 0) {
			return 1;
		} else if (d < 0) {
			return -1;
		} else {
			return d;
		}
	}

	public static float signum(float d) {
		if (Float.isNaN(d)) {
			return Float.NaN;
		} else if (d > 0) {
			return 1;
		} else if (d < 0) {
			return -1;
		} else {
			return d;
		}
	}
	
	public static double sinh(double x) {
		if (Double.isNaN(x)) {
			return Double.NaN;
		} else if (Double.isInfinite(x)) {
			return x;
		} else if (x == 0) {
			return x;
		} else {
			return (Math.exp(x) - Math.exp(-x)) / 2;
		}
	}

	public static double cosh(double x) {
		if (Double.isNaN(x)) {
			return Double.NaN;
		} else if (Double.isInfinite(x)) {
			return Double.POSITIVE_INFINITY;
		} else if (x == 0) {
			return 1.0d;
		} else {
			return (Math.exp(x) + Math.exp(-x)) / 2;
		}
	}

	public static double tanh(double x) {
		if (Double.isNaN(x)) {
			return Double.NaN;
		} else if (x == 0) {
			return x;
		} else if (x == Double.POSITIVE_INFINITY) {
			return 1;
		} else if (x == Double.NEGATIVE_INFINITY) {
			return -1;
		} else {
			double exp1 = Math.exp(x);
			double exp2 = Math.exp(-x);

			return (exp1 - exp2)/(exp1 + exp2);
		}
	}

}
