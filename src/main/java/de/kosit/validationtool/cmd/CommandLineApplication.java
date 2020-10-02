/*
 * Copyright 2017-2020  Koordinierungsstelle für IT-Standards (KoSIT)
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

import static de.kosit.validationtool.cmd.CommandLineOptions.HELP;
import static de.kosit.validationtool.cmd.CommandLineOptions.createHelpOptions;
import static de.kosit.validationtool.cmd.CommandLineOptions.createOptions;
import static de.kosit.validationtool.cmd.CommandLineOptions.printHelp;
import static de.kosit.validationtool.impl.Printer.writeErr;

import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.fusesource.jansi.AnsiRenderer.Code;

import de.kosit.validationtool.cmd.report.Line;
import de.kosit.validationtool.impl.Printer;

/**
 * Commandline interface of the validator. It parses the commandline args and hands over actual execution to
 * {@link Validator}.
 * 
 * This separated from {@link Validator} to configure the slf4j simple logging.
 *
 * @author Andreas Penski
 */
// performance is not a problem here
public class CommandLineApplication {

    private CommandLineApplication() {
        // main class -> hide constructor
    }

    /**
     * Main.
     *
     * @param args die Eingabe-Argumente
     */
    public static void main(final String[] args) {
        final ReturnValue resultStatus = mainProgram(args);
        if (!resultStatus.equals(ReturnValue.DAEMON_MODE)) {
            sayGoodby(resultStatus);
            System.exit(resultStatus.getCode());
        } else {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> Printer.writeOut("Shutting down daemon ...")));
        }
    }

    private static void sayGoodby(final ReturnValue resultStatus) {
        Printer.writeOut("\n##############################");
        if (resultStatus.equals(ReturnValue.SUCCESS)) {
            Printer.writeOut("#   " + new Line(Code.GREEN).add("Validation succesful!").render(false, false) + "    #");
        } else {
            Printer.writeOut("#     " + new Line(Code.RED).add("Validation failed!").render(false, false) + "     #");
        }
        Printer.writeOut("##############################");
    }

    // for testing purposes. Unless jvm is terminated during tests. See above
    static ReturnValue mainProgram(final String[] args) {

        final Options options = createOptions();
        ReturnValue resultStatus;
        try {
            if (isHelpRequested(args)) {
                printHelp(options);
                resultStatus = ReturnValue.SUCCESS;
            } else {
                final CommandLineParser parser = new DefaultParser();
                final CommandLine cmd = parser.parse(options, args);
                configureLogging(cmd);
                resultStatus = Validator.mainProgram(cmd);
            }
        } catch (final ParseException e) {
            writeErr("Error processing command line arguments: {0}", e.getMessage(), e);
            printHelp(options);
            resultStatus = ReturnValue.PARSING_ERROR;
        }
        return resultStatus;
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

    private static void configureLogging(final CommandLine cmd) throws ParseException {
        if (cmd.hasOption(CommandLineOptions.DEBUG_LOG.getOpt())) {
            System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");
        } else if (cmd.hasOption(CommandLineOptions.LOG_LEVEL.getOpt())) {

            final String level = Level.resolve(cmd.getOptionValue(CommandLineOptions.LOG_LEVEL.getOpt()));
            System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, level);
        } else {
            System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "OFF");
        }
    }

    private enum Level {

        INFO, WARN, DEBUG, TRACE, ERROR, OFF;

        static String resolve(final String optionValue) throws ParseException {
            return Arrays.stream(values()).filter(e -> e.name().equalsIgnoreCase(optionValue)).map(Enum::name).findFirst()
                    .orElseThrow(() -> new ParseException("Either specify trace,debug,info,warn,error as log level"));
        }
    }

}
