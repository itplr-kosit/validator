package org.kosit.validator.cmd;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

/**
 * Mit dem Umschwenken auf QuarkusApp für CDI integration stellt das generelle 'Umbiegen' der Outputs ein Problem dar.
 * Daher wird in den Tests nur die Commandline.execute getestst, in welcher mit dieser Klasse der Output ableiten lässt,
 * ohne die Quarkus-Writing-Mechanismen zu beeinflussen.
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
