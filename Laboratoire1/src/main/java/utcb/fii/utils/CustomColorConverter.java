package utcb.fii.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase;

public class CustomColorConverter extends ForegroundCompositeConverterBase<ILoggingEvent> {
    @Override
    protected String getForegroundColorCode(ILoggingEvent event) {
        Level level = event.getLevel();
        return switch (level.toInt()) {
            case Level.ERROR_INT -> "31"; // foreground red
            case Level.WARN_INT -> "38;5;208"; // dark orange
            case Level.INFO_INT -> "32"; // foreground green
            default -> "39"; // default color in IDE console
        };
    }
}
