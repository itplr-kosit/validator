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
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

    private static final Option CHECK_ASSERTIONS = Option.builder("c").longOpt("check-assertions").hasArg()
            .desc("Check the result using defined assertions").argName("assertions-file").build();

    private CommandLineApplication() {
        // main class -> hide constructor
    }

    /**
     * Main-Funktion für die Kommandozeilen-Applikation.
     *
     * @param args die Eingabe-Argumente
     */
    public static void main(String[] args) {
        final int resultStatus = mainProgram(args);
        System.exit(resultStatus);
    }

    /**
     * Hauptprogramm für die Kommandozeilen-Applikation.
     *
     * @param args die Eingabe-Argumente
     */
    static int mainProgram(String[] args) {
        Options options = createOptions();
        if (isHelpRequested(args)) {
            printHelp(options);
        } else {
            try {
                CommandLineParser parser = new DefaultParser();
                final CommandLine cmd = parser.parse(options, args);
                if (cmd.getArgList().isEmpty()) {
                    printHelp(createOptions());
                } else {
                    return processActions(cmd);
                }
            } catch (ParseException e) {
                log.error("Error processing command line arguments: " + e.getMessage());
                printHelp(options);
            }
        }
        return 0;
    }

    private static boolean isHelpRequested(String[] args) {
        Options helpOptions = createHelpOptions();
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(helpOptions, args, true);
            if (cmd.hasOption(HELP.getOpt()) || args.length == 0) {
                return true;
            }
        } catch (ParseException e) {
            // we can ignore that, we just look for the help parameters
        }
        return false;
    }

    private static int processActions(CommandLine cmd) {
        try {

            long start = System.currentTimeMillis();
            CheckConfiguration d = new CheckConfiguration(determineDefinition(cmd));
            d.setScenarioRepository(determineRepository(cmd));
            InternalCheck check = new InternalCheck(d);
            Path outputDirectory = determineOutputDirectory(cmd);

            if (cmd.hasOption(EXTRACT_HTML.getOpt())) {
                check.getCheckSteps().add(new ExtractHtmlContentAction(check.getContentRepository(), outputDirectory));
            }
            check.getCheckSteps().add(new SerializeReportAction(outputDirectory));
            if (cmd.hasOption(PRINT.getOpt())) {
                check.getCheckSteps().add(new PrintReportAction());
            }
            if (cmd.hasOption(CHECK_ASSERTIONS.getOpt())) {
                Assertions assertions = loadAssertions(cmd.getOptionValue(CHECK_ASSERTIONS.getOpt()));
                check.getCheckSteps().add(new CheckAssertionAction(assertions, ObjectFactory.createProcessor()));
            }

            log.info("Setup completed in {}ms\n", System.currentTimeMillis() - start);

            final Collection<Path> targets = determineTestTargets(cmd);
            start = System.currentTimeMillis();
            for (Path p : targets) {
                final Input input = InputFactory.read(p);
                check.checkInput(input);
            }
            boolean result = check.printAndEvaluate();
            log.info("Processing {} object(s) completed in {}ms", targets.size(), System.currentTimeMillis() - start);
            return result ? 0 : 1;

        } catch (Exception e) {
            if (cmd.hasOption(DEBUG.getOpt())) {
                log.error(e.getMessage(), e);
            } else {
                log.error(e.getMessage());
            }
            return -1;
        }
    }

    private static Assertions loadAssertions(String optionValue) {
        Path p = Paths.get(optionValue);
        Assertions a = null;
        if (Files.exists(p)) {
            ConversionService c = new ConversionService();
            c.initialize(de.kosit.validationtool.cmd.assertions.ObjectFactory.class.getPackage());
            a = c.readXml(p.toUri(), Assertions.class);
        }
        return a;
    }

    private static Path determineOutputDirectory(CommandLine cmd) {
        final String value = cmd.getOptionValue(OUTPUT.getOpt());
        Path fir;
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

    private static Collection<Path> determineTestTargets(CommandLine cmd) {
        Collection<Path> targets = new ArrayList<>();
        if (!cmd.getArgList().isEmpty()) {
            cmd.getArgList().forEach(e -> targets.addAll(determineTestTarget(e)));
        }
        if (targets.isEmpty()) {
            throw new IllegalStateException("No test targets found. Nothing to check. Will quit now!");
        }
        return targets;
    }

    private static Collection<Path> determineTestTarget(String s) {
        Path d = Paths.get(s);
        if (Files.isDirectory(d)) {
            return listDirectoryTargets(d);
        } else if (Files.exists(d)) {
            return Collections.singleton(d);
        }
        log.warn("The specified test target {} does not exist. Will be ignored", s);
        return Collections.emptyList();

    }

    private static Collection<Path> listDirectoryTargets(Path d) {
        try {
            return Files.list(d).filter(path -> path.toString().endsWith(".xml")).collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException("IOException while liste directory content. Can not determine test targets.", e);
        }

    }

    private static URI determineRepository(CommandLine cmd) throws MalformedURLException {
        if (checkOptionWithValue(REPOSITORY, cmd)) {
            Path d = Paths.get(cmd.getOptionValue(REPOSITORY.getOpt()));
            if (Files.isDirectory(d)) {
                return d.toUri();
            } else {
                throw new IllegalArgumentException(
                        String.format("Not a valid path for scenario definition specified: '%s'", d.toAbsolutePath()));
            }
        }
        return null;
    }

    private static URI determineDefinition(CommandLine cmd) throws MalformedURLException {
        checkOptionWithValue(SCENARIOS, cmd);
        Path f = Paths.get(cmd.getOptionValue(SCENARIOS.getOpt()));
        if (Files.isRegularFile(f)) {
            return f.toAbsolutePath().toUri();
        } else {
            throw new IllegalArgumentException(
                    String.format("Not a valid path for scenario definition specified: '%s'", f.toAbsolutePath()));
        }
    }

    private static boolean checkOptionWithValue(Option option, CommandLine cmd) {
        String opt = option.getOpt();
        if (cmd.hasOption(opt)) {
            String value = cmd.getOptionValue(opt);
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

    private static void printHelp(Options options) {
        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("check-tool  -s <scenario-config-file> [OPTIONS] [FILE]... ", options, false);
    }

    private static Options createHelpOptions() {
        Options options = new Options();
        options.addOption(HELP);
        return options;
    }

    private static Options createOptions() {
        Options options = new Options();
        options.addOption(HELP);
        options.addOption(SCENARIOS);
        options.addOption(REPOSITORY);
        options.addOption(PRINT);
        options.addOption(OUTPUT);
        options.addOption(EXTRACT_HTML);
        options.addOption(DEBUG);
        options.addOption(CHECK_ASSERTIONS);
        return options;
    }
}
