package dev.mayuna.lostarkbot.util.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import dev.mayuna.mayuslibrary.console.colors.Color;
import dev.mayuna.mayuslibrary.console.colors.Colors;

public class EndColorConverter extends ClassicConverter {

    @Override
    public String convert(ILoggingEvent event) {
        return new Color().setForeground(Colors.RESET).setBackground(Colors.RESET).build();
    }
}
