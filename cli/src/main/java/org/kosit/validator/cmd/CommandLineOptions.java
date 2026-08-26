package org.kosit.validator.cmd;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import org.kosit.validator.cmd.CommandLineApplication.Level;
import org.kosit.validator.impl.EngineInformation;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Commandline Interface definition.
 * 
 * @author Andreas Penski
 */
@Command(description = "Structural and semantic validation of xml files", name = "KoSIT Validator", mixinStandardHelpOptions = false,
         separator = " ", synopsisHeading = CommandLineOptions.SYNOSIS_HEADING)
public class CommandLineOptions implements Callable<ReturnValue> {

    static final String SYNOSIS_HEADING = "Usage: ";

    /**
     * @author Andreas Penski
     */
    static class CliOptions {

        @Option(names = { "-o", "--output-directory" }, description = "Defines the out directory for results.", defaultValue = ".",
                required = true)
        private Path outputPath;

        @Option(names = { "-e", "--extract-reports" }, description = "Extract and save defined reports within result as separate files")
        private boolean extractReport;

        @Option(names = { "--serialize-report-input" }, description = "Serializes the report input to the cwd // deprecated",
                defaultValue = "false")
        private boolean serializeInput;

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

        public Path getOutputPath() {
            return this.outputPath;
        }

        public boolean isExtractReport() {
            return this.extractReport;
        }

        public boolean isSerializeInput() {
            return this.serializeInput;
        }

        public String getReportPostfix() {
            return this.reportPostfix;
        }

        public String getReportPrefix() {
            return this.reportPrefix;
        }

        public boolean isPrintMemoryStats() {
            return this.printMemoryStats;
        }

        public boolean isPrintReport() {
            return this.printReport;
        }

        public List<Path> getFiles() {
            return this.files;
        }

        public CliOptions() {
        }
    }

    /**
     * Definition of logical name and a path for a configuration artifact.
     *
     * @author Andreas Penski
     */
    public static abstract class AbstractDefinition {

        String name;

        Path path;

        public String getName() {
            return this.name;
        }

        public Path getPath() {
            return this.path;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public void setPath(final Path path) {
            this.path = path;
        }
    }

    /**
     * Definition of logical name and a path for a repository.
     *
     * @author Andreas Penski
     */
    public static class RepositoryDefinition extends AbstractDefinition {
        // just for type safety
    }

    /**
     * Definition of logical name and a path for a scenario configuration file.
     *
     * @author Andreas Penski
     */
    public static class ScenarioDefinition extends AbstractDefinition {
        // just for type safety
    }

    private EngineInformation engineInformation;

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

    public boolean isCliModeEnabled() {
        return getCliOptions() != null;
    }

    public EngineInformation getEngineInformation() {
        return this.engineInformation;
    }

    public void setEngineInformation(final EngineInformation engineInformation) {
        this.engineInformation = engineInformation;
    }

    public CliOptions getCliOptions() {
        return this.cliOptions;
    }

    public boolean isDebugOutput() {
        return this.debugOutput;
    }

    public boolean isUsageHelpRequested() {
        return this.usageHelpRequested;
    }

    public boolean isDebugLog() {
        return this.debugLog;
    }

    public Level getLogLevel() {
        return this.logLevel;
    }

    public List<RepositoryDefinition> getRepositories() {
        return this.repositories;
    }

    public List<ScenarioDefinition> getScenarios() {
        return this.scenarios;
    }
}
