/*
 * Copyright 2017-2022  Koordinierungsstelle für IT-Standards (KoSIT)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kosit.validator.cmd;

import static org.kosit.validator.impl.Helper.ASSERTIONS;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.commons.io.FileUtils;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Condition;
import lombok.extern.slf4j.Slf4j;

import org.jboss.logmanager.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kosit.validator.impl.Helper.Simple;

/**
 * Testet die Parameter des Kommandozeilen-Tools.
 * 
 * @author Andreas Penski
 */
@QuarkusTest
@Slf4j
public class CommandlineApplicationTest {

    public static final String RESULT_OUTPUT = "Processing 1 object(s) completed";

    private final Path output = Paths.get("target/test-output");

    picocli.CommandLine commandLine;

    TestWriter testWriter;

    LogCaptureHandler logCapture;

    @Inject
    @TopCommand
    CommandLineOptions options;

    private static void checkForHelp(final List<String> outputLines) {
        assertThat(outputLines.size()).isPositive();
        assertThat(outputLines.stream().filter(l -> l.startsWith("Usage:"))).hasSize(1);
    }

    @BeforeEach
    public void setup() throws IOException {
        testWriter = new TestWriter();
        // Picocli Ausgabe
        commandLine = new picocli.CommandLine(options);
        commandLine.setOut(new PrintWriter(testWriter.getOutWriter()));
        commandLine.setErr(new PrintWriter(testWriter.getErrWriter()));
        System.setIn(new InputStream() {

            @Override
            public int read() throws IOException {
                return 0;
            }
        });
        // Printer Ausgabe
        Printer.configure(new PrintWriter(testWriter.getOutWriter(), true), new PrintWriter(testWriter.getErrWriter(), true));

        // Log capture
        Logger root = Logger.getLogger("");
        logCapture = new LogCaptureHandler(Level.ALL);
        root.addHandler(logCapture);

        if (Files.exists(this.output)) {
            FileUtils.cleanDirectory(this.output.toFile());
        }
        TypeConverter.counter.clear();
    }

    @AfterEach
    public void cleanup() throws IOException {
        Files.list(Paths.get("")).filter(p -> p.getFileName().toString().endsWith("-report.xml")).forEach(path -> {
            try {
                Files.delete(path);
            } catch (final IOException e) {
                log.error("Error deleting file", e);
            }
        });
        Printer.reset();
        Logger.getLogger("").removeHandler(logCapture);
    }

    @Test
    public void testHelp() {
        final String[] args = { "-?" };
        commandLine.execute(args);

        assertThat(testWriter.getErrorOutput()).isEmpty();
        checkForHelp(testWriter.getOutputLines());
    }

    @Test
    public void testNoArguments() {
        final String[] args = {};
        commandLine.execute(args);
        assertThat(testWriter.getErrorOutput()).isNotEmpty();
        checkForHelp(testWriter.getErrorOutputLines());
    }

    @Test
    public void testRequiredScenarioFile() {
        final String[] args = { "arguments", "egal welche", "argumente drin sind" };
        commandLine.execute(args);
        assertThat(testWriter.getErrorOutput()).isNotEmpty();
        assertThat(testWriter.getErrorOutput()).contains("Missing required option: '--scenarios");
    }

    @Test
    public void testNotExistingScenarioFile() {
        final String[] args = { "-s", Paths.get(Simple.NOT_EXISTING).toString(), Paths.get(Simple.NOT_EXISTING).toString() };
        commandLine.execute(args);
        assertThat(testWriter.getErrorOutput()).isNotEmpty();
        assertThat(testWriter.getErrorOutput()).contains("Not a valid path for scenario definition specified");
    }

    @Test
    public void testIncorrectRepository() {
        final String[] args = { "-s", Paths.get(Simple.SCENARIOS).toString(), "-r", Paths.get(Simple.NOT_EXISTING).toString(),
                Paths.get(Simple.SIMPLE_VALID).toString() };
        commandLine.execute(args);
        assertThat(testWriter.getErrorOutput()).isNotEmpty();
        assertThat(testWriter.getErrorOutput()).contains("Not a valid path for repository");
    }

    @Test
    public void testNotExistingTestTarget() {
        final String[] args = { "-s", Paths.get(Simple.SCENARIOS).toString(), "-r", Paths.get(Simple.REPOSITORY_URI).toString(),
                Paths.get(Simple.NOT_EXISTING).toString() };
        commandLine.execute(args);
        assertThat(testWriter.getErrorOutput()).isNotEmpty();
        assertThat(testWriter.getErrorOutput()).contains("No test targets found");
    }

    @Test
    public void testValidMinimalConfiguration() {
        final String[] args = { "-s", Paths.get(Simple.SCENARIOS).toString(), "-r", Paths.get(Simple.REPOSITORY_URI).toString(),
                Paths.get(Simple.SIMPLE_VALID).toString() };
        commandLine.execute(args);
        assertThat(logCapture.getLogs()).contains(RESULT_OUTPUT);
    }

    @Test
    public void testValidMinimalConfigurationWithoutRepositoryPath() {
        final String[] args = { "-s", Paths.get(Simple.SCENARIOS_WITH_RELATIVE_PATHS).toString(),
                Paths.get(Simple.SIMPLE_VALID).toString() };
        commandLine.execute(args);
        assertThat(logCapture.getLogs()).contains(RESULT_OUTPUT);
    }

    @Test
    public void testValidMultipleConfigurations() {
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS_WITH_MANY_CONFIGS).toString(), "-r",
                Paths.get(Simple.REPOSITORY_URI).toString(), Paths.get(Simple.SIMPLE_VALID).toString() };
        commandLine.execute(args);
        assertThat(logCapture.getLogs()).contains(RESULT_OUTPUT);
    }

    @Test
    public void testValidNamingConfiguration() {
        final String[] args = { "-s", Paths.get(Simple.SCENARIOS).toString(), "-r", Paths.get(Simple.REPOSITORY_URI).toString(),
                Paths.get(Simple.SIMPLE_VALID).toString(), "--report-prefix", "somePrefix", "--report-postfix", "somePostfix" };
        commandLine.execute(args);
        assertThat(logCapture.getLogs()).contains(RESULT_OUTPUT);
        assertThat(logCapture.getLogs()).contains("somePrefix-simple-somePostfix");
    }

    @Test
    public void testValidMultipleInput() {
        final String[] args = { "-s", Paths.get(Simple.SCENARIOS).toString(), "-o", this.output.toString(), "-r",
                Paths.get(Simple.REPOSITORY_URI).toString(), Paths.get(Simple.SIMPLE_VALID).toString(), Paths.get(Simple.FOO).toString() };
        commandLine.execute(args);
        assertThat(logCapture.getLogs()).contains("Processing 2 object(s) completed");
    }

    @Test
    public void testValidDirectoryInput() {
        final String[] args = { "-s", Paths.get(Simple.SCENARIOS).toString(), "-o", this.output.toString(), "-r",
                Paths.get(Simple.REPOSITORY_URI).toString(), Paths.get(Simple.EXAMPLES).toString() };
        commandLine.execute(args);
        assertThat(logCapture.getLogs()).contains("Processing 8 object(s) completed");
    }

    @Test
    public void testValidOutputConfiguration() throws IOException {

        final String[] args = { "-s", Paths.get(Simple.SCENARIOS).toString(), "-o", this.output.toString(), "-r",
                Paths.get(Simple.REPOSITORY_URI).toString(), Paths.get(Simple.SIMPLE_VALID).toString() };
        commandLine.execute(args);
        assertThat(logCapture.getLogs()).contains(RESULT_OUTPUT);
        assertThat(this.output).exists();
        assertThat(Files.list(this.output)).hasSize(1);
    }

    @Test
    public void testNoInput() {
        final String[] args = { "-s", Paths.get(Simple.SCENARIOS).toString(), "-r", Paths.get(Simple.REPOSITORY_URI).toString(), };
        commandLine.execute(args);
        assertThat((ReturnValue) commandLine.getExecutionResult()).isEqualTo(ReturnValue.CONFIGURATION_ERROR);
        assertThat(testWriter.getErrorOutput()).contains("No test target found");
    }

    @Test
    public void testPrint() {

        final String[] args = { "-s", Paths.get(Simple.SCENARIOS).toString(), "-p", "-r", Paths.get(Simple.REPOSITORY_URI).toString(), "-o",
                this.output.toString(), Paths.get(Simple.SIMPLE_VALID).toString() };
        commandLine.execute(args);
        assertThat(logCapture.getLogs()).contains(RESULT_OUTPUT);
        assertThat(testWriter.getOutputLines()).haveAtLeastOne(new Condition<>(
                s -> StringUtils.contains(s, "<?xml version=\"1.0\" " + "encoding=\"UTF-8\"?>"), "Must " + "contain xml preambel"));
    }

    @Test
    public void testExtraktion() throws IOException {
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-e", "-o",
                this.output.toAbsolutePath().toString(), "-r", Paths.get(Simple.REPOSITORY_URI).toString(),
                Paths.get(Simple.SIMPLE_VALID).toString() };
        commandLine.execute(args);
        assertThat(logCapture.getLogs()).contains(RESULT_OUTPUT);
        assertThat(Files.list(this.output).filter(f -> f.toString().endsWith(".xml")).count()).isPositive();
    }

    @Test
    public void testMultipleExtraktion() throws IOException {
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS_WITH_MANY_CONFIGS).toString(), "-e", "-o",
                this.output.toAbsolutePath().toString(), "-r", Paths.get(Simple.REPOSITORY_URI).toString(),
                Paths.get(Simple.SIMPLE_VALID).toString() };
        commandLine.execute(args);
        assertThat(logCapture.getLogs()).contains(RESULT_OUTPUT);
        assertThat(Files.list(this.output).filter(f -> f.toString().endsWith(".xml")).count()).isPositive();
    }

    @Test
    public void testDebugFlag() {
        final String[] args = { "-s", Paths.get(Simple.SCENARIOS).toString(), "-r", "unknown", "-o", this.output.toString(), "-d",
                Paths.get(ASSERTIONS).toString() };
        commandLine.execute(args);
        assertThat(logCapture.getLogs()).contains("at org.kosit.validator");
    }

    @Test
    public void testPrintMemoryStats() {
        final String[] args = { "-m", "-s", Paths.get(Simple.SCENARIOS).toString(), "-r", Paths.get(Simple.REPOSITORY_URI).toString(),
                Paths.get(Simple.SIMPLE_VALID).toString() };
        commandLine.execute(args);
        assertThat(logCapture.getLogs()).contains(RESULT_OUTPUT);
        assertThat(logCapture.getLogs()).contains("total");
    }

    @Test
    public void testReadFromPipe() throws IOException {
        final String[] args = { "-s", Paths.get(Simple.SCENARIOS).toString(), "-r", Paths.get(Simple.REPOSITORY_URI).toString() };
        System.setIn(Files.newInputStream(Paths.get(Simple.SIMPLE_VALID)));
        commandLine.execute(args);
        assertThat(logCapture.getLogs()).contains(RESULT_OUTPUT);
    }

    @Test
    public void testParsingError() {
        final String[] args = { "-s", "-r", Paths.get(Simple.REPOSITORY_URI).toString(), Paths.get(Simple.SIMPLE_VALID).toString() };
        commandLine.execute(args);
        assertThat(testWriter.getErrorOutput()).contains("Expected parameter for option");
    }

    @Test
    public void loadMultipleScenarios() {
        final String[] args = { "-s", "s1=" + Paths.get(Simple.SCENARIOS).toString(), "-s",
                "s2=" + Paths.get(Simple.OTHER_SCENARIOS).toString(), "-r", "s1=" + Paths.get(Simple.REPOSITORY_URI).toString(), "-r",
                "s2=" + Paths.get(Simple.REPOSITORY_URI).toString(), Paths.get(Simple.SIMPLE_VALID).toString() };
        commandLine.execute(args);
        assertThat(testWriter.getOutput()).contains("Processing of 1 object(s) completed");
    }

    @Test
    public void loadMultipleScenariosSingleRepository() {
        final String[] args = { "-s", "s1=" + Paths.get(Simple.SCENARIOS).toString(), "-s",
                "s2=" + Paths.get(Simple.OTHER_SCENARIOS).toString(), "-r", Paths.get(Simple.REPOSITORY_URI).toString(),
                Paths.get(Simple.SIMPLE_VALID).toString() };
        commandLine.execute(args);
        assertThat(testWriter.getOutput()).contains("Processing of 1 object(s) completed");
    }

    @Test
    public void loadMultipleScenariosMissingRepository() {
        final String[] args = { "-s", "s1=" + Paths.get(Simple.SCENARIOS).toString(), "-s",
                "s2=" + Paths.get(Simple.OTHER_SCENARIOS).toString(), "-r", "s1=" + Paths.get(Simple.REPOSITORY_URI).toString(), "-r",
                "typo=" + Paths.get(Simple.REPOSITORY_URI).toString(), Paths.get(Simple.SIMPLE_VALID).toString() };
        commandLine.execute(args);
        assertThat(logCapture.getLogs()).contains("No repository location for scenario definition 's2' specified");
    }

    @Test
    public void loadMultipleOrderedScenarios() {
        final String[] args = { "-s", Paths.get(Simple.SCENARIOS).toString(), "-s", Paths.get(Simple.OTHER_SCENARIOS).toString(), "-r",
                Paths.get(Simple.REPOSITORY_URI).toString(), "-r", Paths.get(Simple.REPOSITORY_URI).toString(),
                Paths.get(Simple.SIMPLE_VALID).toString() };
        commandLine.execute(args);
        assertThat(testWriter.getOutput()).contains("Processing of 1 object(s) completed");
    }

    @Test
    public void checkUnusedRepository() {
        final String[] args = { "-s", "s1=" + Paths.get(Simple.SCENARIOS).toString(), "-r",
                "s1=" + Paths.get(Simple.REPOSITORY_URI).toString(), "-r", "unused=" + Paths.get(Simple.REPOSITORY_URI).toString(),
                Paths.get(Simple.SIMPLE_VALID).toString() };
        commandLine.execute(args);
        assertThat(testWriter.getOutput()).contains("Processing of 1 object(s) completed");
        assertThat(testWriter.getErrorOutput()).contains("Warning: repository definition \"unused\" is not used");
    }

    @Test
    public void checkDuplicationScenarioDefinition() {
        final String[] args = { "-s", "s1=" + Paths.get(Simple.SCENARIOS).toString(), "-r",
                "s1=" + Paths.get(Simple.REPOSITORY_URI).toString(), "-r", "unused=" + Paths.get(Simple.REPOSITORY_URI).toString(),
                Paths.get(Simple.SIMPLE_VALID).toString() };
        commandLine.execute(args);
        assertThat(testWriter.getOutput()).contains("Processing of 1 object(s) completed");
        assertThat(testWriter.getErrorOutput()).contains("Warning: repository definition \"unused\" is not used");
    }
}
