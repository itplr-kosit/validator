package org.kosit.validator.cmd;

import org.jboss.logmanager.ExtHandler;
import org.jboss.logmanager.formatters.PatternFormatter;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * In Quarkus, CLI log output is no longer emitted via {@code System.err}, but exclusively via the JBoss LogManager. To
 * keep Picocli and Validator messages testable, a custom log handler captures all LogRecord objects and provides the
 * fully formatted output for assertions.
 */
public class LogCaptureHandler extends ExtHandler {

    private final PatternFormatter formatter = new PatternFormatter("%d{HH:mm:ss} %-5p [%c] %s%e%n");

    private final StringWriter buffer = new StringWriter();

    private final PrintWriter writer = new PrintWriter(buffer, true);

    public LogCaptureHandler(Level level) {
        setLevel(level);
    }

    @Override
    public void publish(LogRecord record) {
        writer.println(formatter.format(record));
    }

    @Override
    public void flush() {
        writer.flush();
    }

    @Override
    public void close() {
        writer.flush();
    }

    public String getLogs() {
        writer.flush();
        return buffer.toString();
    }
}