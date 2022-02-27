package dev.mayuna.lostarkbot.util.logging;

import ch.qos.logback.classic.Level;
import dev.mayuna.lostarkbot.Main;
import lombok.Getter;
import org.slf4j.LoggerFactory;

public class Logger {

    private static @Getter org.slf4j.Logger logger;

    public static void init() {
        logger = LoggerFactory.getLogger(Main.class);
    }

    public static void info(String msg) {
        logger.info(msg);
    }

    public static void warn(String msg) {
        logger.warn(msg);
    }

    public static void error(String msg) {
        logger.error(msg);
    }

    public static void success(String msg) {
        logger.info("[SUCCESS] " + msg);
    }

    public static void debug(String msg) {
        logger.debug(msg);
    }

    public static void trace(String msg) {
        logger.trace(msg);
    }

    public static void setLevel(String level) {
        try {
            ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
            root.setLevel(Level.valueOf(level));
            Logger.success("Log level set to '" + level + "'!");
        } catch (Exception exception) {
            exception.printStackTrace();
            Logger.error("Unable to set level to '" + level + "'!");
        }
    }
}
