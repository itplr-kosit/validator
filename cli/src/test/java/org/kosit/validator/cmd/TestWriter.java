package org.kosit.validator.cmd;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

/**
 * Helper that captures picocli/Printer output into in-memory writers for assertions.
 */
public class TestWriter {

    private StringWriter outWriter = new StringWriter();

    private StringWriter errWriter = new StringWriter();

    public StringWriter getOutWriter() {
        return outWriter;
    }

    public StringWriter getErrWriter() {
        return errWriter;
    }

    public String getOutput() {
        return outWriter.toString();
    }

    public String getErrorOutput() {
        return errWriter.toString();
    }

    public List<String> getOutputLines() {
        return Arrays.stream(getOutput().split("\\R")).toList();
    }

    public List<String> getErrorOutputLines() {
        return Arrays.stream(getErrorOutput().split("\\R")).toList();
    }
}
