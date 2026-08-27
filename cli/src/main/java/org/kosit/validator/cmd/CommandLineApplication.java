package org.kosit.validator.cmd;

import static org.kosit.validator.cmd.Printer.writeErr;

import java.util.Objects;

import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.AnsiRenderer.Code;
import org.kosit.base.string.StringHelper;
import org.kosit.validator.cmd.report.Line;

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
     * @param args the input arguments
     */
    public static void main(final String[] args) {
        System.exit(run(args));
    }

    /**
     * Runs the CLI and returns the process exit code.
     *
     * @param args the input arguments
     * @return exit code
     */
    public static int run(final String... args) {
        AnsiConsole.systemInstall();

        final CommandLineOptions options = new CommandLineOptions();
        options.setEngineInformation(new CliEngineInformation());
        final CommandLine commandLine = new CommandLine(options);
        ReturnValue resultStatus;
        try {
            commandLine.setExecutionExceptionHandler(CommandLineApplication::logExecutionException);

            final int cmdlineRetVal = commandLine.execute(args);
            if (commandLine.isUsageHelpRequested() || cmdlineRetVal == CommandLine.ExitCode.USAGE) {
                resultStatus = ReturnValue.HELP_REQUEST;
            } else {
                resultStatus = Objects.requireNonNullElse(commandLine.getExecutionResult(), ReturnValue.PARSING_ERROR);
                if (resultStatus.isError()) {
                    commandLine.usage(System.out);
                }
            }

            if (!resultStatus.equals(ReturnValue.HELP_REQUEST) && resultStatus.getCode() >= 0) {
                sayGoodby(resultStatus);
            }
        } catch (final Exception e) {
            writeErr("Error processing command line arguments: {0}", e.getMessage(), e);
            resultStatus = ReturnValue.PARSING_ERROR;
        }

        return resultStatus.getCode();
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

    private static int logExecutionException(final Exception ex, final CommandLine cli, final ParseResult parseResult) {
        final String message = StringHelper.isNotEmpty(ex.getMessage()) ? ex.getMessage() : "An error occurred";
        Printer.writeErr(ex, message);
        return 1;
    }

    enum Level {

        INFO, WARN, DEBUG, TRACE, ERROR, OFF;

        public String getID() {
            return name();
        }
    }

}
