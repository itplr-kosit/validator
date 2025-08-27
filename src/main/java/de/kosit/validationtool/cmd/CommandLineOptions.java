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

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import de.kosit.validationtool.cmd.CommandLineApplication.Level;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Commandline Interface definition.
 * 
 * @author Andreas Penski
 */
@Command(description = "Structural and semantic validation of xml files", name = "KoSIT Validator", mixinStandardHelpOptions = false,
         separator = " ", synopsisHeading = CommandLineOptions.SYNOSIS_HEADING)
@Getter
public class CommandLineOptions implements Callable<ReturnValue> {

    static final String SYNOSIS_HEADING = "Usage: ";

    /**
     * @author Andreas Penski
     */
    @Getter
    @NoArgsConstructor
    static class DaemonOptions {

        @Option(names = { "-D", "--daemon" }, description = "Starts a daemon listing for validation requests", defaultValue = "false",
                required = true)
        private boolean daemonMode;

        @Option(names = { "-H", "--host" }, description = "The hostname / IP address to bind the daemon.", defaultValue = "localhost",
                showDefaultValue = Visibility.ALWAYS)
        private String host;

        @Option(names = { "-P", "--port" }, description = "The port to bind the daemon.", defaultValue = "8080",
                showDefaultValue = Visibility.ALWAYS)
        private int port;

        @Option(names = { "-T", "--threads" },
                description = "Number of threads processing validation requests. Default depends on processor count", defaultValue = "-1",
                showDefaultValue = Visibility.NEVER)
        private int workerCount;

        @Option(names = { "-G", "--disable-gui" }, description = "Disables the GUI of the daemon mode")
        private boolean disableGUI;
    }

    /**
     * @author Andreas Penski
     */
    @Getter
    @NoArgsConstructor
    static class CliOptions {

        @Option(names = { "-o", "--output-directory" }, description = "Defines the out directory for results.", defaultValue = ".",
                required = true)
        private Path outputPath;

        @Option(names = { "-h", "--html", "--extract-html" },
                description = "Extract and save any html content within result as a separate file")
        private boolean extractHtml;

        @Option(names = { "--serialize-report-input" }, description = "Serializes the report input to the cwd", defaultValue = "false")
        private boolean serializeInput;

        @Option(names = { "-c", "--check-assertions" }, paramLabel = "assertions-file",
                description = "Check the result using defined assertions")
        private Path assertions;

        @Option(names = { "--report-postfix" }, description = "Postfix of the generated report name")
        private String reportPostfix;

        @Option(names = { "--report-prefix" }, description = "Prefix of the generated report name")
        private String reportPrefix;

        @Option(names = { "-m", "--memory-stats" }, description = "Prints some memory stats")
        private boolean printMemoryStats;

        @Option(names = { "-p", "--print" }, description = "Prints the check result to stdout")
        private boolean printReport;

        @Parameters(arity = "1..*", description = "Files to validate")
        private List<Path> files;

    }

    /**
     * Definition of logical name and a path for a configuration artifact.
     * 
     * @author Andreas Penski
     */
    @Getter
    @Setter
    public abstract static class Definition {

        String name;

        Path path;
    }

    /**
     * Definition of logical name and a path for a repository.
     * 
     * @author Andreas Penski
     */
    public static class RepositoryDefinition extends Definition {
        // just for type safety
    }

    /**
     * Definition of logical name and a path for a scenario configuration file.
     * 
     * @author Andreas Penski
     */
    public static class ScenarioDefinition extends Definition {
        // just for type safety
    }

    @ArgGroup(exclusive = false, heading = "Daemon options\n")
    private DaemonOptions daemonOptions;

    @ArgGroup(exclusive = false, heading = "CLI usage options\n")
    private CliOptions cliOptions;

    @Option(names = { "-d", "--debug" }, description = "Prints some more debug information")
    private boolean debugOutput;

    @Option(names = { "-?", "--help" }, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @Option(names = { "-X", "--debug-logging" }, description = "Enables full debug log. Alias for -l debug")
    private boolean debugLog;

    @Option(names = { "-l", "--log-level" }, description = "Enables a certain log level for debugging purposes", defaultValue = "OFF")
    private Level logLevel;

    @Option(names = { "-r", "--repository" }, paramLabel = "repository-path", description = "Directory containing scenario content",
            converter = TypeConverter.RepositoryConverter.class)
    private List<RepositoryDefinition> repositories;

    @Option(names = { "-s", "--scenarios" }, description = "Location of scenarios.xml", paramLabel = "scenario.xml", required = true,
            converter = TypeConverter.ScenarioConverter.class)
    private List<ScenarioDefinition> scenarios;

    @Override
    public ReturnValue call() throws Exception {
        configureLogging(this);
        return Validator.mainProgram(this);
    }

    private static void configureLogging(final CommandLineOptions cmd) {
        if (cmd.isDebugLog()) {
            System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");
        } else {
            System.setProperty(org.slf4j.simple.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, cmd.getLogLevel().name());
        }
    }

    public boolean isDaemonModeEnabled() {
        return getDaemonOptions() != null;
    }

    public boolean isCliModeEnabled() {
        return getCliOptions() != null;
    }
}
