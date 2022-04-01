package dev.mayuna.lostarkbot.util.logging;

import dev.mayuna.lostarkbot.Main;
import org.apache.logging.log4j.Level;

public class Logger {

    private static final MayuLogger logger = MayuLogger.create(Main.class);

    public static void info(String msg) {
        logger.info(msg);
    }

    public static void warn(String msg) {
        logger.warn(msg);
    }

    public static void error(String msg) {
        logger.error(msg);
    }

    public static void fatal(String msg) {
        logger.fatal(msg);
    }

    public static void success(String msg) {
        logger.success(msg);
    }

    public static void debug(String msg) {
        logger.debug(msg);
    }

    public static void flow(String msg) {
        logger.flow(msg);
    }

    public static void trace(String msg) {
        Logger.flow(msg);
    }

    public static void throwing(Throwable throwable) {
        logger.throwing(throwable);
    }

    public static void throwing(Level level, Throwable throwable) {
        logger.throwing(level, throwable);
    }

    public static MayuLogger get() {
        return logger;
    }
}
