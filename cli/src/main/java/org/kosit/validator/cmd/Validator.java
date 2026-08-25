package org.kosit.validator.cmd;

import static org.apache.commons.lang3.ObjectUtils.getIfNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.fusesource.jansi.AnsiRenderer.Code;
import org.kosit.validator.api.VConfiguration;
import org.kosit.validator.api.VInput;
import org.kosit.validator.api.VInputFactory;
import org.kosit.validator.api.VResult;
import org.kosit.validator.cmd.CommandLineOptions.CliOptions;
import org.kosit.validator.cmd.CommandLineOptions.RepositoryDefinition;
import org.kosit.validator.cmd.CommandLineOptions.ScenarioDefinition;
import org.kosit.validator.cmd.report.Line;
import org.kosit.validator.impl.EngineInformation;
import org.kosit.validator.impl.ScenarioRepository;
import org.kosit.validator.impl.xml.ProcessorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.Processor;

/**
 * Actual evaluation and processing of CommandLineOptions arguments.
 * 
 * @author Andreas Penski
 */
@SuppressWarnings("squid:S3725")
public class Validator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Validator.class);

    private Validator() {
        // hide
    }

    /**
     * Main program for the command line application.
     *
     * @param cmd parsed commandline.
     */
    static ReturnValue mainProgram(final CommandLineOptions cmd) {
        greeting(cmd.getEngineInformation());
        final ReturnValue returnValue;
        try {
            if (cmd.isCliModeEnabled() || isPiped()) {
                returnValue = processActions(cmd);
            } else {
                Printer.writeErr("No test target found");
                returnValue = ReturnValue.CONFIGURATION_ERROR;
            }
        } catch (final Exception e) {
            e.printStackTrace();
            Printer.writeErr(e.getMessage());
            if (cmd.isDebugOutput()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
            return ReturnValue.CONFIGURATION_ERROR;
        }
        return returnValue;
    }

    private static void greeting(final EngineInformation engineInformation) {
        Printer.writeOut("{0} version {1}", engineInformation.getName(), engineInformation.getVersion());
    }

    private static ReturnValue processActions(final CommandLineOptions cmd) throws IOException {
        long start = System.currentTimeMillis();
        final Processor processor = ProcessorProvider.getProcessor();
        final List<VConfiguration> config = getConfiguration(cmd);
        final InternalVCheck check = new InternalVCheck(cmd.getEngineInformation(), processor, config.toArray(new VConfiguration[0]));
        final CommandLineOptions.CliOptions cliOptions = getIfNull(cmd.getCliOptions(), new CliOptions());
        final Path outputDirectory = determineOutputDirectory(cliOptions);
        if (cliOptions.isExtractReport()) {
            check.getCheckSteps().add(new ExtractReportContentAction(processor, outputDirectory));
        }
        check.getCheckSteps()
                .add(new SerializeReportAction(outputDirectory, check.getXvrlConversionService(), determineNamingStrategy(cliOptions)));
        if (cliOptions.isPrintReport()) {
            check.getCheckSteps().add(new PrintReportAction(processor));
        }
        if (cliOptions.isPrintMemoryStats()) {
            check.getCheckSteps().add(new PrintMemoryStats());
        }
        LOGGER.info("Setup completed in {}ms\n", System.currentTimeMillis() - start);
        final Collection<VInput> targets = determineTestTargets(cliOptions);
        start = System.currentTimeMillis();
        final Map<String, VResult> results = new HashMap<>();
        Printer.writeOut("\nProcessing of {0} object(s) started", targets.size());
        long tick = System.currentTimeMillis();
        for (final VInput input : targets) {
            results.put(input.getName(), check.checkInput(input));
            if (((System.currentTimeMillis() - tick) / 1000) > 5) {
                tick = System.currentTimeMillis();
                Printer.writeOut("{0}/{1} object(s) processed", results.size(), targets.size());
            }
        }
        final long processingTime = System.currentTimeMillis() - start;
        Printer.writeOut("Processing of {0} object(s) completed in {1}ms", targets.size(), processingTime);
        check.printResults(results);
        LOGGER.info("Processing {} object(s) completed in {}ms", targets.size(), processingTime);
        return check.isSuccessful(results) ? ReturnValue.SUCCESS : ReturnValue.createFailed(check.getNotAcceptableCount(results));
    }

    /**
     * @param cmd the Command Line Options
     *
     * @return a list of configurations of the scenarios and repositories passed in cmd
     */
    private static List<VConfiguration> getConfiguration(final CommandLineOptions cmd) {
        final List<ScenarioDefinition> scenarios = getIfNull(cmd.getScenarios(), Collections.emptyList());
        // Map from scenario name to scenario path
        final Map<String, Path> mappedScenarios = scenarios.stream()
                .collect(Collectors.toMap(ScenarioDefinition::getName, ScenarioDefinition::getPath));
        final List<RepositoryDefinition> repos = getIfNull(cmd.getRepositories(), Collections.emptyList());
        final Map<String, Path> mappedRepos = repos.stream()
                .collect(Collectors.toMap(RepositoryDefinition::getName, RepositoryDefinition::getPath));
        checkUnused(mappedScenarios, mappedRepos);
        return mappedScenarios.entrySet().stream().map(e -> {
            assertFileExistance(e.getValue(), "scenario");
            final URI scenarioLocation = e.getValue().toUri();
            final URI repositoryLocation = findRepository(scenarioLocation, e.getKey(), mappedRepos);
            reportLoading(scenarioLocation, repositoryLocation);
            final VConfiguration configuration = VConfiguration.load(scenarioLocation, repositoryLocation)
                    .build(ProcessorProvider.getProcessor());
            reportConfiguration(configuration);
            return configuration;
        }).toList();
    }

    private static void checkUnused(final Map<String, Path> scenarios, final Map<String, Path> repositories) {
        // Must use collect for a mutable
        final List<Entry<String, Path>> unused = repositories.entrySet().stream().filter(e -> scenarios.get(e.getKey()) == null)
                .collect(Collectors.toList());
        unused.removeIf(e -> e.getKey().equals(ScenarioRepository.DEFAULT_ID));
        unused.forEach(e -> Printer.writeErr("Warning: repository definition \"{0}\" is not used", e.getKey()));
    }

    private static URI findRepository(final URI scenarioLocation, final String key, final Map<String, Path> repositories) {
        final Path path = repositories.getOrDefault(key, repositories.get(ScenarioRepository.DEFAULT_ID));
        if (path == null) {
            // If it is an unnamed scenario, use the CWD instead
            if (key.startsWith(ScenarioRepository.DEFAULT)) {
                // Assume directory of scenario location instead
                return Paths.get(scenarioLocation).getParent().toUri();
            }
            throw new IllegalArgumentException("No repository location for scenario definition '" + key + "' specified");
        }
        return determineRepository(path);
    }

    private static void reportLoading(final URI scenarioLocation, final URI repositoryLocation) {
        Printer.writeOut("Loading scenarios from  {0}", scenarioLocation);
        Printer.writeOut("Using repository  {0}", repositoryLocation);
        Printer.writeOut(EMPTY);
    }

    private static void reportConfiguration(final VConfiguration configuration) {
        Printer.writeOut("Loaded \"{0}\" by {1} from {2} ", configuration.getName(), configuration.getAuthor(), configuration.getDate());
        Printer.writeOut("The following scenarios are available:");
        configuration.getScenarios().forEach(e -> {
            final Line line = new Line(Code.GREEN);
            line.add("  * " + e.getName());
            Printer.writeOut(line.render(false, false));
        });
        Printer.writeOut(EMPTY);
    }

    private static NamingStrategy determineNamingStrategy(final CommandLineOptions.CliOptions cmd) {
        final DefaultNamingStrategy namingStrategy = new DefaultNamingStrategy();
        if (isNotEmpty(cmd.getReportPrefix())) {
            namingStrategy.setPrefix(cmd.getReportPrefix());
        }
        if (isNotEmpty(cmd.getReportPostfix())) {
            namingStrategy.setPostfix(cmd.getReportPostfix());
        }
        return namingStrategy;
    }

    private static Path determineOutputDirectory(final CommandLineOptions.CliOptions cmd) {
        final Path dir;
        if (cmd.getOutputPath() != null) {
            dir = cmd.getOutputPath();
            if ((!Files.exists(dir) && !dir.toFile().mkdirs()) || !Files.isDirectory(dir)) {
                throw new IllegalStateException("Invalid target directory " + dir + " specified");
            }
        } else {
            dir = Paths.get(""/* cwd */);
        }
        return dir;
    }

    private static Collection<VInput> determineTestTargets(final CommandLineOptions.CliOptions cmd) throws IOException {
        final Collection<VInput> targets = new ArrayList<>();
        if (cmd.getFiles() != null && !cmd.getFiles().isEmpty()) {
            cmd.getFiles().forEach(e -> targets.addAll(determineTestTarget(e)));
        }
        if (isPiped()) {
            targets.add(readFromPipe());
        }
        if (targets.isEmpty()) {
            throw new IllegalStateException("No test targets found. Nothing to check. Will quit now!");
        }
        return targets;
    }

    // sanitation is delegated to xml stack
    @SuppressWarnings("java:S4829")
    private static boolean isPiped() throws IOException {
        return System.in.available() > 0;
    }

    // sanitation is delegated to xml stack
    @SuppressWarnings("java:S4829")
    private static VInput readFromPipe() {
        return VInputFactory.read(System.in, "stdin");
    }

    private static Collection<VInput> determineTestTarget(final Path d) {
        if (Files.isDirectory(d)) {
            return listDirectoryTargets(d);
        }
        if (Files.exists(d)) {
            return Collections.singleton(VInputFactory.read(d));
        }
        LOGGER.warn("The specified test target {} does not exist. Will be ignored", d);
        return Collections.emptyList();
    }

    private static Collection<VInput> listDirectoryTargets(final Path d) {
        try ( Stream<Path> stream = Files.list(d) ) {
            return stream.filter(path -> path.toString().toLowerCase().endsWith(".xml")).map(VInputFactory::read).toList();
        } catch (final IOException e) {
            throw new IllegalStateException("IOException while list directory content. Can not determine test targets.", e);
        }
    }

    private static URI determineRepository(final Path d) {
        if (Files.isDirectory(d)) {
            return d.toUri();
        }
        throw new IllegalArgumentException("Not a valid path for repository definition specified: '" + d.toAbsolutePath() + "'");
    }

    private static void assertFileExistance(final Path f, final String type) {
        if (!Files.isRegularFile(f)) {
            throw new IllegalArgumentException("Not a valid path for " + type + " definition specified: '" + f.toAbsolutePath() + "'");
        }
    }
}
