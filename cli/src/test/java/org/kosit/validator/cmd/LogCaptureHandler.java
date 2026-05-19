package org.kosit.validator.cmd;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Captures slf4j-simple log output by tailing the configured log file. Surefire is configured to set
 * {@code org.slf4j.simpleLogger.logFile} to a file path, into which slf4j-simple writes all log output. Tests record
 * the file size on construction and return only the content appended thereafter.
 */
public class LogCaptureHandler {

    private static final Logger log = LoggerFactory.getLogger(LogCaptureHandler.class);

    private final Path logFile;

    private final long startPos;

    public LogCaptureHandler(final Path logFile) {
        this.logFile = logFile;
        this.startPos = currentSize(logFile);
    }

    private static long currentSize(final Path p) {
        try {
            return Files.exists(p) ? Files.size(p) : 0L;
        } catch (final IOException e) {
            log.warn("Unable to determine size of log file {}", p, e);
            return 0L;
        }
    }

    public String getLogs() {
        try {
            if (!Files.exists(logFile)) {
                return "";
            }
            final byte[] all = Files.readAllBytes(logFile);
            if (all.length <= startPos) {
                return "";
            }
            return new String(all, (int) startPos, (int) (all.length - startPos), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            log.warn("Unable to read log file {}", logFile, e);
            return "";
        }
    }
}
