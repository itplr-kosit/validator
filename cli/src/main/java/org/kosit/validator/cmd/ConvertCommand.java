package org.kosit.validator.cmd;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import org.kosit.base.error.SimpleError;
import org.kosit.validator.scenario.convert.CoordinateProposal;
import org.kosit.validator.scenario.convert.MigrationResult;
import org.kosit.validator.scenario.convert.ScenarioMigration;
import org.kosit.validator.scenario.v2.Scenario2Converter;
import org.kosit.validator.scenario.v3.Scenario3Converter;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Converts a scenario configuration of version 2 into version 3.
 * <p>
 * Version 3 requires a DVR coordinate for every scenario and every resource, version 2 does not, so an old
 * configuration usually has gaps. The coordinate is what a report identifies the scenario and its artifacts by, so this
 * command proposes the missing ones and stops: writing them in needs {@code --accept-proposals}, which is the step
 * where somebody takes responsibility for the identities the configuration will carry from then on.
 * </p>
 * <p>
 * The migration itself is {@link ScenarioMigration} of the module {@code validator-scenario}, so that it is available
 * without the command line; this command reads the options, reports and writes the result.
 * </p>
 *
 * @author Andreas Schmitz
 */
@Command(name = "convert", description = "Converts a scenario configuration of version 2 into version 3", separator = " ",
         mixinStandardHelpOptions = false)
public class ConvertCommand implements Callable<ReturnValue> {

    @Option(names = { "-s", "--scenarios" }, paramLabel = "scenario.xml", required = true,
            description = "The scenario configuration of version 2 to convert")
    private Path source;

    @Option(names = { "-o", "--output" }, paramLabel = "scenario-v3.xml", required = true,
            description = "Where to write the converted configuration")
    private Path target;

    @Option(names = { "-g", "--group-id" }, paramLabel = "group-id",
            description = "Group id of the coordinates proposed for everything the configuration leaves without one")
    private String groupID;

    @Option(names = { "--artifact-version" }, paramLabel = "version", defaultValue = "1.0.0",
            description = "Version of the proposed coordinates (default: ${DEFAULT-VALUE})")
    private String artifactVersion;

    @Option(names = { "--accept-proposals" }, description = "Write the proposed coordinates into the result instead of stopping at them")
    private boolean acceptProposals;

    @Option(names = { "-?", "--help" }, usageHelp = true, description = "display this help message")
    private boolean usageHelpRequested;

    @Override
    public ReturnValue call() throws Exception {
        if (!Files.isReadable(this.source)) {
            Printer.writeErrRaw("The configuration " + this.source + " can not be read");
            return ReturnValue.CONFIGURATION_ERROR;
        }
        final MigrationResult result = ScenarioMigration.toVersion3(this.source, this.groupID, this.artifactVersion, this.acceptProposals);
        final List<CoordinateProposal> proposals = result.proposals();
        switch (result.outcome()) {
            case ALREADY_VERSION_3:
                Printer.writeErrRaw("The configuration " + this.source + " is already written in version 3");
                return ReturnValue.CONFIGURATION_ERROR;
            case NO_SCENARIO_CONFIGURATION:
                Printer.writeErrRaw("The configuration " + this.source + " is no scenario configuration: its root element is in '"
                        + (result.rootNamespace() == null ? "" : result.rootNamespace()) + "' instead of '" + Scenario2Converter.NS_URI
                        + "'");
                return ReturnValue.CONFIGURATION_ERROR;
            case COORDINATES_PROPOSED:
                report(proposals);
                return ReturnValue.createFailed(proposals.size());
            case NOT_EXPRESSIBLE:
                printFindings(result.findings());
                Printer.writeErrRaw("The configuration was not converted, because version 3 can not express it as it stands");
                return ReturnValue.createFailed(1);
            case CONVERTED:
            default:
                break;
        }

        if (!proposals.isEmpty()) {
            report(proposals);
        }
        printFindings(result.findings());
        new Scenario3Converter().withFormattedOutput(true).writeXml(result.converted(), this.target);
        Printer.writeOut("Converted {0} scenario(s) into {1}", String.valueOf(result.converted().getScenarioXmlOrScenarioPdf().size()),
                this.target.toString());
        if (!proposals.isEmpty()) {
            Printer.writeOut("{0} proposed coordinate(s) were written in - check them before the configuration is published",
                    String.valueOf(proposals.size()));
        }
        return ReturnValue.SUCCESS;
    }

    private static void printFindings(final List<SimpleError> findings) {
        for (final SimpleError finding : findings) {
            Printer.writeErrRaw(finding.getSeverity() + ": " + finding.getMessage());
        }
    }

    /**
     * Prints what the configuration leaves without a coordinate, and why nothing was written if nothing was.
     *
     * @param proposals what the migration fills in, or would fill in
     */
    private void report(final List<CoordinateProposal> proposals) {
        Printer.writeOut("The configuration leaves {0} coordinate(s) off, which version 3 requires:", String.valueOf(proposals.size()));
        for (final CoordinateProposal proposal : proposals) {
            // the proposal goes in as an argument, not as the pattern: MessageFormat would eat its quotes
            Printer.writeOut("  {0}", proposal.toString());
        }
        if (this.groupID == null) {
            Printer.writeErrRaw("Nothing was written. Pass --group-id to say under which group id these coordinates are published,"
                    + " and --accept-proposals to write them in. A coordinate becomes the identity a report names the scenario"
                    + " and its artifacts by, so it is nothing this command settles on its own");
        } else if (!this.acceptProposals) {
            Printer.writeErrRaw("Nothing was written. Either state these coordinates in the source configuration, or pass"
                    + " --accept-proposals to write the proposals above into the result");
        }
        // else: accepted, the proposals are written in
    }
}
