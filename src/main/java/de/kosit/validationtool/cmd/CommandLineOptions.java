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

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;

/**
 * @author Andreas Penski
 */
public class CommandLineOptions {

    static final Option HELP = Option.builder("?").longOpt("help").argName("Help").desc("Displays this help").build();

    static final Option SCENARIOS = Option.builder("s").required().longOpt("scenarios").hasArg().desc("Location of scenarios.xml e.g.")
            .build();

    static final Option REPOSITORY = Option.builder("r").longOpt("repository").hasArg().desc("Directory containing scenario content")
            .build();

    static final Option PRINT = Option.builder("p").longOpt("print").desc("Prints the check result to stdout").build();

    static final Option OUTPUT = Option.builder("o").longOpt("output-directory")
            .desc("Defines the out directory for results. Defaults to cwd").hasArg().build();

    static final Option EXTRACT_HTML = Option.builder("h").longOpt("html")
            .desc("Extract and save any html content within  result as a separate file ").build();

    static final Option DEBUG = Option.builder("d").longOpt("debug").desc("Prints some more debug information").build();

    static final Option SERIALIZE_REPORT_INPUT = Option.builder("c").longOpt("serialize-report-input")
            .desc("Serializes the report input to the cwd").build();

    static final Option CHECK_ASSERTIONS = Option.builder("c").longOpt("check-assertions").hasArg()
            .desc("Check the result using defined assertions").argName("assertions-file").build();

    static final Option SERVER = Option.builder("D").longOpt("daemon").desc("Starts a daemon listing for validation requests").build();

    static final Option HOST = Option.builder("H").longOpt("host").hasArg()
            .desc("The hostname / IP address to bind the daemon. Default is localhost").build();

    static final Option PORT = Option.builder("P").longOpt("port").hasArg().desc("The port to bind the daemon. Default is 8080").build();

    static final Option WORKER_COUNT = Option.builder("T").longOpt("threads").hasArg()
            .desc("Number of threads processing validation requests").build();

    static final Option DISABLE_GUI = Option.builder("G").longOpt("disable-gui").desc("Disables the GUI of the daemon mode").build();

    static final Option REPORT_POSTFIX = Option.builder(null).longOpt("report-postfix").hasArg()
            .desc("Postfix of the generated report name").build();

    static final Option REPORT_PREFIX = Option.builder(null).longOpt("report-prefix").hasArg().desc("Prefix of the generated report name")
            .build();

    static final Option DEBUG_LOG = Option.builder("X").longOpt("debug-logging").desc("Enables full debug log. Alias for -l debug").build();

    static final Option LOG_LEVEL = Option.builder("l").longOpt("log-level").hasArg()
            .desc("Enables a certain log level for debugging " + "purposes").build();

    static final Option PRINT_MEM_STATS = Option.builder("m").longOpt("memory-stats").desc("Prints some memory stats").build();

    private CommandLineOptions() {
        // hide
    }

    static org.apache.commons.cli.Options createOptions() {
        final org.apache.commons.cli.Options options = new org.apache.commons.cli.Options();
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
        options.addOption(DISABLE_GUI);
        options.addOption(REPORT_POSTFIX);
        options.addOption(REPORT_PREFIX);
        options.addOption(LOG_LEVEL);
        options.addOption(DEBUG_LOG);
        return options;
    }

    static void printHelp(final org.apache.commons.cli.Options options) {
        // automatically generate the help statement
        final HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("check-tool  -s <scenario-config-file> [OPTIONS] [FILE]... ", options, false);
    }

    static org.apache.commons.cli.Options createHelpOptions() {
        final org.apache.commons.cli.Options options = new org.apache.commons.cli.Options();
        options.addOption(HELP);
        return options;
    }

}
