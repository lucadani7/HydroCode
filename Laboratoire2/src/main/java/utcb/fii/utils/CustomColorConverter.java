package utcb.fii.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;

/**
 * CustomColorConverter is a specialized Logback ForegroundCompositeConverterBase used for applying
 * custom color codes to log messages based on their logging level. This class is particularly
 * useful for enhancing log readability by displaying logs in different colors in the console.
 * <p>
 * This converter maps logging levels to specific ANSI color codes as follows:
 * - ERROR: Red (ANSI code 31)
 * - WARN: Dark Orange (ANSI code 38;5;208)
 * - INFO: Green (ANSI code 32)
 * - Other levels: Default console color (ANSI code 39)
 * <p>
 * It is designed to be used in Logback configuration and integrates seamlessly with the `logback.xml`
 * file by registering it as a custom conversion rule.
 */
public class CustomColorConverter extends ForegroundCompositeConverterBase<ILoggingEvent> {
    @Override
    protected String getForegroundColorCode(ILoggingEvent event) {
        Level level = event.getLevel();
        return switch (level.toInt()) {
            case Level.ERROR_INT -> "31";
            case Level.WARN_INT -> "38;5;208";
            case Level.INFO_INT -> "32";
            default -> "39";
        };
    }
}
