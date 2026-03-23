package utcb.fii.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MeasurementTest {
    @Test
    @DisplayName("Checking hidraulic computations (v, Zeta, Re) and aprox")
    void testCalculationsAndRounding() {
        Measurement m = new Measurement(1, 0.41, 631, "1 rouge");
        assertFalse(Double.isNaN(m.getVelocity()), "Velocity is not NaN");
        assertFalse(Double.isInfinite(m.getZeta()), "Zeta is not infinite");
        double zeta = m.getZeta();
        String zetaStr = String.valueOf(zeta);
        if (zetaStr.contains(".")) {
            int decimals = zetaStr.length() - zetaStr.indexOf('.') - 1;
            assertTrue(decimals <= 5, "Zeta must contain maxim 5 decimals, it has : " + decimals + " decimals");
        }
        assertTrue(m.getReynolds() > 2300, "Turbulent regime (Re > 2300)");
    }

    @Test
    @DisplayName("Checking comportament at zero debit (avoiding division by zero)")
    void testZeroValues() {
        Measurement mZero = new Measurement(0, 0.0, 0.0, "static");
        assertEquals(0.0, mZero.getVelocity(), "Velocity must be 0 at Q=0");
        assertEquals(0.0, mZero.getZeta(), "Zeta must be 0 at Q=0 (avoid NaN)");
        assertEquals(0.0, mZero.getReynolds(), "Reynolds must be 0 at Q=0");
    }
}
