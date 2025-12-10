package org.kosit.validator.cmd;

import org.jboss.logmanager.ExtHandler;
import org.jboss.logmanager.formatters.PatternFormatter;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * In Quarkus werden CLI-Logausgaben nicht mehr über {@code System.err} ausgegeben, sondern ausschließlich über den
 * JBoss LogManager. Damit Picocli- und Validator-Meldungen weiterhin testbar bleiben, fängt ein eigener Log-Handler
 * alle LogRecord-Objekte ab und stellt die vollständig formatierte Ausgabe für Assertions bereit.
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