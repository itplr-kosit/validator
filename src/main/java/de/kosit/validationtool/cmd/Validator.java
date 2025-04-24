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

import de.kosit.validationtool.api.Configuration;
import de.kosit.validationtool.api.Input;
import de.kosit.validationtool.api.InputFactory;
import de.kosit.validationtool.api.Result;
import de.kosit.validationtool.cmd.CommandLineOptions.CliOptions;
import de.kosit.validationtool.cmd.CommandLineOptions.Definition;
import de.kosit.validationtool.cmd.CommandLineOptions.RepositoryDefinition;
import de.kosit.validationtool.cmd.CommandLineOptions.ScenarioDefinition;
import de.kosit.validationtool.cmd.assertions.Assertions;
import de.kosit.validationtool.cmd.report.Line;
import de.kosit.validationtool.daemon.Daemon;
import de.kosit.validationtool.impl.ConversionService;
import de.kosit.validationtool.impl.EngineInformation;
import de.kosit.validationtool.impl.Printer;
import de.kosit.validationtool.impl.ScenarioRepository;
import de.kosit.validationtool.impl.xml.ProcessorProvider;
import lombok.extern.slf4j.Slf4j;
import net.sf.saxon.s9api.Processor;
import org.fusesource.jansi.AnsiRenderer.Code;

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

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Actual evaluation and processing of CommandLineOptions argumtens.
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
    static ReturnValue mainProgram(final CommandLineOptions cmd) {
        greeting();
        final ReturnValue returnValue;
        try {
            if (cmd.isDaemonModeEnabled()) {
                startDaemonMode(cmd);
                returnValue = ReturnValue.DAEMON_MODE;
            } else if (cmd.isCliModeEnabled() || isPiped()) {
                returnValue = processActions(cmd);
            } else {
                Printer.writeErr("No test target found");
                returnValue = ReturnValue.CONFIGURATION_ERROR;
            }
        } catch (final Exception e) {
            Printer.writeErr(e.getMessage());
            if (cmd.isDebugOutput()) {
                log.error(e.getMessage(), e);
            } else {
                log.error(e.getMessage());
            }
            return ReturnValue.CONFIGURATION_ERROR;
        }
        return returnValue;
    }

    private static void greeting() {
        Printer.writeOut("{0} version {1}", EngineInformation.getName(), EngineInformation.getVersion());
    }

    private static int determineThreads(final CommandLineOptions.DaemonOptions cmd) {
        int threads = Runtime.getRuntime().availableProcessors();
        if (cmd.getWorkerCount() > 0) {
            threads = cmd.getWorkerCount();
        }
        return threads;
    }

    private static void startDaemonMode(final CommandLineOptions cmd) {
        if (cmd.isCliModeEnabled()) {
            Printer.writeErr("Mixed mode configuration detected. Use either daemon mode or cli mode commandline options. They are mutual "
                    + "exclusive. Will ignore cli mode options");
        }
        final List<Configuration> configuration = getConfiguration(cmd);
        final CommandLineOptions.DaemonOptions daemonOptions = cmd.getDaemonOptions();
        final Daemon validDaemon = new Daemon(daemonOptions.getHost(), daemonOptions.getPort(), determineThreads(daemonOptions));
        validDaemon.setGuiEnabled(!daemonOptions.isDisableGUI());
        Printer.writeOut("\nStarting daemon mode ...");
        validDaemon.startServer(ProcessorProvider.getProcessor(), configuration.toArray(new Configuration[configuration.size()]));
    }

    private static ReturnValue processActions(final CommandLineOptions cmd) throws IOException {
        long start = System.currentTimeMillis();
        final Processor processor = ProcessorProvider.getProcessor();
        final List<Configuration> config = getConfiguration(cmd);
        final InternalCheck check = new InternalCheck(processor, config.toArray(new Configuration[0]));
        final CommandLineOptions.CliOptions cliOptions = defaultIfNull(cmd.getCliOptions(), new CliOptions());
        final Path outputDirectory = determineOutputDirectory(cliOptions);
        if (cliOptions.isExtractHtml()) {
            check.getCheckSteps().add(new ExtractHtmlContentAction(processor, outputDirectory));
        }
        check.getCheckSteps().add(new SerializeReportAction(outputDirectory, processor, determineNamingStrategy(cliOptions)));
        if (cliOptions.isSerializeInput()) {
            check.getCheckSteps().add(new SerializeReportInputAction(outputDirectory, check.getConversionService()));
        }
        if (cliOptions.isPrintReport()) {
            check.getCheckSteps().add(new PrintReportAction(processor));
        }

        if (cliOptions.getAssertions() != null) {
            final Assertions assertions = loadAssertions(cliOptions.getAssertions());
            check.getCheckSteps().add(new CheckAssertionAction(assertions, processor));
        }
        if (cliOptions.isPrintMemoryStats()) {
            check.getCheckSteps().add(new PrintMemoryStats());
        }
        log.info("Setup completed in {}ms\n", System.currentTimeMillis() - start);

        final Collection<Input> targets = determineTestTargets(cliOptions);
        start = System.currentTimeMillis();
        final Map<String, Result> results = new HashMap<>();
        Printer.writeOut("\nProcessing of {0} objects started", targets.size());
        long tick = System.currentTimeMillis();
        for (final Input input : targets) {
            results.put(input.getName(), check.checkInput(input));
            if (((System.currentTimeMillis() - tick) / 1000) > 5) {
                tick = System.currentTimeMillis();
                Printer.writeOut("{0}/{1} objects processed", results.size(), targets.size());
            }
        }
        final long processingTime = System.currentTimeMillis() - start;
        Printer.writeOut("Processing of {0} objects completed in {1}ms", targets.size(), processingTime);

        check.printResults(results);
        log.info("Processing {} object(s) completed in {}ms", targets.size(), processingTime);
        return check.isSuccessful(results) ? ReturnValue.SUCCESS : ReturnValue.createFailed(check.getNotAcceptableCount(results));
    }

    /**
     * @param cmd the Command Line Options
     *
     * @return a list of configurations of the scenarios and repositories passed in cmd
     */
    private static List<Configuration> getConfiguration(final CommandLineOptions cmd) {
        final List<ScenarioDefinition> scenarios = defaultIfNull(cmd.getScenarios(), Collections.emptyList());
        final Map<String, Path> mappedScenarios = scenarios.stream()
                .collect(Collectors.toMap(ScenarioDefinition::getName, ScenarioDefinition::getPath));
        final List<RepositoryDefinition> repos = defaultIfNull(cmd.getRepositories(), Collections.emptyList());
        final Map<String, Path> mappedRepos = repos.stream().collect(Collectors.toMap(Definition::getName, Definition::getPath));
        checkUnused(mappedScenarios, mappedRepos);

        return mappedScenarios.entrySet().stream().map(e -> {
            assertFileExistance(e.getValue(), "scenario");
            final URI scenarioLocation = e.getValue().toUri();
            final URI repositoryLocation = findRepository(e.getKey(), mappedRepos);

            reportLoading(scenarioLocation, repositoryLocation);
            final Configuration configuration = Configuration.load(scenarioLocation, repositoryLocation)
                    .build(ProcessorProvider.getProcessor());
            reportConfiguration(configuration);
            return configuration;
        }).collect(Collectors.toList());

    }

    private static void checkUnused(final Map<String, Path> scenarios, final Map<String, Path> repositories) {
        final List<Entry<String, Path>> unused = repositories.entrySet().stream().filter(e -> scenarios.get(e.getKey()) == null)
                .collect(Collectors.toList());
        unused.removeIf(e -> e.getKey().equals(ScenarioRepository.DEFAULT_ID));
        unused.forEach(e -> Printer.writeErr("Warning: repository definition \"{0}\" is not used", e.getKey()));
    }

    private static URI findRepository(final String key, final Map<String, Path> repositories) {
        final Path path = repositories.getOrDefault(key, repositories.get(ScenarioRepository.DEFAULT_ID));
        if (path == null) {
            throw new IllegalArgumentException(String.format("No repository location for scenario definition '%s' specified", key));
        }
        return determineRepository(path);
    }

    private static void reportLoading(final URI scenarioLocation, final URI repositoryLocation) {
        Printer.writeOut("Loading scenarios from  {0}", scenarioLocation);
        Printer.writeOut("Using repository  {0}", repositoryLocation);
        Printer.writeOut(EMPTY);
    }

    private static void reportConfiguration(final Configuration configuration) {
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

    private static Assertions loadAssertions(final Path p) {
        Assertions a = null;
        if (Files.exists(p)) {
            final ConversionService c = new ConversionService();
            c.initialize(de.kosit.validationtool.cmd.assertions.ObjectFactory.class.getPackage());
            a = c.readXml(p.toUri(), Assertions.class);
        }
        return a;
    }

    private static Path determineOutputDirectory(final CommandLineOptions.CliOptions cmd) {
        final Path dir;
        if (cmd.getOutputPath() != null) {
            dir = cmd.getOutputPath();
            if ((!Files.exists(dir) && !dir.toFile().mkdirs()) || !Files.isDirectory(dir)) {
                throw new IllegalStateException(String.format("Invalid target directory %s specified", dir));
            }
        } else {
            dir = Paths.get(""/* cwd */);
        }
        return dir;
    }

    private static Collection<Input> determineTestTargets(final CommandLineOptions.CliOptions cmd) throws IOException {
        final Collection<Input> targets = new ArrayList<>();
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

    @SuppressWarnings("java:S4829") // sanitation is delegated to xml stack
    private static boolean isPiped() throws IOException {
        return System.in.available() > 0;
    }

    @SuppressWarnings("java:S4829") // sanitation is delegated to xml stack
    private static Input readFromPipe() {
        return InputFactory.read(System.in, "stdin");
    }

    private static Collection<Input> determineTestTarget(final Path d) {
        if (Files.isDirectory(d)) {
            return listDirectoryTargets(d);
        } else if (Files.exists(d)) {
            return Collections.singleton(InputFactory.read(d));
        }
        log.warn("The specified test target {} does not exist. Will be ignored", d);
        return Collections.emptyList();

    }

    private static Collection<Input> listDirectoryTargets(final Path d) {
        try ( final Stream<Path> stream = Files.list(d) ) {
            return stream.filter(path -> path.toString().toLowerCase().endsWith(".xml")).map(InputFactory::read)
                    .collect(Collectors.toList());
        } catch (final IOException e) {
            throw new IllegalStateException("IOException while list directory content. Can not determine test targets.", e);
        }

    }

    private static URI determineRepository(final Path d) {
        if (Files.isDirectory(d)) {
            return d.toUri();
        } else {
            throw new IllegalArgumentException(
                    String.format("Not a valid path for repository definition specified: '%s'", d.toAbsolutePath()));
        }

    }

    @SuppressWarnings("SameParameterValue")
    private static void assertFileExistance(final Path f, final String type) {
        if (!Files.isRegularFile(f)) {
            throw new IllegalArgumentException(
                    String.format("Not a valid path for %s definition specified: '%s'", type, f.toAbsolutePath()));
        }
    }

}
