package dev.mayuna.lostarkbot.util.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import dev.mayuna.mayuslibrary.console.colors.Color;
import dev.mayuna.mayuslibrary.console.colors.Colors;

public class ColorConverter extends ClassicConverter {

    @Override
    public String convert(ILoggingEvent event) {
        Level level = event.getLevel();

        if (event.getMessage().startsWith("[SUCCESS] ")) {
            return new Color().setForeground(Colors.LIGHT_GREEN).build();
        }

        return switch (level.toInt()) {
            case Level.INFO_INT -> new Color().setForeground(Colors.WHITE).build();
            case Level.WARN_INT -> new Color().setForeground(Colors.LIGHT_YELLOW).build();
            case Level.ERROR_INT -> new Color().setForeground(Colors.RED).build();
            case Level.DEBUG_INT -> new Color().setForeground(Colors.BLUE).build();
            case Level.TRACE_INT -> new Color().setForeground(Colors.DARK_GRAY).build();
            default -> "";
        };
    }
}
