package de.kosit.validationtool.cmd;

import static de.kosit.validationtool.cmd.CommandLineOptions.CHECK_ASSERTIONS;
import static de.kosit.validationtool.cmd.CommandLineOptions.DAEMON_SIGNAL;
import static de.kosit.validationtool.cmd.CommandLineOptions.DEBUG;
import static de.kosit.validationtool.cmd.CommandLineOptions.DISABLE_GUI;
import static de.kosit.validationtool.cmd.CommandLineOptions.EXTRACT_HTML;
import static de.kosit.validationtool.cmd.CommandLineOptions.HELP;
import static de.kosit.validationtool.cmd.CommandLineOptions.HOST;
import static de.kosit.validationtool.cmd.CommandLineOptions.OUTPUT;
import static de.kosit.validationtool.cmd.CommandLineOptions.PORT;
import static de.kosit.validationtool.cmd.CommandLineOptions.PRINT;
import static de.kosit.validationtool.cmd.CommandLineOptions.PRINT_MEM_STATS;
import static de.kosit.validationtool.cmd.CommandLineOptions.REPORT_POSTFIX;
import static de.kosit.validationtool.cmd.CommandLineOptions.REPORT_PREFIX;
import static de.kosit.validationtool.cmd.CommandLineOptions.REPOSITORY;
import static de.kosit.validationtool.cmd.CommandLineOptions.SCENARIOS;
import static de.kosit.validationtool.cmd.CommandLineOptions.SERIALIZE_REPORT_INPUT;
import static de.kosit.validationtool.cmd.CommandLineOptions.SERVER;
import static de.kosit.validationtool.cmd.CommandLineOptions.WORKER_COUNT;
import static de.kosit.validationtool.cmd.CommandLineOptions.createOptions;
import static de.kosit.validationtool.cmd.CommandLineOptions.printHelp;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.AnsiRenderer.Code;

import lombok.extern.slf4j.Slf4j;

import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.api.Input;
import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.api.Result;
import de.kosit.validationtool.cmd.assertions.Assertions;
import de.kosit.validationtool.cmd.report.Line;
import de.kosit.validationtool.config.ConfigurationLoader;
import de.kosit.validationtool.daemon.Daemon;
import de.kosit.validationtool.impl.ConversionService;
import de.kosit.validationtool.impl.EngineInformation;
import de.kosit.validationtool.impl.Printer;

import net.sf.saxon.s9api.Processor;

/**
 * Actual evaluation and processing of commandline argumtens.
 * 
 * @author Andreas Penski
 */
@Slf4j
@SuppressWarnings("squid:S3725")
public class Validator {

    private Validator() {
        // hide
    }

    /**
     * Hauptprogramm für die Kommandozeilen-Applikation.
     *
     * @param cmd parsed commandline.
     */
    static int mainProgram(final CommandLine cmd) {
        greeting();
        final org.apache.commons.cli.Options options = createOptions();
        int returnValue = 0;
        if (cmd.hasOption(SERVER.getOpt())) {
            returnValue = startDaemonMode(cmd);
        } else if (cmd.hasOption(HELP.getOpt()) || cmd.getArgList().isEmpty()) {
            printHelp(options);
        } else if (cmd.getArgList().isEmpty()) {
            printHelp(options);
        } else {
            returnValue = processActions(cmd);
        }

        return returnValue;
    }

    private static void greeting() {
        Printer.writeOut("{0} version {1}", EngineInformation.getName(), EngineInformation.getVersion());
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
        final Option[] unavailable = new Option[] { PRINT, CHECK_ASSERTIONS, DEBUG, OUTPUT, EXTRACT_HTML, REPORT_POSTFIX, REPORT_PREFIX };
        warnUnusedOptions(cmd, unavailable, true);
        final ConfigurationLoader config = Configuration.load(determineDefinition(cmd), determineRepository(cmd));
        final Daemon validDaemon = new Daemon(determineHost(cmd), determinePort(cmd), determineThreads(cmd));
        if (cmd.hasOption(DISABLE_GUI.getOpt())) {
            validDaemon.setGuiEnabled(false);
        }
        validDaemon.startServer(config.build());
        return DAEMON_SIGNAL;
    }

    private static void warnUnusedOptions(final CommandLine cmd, final Option[] unavailable, final boolean daemon) {
        Arrays.stream(cmd.getOptions()).filter(o -> ArrayUtils.contains(unavailable, o))
                .map(o -> "The option " + o.getLongOpt() + " is not available in daemon mode").forEach(log::error);
        if (daemon && !cmd.getArgList().isEmpty()) {
            log.info("Ignoring test targets in daemon mode");
        }
    }

    private static int processActions(final CommandLine cmd) {
        try {

            long start = System.currentTimeMillis();
            final Option[] unavailable = new Option[] { HOST, PORT, WORKER_COUNT, DISABLE_GUI };
            warnUnusedOptions(cmd, unavailable, false);
            final URI scenarioLocation = determineDefinition(cmd);
            final URI repositoryLocation = determineRepository(cmd);
            reportConfiguration(scenarioLocation, repositoryLocation);
            final Configuration config = Configuration.load(scenarioLocation, repositoryLocation).build();

            final InternalCheck check = new InternalCheck(config);
            final Path outputDirectory = determineOutputDirectory(cmd);

            final Processor processor = config.getContentRepository().getProcessor();
            if (cmd.hasOption(EXTRACT_HTML.getOpt())) {
                check.getCheckSteps().add(new ExtractHtmlContentAction(processor, outputDirectory));
            }
            check.getCheckSteps().add(new SerializeReportAction(outputDirectory, processor, determineNamingStrategy(cmd)));
            if (cmd.hasOption(SERIALIZE_REPORT_INPUT.getOpt())) {
                check.getCheckSteps().add(new SerializeReportInputAction(outputDirectory, check.getConversionService()));
            }
            if (cmd.hasOption(PRINT.getOpt())) {
                check.getCheckSteps().add(new PrintReportAction(processor));
            }

            if (cmd.hasOption(CHECK_ASSERTIONS.getOpt())) {
                final Assertions assertions = loadAssertions(cmd.getOptionValue(CHECK_ASSERTIONS.getOpt()));
                check.getCheckSteps().add(new CheckAssertionAction(assertions, processor));
            }
            if (cmd.hasOption(PRINT_MEM_STATS.getOpt())) {
                check.getCheckSteps().add(new PrintMemoryStats());
            }
            printScenarios(check.getConfiguration());
            log.info("Setup completed in {}ms\n", System.currentTimeMillis() - start);

            final Collection<Path> targets = determineTestTargets(cmd);
            start = System.currentTimeMillis();
            final Map<Path, Result> results = new HashMap<>();
            Printer.writeOut("\nProcessing of {0} objects started", targets.size());
            long tick = System.currentTimeMillis();
            for (final Path p : targets) {
                final Input input = InputFactory.read(p);
                results.put(p, check.checkInput(input));
                if (((System.currentTimeMillis() - tick) / 1000) > 5) {
                    tick = System.currentTimeMillis();
                    Printer.writeOut("{0}/{1} objects processed", results.size(), targets.size());
                }
            }
            final long processingTime = System.currentTimeMillis() - start;
            Printer.writeOut("Processing of {0} objects completed in {1}ms", targets.size(), processingTime);
            Printer.writeOut("Results:");
            final boolean result = check.printAndEvaluate(results);
            log.info("Processing {} object(s) completed in {}ms", targets.size(), processingTime);
            return result ? 0 : 1;

        } catch (final Exception e) {
            e.printStackTrace();// NOSONAR
            if (cmd.hasOption(DEBUG.getOpt())) {
                log.error(e.getMessage(), e);
            } else {
                log.error(e.getMessage());
            }
            return -1;
        }
    }

    private static void reportConfiguration(final URI scenarioLocation, final URI repositoryLocation) {
        Printer.writeOut("Loading scenarios from  {0}", scenarioLocation);
        Printer.writeOut("Using repository  {0}", repositoryLocation);
    }

    private static void printScenarios(final Configuration configuration) {
        Printer.writeOut("Loaded \"{0} {1}\" by {2} from {3} ", configuration.getName(), "1", configuration.getAuthor(),
                configuration.getDate());
        Printer.writeOut("\nThe following scenarios are available:");
        configuration.getScenarios().forEach(e -> {
            final Line line = new Line(Code.GREEN);
            line.add("  * " + e.getName());
            Printer.writeOut(line.render(false, false));
        });
    }

    private static NamingStrategy determineNamingStrategy(final CommandLine cmd) {
        final DefaultNamingStrategy namingStrategy = new DefaultNamingStrategy();
        if (cmd.hasOption(REPORT_PREFIX.getLongOpt())) {
            namingStrategy.setPrefix(cmd.getOptionValue(REPORT_PREFIX.getLongOpt()));
        }
        if (cmd.hasOption(REPORT_POSTFIX.getLongOpt())) {
            namingStrategy.setPostfix(cmd.getOptionValue(REPORT_POSTFIX.getLongOpt()));
        }

        return namingStrategy;
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
        try ( final Stream<Path> stream = Files.list(d) ) {
            return stream.filter(path -> path.toString().endsWith(".xml")).collect(Collectors.toList());
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

}
