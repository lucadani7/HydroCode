package utcb.fii.model;

import java.util.Objects;

public record Measurement(int nr, double q, double dp, String type) {
    private static final double D = 0.015;
    private static final double RHO = 998.2;
    private static final double NIU = 1.004e-6;
    private static final double AREA = Math.PI * Math.pow(D / 2, 2);

    public double getVelocity() {
        return q <= 0 ? 0 : roundTo5Decimals((q / 1000.0) / AREA);
    }

    public double getReynolds() {
        return roundTo5Decimals((getVelocity() * D) / NIU);
    }

    public double getZeta() {
        double v = getVelocity();
        if (v <= 1e-4) {
            return 0;
        }
        double dpPascal = dp * 100.0;
        return roundTo5Decimals((2 * dpPascal) / (RHO * Math.pow(v, 2)));
    }

    /**
     * Rounds the given double value to 5 decimal places.
     * If the value is not a valid number (NaN) or infinite, the original value is returned.
     *
     * @param value the double value to be rounded
     * @return the rounded value to 5 decimal places, or the original value if it is NaN or infinite
     */
    private double roundTo5Decimals(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return value;
        }
        return Math.round(value * 100000.0) / 100000.0;
    }

    public void validate() {
        Objects.requireNonNull(type, "Argument type cannot be null");
        if (nr < 0) {
            throw new IllegalArgumentException("Argument nr cannot be negative");
        }
        if (q < 0 || dp < 0) {
            throw new IllegalArgumentException("Negative values detected at nr " + nr);
        }
    }
}
