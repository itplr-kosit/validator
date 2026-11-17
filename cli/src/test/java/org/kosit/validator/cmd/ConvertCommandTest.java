package org.kosit.validator.cmd;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.validator.api.ScenarioSet;
import org.kosit.validator.impl.Scenario;
import org.kosit.validator.testdata.TestResources;
import org.kost.validator.api.saxon.ProcessorProvider;

/**
 * The {@code convert} subcommand: a configuration of version 2 becomes one of version 3, and the coordinates version 3
 * requires but version 2 was allowed to leave off are proposed instead of being settled on.
 *
 * @author Andreas Schmitz
 */
public class ConvertCommandTest {

    private final Path output = Paths.get("target/test-output");

    private picocli.CommandLine commandLine;

    private TestWriter testWriter;

    @BeforeEach
    public void setup() throws IOException {
        this.testWriter = new TestWriter();
        final CommandLineOptions options = new CommandLineOptions();
        this.commandLine = new picocli.CommandLine(options);
        this.commandLine.setOut(new PrintWriter(this.testWriter.getOutWriter(), true));
        this.commandLine.setErr(new PrintWriter(this.testWriter.getErrWriter(), true));
        Printer.configure(new PrintWriter(this.testWriter.getOutWriter(), true), new PrintWriter(this.testWriter.getErrWriter(), true));
        Files.createDirectories(this.output);
        // a result of an earlier run would make "nothing was written" pass for the wrong reason
        try ( java.util.stream.Stream<Path> left = Files.list(this.output) ) {
            for (final Path file : left.filter(p -> p.getFileName().toString().endsWith(".xml")).toList()) {
                Files.delete(file);
            }
        }
    }

    @AfterEach
    public void cleanup() {
        Printer.reset();
    }

    /** Runs the subcommand and returns what it returned - a subcommand keeps its result on its own command line. */
    private ReturnValue convert(final String... args) {
        this.commandLine.execute(args);
        return this.commandLine.getParseResult().subcommand().commandSpec().commandLine().getExecutionResult();
    }

    @Test
    public void testAConfigurationThatStatesItsCoordinatesIsConverted() throws Exception {
        final Path target = this.output.resolve("converted-complete.xml");

        final ReturnValue result = convert("convert", "-s", Paths.get(TestResources.Simple.SCENARIOS_WRITTEN_FOR_2_0).toString(), "-o",
                target.toString());

        assertThat(result.getCode()).isZero();
        assertThat(this.testWriter.getOutput()).contains("Converted 1 scenario(s)").doesNotContain("proposed");
        final String written = Files.readString(target, StandardCharsets.UTF_8);
        assertThat(written).contains("urn:kosit:validator:scenario:3").contains("<artifactId>simple</artifactId>");

        // the real test of a conversion: the result is a configuration the engine loads, with the identity unchanged
        final Scenario converted = ScenarioSet.load(target.toUri(), TestResources.Simple.REPOSITORY_URI)
                .build(ProcessorProvider.getProcessor()).getScenarios().get(0);
        assertThat(converted.getName()).isEqualTo("Simple");
        assertThat(converted.getDeclaredId()).isEqualTo("org.kosit.validator.test:simple:1.0.0");
    }

    @Test
    public void testMissingCoordinatesAreProposedAndNothingIsWritten() {
        final Path target = this.output.resolve("not-written.xml");

        final ReturnValue result = convert("convert", "-s", Paths.get(TestResources.Simple.SCENARIOS_WITHOUT_IDS).toString(), "-o",
                target.toString());

        // one per scenario and one per artifact, and the exit code says how many
        assertThat(result.getCode()).isEqualTo(3);
        assertThat(this.testWriter.getOutput()).contains("leaves 3 coordinate(s) off")
                .contains("the scenario 'Simple' -> your-group-id:simple:1.0.0")
                .contains("the artifact 'simple.xsd' -> your-group-id:simple-xsd:1.0.0");
        assertThat(this.testWriter.getErrorOutput()).contains("Pass --group-id");
        assertThat(target).doesNotExist();
    }

    @Test
    public void testProposalsAreWrittenOnlyWhenTheyAreAccepted() throws Exception {
        final Path target = this.output.resolve("converted-proposed.xml");
        final String source = Paths.get(TestResources.Simple.SCENARIOS_WITHOUT_IDS).toString();

        // a group id alone is not enough - the proposals still have to be accepted
        assertThat(convert("convert", "-s", source, "-o", target.toString(), "-g", "de.example").getCode()).isEqualTo(3);
        assertThat(this.testWriter.getErrorOutput()).contains("--accept-proposals");
        assertThat(target).doesNotExist();

        final ReturnValue result = convert("convert", "-s", source, "-o", target.toString(), "-g", "de.example", "--accept-proposals");

        assertThat(result.getCode()).isZero();
        assertThat(this.testWriter.getOutput()).contains("3 proposed coordinate(s) were written in");
        final Scenario converted = ScenarioSet.load(target.toUri(), TestResources.Simple.REPOSITORY_URI)
                .build(ProcessorProvider.getProcessor()).getScenarios().get(0);
        assertThat(converted.getDeclaredId()).isEqualTo("de.example:simple:1.0.0");
    }

    @Test
    public void testAConfigurationThatIsAlreadyVersion3IsRefused() {
        final Path target = this.output.resolve("not-written-v3.xml");

        final ReturnValue result = convert("convert", "-s", Paths.get(TestResources.Simple.SCENARIOS_V3).toString(), "-o",
                target.toString());

        assertThat(result.getCode()).isNegative();
        assertThat(this.testWriter.getErrorOutput()).contains("is already written in version 3");
        assertThat(target).doesNotExist();
    }
}
