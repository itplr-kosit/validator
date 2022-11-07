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

package de.kosit.validationtool.cmd;

import static de.kosit.validationtool.impl.Helper.ASSERTIONS;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Condition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.impl.Helper.Simple;

/**
 * Testet die Parameter des Kommandozeilen-Tools.
 * 
 * @author Andreas Penski
 */
@Slf4j
public class CommandlineApplicationTest {

    public static final String RESULT_OUTPUT = "Processing 1 object(s) completed";

    private CommandLine commandLine;

    private final Path output = Paths.get("target/test-output");

    @Before
    public void setup() throws IOException {
        this.commandLine = new CommandLine();
        CommandLine.activate();
        if (Files.exists(this.output)) {
            FileUtils.deleteDirectory(this.output.toFile());
        }
        TypeConverter.counter.clear();
    }

    @After
    public void cleanup() throws IOException {
        Files.list(Paths.get("")).filter(p -> p.getFileName().toString().endsWith("-report.xml")).forEach(path -> {
            try {
                Files.delete(path);
            } catch (final IOException e) {
                log.error("Error deleting file", e);
            }
        });
        CommandLine.deactivate();
    }

    @Test
    public void testHelp() {
        final String[] args = new String[] { "-?" };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).isEmpty();
        checkForHelp(CommandLine.getOutputLines());
    }

    private static void checkForHelp(final List<String> outputLines) {
        assertThat(outputLines.size()).isPositive();
        assertThat(outputLines.stream().filter(l -> l.startsWith("Usage: KoSIT Validator"))).hasSize(1);
    }

    @Test
    public void testRequiredScenarioFile() {
        final String[] args = new String[] { "arguments", "egal welche", "argumente drin sind" };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).isNotEmpty();
        assertThat(CommandLine.getErrorOutput()).contains("Missing required option: '--scenarios");
    }

    @Test
    public void testNotExistingScenarioFile() {
        final String[] args = new String[] { "-s", Paths.get(Simple.NOT_EXISTING).toString(), Paths.get(Simple.NOT_EXISTING).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).isNotEmpty();
        assertThat(CommandLine.getErrorOutput()).contains("Not a valid path for scenario definition specified");
    }

    @Test
    public void testIncorrectRepository() {
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-r", Paths.get(Simple.NOT_EXISTING).toString(),
                Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).isNotEmpty();
        assertThat(CommandLine.getErrorOutput()).contains("Not a valid path for repository");
    }

    @Test
    public void testNotExistingTestTarget() {
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-r",
                Paths.get(Simple.REPOSITORY_URI).toString(), Paths.get(Simple.NOT_EXISTING).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).isNotEmpty();
        assertThat(CommandLine.getErrorOutput()).contains("No test targets found");
    }

    @Test
    public void testValidMinimalConfiguration() {
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-r",
                Paths.get(Simple.REPOSITORY_URI).toString(), Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).contains(RESULT_OUTPUT);
    }

    @Test
    public void testValidNamingConfiguration() {
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-r",
                Paths.get(Simple.REPOSITORY_URI).toString(), Paths.get(Simple.SIMPLE_VALID).toString(), "--report-prefix", "somePrefix",
                "--report-postfix", "somePostfix" };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).contains(RESULT_OUTPUT);
        assertThat(CommandLine.getErrorOutput()).contains("somePrefix-simple-somePostfix");
    }

    @Test
    public void testValidMultipleInput() {
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-o", this.output.toString(), "-r",
                Paths.get(Simple.REPOSITORY_URI).toString(), Paths.get(Simple.SIMPLE_VALID).toString(), Paths.get(Simple.FOO).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).contains("Processing 2 object(s) completed");
    }

    @Test
    public void testValidDirectoryInput() {
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-o", this.output.toString(), "-r",
                Paths.get(Simple.REPOSITORY_URI).toString(), Paths.get(Simple.EXAMPLES).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).contains("Processing 8 object(s) completed");
    }

    @Test
    public void testValidOutputConfiguration() throws IOException {

        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-o", this.output.toString(), "-r",
                Paths.get(Simple.REPOSITORY_URI).toString(), Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).contains(RESULT_OUTPUT);
        assertThat(this.output).exists();
        assertThat(Files.list(this.output)).hasSize(1);
    }

    @Test
    public void testNoInput() {
        // assertThat(output).doesNotExist();
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-r",
                Paths.get(Simple.REPOSITORY_URI).toString(), };
        CommandLineApplication.mainProgram(args);
        checkForHelp(CommandLine.getOutputLines());
    }

    @Test
    public void testPrint() {

        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-p", "-r",
                Paths.get(Simple.REPOSITORY_URI).toString(), "-o", this.output.toString(), Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).contains(RESULT_OUTPUT);
        assertThat(CommandLine.getOutputLines()).haveAtLeastOne(new Condition<>(
                s -> StringUtils.contains(s, "<?xml version=\"1.0\" " + "encoding=\"UTF-8\"?>"), "Must " + "contain xml preambel"));
    }

    @Test
    public void testHtmlExtraktion() throws IOException {
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-h", "-o",
                this.output.toAbsolutePath().toString(), "-r", Paths.get(Simple.REPOSITORY_URI).toString(),
                Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).contains(RESULT_OUTPUT);
        assertThat(Files.list(this.output).filter(f -> f.toString().endsWith(".html")).count()).isPositive();
    }

    @Test
    public void testAssertionsExtraktion() {
        final String[] args = new String[] { "-d", "-s", Paths.get(Simple.SCENARIOS).toString(), "-r",
                Paths.get(Simple.REPOSITORY_URI).toString(), "-o", this.output.toString(), "-c", Paths.get(ASSERTIONS).toString(),
                Paths.get(Simple.REPOSITORY_URI).toString(), Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).contains(RESULT_OUTPUT);
        assertThat(CommandLine.getErrorOutput()).contains("Can not find assertions for ");
    }

    @Test
    public void testDebugFlag() {
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-r", "unknown", "-o", this.output.toString(),
                "-d", Paths.get(ASSERTIONS).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).contains("at de.kosit.validationtool");
    }

    @Test
    public void testPrintMemoryStats() {
        final String[] args = new String[] { "-m", "-s", Paths.get(Simple.SCENARIOS).toString(), "-r",
                Paths.get(Simple.REPOSITORY_URI).toString(), Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).contains(RESULT_OUTPUT);
        assertThat(CommandLine.getErrorOutput()).contains("total");
    }

    @Test
    public void testReadFromPipe() throws IOException {
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-r",
                Paths.get(Simple.REPOSITORY_URI).toString() };
        CommandLine.setStandardInput(Files.newInputStream(Paths.get(Simple.SIMPLE_VALID)));
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).contains(RESULT_OUTPUT);
    }

    @Test
    public void testUnexpectedDaemonFlag() {
        final String[] args = new String[] { "-D", "-s", Paths.get(Simple.SCENARIOS).toString(), "-r",
                Paths.get(Simple.REPOSITORY_URI).toString(), Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).contains("Will ignore cli mode options");
    }

    @Test
    public void testParsingError() {
        final String[] args = new String[] { "-s", "-r", Paths.get(Simple.REPOSITORY_URI).toString(),
                Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).contains("Expected parameter for option");
    }

    @Test
    public void loadMultipleScenarios() {
        final String[] args = new String[] { "-s", "s1=" + Paths.get(Simple.SCENARIOS).toString(), "-s",
                "s2=" + Paths.get(Simple.OTHER_SCENARIOS).toString(), "-r", "s1=" + Paths.get(Simple.REPOSITORY_URI).toString(), "-r",
                "s2=" + Paths.get(Simple.REPOSITORY_URI).toString(), Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getOutput()).contains("Processing of 1 objects completed");
    }

    @Test
    public void loadMultipleScenariosSingleRepository() {
        final String[] args = new String[] { "-s", "s1=" + Paths.get(Simple.SCENARIOS).toString(), "-s",
                "s2=" + Paths.get(Simple.OTHER_SCENARIOS).toString(), "-r", Paths.get(Simple.REPOSITORY_URI).toString(),
                Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getOutput()).contains("Processing of 1 objects completed");
    }

    @Test
    public void loadMultipleScenariosMissingRepository() {
        final String[] args = new String[] { "-s", "s1=" + Paths.get(Simple.SCENARIOS).toString(), "-s",
                "s2=" + Paths.get(Simple.OTHER_SCENARIOS).toString(), "-r", "s1=" + Paths.get(Simple.REPOSITORY_URI).toString(), "-r",
                "typo=" + Paths.get(Simple.REPOSITORY_URI).toString(), Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).contains("No repository location for scenario definition 's2' specified");
    }

    @Test
    public void loadMultipleOrderedScenarios() {
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-s",
                Paths.get(Simple.OTHER_SCENARIOS).toString(), "-r", Paths.get(Simple.REPOSITORY_URI).toString(), "-r",
                Paths.get(Simple.REPOSITORY_URI).toString(), Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getOutput()).contains("Processing of 1 objects completed");
    }

    @Test
    public void checkUnusedRepository() {
        final String[] args = new String[] { "-s", "s1=" + Paths.get(Simple.SCENARIOS).toString(), "-r",
                "s1=" + Paths.get(Simple.REPOSITORY_URI).toString(), "-r", "unused=" + Paths.get(Simple.REPOSITORY_URI).toString(),
                Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getOutput()).contains("Processing of 1 objects completed");
        assertThat(CommandLine.getErrorOutput()).contains("Warning: repository definition \"unused\" is not used");
    }

    @Test
    public void checkDuplicationScenarioDefinition() {
        final String[] args = new String[] { "-s", "s1=" + Paths.get(Simple.SCENARIOS).toString(), "-r",
                "s1=" + Paths.get(Simple.REPOSITORY_URI).toString(), "-r", "unused=" + Paths.get(Simple.REPOSITORY_URI).toString(),
                Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getOutput()).contains("Processing of 1 objects completed");
        assertThat(CommandLine.getErrorOutput()).contains("Warning: repository definition \"unused\" is not used");
    }
}
