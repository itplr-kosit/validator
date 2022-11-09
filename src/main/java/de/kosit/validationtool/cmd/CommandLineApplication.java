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

import static de.kosit.validationtool.impl.Printer.writeErr;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import org.apache.commons.lang3.ObjectUtils;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.AnsiRenderer.Code;

import de.kosit.validationtool.cmd.report.Line;
import de.kosit.validationtool.impl.Printer;

import picocli.CommandLine;
import picocli.CommandLine.ParseResult;

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
        AnsiConsole.systemInstall();
        final ReturnValue resultStatus = mainProgram(args);
        if (!resultStatus.equals(ReturnValue.DAEMON_MODE)) {
            if (!resultStatus.equals(ReturnValue.HELP_REQUEST) && resultStatus.getCode() >= 0) {
                sayGoodby(resultStatus);
            }
            System.exit(resultStatus.getCode());
        } else {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> Printer.writeOut("Shutting down daemon ...")));
        }
    }

    private static void sayGoodby(final ReturnValue resultStatus) {
        Printer.writeOut("\n##############################");
        if (resultStatus.equals(ReturnValue.SUCCESS)) {
            Printer.writeOut("#   " + new Line(Code.GREEN).add("Validation successful!").render(false, false) + "   #");
        } else {
            Printer.writeOut("#     " + new Line(Code.RED).add("Validation failed!").render(false, false) + "     #");
        }
        Printer.writeOut("##############################");
    }

    // for testing purposes. Unless jvm is terminated during tests. See above
    static ReturnValue mainProgram(final String[] args) {

        ReturnValue resultStatus;
        final CommandLine commandLine = new CommandLine(new CommandLineOptions());
        try {
            commandLine.setExecutionExceptionHandler(CommandLineApplication::logExecutionException);
            commandLine.execute(args);
            if (commandLine.isUsageHelpRequested()) {
                resultStatus = ReturnValue.HELP_REQUEST;
            } else {
                resultStatus = ObjectUtils.defaultIfNull(commandLine.getExecutionResult(), ReturnValue.PARSING_ERROR);
                if (resultStatus.getCode() < 0) {
                    commandLine.usage(System.out);
                }
            }

        } catch (final Exception e) {
            writeErr("Error processing command line arguments: {0}", e.getMessage(), e);
            resultStatus = ReturnValue.PARSING_ERROR;
        }
        return resultStatus;
    }

    private static int logExecutionException(final Exception ex, final CommandLine cli, final ParseResult parseResult) {
        final String message = isNotEmpty(ex.getMessage()) ? ex.getMessage() : "Es ist eine Fehler aufgetreten";
        Printer.writeErr(ex, message);
        return 1;
    }

    enum Level {

        INFO, WARN, DEBUG, TRACE, ERROR, OFF;

    }

}
