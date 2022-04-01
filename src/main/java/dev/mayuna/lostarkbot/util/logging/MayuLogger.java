package dev.mayuna.lostarkbot.util.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.apache.logging.log4j.spi.ExtendedLoggerWrapper;
import org.apache.logging.log4j.util.MessageSupplier;
import org.apache.logging.log4j.util.Supplier;

/**
 * Extended Logger interface with convenience methods for
 * the FLOW and SUCCESS custom log levels.
 * <p>Compatible with Log4j 2.6 or higher.</p>
 */
public final class MayuLogger extends ExtendedLoggerWrapper {
    private static final long serialVersionUID = 23416029248500L;
    private final ExtendedLoggerWrapper logger;

    private static final String FQCN = MayuLogger.class.getName();
    private static final Level FLOW = Level.forName("FLOW", 550);
    private static final Level SUCCESS = Level.forName("SUCCESS", 420);

    private MayuLogger(final Logger logger) {
        super((AbstractLogger) logger, logger.getName(), logger.getMessageFactory());
        this.logger = this;
    }

    /**
     * Returns a custom Logger with the name of the calling class.
     *
     * @return The custom Logger for the calling class.
     */
    public static MayuLogger create() {
        final Logger wrapped = LogManager.getLogger();
        return new MayuLogger(wrapped);
    }

    /**
     * Returns a custom Logger using the fully qualified name of the Class as
     * the Logger name.
     *
     * @param loggerName The Class whose name should be used as the Logger name.
     *            If null it will default to the calling class.
     * @return The custom Logger.
     */
    public static MayuLogger create(final Class<?> loggerName) {
        final Logger wrapped = LogManager.getLogger(loggerName);
        return new MayuLogger(wrapped);
    }

    /**
     * Returns a custom Logger using the fully qualified name of the Class as
     * the Logger name.
     *
     * @param loggerName The Class whose name should be used as the Logger name.
     *            If null it will default to the calling class.
     * @param messageFactory The message factory is used only when creating a
     *            logger, subsequent use does not change the logger but will log
     *            a warning if mismatched.
     * @return The custom Logger.
     */
    public static MayuLogger create(final Class<?> loggerName, final MessageFactory messageFactory) {
        final Logger wrapped = LogManager.getLogger(loggerName, messageFactory);
        return new MayuLogger(wrapped);
    }

    /**
     * Returns a custom Logger using the fully qualified class name of the value
     * as the Logger name.
     *
     * @param value The value whose class name should be used as the Logger
     *            name. If null the name of the calling class will be used as
     *            the logger name.
     * @return The custom Logger.
     */
    public static MayuLogger create(final Object value) {
        final Logger wrapped = LogManager.getLogger(value);
        return new MayuLogger(wrapped);
    }

    /**
     * Returns a custom Logger using the fully qualified class name of the value
     * as the Logger name.
     *
     * @param value The value whose class name should be used as the Logger
     *            name. If null the name of the calling class will be used as
     *            the logger name.
     * @param messageFactory The message factory is used only when creating a
     *            logger, subsequent use does not change the logger but will log
     *            a warning if mismatched.
     * @return The custom Logger.
     */
    public static MayuLogger create(final Object value, final MessageFactory messageFactory) {
        final Logger wrapped = LogManager.getLogger(value, messageFactory);
        return new MayuLogger(wrapped);
    }

    /**
     * Returns a custom Logger with the specified name.
     *
     * @param name The logger name. If null the name of the calling class will
     *            be used.
     * @return The custom Logger.
     */
    public static MayuLogger create(final String name) {
        final Logger wrapped = LogManager.getLogger(name);
        return new MayuLogger(wrapped);
    }

    /**
     * Returns a custom Logger with the specified name.
     *
     * @param name The logger name. If null the name of the calling class will
     *            be used.
     * @param messageFactory The message factory is used only when creating a
     *            logger, subsequent use does not change the logger but will log
     *            a warning if mismatched.
     * @return The custom Logger.
     */
    public static MayuLogger create(final String name, final MessageFactory messageFactory) {
        final Logger wrapped = LogManager.getLogger(name, messageFactory);
        return new MayuLogger(wrapped);
    }

    /**
     * Logs a message with the specific Marker at the {@code FLOW} level.
     *
     * @param marker the marker data specific to this log statement
     * @param msg the message string to be logged
     */
    public void flow(final Marker marker, final Message msg) {
        logger.logIfEnabled(FQCN, FLOW, marker, msg, (Throwable) null);
    }

    /**
     * Logs a message with the specific Marker at the {@code FLOW} level.
     *
     * @param marker the marker data specific to this log statement
     * @param msg the message string to be logged
     * @param t A Throwable or null.
     */
    public void flow(final Marker marker, final Message msg, final Throwable t) {
        logger.logIfEnabled(FQCN, FLOW, marker, msg, t);
    }

    /**
     * Logs a message object with the {@code FLOW} level.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message object to log.
     */
    public void flow(final Marker marker, final Object message) {
        logger.logIfEnabled(FQCN, FLOW, marker, message, (Throwable) null);
    }

    /**
     * Logs a message CharSequence with the {@code FLOW} level.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message CharSequence to log.
     * @since Log4j-2.6
     */
    public void flow(final Marker marker, final CharSequence message) {
        logger.logIfEnabled(FQCN, FLOW, marker, message, (Throwable) null);
    }

    /**
     * Logs a message at the {@code FLOW} level including the stack trace of
     * the {@link Throwable} {@code t} passed as parameter.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message to log.
     * @param t the exception to log, including its stack trace.
     */
    public void flow(final Marker marker, final Object message, final Throwable t) {
        logger.logIfEnabled(FQCN, FLOW, marker, message, t);
    }

    /**
     * Logs a message at the {@code FLOW} level including the stack trace of
     * the {@link Throwable} {@code t} passed as parameter.
     *
     * @param marker the marker data specific to this log statement
     * @param message the CharSequence to log.
     * @param t the exception to log, including its stack trace.
     * @since Log4j-2.6
     */
    public void flow(final Marker marker, final CharSequence message, final Throwable t) {
        logger.logIfEnabled(FQCN, FLOW, marker, message, t);
    }

    /**
     * Logs a message object with the {@code FLOW} level.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message object to log.
     */
    public void flow(final Marker marker, final String message) {
        logger.logIfEnabled(FQCN, FLOW, marker, message, (Throwable) null);
    }

    /**
     * Logs a message with parameters at the {@code FLOW} level.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message to log; the format depends on the message factory.
     * @param params parameters to the message.
     * @see #getMessageFactory()
     */
    public void flow(final Marker marker, final String message, final Object... params) {
        logger.logIfEnabled(FQCN, FLOW, marker, message, params);
    }

    /**
     * Logs a message with parameters at the {@code FLOW} level.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void flow(final Marker marker, final String message, final Object p0) {
        logger.logIfEnabled(FQCN, FLOW, marker, message, p0);
    }

    /**
     * Logs a message with parameters at the {@code FLOW} level.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void flow(final Marker marker, final String message, final Object p0, final Object p1) {
        logger.logIfEnabled(FQCN, FLOW, marker, message, p0, p1);
    }

    /**
     * Logs a message with parameters at the {@code FLOW} level.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void flow(final Marker marker, final String message, final Object p0, final Object p1, final Object p2) {
        logger.logIfEnabled(FQCN, FLOW, marker, message, p0, p1, p2);
    }

    /**
     * Logs a message with parameters at the {@code FLOW} level.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @param p3 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void flow(final Marker marker, final String message, final Object p0, final Object p1, final Object p2,
            final Object p3) {
        logger.logIfEnabled(FQCN, FLOW, marker, message, p0, p1, p2, p3);
    }

    /**
     * Logs a message with parameters at the {@code FLOW} level.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @param p3 parameter to the message.
     * @param p4 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void flow(final Marker marker, final String message, final Object p0, final Object p1, final Object p2,
            final Object p3, final Object p4) {
        logger.logIfEnabled(FQCN, FLOW, marker, message, p0, p1, p2, p3, p4);
    }

    /**
     * Logs a message with parameters at the {@code FLOW} level.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @param p3 parameter to the message.
     * @param p4 parameter to the message.
     * @param p5 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void flow(final Marker marker, final String message, final Object p0, final Object p1, final Object p2,
            final Object p3, final Object p4, final Object p5) {
        logger.logIfEnabled(FQCN, FLOW, marker, message, p0, p1, p2, p3, p4, p5);
    }

    /**
     * Logs a message with parameters at the {@code FLOW} level.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @param p3 parameter to the message.
     * @param p4 parameter to the message.
     * @param p5 parameter to the message.
     * @param p6 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void flow(final Marker marker, final String message, final Object p0, final Object p1, final Object p2,
            final Object p3, final Object p4, final Object p5, final Object p6) {
        logger.logIfEnabled(FQCN, FLOW, marker, message, p0, p1, p2, p3, p4, p5, p6);
    }

    /**
     * Logs a message with parameters at the {@code FLOW} level.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @param p3 parameter to the message.
     * @param p4 parameter to the message.
     * @param p5 parameter to the message.
     * @param p6 parameter to the message.
     * @param p7 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void flow(final Marker marker, final String message, final Object p0, final Object p1, final Object p2,
            final Object p3, final Object p4, final Object p5, final Object p6,
            final Object p7) {
        logger.logIfEnabled(FQCN, FLOW, marker, message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    /**
     * Logs a message with parameters at the {@code FLOW} level.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @param p3 parameter to the message.
     * @param p4 parameter to the message.
     * @param p5 parameter to the message.
     * @param p6 parameter to the message.
     * @param p7 parameter to the message.
     * @param p8 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void flow(final Marker marker, final String message, final Object p0, final Object p1, final Object p2,
            final Object p3, final Object p4, final Object p5, final Object p6,
            final Object p7, final Object p8) {
        logger.logIfEnabled(FQCN, FLOW, marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    /**
     * Logs a message with parameters at the {@code FLOW} level.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @param p3 parameter to the message.
     * @param p4 parameter to the message.
     * @param p5 parameter to the message.
     * @param p6 parameter to the message.
     * @param p7 parameter to the message.
     * @param p8 parameter to the message.
     * @param p9 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void flow(final Marker marker, final String message, final Object p0, final Object p1, final Object p2,
            final Object p3, final Object p4, final Object p5, final Object p6,
            final Object p7, final Object p8, final Object p9) {
        logger.logIfEnabled(FQCN, FLOW, marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    /**
     * Logs a message at the {@code FLOW} level including the stack trace of
     * the {@link Throwable} {@code t} passed as parameter.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message to log.
     * @param t the exception to log, including its stack trace.
     */
    public void flow(final Marker marker, final String message, final Throwable t) {
        logger.logIfEnabled(FQCN, FLOW, marker, message, t);
    }

    /**
     * Logs the specified Message at the {@code FLOW} level.
     *
     * @param msg the message string to be logged
     */
    public void flow(final Message msg) {
        logger.logIfEnabled(FQCN, FLOW, null, msg, (Throwable) null);
    }

    /**
     * Logs the specified Message at the {@code FLOW} level.
     *
     * @param msg the message string to be logged
     * @param t A Throwable or null.
     */
    public void flow(final Message msg, final Throwable t) {
        logger.logIfEnabled(FQCN, FLOW, null, msg, t);
    }

    /**
     * Logs a message object with the {@code FLOW} level.
     *
     * @param message the message object to log.
     */
    public void flow(final Object message) {
        logger.logIfEnabled(FQCN, FLOW, null, message, (Throwable) null);
    }

    /**
     * Logs a message at the {@code FLOW} level including the stack trace of
     * the {@link Throwable} {@code t} passed as parameter.
     *
     * @param message the message to log.
     * @param t the exception to log, including its stack trace.
     */
    public void flow(final Object message, final Throwable t) {
        logger.logIfEnabled(FQCN, FLOW, null, message, t);
    }

    /**
     * Logs a message CharSequence with the {@code FLOW} level.
     *
     * @param message the message CharSequence to log.
     * @since Log4j-2.6
     */
    public void flow(final CharSequence message) {
        logger.logIfEnabled(FQCN, FLOW, null, message, (Throwable) null);
    }

    /**
     * Logs a CharSequence at the {@code FLOW} level including the stack trace of
     * the {@link Throwable} {@code t} passed as parameter.
     *
     * @param message the CharSequence to log.
     * @param t the exception to log, including its stack trace.
     * @since Log4j-2.6
     */
    public void flow(final CharSequence message, final Throwable t) {
        logger.logIfEnabled(FQCN, FLOW, null, message, t);
    }

    /**
     * Logs a message object with the {@code FLOW} level.
     *
     * @param message the message object to log.
     */
    public void flow(final String message) {
        logger.logIfEnabled(FQCN, FLOW, null, message, (Throwable) null);
    }

    /**
     * Logs a message with parameters at the {@code FLOW} level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param params parameters to the message.
     * @see #getMessageFactory()
     */
    public void flow(final String message, final Object... params) {
        logger.logIfEnabled(FQCN, FLOW, null, message, params);
    }

    /**
     * Logs a message with parameters at the {@code FLOW} level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void flow(final String message, final Object p0) {
        logger.logIfEnabled(FQCN, FLOW, null, message, p0);
    }

    /**
     * Logs a message with parameters at the {@code FLOW} level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void flow(final String message, final Object p0, final Object p1) {
        logger.logIfEnabled(FQCN, FLOW, null, message, p0, p1);
    }

    /**
     * Logs a message with parameters at the {@code FLOW} level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void flow(final String message, final Object p0, final Object p1, final Object p2) {
        logger.logIfEnabled(FQCN, FLOW, null, message, p0, p1, p2);
    }

    /**
     * Logs a message with parameters at the {@code FLOW} level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @param p3 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void flow(final String message, final Object p0, final Object p1, final Object p2,
            final Object p3) {
        logger.logIfEnabled(FQCN, FLOW, null, message, p0, p1, p2, p3);
    }

    /**
     * Logs a message with parameters at the {@code FLOW} level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @param p3 parameter to the message.
     * @param p4 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void flow(final String message, final Object p0, final Object p1, final Object p2,
            final Object p3, final Object p4) {
        logger.logIfEnabled(FQCN, FLOW, null, message, p0, p1, p2, p3, p4);
    }

    /**
     * Logs a message with parameters at the {@code FLOW} level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @param p3 parameter to the message.
     * @param p4 parameter to the message.
     * @param p5 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void flow(final String message, final Object p0, final Object p1, final Object p2,
            final Object p3, final Object p4, final Object p5) {
        logger.logIfEnabled(FQCN, FLOW, null, message, p0, p1, p2, p3, p4, p5);
    }

    /**
     * Logs a message with parameters at the {@code FLOW} level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @param p3 parameter to the message.
     * @param p4 parameter to the message.
     * @param p5 parameter to the message.
     * @param p6 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void flow(final String message, final Object p0, final Object p1, final Object p2,
            final Object p3, final Object p4, final Object p5, final Object p6) {
        logger.logIfEnabled(FQCN, FLOW, null, message, p0, p1, p2, p3, p4, p5, p6);
    }

    /**
     * Logs a message with parameters at the {@code FLOW} level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @param p3 parameter to the message.
     * @param p4 parameter to the message.
     * @param p5 parameter to the message.
     * @param p6 parameter to the message.
     * @param p7 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void flow(final String message, final Object p0, final Object p1, final Object p2,
            final Object p3, final Object p4, final Object p5, final Object p6,
            final Object p7) {
        logger.logIfEnabled(FQCN, FLOW, null, message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    /**
     * Logs a message with parameters at the {@code FLOW} level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @param p3 parameter to the message.
     * @param p4 parameter to the message.
     * @param p5 parameter to the message.
     * @param p6 parameter to the message.
     * @param p7 parameter to the message.
     * @param p8 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void flow(final String message, final Object p0, final Object p1, final Object p2,
            final Object p3, final Object p4, final Object p5, final Object p6,
            final Object p7, final Object p8) {
        logger.logIfEnabled(FQCN, FLOW, null, message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    /**
     * Logs a message with parameters at the {@code FLOW} level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @param p3 parameter to the message.
     * @param p4 parameter to the message.
     * @param p5 parameter to the message.
     * @param p6 parameter to the message.
     * @param p7 parameter to the message.
     * @param p8 parameter to the message.
     * @param p9 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void flow(final String message, final Object p0, final Object p1, final Object p2,
            final Object p3, final Object p4, final Object p5, final Object p6,
            final Object p7, final Object p8, final Object p9) {
        logger.logIfEnabled(FQCN, FLOW, null, message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    /**
     * Logs a message at the {@code FLOW} level including the stack trace of
     * the {@link Throwable} {@code t} passed as parameter.
     *
     * @param message the message to log.
     * @param t the exception to log, including its stack trace.
     */
    public void flow(final String message, final Throwable t) {
        logger.logIfEnabled(FQCN, FLOW, null, message, t);
    }

    /**
     * Logs a message which is only to be constructed if the logging level is the {@code FLOW}level.
     *
     * @param msgSupplier A function, which when called, produces the desired log message;
     *            the format depends on the message factory.
     * @since Log4j-2.4
     */
    public void flow(final Supplier<?> msgSupplier) {
        logger.logIfEnabled(FQCN, FLOW, null, msgSupplier, (Throwable) null);
    }

    /**
     * Logs a message (only to be constructed if the logging level is the {@code FLOW}
     * level) including the stack trace of the {@link Throwable} <code>t</code> passed as parameter.
     *
     * @param msgSupplier A function, which when called, produces the desired log message;
     *            the format depends on the message factory.
     * @param t the exception to log, including its stack trace.
     * @since Log4j-2.4
     */
    public void flow(final Supplier<?> msgSupplier, final Throwable t) {
        logger.logIfEnabled(FQCN, FLOW, null, msgSupplier, t);
    }

    /**
     * Logs a message which is only to be constructed if the logging level is the
     * {@code FLOW} level with the specified Marker.
     *
     * @param marker the marker data specific to this log statement
     * @param msgSupplier A function, which when called, produces the desired log message;
     *            the format depends on the message factory.
     * @since Log4j-2.4
     */
    public void flow(final Marker marker, final Supplier<?> msgSupplier) {
        logger.logIfEnabled(FQCN, FLOW, marker, msgSupplier, (Throwable) null);
    }

    /**
     * Logs a message with parameters which are only to be constructed if the logging level is the
     * {@code FLOW} level.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message to log; the format depends on the message factory.
     * @param paramSuppliers An array of functions, which when called, produce the desired log message parameters.
     * @since Log4j-2.4
     */
    public void flow(final Marker marker, final String message, final Supplier<?>... paramSuppliers) {
        logger.logIfEnabled(FQCN, FLOW, marker, message, paramSuppliers);
    }

    /**
     * Logs a message (only to be constructed if the logging level is the {@code FLOW}
     * level) with the specified Marker and including the stack trace of the {@link Throwable}
     * <code>t</code> passed as parameter.
     *
     * @param marker the marker data specific to this log statement
     * @param msgSupplier A function, which when called, produces the desired log message;
     *            the format depends on the message factory.
     * @param t A Throwable or null.
     * @since Log4j-2.4
     */
    public void flow(final Marker marker, final Supplier<?> msgSupplier, final Throwable t) {
        logger.logIfEnabled(FQCN, FLOW, marker, msgSupplier, t);
    }

    /**
     * Logs a message with parameters which are only to be constructed if the logging level is
     * the {@code FLOW} level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param paramSuppliers An array of functions, which when called, produce the desired log message parameters.
     * @since Log4j-2.4
     */
    public void flow(final String message, final Supplier<?>... paramSuppliers) {
        logger.logIfEnabled(FQCN, FLOW, null, message, paramSuppliers);
    }

    /**
     * Logs a message which is only to be constructed if the logging level is the
     * {@code FLOW} level with the specified Marker. The {@code MessageSupplier} may or may
     * not use the {@link MessageFactory} to construct the {@code Message}.
     *
     * @param marker the marker data specific to this log statement
     * @param msgSupplier A function, which when called, produces the desired log message.
     * @since Log4j-2.4
     */
    public void flow(final Marker marker, final MessageSupplier msgSupplier) {
        logger.logIfEnabled(FQCN, FLOW, marker, msgSupplier, (Throwable) null);
    }

    /**
     * Logs a message (only to be constructed if the logging level is the {@code FLOW}
     * level) with the specified Marker and including the stack trace of the {@link Throwable}
     * <code>t</code> passed as parameter. The {@code MessageSupplier} may or may not use the
     * {@link MessageFactory} to construct the {@code Message}.
     *
     * @param marker the marker data specific to this log statement
     * @param msgSupplier A function, which when called, produces the desired log message.
     * @param t A Throwable or null.
     * @since Log4j-2.4
     */
    public void flow(final Marker marker, final MessageSupplier msgSupplier, final Throwable t) {
        logger.logIfEnabled(FQCN, FLOW, marker, msgSupplier, t);
    }

    /**
     * Logs a message which is only to be constructed if the logging level is the
     * {@code FLOW} level. The {@code MessageSupplier} may or may not use the
     * {@link MessageFactory} to construct the {@code Message}.
     *
     * @param msgSupplier A function, which when called, produces the desired log message.
     * @since Log4j-2.4
     */
    public void flow(final MessageSupplier msgSupplier) {
        logger.logIfEnabled(FQCN, FLOW, null, msgSupplier, (Throwable) null);
    }

    /**
     * Logs a message (only to be constructed if the logging level is the {@code FLOW}
     * level) including the stack trace of the {@link Throwable} <code>t</code> passed as parameter.
     * The {@code MessageSupplier} may or may not use the {@link MessageFactory} to construct the
     * {@code Message}.
     *
     * @param msgSupplier A function, which when called, produces the desired log message.
     * @param t the exception to log, including its stack trace.
     * @since Log4j-2.4
     */
    public void flow(final MessageSupplier msgSupplier, final Throwable t) {
        logger.logIfEnabled(FQCN, FLOW, null, msgSupplier, t);
    }

    /**
     * Logs a message with the specific Marker at the {@code SUCCESS} level.
     *
     * @param marker the marker data specific to this log statement
     * @param msg the message string to be logged
     */
    public void success(final Marker marker, final Message msg) {
        logger.logIfEnabled(FQCN, SUCCESS, marker, msg, (Throwable) null);
    }

    /**
     * Logs a message with the specific Marker at the {@code SUCCESS} level.
     *
     * @param marker the marker data specific to this log statement
     * @param msg the message string to be logged
     * @param t A Throwable or null.
     */
    public void success(final Marker marker, final Message msg, final Throwable t) {
        logger.logIfEnabled(FQCN, SUCCESS, marker, msg, t);
    }

    /**
     * Logs a message object with the {@code SUCCESS} level.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message object to log.
     */
    public void success(final Marker marker, final Object message) {
        logger.logIfEnabled(FQCN, SUCCESS, marker, message, (Throwable) null);
    }

    /**
     * Logs a message CharSequence with the {@code SUCCESS} level.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message CharSequence to log.
     * @since Log4j-2.6
     */
    public void success(final Marker marker, final CharSequence message) {
        logger.logIfEnabled(FQCN, SUCCESS, marker, message, (Throwable) null);
    }

    /**
     * Logs a message at the {@code SUCCESS} level including the stack trace of
     * the {@link Throwable} {@code t} passed as parameter.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message to log.
     * @param t the exception to log, including its stack trace.
     */
    public void success(final Marker marker, final Object message, final Throwable t) {
        logger.logIfEnabled(FQCN, SUCCESS, marker, message, t);
    }

    /**
     * Logs a message at the {@code SUCCESS} level including the stack trace of
     * the {@link Throwable} {@code t} passed as parameter.
     *
     * @param marker the marker data specific to this log statement
     * @param message the CharSequence to log.
     * @param t the exception to log, including its stack trace.
     * @since Log4j-2.6
     */
    public void success(final Marker marker, final CharSequence message, final Throwable t) {
        logger.logIfEnabled(FQCN, SUCCESS, marker, message, t);
    }

    /**
     * Logs a message object with the {@code SUCCESS} level.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message object to log.
     */
    public void success(final Marker marker, final String message) {
        logger.logIfEnabled(FQCN, SUCCESS, marker, message, (Throwable) null);
    }

    /**
     * Logs a message with parameters at the {@code SUCCESS} level.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message to log; the format depends on the message factory.
     * @param params parameters to the message.
     * @see #getMessageFactory()
     */
    public void success(final Marker marker, final String message, final Object... params) {
        logger.logIfEnabled(FQCN, SUCCESS, marker, message, params);
    }

    /**
     * Logs a message with parameters at the {@code SUCCESS} level.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void success(final Marker marker, final String message, final Object p0) {
        logger.logIfEnabled(FQCN, SUCCESS, marker, message, p0);
    }

    /**
     * Logs a message with parameters at the {@code SUCCESS} level.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void success(final Marker marker, final String message, final Object p0, final Object p1) {
        logger.logIfEnabled(FQCN, SUCCESS, marker, message, p0, p1);
    }

    /**
     * Logs a message with parameters at the {@code SUCCESS} level.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void success(final Marker marker, final String message, final Object p0, final Object p1, final Object p2) {
        logger.logIfEnabled(FQCN, SUCCESS, marker, message, p0, p1, p2);
    }

    /**
     * Logs a message with parameters at the {@code SUCCESS} level.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @param p3 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void success(final Marker marker, final String message, final Object p0, final Object p1, final Object p2,
            final Object p3) {
        logger.logIfEnabled(FQCN, SUCCESS, marker, message, p0, p1, p2, p3);
    }

    /**
     * Logs a message with parameters at the {@code SUCCESS} level.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @param p3 parameter to the message.
     * @param p4 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void success(final Marker marker, final String message, final Object p0, final Object p1, final Object p2,
            final Object p3, final Object p4) {
        logger.logIfEnabled(FQCN, SUCCESS, marker, message, p0, p1, p2, p3, p4);
    }

    /**
     * Logs a message with parameters at the {@code SUCCESS} level.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @param p3 parameter to the message.
     * @param p4 parameter to the message.
     * @param p5 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void success(final Marker marker, final String message, final Object p0, final Object p1, final Object p2,
            final Object p3, final Object p4, final Object p5) {
        logger.logIfEnabled(FQCN, SUCCESS, marker, message, p0, p1, p2, p3, p4, p5);
    }

    /**
     * Logs a message with parameters at the {@code SUCCESS} level.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @param p3 parameter to the message.
     * @param p4 parameter to the message.
     * @param p5 parameter to the message.
     * @param p6 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void success(final Marker marker, final String message, final Object p0, final Object p1, final Object p2,
            final Object p3, final Object p4, final Object p5, final Object p6) {
        logger.logIfEnabled(FQCN, SUCCESS, marker, message, p0, p1, p2, p3, p4, p5, p6);
    }

    /**
     * Logs a message with parameters at the {@code SUCCESS} level.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @param p3 parameter to the message.
     * @param p4 parameter to the message.
     * @param p5 parameter to the message.
     * @param p6 parameter to the message.
     * @param p7 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void success(final Marker marker, final String message, final Object p0, final Object p1, final Object p2,
            final Object p3, final Object p4, final Object p5, final Object p6,
            final Object p7) {
        logger.logIfEnabled(FQCN, SUCCESS, marker, message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    /**
     * Logs a message with parameters at the {@code SUCCESS} level.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @param p3 parameter to the message.
     * @param p4 parameter to the message.
     * @param p5 parameter to the message.
     * @param p6 parameter to the message.
     * @param p7 parameter to the message.
     * @param p8 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void success(final Marker marker, final String message, final Object p0, final Object p1, final Object p2,
            final Object p3, final Object p4, final Object p5, final Object p6,
            final Object p7, final Object p8) {
        logger.logIfEnabled(FQCN, SUCCESS, marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    /**
     * Logs a message with parameters at the {@code SUCCESS} level.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @param p3 parameter to the message.
     * @param p4 parameter to the message.
     * @param p5 parameter to the message.
     * @param p6 parameter to the message.
     * @param p7 parameter to the message.
     * @param p8 parameter to the message.
     * @param p9 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void success(final Marker marker, final String message, final Object p0, final Object p1, final Object p2,
            final Object p3, final Object p4, final Object p5, final Object p6,
            final Object p7, final Object p8, final Object p9) {
        logger.logIfEnabled(FQCN, SUCCESS, marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    /**
     * Logs a message at the {@code SUCCESS} level including the stack trace of
     * the {@link Throwable} {@code t} passed as parameter.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message to log.
     * @param t the exception to log, including its stack trace.
     */
    public void success(final Marker marker, final String message, final Throwable t) {
        logger.logIfEnabled(FQCN, SUCCESS, marker, message, t);
    }

    /**
     * Logs the specified Message at the {@code SUCCESS} level.
     *
     * @param msg the message string to be logged
     */
    public void success(final Message msg) {
        logger.logIfEnabled(FQCN, SUCCESS, null, msg, (Throwable) null);
    }

    /**
     * Logs the specified Message at the {@code SUCCESS} level.
     *
     * @param msg the message string to be logged
     * @param t A Throwable or null.
     */
    public void success(final Message msg, final Throwable t) {
        logger.logIfEnabled(FQCN, SUCCESS, null, msg, t);
    }

    /**
     * Logs a message object with the {@code SUCCESS} level.
     *
     * @param message the message object to log.
     */
    public void success(final Object message) {
        logger.logIfEnabled(FQCN, SUCCESS, null, message, (Throwable) null);
    }

    /**
     * Logs a message at the {@code SUCCESS} level including the stack trace of
     * the {@link Throwable} {@code t} passed as parameter.
     *
     * @param message the message to log.
     * @param t the exception to log, including its stack trace.
     */
    public void success(final Object message, final Throwable t) {
        logger.logIfEnabled(FQCN, SUCCESS, null, message, t);
    }

    /**
     * Logs a message CharSequence with the {@code SUCCESS} level.
     *
     * @param message the message CharSequence to log.
     * @since Log4j-2.6
     */
    public void success(final CharSequence message) {
        logger.logIfEnabled(FQCN, SUCCESS, null, message, (Throwable) null);
    }

    /**
     * Logs a CharSequence at the {@code SUCCESS} level including the stack trace of
     * the {@link Throwable} {@code t} passed as parameter.
     *
     * @param message the CharSequence to log.
     * @param t the exception to log, including its stack trace.
     * @since Log4j-2.6
     */
    public void success(final CharSequence message, final Throwable t) {
        logger.logIfEnabled(FQCN, SUCCESS, null, message, t);
    }

    /**
     * Logs a message object with the {@code SUCCESS} level.
     *
     * @param message the message object to log.
     */
    public void success(final String message) {
        logger.logIfEnabled(FQCN, SUCCESS, null, message, (Throwable) null);
    }

    /**
     * Logs a message with parameters at the {@code SUCCESS} level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param params parameters to the message.
     * @see #getMessageFactory()
     */
    public void success(final String message, final Object... params) {
        logger.logIfEnabled(FQCN, SUCCESS, null, message, params);
    }

    /**
     * Logs a message with parameters at the {@code SUCCESS} level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void success(final String message, final Object p0) {
        logger.logIfEnabled(FQCN, SUCCESS, null, message, p0);
    }

    /**
     * Logs a message with parameters at the {@code SUCCESS} level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void success(final String message, final Object p0, final Object p1) {
        logger.logIfEnabled(FQCN, SUCCESS, null, message, p0, p1);
    }

    /**
     * Logs a message with parameters at the {@code SUCCESS} level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void success(final String message, final Object p0, final Object p1, final Object p2) {
        logger.logIfEnabled(FQCN, SUCCESS, null, message, p0, p1, p2);
    }

    /**
     * Logs a message with parameters at the {@code SUCCESS} level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @param p3 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void success(final String message, final Object p0, final Object p1, final Object p2,
            final Object p3) {
        logger.logIfEnabled(FQCN, SUCCESS, null, message, p0, p1, p2, p3);
    }

    /**
     * Logs a message with parameters at the {@code SUCCESS} level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @param p3 parameter to the message.
     * @param p4 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void success(final String message, final Object p0, final Object p1, final Object p2,
            final Object p3, final Object p4) {
        logger.logIfEnabled(FQCN, SUCCESS, null, message, p0, p1, p2, p3, p4);
    }

    /**
     * Logs a message with parameters at the {@code SUCCESS} level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @param p3 parameter to the message.
     * @param p4 parameter to the message.
     * @param p5 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void success(final String message, final Object p0, final Object p1, final Object p2,
            final Object p3, final Object p4, final Object p5) {
        logger.logIfEnabled(FQCN, SUCCESS, null, message, p0, p1, p2, p3, p4, p5);
    }

    /**
     * Logs a message with parameters at the {@code SUCCESS} level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @param p3 parameter to the message.
     * @param p4 parameter to the message.
     * @param p5 parameter to the message.
     * @param p6 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void success(final String message, final Object p0, final Object p1, final Object p2,
            final Object p3, final Object p4, final Object p5, final Object p6) {
        logger.logIfEnabled(FQCN, SUCCESS, null, message, p0, p1, p2, p3, p4, p5, p6);
    }

    /**
     * Logs a message with parameters at the {@code SUCCESS} level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @param p3 parameter to the message.
     * @param p4 parameter to the message.
     * @param p5 parameter to the message.
     * @param p6 parameter to the message.
     * @param p7 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void success(final String message, final Object p0, final Object p1, final Object p2,
            final Object p3, final Object p4, final Object p5, final Object p6,
            final Object p7) {
        logger.logIfEnabled(FQCN, SUCCESS, null, message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    /**
     * Logs a message with parameters at the {@code SUCCESS} level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @param p3 parameter to the message.
     * @param p4 parameter to the message.
     * @param p5 parameter to the message.
     * @param p6 parameter to the message.
     * @param p7 parameter to the message.
     * @param p8 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void success(final String message, final Object p0, final Object p1, final Object p2,
            final Object p3, final Object p4, final Object p5, final Object p6,
            final Object p7, final Object p8) {
        logger.logIfEnabled(FQCN, SUCCESS, null, message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    /**
     * Logs a message with parameters at the {@code SUCCESS} level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param p0 parameter to the message.
     * @param p1 parameter to the message.
     * @param p2 parameter to the message.
     * @param p3 parameter to the message.
     * @param p4 parameter to the message.
     * @param p5 parameter to the message.
     * @param p6 parameter to the message.
     * @param p7 parameter to the message.
     * @param p8 parameter to the message.
     * @param p9 parameter to the message.
     * @see #getMessageFactory()
     * @since Log4j-2.6
     */
    public void success(final String message, final Object p0, final Object p1, final Object p2,
            final Object p3, final Object p4, final Object p5, final Object p6,
            final Object p7, final Object p8, final Object p9) {
        logger.logIfEnabled(FQCN, SUCCESS, null, message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    /**
     * Logs a message at the {@code SUCCESS} level including the stack trace of
     * the {@link Throwable} {@code t} passed as parameter.
     *
     * @param message the message to log.
     * @param t the exception to log, including its stack trace.
     */
    public void success(final String message, final Throwable t) {
        logger.logIfEnabled(FQCN, SUCCESS, null, message, t);
    }

    /**
     * Logs a message which is only to be constructed if the logging level is the {@code SUCCESS}level.
     *
     * @param msgSupplier A function, which when called, produces the desired log message;
     *            the format depends on the message factory.
     * @since Log4j-2.4
     */
    public void success(final Supplier<?> msgSupplier) {
        logger.logIfEnabled(FQCN, SUCCESS, null, msgSupplier, (Throwable) null);
    }

    /**
     * Logs a message (only to be constructed if the logging level is the {@code SUCCESS}
     * level) including the stack trace of the {@link Throwable} <code>t</code> passed as parameter.
     *
     * @param msgSupplier A function, which when called, produces the desired log message;
     *            the format depends on the message factory.
     * @param t the exception to log, including its stack trace.
     * @since Log4j-2.4
     */
    public void success(final Supplier<?> msgSupplier, final Throwable t) {
        logger.logIfEnabled(FQCN, SUCCESS, null, msgSupplier, t);
    }

    /**
     * Logs a message which is only to be constructed if the logging level is the
     * {@code SUCCESS} level with the specified Marker.
     *
     * @param marker the marker data specific to this log statement
     * @param msgSupplier A function, which when called, produces the desired log message;
     *            the format depends on the message factory.
     * @since Log4j-2.4
     */
    public void success(final Marker marker, final Supplier<?> msgSupplier) {
        logger.logIfEnabled(FQCN, SUCCESS, marker, msgSupplier, (Throwable) null);
    }

    /**
     * Logs a message with parameters which are only to be constructed if the logging level is the
     * {@code SUCCESS} level.
     *
     * @param marker the marker data specific to this log statement
     * @param message the message to log; the format depends on the message factory.
     * @param paramSuppliers An array of functions, which when called, produce the desired log message parameters.
     * @since Log4j-2.4
     */
    public void success(final Marker marker, final String message, final Supplier<?>... paramSuppliers) {
        logger.logIfEnabled(FQCN, SUCCESS, marker, message, paramSuppliers);
    }

    /**
     * Logs a message (only to be constructed if the logging level is the {@code SUCCESS}
     * level) with the specified Marker and including the stack trace of the {@link Throwable}
     * <code>t</code> passed as parameter.
     *
     * @param marker the marker data specific to this log statement
     * @param msgSupplier A function, which when called, produces the desired log message;
     *            the format depends on the message factory.
     * @param t A Throwable or null.
     * @since Log4j-2.4
     */
    public void success(final Marker marker, final Supplier<?> msgSupplier, final Throwable t) {
        logger.logIfEnabled(FQCN, SUCCESS, marker, msgSupplier, t);
    }

    /**
     * Logs a message with parameters which are only to be constructed if the logging level is
     * the {@code SUCCESS} level.
     *
     * @param message the message to log; the format depends on the message factory.
     * @param paramSuppliers An array of functions, which when called, produce the desired log message parameters.
     * @since Log4j-2.4
     */
    public void success(final String message, final Supplier<?>... paramSuppliers) {
        logger.logIfEnabled(FQCN, SUCCESS, null, message, paramSuppliers);
    }

    /**
     * Logs a message which is only to be constructed if the logging level is the
     * {@code SUCCESS} level with the specified Marker. The {@code MessageSupplier} may or may
     * not use the {@link MessageFactory} to construct the {@code Message}.
     *
     * @param marker the marker data specific to this log statement
     * @param msgSupplier A function, which when called, produces the desired log message.
     * @since Log4j-2.4
     */
    public void success(final Marker marker, final MessageSupplier msgSupplier) {
        logger.logIfEnabled(FQCN, SUCCESS, marker, msgSupplier, (Throwable) null);
    }

    /**
     * Logs a message (only to be constructed if the logging level is the {@code SUCCESS}
     * level) with the specified Marker and including the stack trace of the {@link Throwable}
     * <code>t</code> passed as parameter. The {@code MessageSupplier} may or may not use the
     * {@link MessageFactory} to construct the {@code Message}.
     *
     * @param marker the marker data specific to this log statement
     * @param msgSupplier A function, which when called, produces the desired log message.
     * @param t A Throwable or null.
     * @since Log4j-2.4
     */
    public void success(final Marker marker, final MessageSupplier msgSupplier, final Throwable t) {
        logger.logIfEnabled(FQCN, SUCCESS, marker, msgSupplier, t);
    }

    /**
     * Logs a message which is only to be constructed if the logging level is the
     * {@code SUCCESS} level. The {@code MessageSupplier} may or may not use the
     * {@link MessageFactory} to construct the {@code Message}.
     *
     * @param msgSupplier A function, which when called, produces the desired log message.
     * @since Log4j-2.4
     */
    public void success(final MessageSupplier msgSupplier) {
        logger.logIfEnabled(FQCN, SUCCESS, null, msgSupplier, (Throwable) null);
    }

    /**
     * Logs a message (only to be constructed if the logging level is the {@code SUCCESS}
     * level) including the stack trace of the {@link Throwable} <code>t</code> passed as parameter.
     * The {@code MessageSupplier} may or may not use the {@link MessageFactory} to construct the
     * {@code Message}.
     *
     * @param msgSupplier A function, which when called, produces the desired log message.
     * @param t the exception to log, including its stack trace.
     * @since Log4j-2.4
     */
    public void success(final MessageSupplier msgSupplier, final Throwable t) {
        logger.logIfEnabled(FQCN, SUCCESS, null, msgSupplier, t);
    }
}

