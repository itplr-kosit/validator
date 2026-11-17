package org.kosit.validator.scenario.convert;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.conformatron.api.annotation.Nonempty;
import org.conformatron.api.model.detection.CTStandardSeverity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kosit.base.ObjectHelper;
import org.kosit.base.error.SimpleError;
import org.kosit.validator.scenario.generic.ScenarioConfiguration;
import org.kosit.validator.scenario.v2.Scenario2Converter;
import org.kosit.validator.scenario.v2.Scenario2Mapper;
import org.kosit.validator.scenario.v3.Scenario3Converter;
import org.kosit.validator.scenario.v3.Scenario3Mapper;
import org.kosit.validator.scenario.v3.Scenarios;

/**
 * Migrates a scenario configuration of version 2 to version 3.
 * <p>
 * Version 3 requires a DVR coordinate for every scenario and every resource, version 2 does not, so an old
 * configuration usually has gaps. The coordinate is what a report identifies the scenario and its artifacts by, so the
 * migration proposes the missing ones ({@link CoordinateProposals}) and writes them in only when a group id is given
 * and the proposals are accepted - that is the step where somebody takes responsibility for the identities the
 * configuration carries from then on.
 * </p>
 * <p>
 * The migration produces the configuration of version 3; writing it is left to the caller, see
 * {@link Scenario3Converter}.
 * </p>
 *
 * @author Andreas Schmitz
 */
public final class ScenarioMigration {

    /** The group id the proposals are made with as long as no real one is given. */
    public static final String PLACEHOLDER_GROUP_ID = "your-group-id";

    /**
     * Migrates the configuration in the given file.
     *
     * @param source the configuration to migrate. May not be <code>null</code>.
     * @param groupID the group id of the proposed coordinates. May be <code>null</code>, in which case the proposals
     *            are made with {@link #PLACEHOLDER_GROUP_ID} and can not be accepted.
     * @param version the version of the proposed coordinates. May neither be <code>null</code> nor empty.
     * @param acceptProposals <code>true</code> to write the proposed coordinates into the result
     * @return the result. Never <code>null</code>.
     * @throws IOException if the file can not be read
     */
    public static @NonNull MigrationResult toVersion3(@NonNull final Path source, final @Nullable String groupID,
            @NonNull @Nonempty final String version, final boolean acceptProposals) throws IOException {
        ObjectHelper.requireNonNull(source, "Source");
        final String namespace = rootNamespace(source);
        if (Scenario3Converter.NS_URI.equals(namespace)) {
            return new MigrationResult(EMigrationOutcome.ALREADY_VERSION_3, namespace, List.of(), List.of(), null);
        }
        if (!Scenario2Converter.NS_URI.equals(namespace)) {
            return new MigrationResult(EMigrationOutcome.NO_SCENARIO_CONFIGURATION, namespace, List.of(), List.of(), null);
        }
        return toVersion3(new Scenario2Converter().readXml(source), groupID, version, acceptProposals);
    }

    /**
     * Migrates a configuration of version 2 that is read already.
     *
     * @param source the configuration to migrate. May not be <code>null</code>.
     * @param groupID the group id of the proposed coordinates. May be <code>null</code>, in which case the proposals
     *            are made with {@link #PLACEHOLDER_GROUP_ID} and can not be accepted.
     * @param version the version of the proposed coordinates. May neither be <code>null</code> nor empty.
     * @param acceptProposals <code>true</code> to write the proposed coordinates into the result
     * @return the result. Never <code>null</code>.
     */
    public static @NonNull MigrationResult toVersion3(final org.kosit.validator.scenario.v2.Scenarios source,
            final @Nullable String groupID, @NonNull @Nonempty final String version, final boolean acceptProposals) {
        ObjectHelper.requireNonNull(source, "Source");
        final ScenarioConfiguration configuration = Scenario2Mapper.toGeneric(source);
        final String group = groupID == null ? PLACEHOLDER_GROUP_ID : groupID;
        final List<CoordinateProposal> proposals = CoordinateProposals.propose(configuration, group, version);
        if (!proposals.isEmpty() && (groupID == null || !acceptProposals)) {
            return new MigrationResult(EMigrationOutcome.COORDINATES_PROPOSED, Scenario2Converter.NS_URI, proposals, List.of(), null);
        }
        CoordinateProposals.apply(configuration, group, version);

        final List<SimpleError> findings = new ArrayList<>();
        final Scenarios converted = Scenario3Mapper.fromGeneric(configuration, findings);
        if (findings.stream().anyMatch(f -> f.getSeverity() == CTStandardSeverity.ERROR)) {
            return new MigrationResult(EMigrationOutcome.NOT_EXPRESSIBLE, Scenario2Converter.NS_URI, proposals, findings, null);
        }
        return new MigrationResult(EMigrationOutcome.CONVERTED, Scenario2Converter.NS_URI, proposals, findings, converted);
    }

    /**
     * @return the namespace URI of the root element, so that the migration can say what the file is instead of failing
     *         on it. <code>null</code> if the root element has none, or the file is no XML at all.
     */
    private static @Nullable String rootNamespace(@NonNull final Path file) throws IOException {
        final XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        try ( final InputStream in = Files.newInputStream(file) ) {
            final XMLStreamReader reader = factory.createXMLStreamReader(in);
            try {
                while (reader.hasNext()) {
                    if (reader.next() == XMLStreamConstants.START_ELEMENT) {
                        return reader.getNamespaceURI();
                    }
                }
                return null;
            } finally {
                reader.close();
            }
        } catch (final XMLStreamException e) {
            // no XML, so no scenario configuration either - the caller is told that, not given a parser error
            return null;
        }
    }

    private ScenarioMigration() {
    }
}
