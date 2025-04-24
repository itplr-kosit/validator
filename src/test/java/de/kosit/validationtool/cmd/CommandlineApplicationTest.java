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

import de.kosit.validationtool.impl.Helper.Simple;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static de.kosit.validationtool.impl.Helper.ASSERTIONS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testet die Parameter des Kommandozeilen-Tools.
 *
 * @author Andreas Penski
 */
@Slf4j
public class CommandlineApplicationTest {

    public static final String RESULT_OUTPUT = "Processing 1 object(s) completed";

    private final Path output = Paths.get("target/test-output");

    @BeforeEach
    void setup() throws IOException {
        CommandLine.activate();
        if (Files.exists(this.output)) {
            FileUtils.deleteDirectory(this.output.toFile());
        }
        TypeConverter.counter.clear();
    }

    @AfterEach
    void cleanup() throws IOException {
        try ( Stream<Path> stream = Files.list(Paths.get("")) ) {
            stream.filter(p -> p.getFileName().toString().endsWith("-report.xml")).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (final IOException e) {
                    log.error("Error deleting file", e);
                }
            });
        }
        CommandLine.deactivate();
    }

    @Test
    void help() {
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
    void requiredScenarioFile() {
        final String[] args = new String[] { "arguments", "egal welche", "argumente drin sind" };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).isNotEmpty();
        assertThat(CommandLine.getErrorOutput()).contains("Missing required option: '--scenarios");
    }

    @Test
    void notExistingScenarioFile() {
        final String s = Paths.get(Simple.NOT_EXISTING).toString();
        final String[] args = new String[] { "-s", s, s };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).isNotEmpty();
        assertThat(CommandLine.getErrorOutput()).contains("Not a valid path for scenario definition specified");
    }

    @Test
    void incorrectRepository() {
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-r", Paths.get(Simple.NOT_EXISTING).toString(),
                Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).isNotEmpty();
        assertThat(CommandLine.getErrorOutput()).contains("Not a valid path for repository");
    }

    @Test
    void notExistingTestTarget() {
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-r",
                Paths.get(Simple.REPOSITORY_URI).toString(), Paths.get(Simple.NOT_EXISTING).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).isNotEmpty();
        assertThat(CommandLine.getErrorOutput()).contains("No test targets found");
    }

    @Test
    void validMinimalConfiguration() {
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-r",
                Paths.get(Simple.REPOSITORY_URI).toString(), Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).contains(RESULT_OUTPUT);
    }

    @Test
    void validNamingConfiguration() {
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-r",
                Paths.get(Simple.REPOSITORY_URI).toString(), Paths.get(Simple.SIMPLE_VALID).toString(), "--report-prefix", "somePrefix",
                "--report-postfix", "somePostfix" };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).contains(RESULT_OUTPUT);
        assertThat(CommandLine.getErrorOutput()).contains("somePrefix-simple-somePostfix");
    }

    @Test
    void validMultipleInput() {
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-o", this.output.toString(), "-r",
                Paths.get(Simple.REPOSITORY_URI).toString(), Paths.get(Simple.SIMPLE_VALID).toString(), Paths.get(Simple.FOO).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).contains("Processing 2 object(s) completed");
    }

    @Test
    void validDirectoryInput() {
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-o", this.output.toString(), "-r",
                Paths.get(Simple.REPOSITORY_URI).toString(), Paths.get(Simple.EXAMPLES).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).contains("Processing 8 object(s) completed");
    }

    @Test
    void validOutputConfiguration() throws IOException {
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-o", this.output.toString(), "-r",
                Paths.get(Simple.REPOSITORY_URI).toString(), Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).contains(RESULT_OUTPUT);
        assertThat(this.output).exists();
        assertThat(Files.list(this.output)).hasSize(1);
    }

    @Test
    void noInput() {
        // assertThat(output).doesNotExist();
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-r",
                Paths.get(Simple.REPOSITORY_URI).toString(), };
        CommandLineApplication.mainProgram(args);
        checkForHelp(CommandLine.getOutputLines());
    }

    @Test
    void print() {
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-p", "-r",
                Paths.get(Simple.REPOSITORY_URI).toString(), "-o", this.output.toString(), Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).contains(RESULT_OUTPUT);
        assertThat(CommandLine.getOutputLines()).haveAtLeastOne(new Condition<>(
                s -> StringUtils.contains(s, "<?xml version=\"1.0\" " + "encoding=\"UTF-8\"?>"), "Must " + "contain xml preamble"));
    }

    @Test
    void htmlExtraktion() throws IOException {
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-h", "-o",
                this.output.toAbsolutePath().toString(), "-r", Paths.get(Simple.REPOSITORY_URI).toString(),
                Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).contains(RESULT_OUTPUT);
        try ( Stream<Path> stream = Files.list(this.output) ) {
            assertThat(stream.filter(f -> f.toString().endsWith(".html")).count()).isPositive();
        }
    }

    @Test
    void assertionsExtraktion() {
        final String s = Paths.get(Simple.REPOSITORY_URI).toString();
        final String[] args = new String[] { "-d", "-s", Paths.get(Simple.SCENARIOS).toString(), "-r", s, "-o", this.output.toString(),
                "-c", Paths.get(ASSERTIONS).toString(), s, Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).contains(RESULT_OUTPUT);
        assertThat(CommandLine.getErrorOutput()).contains("Can not find assertions for ");
    }

    @Test
    void debugFlag() {
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-r", "unknown", "-o", this.output.toString(),
                "-d", Paths.get(ASSERTIONS).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).contains("at de.kosit.validationtool");
    }

    @Test
    void printMemoryStats() {
        final String[] args = new String[] { "-m", "-s", Paths.get(Simple.SCENARIOS).toString(), "-r",
                Paths.get(Simple.REPOSITORY_URI).toString(), Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).contains(RESULT_OUTPUT);
        assertThat(CommandLine.getErrorOutput()).contains("total");
    }

    @Test
    void readFromPipe() throws IOException {
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-r",
                Paths.get(Simple.REPOSITORY_URI).toString() };
        CommandLine.setStandardInput(Files.newInputStream(Paths.get(Simple.SIMPLE_VALID)));
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).contains(RESULT_OUTPUT);
    }

    @Test
    void unexpectedDaemonFlag() {
        final String[] args = new String[] { "-D", "-s", Paths.get(Simple.SCENARIOS).toString(), "-r",
                Paths.get(Simple.REPOSITORY_URI).toString(), Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).contains("Will ignore cli mode options");
    }

    @Test
    void parsingError() {
        final String[] args = new String[] { "-s", "-r", Paths.get(Simple.REPOSITORY_URI).toString(),
                Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).contains("Expected parameter for option");
    }

    @Test
    void loadMultipleScenarios() {
        final Path p = Paths.get(Simple.REPOSITORY_URI);
        final String[] args = new String[] { "-s", "s1=" + Paths.get(Simple.SCENARIOS), "-s", "s2=" + Paths.get(Simple.OTHER_SCENARIOS),
                "-r", "s1=" + p, "-r", "s2=" + p, Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getOutput()).contains("Processing of 1 objects completed");
    }

    @Test
    void loadMultipleScenariosSingleRepository() {
        final String[] args = new String[] { "-s", "s1=" + Paths.get(Simple.SCENARIOS), "-s", "s2=" + Paths.get(Simple.OTHER_SCENARIOS),
                "-r", Paths.get(Simple.REPOSITORY_URI).toString(), Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getOutput()).contains("Processing of 1 objects completed");
    }

    @Test
    void loadMultipleScenariosMissingRepository() {
        final Path p = Paths.get(Simple.REPOSITORY_URI);
        final String[] args = new String[] { "-s", "s1=" + Paths.get(Simple.SCENARIOS), "-s", "s2=" + Paths.get(Simple.OTHER_SCENARIOS),
                "-r", "s1=" + p, "-r", "typo=" + p, Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getErrorOutput()).contains("No repository location for scenario definition 's2' specified");
    }

    @Test
    void loadMultipleOrderedScenarios() {
        final String s = Paths.get(Simple.REPOSITORY_URI).toString();
        final String[] args = new String[] { "-s", Paths.get(Simple.SCENARIOS).toString(), "-s",
                Paths.get(Simple.OTHER_SCENARIOS).toString(), "-r", s, "-r", s, Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getOutput()).contains("Processing of 1 objects completed");
    }

    @Test
    void checkUnusedRepository() {
        final Path p = Paths.get(Simple.REPOSITORY_URI);
        final String[] args = new String[] { "-s", "s1=" + Paths.get(Simple.SCENARIOS), "-r", "s1=" + p, "-r", "unused=" + p,
                Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getOutput()).contains("Processing of 1 objects completed");
        assertThat(CommandLine.getErrorOutput()).contains("Warning: repository definition \"unused\" is not used");
    }

    @Test
    void checkDuplicationScenarioDefinition() {
        final Path p = Paths.get(Simple.REPOSITORY_URI);
        final String[] args = new String[] { "-s", "s1=" + Paths.get(Simple.SCENARIOS), "-r", "s1=" + p, "-r", "unused=" + p,
                Paths.get(Simple.SIMPLE_VALID).toString() };
        CommandLineApplication.mainProgram(args);
        assertThat(CommandLine.getOutput()).contains("Processing of 1 objects completed");
        assertThat(CommandLine.getErrorOutput()).contains("Warning: repository definition \"unused\" is not used");
    }
}
