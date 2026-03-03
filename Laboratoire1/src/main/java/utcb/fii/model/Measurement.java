package utcb.fii.model;

/**
 * A record representing a measurement used in hydraulic computations.
 * This record holds various properties of a measurement and is immutable.
 *
 * @param nr        The sequence number of the measurement.
 * @param q_ls      The measured flow rate in liters per second.
 * @param dp_mbar   The measured pressure drop in millibars.
 * @param dp_pa     The calculated pressure drop in Pascals.
 * @param d_mm      The measured pipe diameter in millimeters.
 * @param re        The calculated Reynolds number, a dimensionless quantity used to predict flow patterns.
 * @param lambda    The calculated friction factor, representing the loss coefficient.
 */
public record Measurement(int nr, double q_ls, double dp_mbar, double dp_pa, double d_mm, double re, double lambda) {
}
