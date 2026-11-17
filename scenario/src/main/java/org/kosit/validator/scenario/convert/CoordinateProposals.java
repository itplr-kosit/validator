package org.kosit.validator.scenario.convert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.conformatron.api.annotation.Nonempty;
import org.jspecify.annotations.NonNull;
import org.kosit.base.ObjectHelper;
import org.kosit.base.string.StringHelper;
import org.kosit.validator.scenario.generic.Scenario;
import org.kosit.validator.scenario.generic.ScenarioConfiguration;
import org.kosit.validator.scenario.generic.ScenarioCoordinate;
import org.kosit.validator.scenario.generic.ScenarioResource;
import org.kosit.validator.scenario.generic.ScenarioSchematron;

/**
 * Proposes the coordinates a configuration written for version 2 is missing, so that it can be converted to version 3,
 * which requires one for every scenario and every resource.
 * <p>
 * The proposal is deterministic and derived from what the configuration already says: the artifact id is the name of
 * the scenario, respectively the file name of the resource, reduced to the character set a coordinate part allows. The
 * group id and the version can not be derived from anything - they belong to whoever publishes the configuration - and
 * are therefore passed in.
 * </p>
 * <p>
 * {@link #propose(ScenarioConfiguration, String, String)} only reports;
 * {@link #apply(ScenarioConfiguration, String, String)} is the same walk with the proposals set. The two are separate
 * on purpose: the coordinate becomes the identity the report names the scenario and its artifacts by, so a conversion
 * that invented it silently would decide something the publisher has to decide.
 * </p>
 *
 * @author Andreas Schmitz
 */
public final class CoordinateProposals {

    private CoordinateProposals() {
    }

    /**
     * @param configuration the configuration to examine. May not be <code>null</code>.
     * @param groupID the group id every proposed coordinate gets. May neither be <code>null</code> nor empty.
     * @param version the version every proposed coordinate gets. May neither be <code>null</code> nor empty.
     * @return one proposal per scenario and per resource that has no coordinate, in configuration order. Never
     *         <code>null</code>, and empty for a configuration that states all of its coordinates.
     */
    public static @NonNull List<CoordinateProposal> propose(@NonNull final ScenarioConfiguration configuration,
            @NonNull @Nonempty final String groupID, @NonNull @Nonempty final String version) {
        return walk(configuration, groupID, version, false);
    }

    /**
     * Sets on the configuration what {@link #propose(ScenarioConfiguration, String, String)} reports, and leaves every
     * coordinate the configuration already states untouched.
     *
     * @param configuration the configuration to complete. May not be <code>null</code>.
     * @param groupID the group id every proposed coordinate gets. May neither be <code>null</code> nor empty.
     * @param version the version every proposed coordinate gets. May neither be <code>null</code> nor empty.
     * @return the proposals that were set, in configuration order. Never <code>null</code>.
     */
    public static @NonNull List<CoordinateProposal> apply(@NonNull final ScenarioConfiguration configuration,
            @NonNull @Nonempty final String groupID, @NonNull @Nonempty final String version) {
        return walk(configuration, groupID, version, true);
    }

    private static @NonNull List<CoordinateProposal> walk(@NonNull final ScenarioConfiguration configuration,
            @NonNull @Nonempty final String groupID, @NonNull @Nonempty final String version, final boolean set) {
        ObjectHelper.requireNonNull(configuration, "Configuration");
        requirePart(groupID, "Group id");
        requirePart(version, "Version");

        final List<CoordinateProposal> proposals = new ArrayList<>();
        // a coordinate identifies, so no two artifacts may get the same one, and one artifact may not get two: what
        // the configuration already states is taken, and an artifact several scenarios share is proposed for once
        final Set<String> taken = new HashSet<>();
        final Map<String, ScenarioCoordinate> byArtifact = new HashMap<>();
        for (final Scenario scenario : configuration.getScenarios()) {
            if (scenario.hasCoordinate()) {
                taken.add(scenario.getCoordinate().getArtifactID());
            }
            for (final ScenarioResource resource : resourcesOf(scenario)) {
                if (resource.hasCoordinate()) {
                    taken.add(resource.getCoordinate().getArtifactID());
                    if (resource.hasLocation()) {
                        byArtifact.put(keyOf(resource), resource.getCoordinate());
                    }
                }
            }
        }

        for (final Scenario scenario : configuration.getScenarios()) {
            if (!scenario.hasCoordinate()) {
                final ScenarioCoordinate coordinate = ScenarioCoordinate.of(groupID, unique(part(scenario.getName()), taken), version);
                proposals.add(new CoordinateProposal("the scenario '" + scenario.getName() + "'", coordinate));
                if (set) {
                    scenario.setCoordinate(coordinate);
                }
            }
            for (final ScenarioResource resource : resourcesOf(scenario)) {
                if (resource.hasCoordinate()) {
                    continue;
                }
                // the same file is the same artifact, however many scenarios validate with it - it gets one
                // coordinate, and it is proposed once
                final ScenarioCoordinate known = resource.hasLocation() ? byArtifact.get(keyOf(resource)) : null;
                final ScenarioCoordinate coordinate = known != null ? known
                        : ScenarioCoordinate.of(groupID, unique(artifactIDOf(resource), taken), version);
                if (known == null) {
                    proposals.add(new CoordinateProposal(subjectOf(resource, scenario), coordinate));
                    if (resource.hasLocation()) {
                        byArtifact.put(keyOf(resource), coordinate);
                    }
                }
                if (set) {
                    resource.setCoordinate(coordinate);
                }
            }
        }
        return proposals;
    }

    /** What identifies an artifact within the configuration: where it lies, in the repository it lies in. */
    private static @NonNull String keyOf(@NonNull final ScenarioResource resource) {
        return (resource.hasRepository() ? resource.getRepository() : "") + "/" + resource.getLocation();
    }

    /** How the proposal names what it is for: by the artifact where there is one, and by the resource otherwise. */
    private static @NonNull String subjectOf(@NonNull final ScenarioResource resource, @NonNull final Scenario scenario) {
        return resource.hasLocation() ? "the artifact '" + resource.getLocation() + "'"
                : "the resource '" + resource.getName() + "' of the scenario '" + scenario.getName() + "'";
    }

    /**
     * @param artifactID the artifact id derived from the name or the location
     * @param taken the artifact ids already spoken for; the returned one is added
     * @return the passed id, or the first free one with a counter appended
     */
    private static @NonNull @Nonempty String unique(@NonNull @Nonempty final String artifactID, @NonNull final Set<String> taken) {
        String ret = artifactID;
        for (int i = 2; !taken.add(ret); ++i) {
            ret = artifactID + "-" + i;
        }
        return ret;
    }

    private static @NonNull List<ScenarioResource> resourcesOf(@NonNull final Scenario scenario) {
        final List<ScenarioResource> resources = new ArrayList<>(scenario.getXmlSchemas());
        for (final ScenarioSchematron schematron : scenario.getSchematrons()) {
            resources.add(schematron.getResource());
        }
        return resources;
    }

    /**
     * The artifact id of a resource: its file name, with the extension kept as a suffix - {@code simple.xsd} becomes
     * {@code simple-xsd}. The extension is what tells the schema and the rule set of a scenario apart where both are
     * named after the document they check. A resource without a location falls back to its display name.
     */
    private static @NonNull @Nonempty String artifactIDOf(@NonNull final ScenarioResource resource) {
        if (!resource.hasLocation()) {
            return part(resource.getName());
        }
        final String location = resource.getLocation();
        return part(location.substring(location.lastIndexOf('/') + 1).replace('.', '-'));
    }

    /**
     * Reduces a name to what a coordinate part allows ({@code [a-zA-Z0-9_\-\.]+}): every other character becomes a
     * hyphen, repetitions collapse, and the result is lower case.
     */
    private static @NonNull @Nonempty String part(@NonNull final String name) {
        final StringBuilder ret = new StringBuilder(name.length());
        for (final char c : name.toLowerCase(Locale.ROOT).toCharArray()) {
            if (c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '_' || c == '-' || c == '.') {
                ret.append(c);
            } else if (ret.length() > 0 && ret.charAt(ret.length() - 1) != '-') {
                ret.append('-');
            }
        }
        while (ret.length() > 0 && ret.charAt(ret.length() - 1) == '-') {
            ret.setLength(ret.length() - 1);
        }
        // a name made of nothing but separators leaves nothing to propose, so the proposal says so instead of being
        // empty
        return ret.length() == 0 ? "unnamed" : ret.toString();
    }

    private static void requirePart(final String value, final String what) {
        if (StringHelper.isEmpty(value)) {
            throw new IllegalArgumentException(what + " must not be empty");
        }
        if (!value.matches("[a-zA-Z0-9_\\-.]+")) {
            throw new IllegalArgumentException(what + " '" + value + "' is not a valid coordinate part");
        }
    }
}
