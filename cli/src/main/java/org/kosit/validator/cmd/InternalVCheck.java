package org.kosit.validator.cmd;

import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.conformatron.api.model.source.CTReadResource;
import org.fusesource.jansi.AnsiRenderer.Code;
import org.kosit.validator.api.VConfiguration;
import org.kosit.validator.api.VResult;
import org.kosit.validator.api.xmlerror.XmlError;
import org.kosit.validator.cmd.report.Grid;
import org.kosit.validator.cmd.report.Grid.ColumnDefinition;
import org.kosit.validator.cmd.report.Justify;
import org.kosit.validator.cmd.report.Line;
import org.kosit.validator.impl.DefaultVCheck;
import org.kosit.validator.impl.EngineInformation;
import org.kosit.validator.impl.tasks.CheckTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.Processor;

/**
 * Simple extension of the {@link DefaultVCheck} class to evaluate and output the result of the assertion check. This
 * class does not represent a functional extension of the actual validation process!
 *
 * @author Andreas Penski
 */
@Deprecated(since = "2.0.0", forRemoval = true)
class InternalVCheck extends DefaultVCheck {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalVCheck.class);

    private final int checkAssertions = 0;

    private final int failedAssertions = 0;

    /**
     * Creates a new instance with the given configuration.
     *
     * @param configuration the configuration
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    InternalVCheck(final EngineInformation engineInformation, final Processor processor, final VConfiguration... configuration) {
        super(engineInformation, processor, configuration);
    }

    private static String createStatusLine(final Map<String, VResult> results) {
        final long acceptable = results.entrySet().stream().filter(e -> e.getValue().isAcceptable()).count();
        final long rejected = results.entrySet().stream().filter(e -> !e.getValue().isAcceptable()).count();
        final long errors = results.entrySet().stream().filter(e -> !e.getValue().isProcessingSuccessful()).count();
        final Line line = new Line();
        line.add("Acceptable: ").add(acceptable, Code.GREEN);
        line.add(" Rejected: ").add(rejected, Code.RED);
        if (errors > 0) {
            line.add(" Processing errors: ").add(errors, Code.RED);
        }
        return line.render(true, false);
    }

    private static Grid createResultGrid(final Map<String, VResult> results) {
        final Grid grid = new Grid(
        //@formatter:off
        new ColumnDefinition("File", 60, 10, 1), new ColumnDefinition("Schema", 7).justify(Justify.CENTER), new ColumnDefinition("Schematron", 10).justify(Justify.CENTER), new ColumnDefinition("Acceptance", 10, 5).justify(Justify.CENTER), new ColumnDefinition("Error/Description", 60, 20, 3));
        //@formatter:on
        results.entrySet().stream().sorted(Entry.comparingByKey()).forEach(e -> {
            final VResult value = e.getValue();
            final Code textcolor = value.isAcceptable() ? Code.GREEN : Code.RED;
            grid.addCell(e.getKey(), textcolor);
            grid.addCell(value.isSchemaValid() ? "Y" : "N", textcolor);
            grid.addCell(value.isSchematronValid() ? "Y" : "N", textcolor);
            grid.addCell(value.getAcceptRecommendation(), textcolor);
            grid.addCell(joinErrors(value));
        });
        return grid;
    }

    private static String joinErrors(final VResult value) {
        final StringBuilder b = new StringBuilder();
        b.append(String.join(";", value.getProcessingErrors()));
        if (value.getSchemaViolations() != null && !value.getSchemaViolations().isEmpty()) {
            b.append(b.length() > 0 ? ";" : "");
            b.append(value.getSchemaViolations().stream().map(XmlError::getMessage).collect(Collectors.joining(";")));
        }
        if (value.getSchematronResult() != null && !value.getSchematronResult().isEmpty()) {
            b.append(b.length() > 0 ? ";" : "");
            b.append(value.getSchematronResult().stream().flatMap(e -> e.getMessages().stream()).collect(Collectors.joining(";")));
        }
        return b.toString();
    }

    /**
     * Validates the test documents and outputs information about any assertions.
     *
     * @param input the test documents
     * @return false if there are assertion errors, otherwise true
     */
    @Deprecated(since = "2.0.0", forRemoval = true)
    @Override
    public VResult checkInput(final CTReadResource input) {
        final CheckTask.Process process = new CheckTask.Process(input, createXVRLMetadata());
        return runCheckInternal(process);
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    void printResults(final Map<String, VResult> results) {
        final PrintWriter writer = new PrintWriter(System.out); // NOSONAR
        writer.write("Results:\n");
        writer.write(createResultGrid(results).render());
        writer.write(createStatusLine(results));
        writer.write(createAssertionStatus());
        writer.flush();
    }

    private String createAssertionStatus() {
        final Line line = new Line();
        if (this.failedAssertions > 0) {
            LOGGER.error("Assertion check failed.\n\nAssertions run: {}, Assertions failed: {}\n", this.checkAssertions,
                    this.failedAssertions);
            line.add(MessageFormat.format("Assertions run: {0}, Assertions failed: ", this.checkAssertions));
            line.add(this.failedAssertions, Code.RED);
        } else if (this.checkAssertions > 0) {
            LOGGER.info("Assertion check successful.\n\nAssertions run: {}, Assertions failed: {}\n", this.checkAssertions,
                    this.failedAssertions);
            line.add(MessageFormat.format("Assertions run: {0}, Assertions failed: {1}", this.checkAssertions, this.failedAssertions));
        }
        return line.render(true, false);
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    @Override
    public boolean isSuccessful(final Map<String, VResult> results) {
        if (this.checkAssertions > 0) {
            return this.failedAssertions == 0;
        }
        return super.isSuccessful(results);
    }

    @Deprecated(since = "2.0.0", forRemoval = true)
    public int getNotAcceptableCount(final Map<String, VResult> results) {
        return (int) (this.failedAssertions + results.values().stream().filter(e -> !e.isAcceptable()).count());
    }
}
