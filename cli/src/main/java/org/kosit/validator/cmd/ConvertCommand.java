package org.kosit.validator.cmd;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.conformatron.api.model.detection.CTStandardSeverity;
import org.kosit.base.error.SimpleError;
import org.kosit.validator.scenario.convert.CoordinateProposal;
import org.kosit.validator.scenario.convert.CoordinateProposals;
import org.kosit.validator.scenario.generic.ScenarioConfiguration;
import org.kosit.validator.scenario.v2.Scenario2Converter;
import org.kosit.validator.scenario.v2.Scenario2Mapper;
import org.kosit.validator.scenario.v3.Scenario3Converter;
import org.kosit.validator.scenario.v3.Scenario3Mapper;
import org.kosit.validator.scenario.v3.Scenarios;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

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
 *
 * @author Andreas Schmitz
 */
@Command(name = "convert", description = "Converts a scenario configuration of version 2 into version 3", separator = " ",
         mixinStandardHelpOptions = false)
public class ConvertCommand implements Callable<ReturnValue> {

    /** The group id the proposals are printed with as long as no real one is given. */
    private static final String PLACEHOLDER_GROUP_ID = "your-group-id";

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
        final String namespace = rootNamespace(this.source);
        if (Scenario3Converter.NS_URI.equals(namespace)) {
            Printer.writeErrRaw("The configuration " + this.source + " is already written in version 3");
            return ReturnValue.CONFIGURATION_ERROR;
        }
        if (!Scenario2Converter.NS_URI.equals(namespace)) {
            Printer.writeErrRaw("The configuration " + this.source + " is no scenario configuration: its root element is in '" + namespace
                    + "' instead of '" + Scenario2Converter.NS_URI + "'");
            return ReturnValue.CONFIGURATION_ERROR;
        }

        final ScenarioConfiguration configuration = Scenario2Mapper.toGeneric(new Scenario2Converter().readXml(this.source));
        final List<CoordinateProposal> proposals = CoordinateProposals.propose(configuration,
                this.groupID == null ? PLACEHOLDER_GROUP_ID : this.groupID, this.artifactVersion);
        if (!proposals.isEmpty() && !report(proposals)) {
            return ReturnValue.createFailed(proposals.size());
        }
        CoordinateProposals.apply(configuration, this.groupID == null ? PLACEHOLDER_GROUP_ID : this.groupID, this.artifactVersion);

        final List<SimpleError> findings = new ArrayList<>();
        final Scenarios converted = Scenario3Mapper.fromGeneric(configuration, findings);
        for (final SimpleError finding : findings) {
            Printer.writeErrRaw(finding.getSeverity() + ": " + finding.getMessage());
        }
        if (findings.stream().anyMatch(f -> f.getSeverity() == CTStandardSeverity.ERROR)) {
            Printer.writeErrRaw("The configuration was not converted, because version 3 can not express it as it stands");
            return ReturnValue.createFailed(1);
        }

        new Scenario3Converter().withFormattedOutput(true).writeXml(converted, this.target);
        Printer.writeOut("Converted {0} scenario(s) into {1}", String.valueOf(configuration.getScenarios().size()), this.target.toString());
        if (!proposals.isEmpty()) {
            Printer.writeOut("{0} proposed coordinate(s) were written in - check them before the configuration is published",
                    String.valueOf(proposals.size()));
        }
        return ReturnValue.SUCCESS;
    }

    /**
     * Prints what the configuration leaves without a coordinate.
     *
     * @param proposals what the conversion would fill in
     * @return <code>true</code> if the conversion may continue and write the proposals, <code>false</code> if it has to
     *         stop
     */
    private boolean report(final List<CoordinateProposal> proposals) {
        Printer.writeOut("The configuration leaves {0} coordinate(s) off, which version 3 requires:", String.valueOf(proposals.size()));
        for (final CoordinateProposal proposal : proposals) {
            // the proposal goes in as an argument, not as the pattern: MessageFormat would eat its quotes
            Printer.writeOut("  {0}", proposal.toString());
        }
        if (this.groupID == null) {
            Printer.writeErrRaw("Nothing was written. Pass --group-id to say under which group id these coordinates are published,"
                    + " and --accept-proposals to write them in. A coordinate becomes the identity a report names the scenario"
                    + " and its artifacts by, so it is nothing this command settles on its own");
            return false;
        }
        if (!this.acceptProposals) {
            Printer.writeErrRaw("Nothing was written. Either state these coordinates in the source configuration, or pass"
                    + " --accept-proposals to write the proposals above into the result");
            return false;
        }
        return true;
    }

    /**
     * @return the namespace URI of the root element, so that the command says what the file is instead of failing on it
     */
    private static String rootNamespace(final Path file) throws Exception {
        final XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        try ( InputStream in = Files.newInputStream(file) ) {
            final XMLStreamReader reader = factory.createXMLStreamReader(in);
            while (reader.hasNext()) {
                if (reader.next() == XMLStreamConstants.START_ELEMENT) {
                    final String ret = reader.getNamespaceURI();
                    reader.close();
                    return ret == null ? "" : ret;
                }
            }
            reader.close();
        }
        return "";
    }
}
