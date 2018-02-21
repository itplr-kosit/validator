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

import static de.kosit.validationtool.impl.Helper.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * Testet die Parameter des Kommandozeilen-Tools.
 * 
 * @author Andreas Penski
 */
public class CommandlineApplicationTest {

    public static final String RESULT_OUTPUT = "Processing 1 object(s) completed";

    private CommandLine commandLine;


    @Before
    public void setup() throws IOException {
        commandLine = new CommandLine();
        commandLine.activate();
    }

    @Test
    public void testHelp() {
        String[] args = new String[] { "-?" };
        CommandLineApplication.mainProgram(args);
        assertThat(commandLine.getErrorOutput()).isEmpty();
        checkForHelp(commandLine.getOutputLines());
    }

    private void checkForHelp(List<String> outputLines) {
        assertThat(outputLines.size()).isGreaterThan(0);
        outputLines.subList(1, outputLines.size() - 1).forEach(l -> assertThat(l.startsWith(" -") || l.startsWith("  ")));
    }

    @Test
    public void testRequiredScenarioFile() {
        String[] args = new String[] { "-d", "arguments", "egal welche", "argument drin sind" };
        CommandLineApplication.mainProgram(args);
        assertThat(commandLine.getErrorOutput()).isNotEmpty();
        assertThat(commandLine.getErrorOutput()).contains("Missing required option: s");
    }

    @Test
    public void testNotExistingScenarioFile() {
        String[] args = new String[] { "-s", Paths.get(NOT_EXISTING).toString(), Paths.get(NOT_EXISTING).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(commandLine.getErrorOutput()).isNotEmpty();
        assertThat(commandLine.getErrorOutput()).contains("Not a valid path for scenario definition specified");
    }

    @Test
    public void testIncorrectRepository() {
        String[] args = new String[] { "-s", Paths.get(SCENARIO_FILE).toString(), Paths.get(NOT_EXISTING).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(commandLine.getErrorOutput()).isNotEmpty();
        assertThat(commandLine.getErrorOutput()).contains("Can not load schema from sources");
    }

    @Test
    public void testNotExistingTestTarget() {
        String[] args = new String[] { "-s", Paths.get(SCENARIO_FILE).toString(), "-r", Paths.get(REPOSITORY).toString(),
                Paths.get(NOT_EXISTING).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(commandLine.getErrorOutput()).isNotEmpty();
        assertThat(commandLine.getErrorOutput()).contains("No test targets found");
    }

    @Test
    public void testValidMinimalConfiguration() {
        String[] args = new String[] { "-s", Paths.get(SCENARIO_FILE).toString(), "-r", Paths.get(REPOSITORY).toString(),
                Paths.get(SAMPLE).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(commandLine.getErrorOutput()).contains(RESULT_OUTPUT);
    }

    @Test
    public void testValidMultipleInput() {
        String[] args = new String[] { "-s", Paths.get(SCENARIO_FILE).toString(), "-r", Paths.get(REPOSITORY).toString(),
                Paths.get(SAMPLE).toString(), Paths.get(SAMPLE2).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(commandLine.getErrorOutput()).contains("Processing 2 object(s) completed");
    }

    @Test
    public void testValidDirectoryInput() {
        String[] args = new String[] { "-s", Paths.get(SCENARIO_FILE).toString(), "-r", Paths.get(REPOSITORY).toString(),
                Paths.get(SAMPLE_DIR).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(commandLine.getErrorOutput()).contains("Processing 4 object(s) completed");
    }

    @Test
    public void testValidOutputConfiguration() throws IOException {
        Path output = Paths.get("output");
        // assertThat(output).doesNotExist();
        String[] args = new String[] { "-s", Paths.get(SCENARIO_FILE).toString(), "-o", output.getFileName().toString(), "-r",
                Paths.get(REPOSITORY).toString(), Paths.get(SAMPLE).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(commandLine.getErrorOutput()).contains(RESULT_OUTPUT);
        assertThat(output).exists();
        assertThat(Files.list(output)).hasSize(1);
    }

    @Test
    public void testNoInput() {
        // assertThat(output).doesNotExist();
        String[] args = new String[] { "-s", Paths.get(SCENARIO_FILE).toString(), "-r", Paths.get(REPOSITORY).toString(), };
        CommandLineApplication.mainProgram(args);
        checkForHelp(commandLine.getOutputLines());
    }

    @Test
    public void testPrint() {

        String[] args = new String[] { "-s", Paths.get(SCENARIO_FILE).toString(), "-p", "-r", Paths.get(REPOSITORY).toString(),
                Paths.get(SAMPLE).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(commandLine.getErrorOutput()).contains(RESULT_OUTPUT);
        assertThat(commandLine.getOutputLines()).contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    }

    @Test
    public void testHtmlExtraktion() throws IOException {
        Path output = Files.createTempDirectory("pruef-tool-test");
        String[] args = new String[] { "-s", Paths.get(SCENARIO_FILE).toString(), "-h", "-o", output.toAbsolutePath().toString(), "-r",
                Paths.get(REPOSITORY).toString(), Paths.get(SAMPLE).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(commandLine.getErrorOutput()).contains(RESULT_OUTPUT);
        assertThat(Files.list(output).filter(f -> f.toString().endsWith(".html")).count()).isGreaterThan(0);
    }

    @Test
    public void testAssertionsExtraktion() throws IOException {
        String[] args = new String[] { "-d", "-s", Paths.get(SCENARIO_FILE).toString(), "-r", Paths.get(REPOSITORY).toString(), "-c",
                Paths.get(ASSERTIONS).toString(), Paths.get(REPOSITORY).toString(), Paths.get(SAMPLE).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(commandLine.getErrorOutput()).contains(RESULT_OUTPUT);
        assertThat(commandLine.getErrorOutput()).contains("Can not find assertions for ");
    }

    @Test
    public void testDebugFlag() throws IOException {
        String[] args = new String[] { "-s", Paths.get(SCENARIO_FILE).toString(), "-r", "unknown", "-d", Paths.get(ASSERTIONS).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(commandLine.getErrorOutput()).contains("at de.kosit.validationtool");
    }

}
