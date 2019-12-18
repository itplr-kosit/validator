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

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.api.CheckConfiguration;
import de.kosit.validationtool.api.Input;
import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.cmd.assertions.Assertions;
import de.kosit.validationtool.impl.ConversionService;
import de.kosit.validationtool.impl.ObjectFactory;

/**
 * Commandline Version des Prüftools. Parsed die Kommandozeile und führt die konfigurierten Aktionen aus.
 *
 * @author Andreas Penski
 */
@Slf4j
public class CommandLineApplication {

    private static final Option HELP = Option.builder("?").longOpt("help").argName("Help").desc("Displays this help").build();

    private static final Option SCENARIOS = Option.builder("s").required().longOpt("scenarios").hasArg()
            .desc("Location of scenarios.xml e.g.").build();

    private static final Option REPOSITORY = Option.builder("r").longOpt("repository").hasArg()
            .desc("Directory containing scenario content").build();

    private static final Option PRINT = Option.builder("p").longOpt("print").desc("Prints the check result to stdout").build();

    private static final Option OUTPUT = Option.builder("o").longOpt("output-directory")
            .desc("Defines the out directory for results. Defaults to cwd").hasArg().build();

    private static final Option EXTRACT_HTML = Option.builder("h").longOpt("html")
            .desc("Extract and save any html content within  result as a separate file ").build();

    private static final Option DEBUG = Option.builder("d").longOpt("debug").desc("Prints some more debug information").build();

    private static final Option SERIALIZE_REPORT_INPUT = Option.builder("c").longOpt("serialize-report-input")
            .desc("Serializes the report input to the cwd").build();

    private static final Option CHECK_ASSERTIONS = Option.builder("c").longOpt("check-assertions").hasArg()
            .desc("Check the result using defined assertions").argName("assertions-file").build();

    private static final Option SERVER = Option.builder("D").longOpt("daemon").desc("Starts a daemon listing for validation requests")
            .build();

    private static final Option HOST = Option.builder("H").longOpt("host").hasArg()
            .desc("The hostname / IP address to bind the daemon. Default is localhost").build();

    private static final Option PORT = Option.builder("P").longOpt("port").hasArg().desc("The port to bind the daemon. Default is 8080")
            .build();

    private static final Option WORKER_COUNT = Option.builder("T").longOpt("threads").hasArg()
            .desc("Number of threads processing validation requests").build();

    public static final int DAEMON_SIGNAL = 100;

    private static final Option PRINT_MEM_STATS = Option.builder("m").longOpt("memory-stats").desc("Prints some memory stats").build();

    private CommandLineApplication() {
        // main class -> hide constructor
    }

    /**
     * Main-Funktion für die Kommandozeilen-Applikation.
     *
     * @param args die Eingabe-Argumente
     */
    public static void main(final String[] args) {
        final int resultStatus = mainProgram(args);
        if (DAEMON_SIGNAL != resultStatus) {
            System.exit(resultStatus);
        }
    }

    /**
     * Hauptprogramm für die Kommandozeilen-Applikation.
     *
     * @param args die Eingabe-Argumente
     */
    static int mainProgram(final String[] args) {
        int returnValue = 0;
        final Options options = createOptions();
        if (isHelpRequested(args)) {
            printHelp(options);
        } else {
            try {
                final CommandLineParser parser = new DefaultParser();
                final CommandLine cmd = parser.parse(options, args);
                if (cmd.hasOption(SERVER.getOpt())) {
                    returnValue = startDaemonMode(cmd);
                } else if (cmd.getArgList().isEmpty()) {
                    printHelp(createOptions());
                } else {
                    returnValue = processActions(cmd);
                }
            } catch (final ParseException e) {
                log.error("Error processing command line arguments: " + e.getMessage());
                printHelp(options);
            }
        }
        return returnValue;
    }

    private static int determinePort(final CommandLine cmd) {
        int port = 8080;
        if (checkOptionWithValue(PORT, cmd)) {
            port = Integer.parseInt(cmd.getOptionValue(PORT.getOpt()));
        }
        return port;
    }

    private static int determineThreads(final CommandLine cmd) {
        int threads = Runtime.getRuntime().availableProcessors();
        if (checkOptionWithValue(WORKER_COUNT, cmd)) {
            threads = Integer.parseInt(cmd.getOptionValue(WORKER_COUNT.getOpt()));
        }
        return threads;
    }

    private static String determineHost(final CommandLine cmd) {
        String host = "localhost";
        if (checkOptionWithValue(HOST, cmd)) {
            host = cmd.getOptionValue(HOST.getOpt());
        }
        return host;
    }

    private static int startDaemonMode(final CommandLine cmd) {
        final Option[] unavailable = new Option[] { PRINT, CHECK_ASSERTIONS, DEBUG, OUTPUT, EXTRACT_HTML };
        warnUnusedOptions(cmd, unavailable, true);
        final Daemon validDaemon = new Daemon(determineDefinition(cmd), determineRepository(cmd), determineHost(cmd), determinePort(cmd),
                determineThreads(cmd));
        validDaemon.startServer();
        return DAEMON_SIGNAL;
    }

    private static void warnUnusedOptions(final CommandLine cmd, final Option[] unavailable, final boolean daemon) {
        Arrays.stream(cmd.getOptions()).filter(o -> ArrayUtils.contains(unavailable, o))
                .map(o -> "The option " + o.getLongOpt() + " is not available in daemon mode").forEach(log::error);
        if (daemon && !cmd.getArgList().isEmpty()) {
            log.info("Ignoring test targets in daemon mode");
        }
    }

    private static boolean isHelpRequested(final String[] args) {
        final Options helpOptions = createHelpOptions();
        try {
            final CommandLineParser parser = new DefaultParser();
            final CommandLine cmd = parser.parse(helpOptions, args, true);
            if (cmd.hasOption(HELP.getOpt()) || args.length == 0) {
                return true;
            }
        } catch (final ParseException e) {
            // we can ignore that, we just look for the help parameters
        }
        return false;
    }

    private static int processActions(final CommandLine cmd) {
        try {

            long start = System.currentTimeMillis();
            final Option[] unavailable = new Option[] { HOST, PORT, WORKER_COUNT };
            warnUnusedOptions(cmd, unavailable, false);
            final CheckConfiguration d = new CheckConfiguration(determineDefinition(cmd));
            d.setScenarioRepository(determineRepository(cmd));
            final InternalCheck check = new InternalCheck(d);
            final Path outputDirectory = determineOutputDirectory(cmd);

            if (cmd.hasOption(EXTRACT_HTML.getOpt())) {
                check.getCheckSteps().add(new ExtractHtmlContentAction(check.getContentRepository(), outputDirectory));
            }
            check.getCheckSteps().add(new SerializeReportAction(outputDirectory));
            if (cmd.hasOption(SERIALIZE_REPORT_INPUT.getOpt())) {
                check.getCheckSteps().add(new SerializeReportInputAction(outputDirectory, check.getConversionService()));
            }
            if (cmd.hasOption(PRINT.getOpt())) {
                check.getCheckSteps().add(new PrintReportAction());
            }

            if (cmd.hasOption(CHECK_ASSERTIONS.getOpt())) {
                final Assertions assertions = loadAssertions(cmd.getOptionValue(CHECK_ASSERTIONS.getOpt()));
                check.getCheckSteps().add(new CheckAssertionAction(assertions, ObjectFactory.createProcessor()));
            }
            if (cmd.hasOption(PRINT_MEM_STATS.getOpt())) {
                check.getCheckSteps().add(new PrintMemoryStats());
            }

            log.info("Setup completed in {}ms\n", System.currentTimeMillis() - start);

            final Collection<Path> targets = determineTestTargets(cmd);
            start = System.currentTimeMillis();
            for (final Path p : targets) {
                final Input input = InputFactory.read(p);
                check.checkInput(input);
            }
            final boolean result = check.printAndEvaluate();
            log.info("Processing {} object(s) completed in {}ms", targets.size(), System.currentTimeMillis() - start);
            return result ? 0 : 1;

        } catch (final Exception e) {
            e.printStackTrace();
            if (cmd.hasOption(DEBUG.getOpt())) {
                log.error(e.getMessage(), e);
            } else {
                log.error(e.getMessage());
            }
            return -1;
        }
    }

    private static Assertions loadAssertions(final String optionValue) {
        final Path p = Paths.get(optionValue);
        Assertions a = null;
        if (Files.exists(p)) {
            final ConversionService c = new ConversionService();
            c.initialize(de.kosit.validationtool.cmd.assertions.ObjectFactory.class.getPackage());
            a = c.readXml(p.toUri(), Assertions.class);
        }
        return a;
    }

    private static Path determineOutputDirectory(final CommandLine cmd) {
        final String value = cmd.getOptionValue(OUTPUT.getOpt());
        final Path fir;
        if (StringUtils.isNotBlank(value)) {
            fir = Paths.get(value);
            if ((!Files.exists(fir) && !fir.toFile().mkdirs()) || !Files.isDirectory(fir)) {
                throw new IllegalStateException(String.format("Invalid target directory %s specified", value));
            }
        } else {
            fir = Paths.get(""/* cwd */);
        }
        return fir;
    }

    private static Collection<Path> determineTestTargets(final CommandLine cmd) {
        final Collection<Path> targets = new ArrayList<>();
        if (!cmd.getArgList().isEmpty()) {
            cmd.getArgList().forEach(e -> targets.addAll(determineTestTarget(e)));
        }
        if (targets.isEmpty()) {
            throw new IllegalStateException("No test targets found. Nothing to check. Will quit now!");
        }
        return targets;
    }

    private static Collection<Path> determineTestTarget(final String s) {
        final Path d = Paths.get(s);
        if (Files.isDirectory(d)) {
            return listDirectoryTargets(d);
        } else if (Files.exists(d)) {
            return Collections.singleton(d);
        }
        log.warn("The specified test target {} does not exist. Will be ignored", s);
        return Collections.emptyList();

    }

    private static Collection<Path> listDirectoryTargets(final Path d) {
        try {
            return Files.list(d).filter(path -> path.toString().endsWith(".xml")).collect(Collectors.toList());
        } catch (final IOException e) {
            throw new IllegalStateException("IOException while list directory content. Can not determine test targets.", e);
        }

    }

    private static URI determineRepository(final CommandLine cmd) {
        if (checkOptionWithValue(REPOSITORY, cmd)) {
            final Path d = Paths.get(cmd.getOptionValue(REPOSITORY.getOpt()));
            if (Files.isDirectory(d)) {
                return d.toUri();
            } else {
                throw new IllegalArgumentException(
                        String.format("Not a valid path for repository definition specified: '%s'", d.toAbsolutePath()));
            }
        }
        return null;
    }

    private static URI determineDefinition(final CommandLine cmd) {
        checkOptionWithValue(SCENARIOS, cmd);
        final Path f = Paths.get(cmd.getOptionValue(SCENARIOS.getOpt()));
        if (Files.isRegularFile(f)) {
            return f.toAbsolutePath().toUri();
        } else {
            throw new IllegalArgumentException(
                    String.format("Not a valid path for scenario definition specified: '%s'", f.toAbsolutePath()));
        }
    }

    private static boolean checkOptionWithValue(final Option option, final CommandLine cmd) {
        final String opt = option.getOpt();
        if (cmd.hasOption(opt)) {
            final String value = cmd.getOptionValue(opt);
            if (StringUtils.isNoneBlank(value)) {
                return true;
            } else {
                throw new IllegalArgumentException(String.format("Option value required for Option '%s'", option.getLongOpt()));
            }
        } else if (option.isRequired()) {

            throw new IllegalArgumentException(String.format("Option '%s' required ", option.getLongOpt()));
        }
        return false;
    }

    private static void printHelp(final Options options) {
        // automatically generate the help statement
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("check-tool  -s <scenario-config-file> [OPTIONS] [FILE]... ", options, false);
    }

    private static Options createHelpOptions() {
        final Options options = new Options();
        options.addOption(HELP);
        return options;
    }

    private static Options createOptions() {
        final Options options = new Options();
        options.addOption(HELP);
        options.addOption(SERVER);
        options.addOption(HOST);
        options.addOption(PORT);
        options.addOption(SCENARIOS);
        options.addOption(REPOSITORY);
        options.addOption(PRINT);
        options.addOption(OUTPUT);
        options.addOption(EXTRACT_HTML);
        options.addOption(DEBUG);
        options.addOption(CHECK_ASSERTIONS);
        options.addOption(PRINT_MEM_STATS);
        options.addOption(WORKER_COUNT);
        return options;
    }
}
