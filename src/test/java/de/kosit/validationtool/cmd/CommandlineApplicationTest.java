/*
 * Licensed to the Koordinierungsstelle für IT-Standards (KoSIT) under
 * one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  KoSIT licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
        this.commandLine.activate();
        if (Files.exists(this.output)) {
            FileUtils.deleteDirectory(this.output.toFile());
        }
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
    }

    @Test
    public void testHelp() {
        final String[] args = new String[] { "-?" };
        CommandLineApplication.mainProgram(args);
        assertThat(this.commandLine.getErrorOutput()).isEmpty();
        checkForHelp(this.commandLine.getOutputLines());
    }

    private static void checkForHelp(final List<String> outputLines) {
        assertThat(outputLines.size()).isGreaterThan(0);
        outputLines.subList(1, outputLines.size() - 1).forEach(l -> assertThat(l.startsWith(" -") || l.startsWith("  ")));
    }

    @Test
    public void testRequiredScenarioFile() {
        final String[] args = new String[] { "-d", "arguments", "egal welche", "argument drin sind" };
        CommandLineApplication.mainProgram(args);
        assertThat(this.commandLine.getErrorOutput()).isNotEmpty();
        assertThat(this.commandLine.getErrorOutput()).contains("Missing required option: s");
    }

    @Test
    public void testNotExistingScenarioFile() {
        final String[] args = new String[] { "-s", Paths.get(Simple.NOT_EXISTING).toString(), Paths.get(Simple.NOT_EXISTING).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(this.commandLine.getErrorOutput()).isNotEmpty();
        assertThat(this.commandLine.getErrorOutput()).contains("Not a valid path for scenario definition specified");
    }

    @Test
    public void testIncorrectRepository() {
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), Paths.get(Simple.NOT_EXISTING).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(this.commandLine.getErrorOutput()).isNotEmpty();
        assertThat(this.commandLine.getErrorOutput()).contains("Can not load schema from sources");
    }

    @Test
    public void testNotExistingTestTarget() {
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-r", Paths.get(Simple.REPOSITORY).toString(),
                Paths.get(Simple.NOT_EXISTING).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(this.commandLine.getErrorOutput()).isNotEmpty();
        assertThat(this.commandLine.getErrorOutput()).contains("No test targets found");
    }

    @Test
    public void testValidMinimalConfiguration() {
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-r", Paths.get(Simple.REPOSITORY).toString(),
                Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(this.commandLine.getErrorOutput()).contains(RESULT_OUTPUT);
    }

    @Test
    public void testValidMultipleInput() {
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-o", this.output.toString(), "-r",
                Paths.get(Simple.REPOSITORY).toString(), Paths.get(Simple.SIMPLE_VALID).toString(), Paths.get(Simple.FOO).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(this.commandLine.getErrorOutput()).contains("Processing 2 object(s) completed");
    }

    @Test
    public void testValidDirectoryInput() {
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-o", this.output.toString(), "-r",
                Paths.get(Simple.REPOSITORY).toString(), Paths.get(Simple.EXAMPLES).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(this.commandLine.getErrorOutput()).contains("Processing 5 object(s) completed");
    }

    @Test
    public void testValidOutputConfiguration() throws IOException {

        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-o", this.output.toString(), "-r",
                Paths.get(Simple.REPOSITORY).toString(), Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(this.commandLine.getErrorOutput()).contains(RESULT_OUTPUT);
        assertThat(this.output).exists();
        assertThat(Files.list(this.output)).hasSize(1);
    }

    @Test
    public void testNoInput() {
        // assertThat(output).doesNotExist();
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-r", Paths.get(Simple.REPOSITORY).toString(), };
        CommandLineApplication.mainProgram(args);
        checkForHelp(this.commandLine.getOutputLines());
    }

    @Test
    public void testPrint() {

        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-p", "-r",
                Paths.get(Simple.REPOSITORY).toString(), "-o", this.output.toString(), Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(this.commandLine.getErrorOutput()).contains(RESULT_OUTPUT);
        assertThat(this.commandLine.getOutputLines().get(0)).contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    }

    @Test
    public void testHtmlExtraktion() throws IOException {
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-h", "-o",
                this.output.toAbsolutePath().toString(),
                "-r", Paths.get(Simple.REPOSITORY).toString(), Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(this.commandLine.getErrorOutput()).contains(RESULT_OUTPUT);
        assertThat(Files.list(this.output).filter(f -> f.toString().endsWith(".html")).count()).isGreaterThan(0);
    }

    @Test
    public void testAssertionsExtraktion() {
        final String[] args = new String[] { "-d", "-s", Paths.get(Simple.SCENARIOS).toString(), "-r",
                Paths.get(Simple.REPOSITORY).toString(), "-o", this.output.toString(), "-c", Paths.get(ASSERTIONS).toString(),
                Paths.get(Simple.REPOSITORY).toString(),
                Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(this.commandLine.getErrorOutput()).contains(RESULT_OUTPUT);
        assertThat(this.commandLine.getErrorOutput()).contains("Can not find assertions for ");
    }

    @Test
    public void testDebugFlag() {
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-r", "unknown", "-o", this.output.toString(),
                "-d",
                Paths.get(ASSERTIONS).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(this.commandLine.getErrorOutput()).contains("at de.kosit.validationtool");
    }

}
