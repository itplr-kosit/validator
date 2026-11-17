package org.kosit.validator.impl;

import java.net.URI;

import org.jspecify.annotations.Nullable;
import org.kosit.schematron.ContentRepository;
import org.kosit.validator.TestHelper;
import org.kosit.validator.impl.conformatron.util.ScenarioXml;
import org.kosit.validator.scenario.generic.EScenarioKind;
import org.kosit.validator.scenario.generic.ScenarioResource;
import org.kosit.validator.testdata.TestResources;
import org.kost.validator.api.saxon.ProcessorProvider;

/**
 * Scenarios for tests that need one without a scenarios.xml: schema validation against {@code simple.xsd} of the shared
 * test repository, name and match as the test says.
 */
public class TestScenarioBuilder {

    /** @return the artifact repository of the shared simple test data */
    public static ContentRepository createRepository() {
        return new ContentRepository(ProcessorProvider.getProcessor(), TestHelper.getTestResolvingStrategy(),
                TestResources.Simple.REPOSITORY_URI);
    }

    /** @return the scenario "simple" matching every document root */
    public static Scenario createDefault() {
        return createScenario("simple", "/*");
    }

    /**
     * @param name the scenario name
     * @param match the match expression; {@code null} for a scenario that applies unconditionally
     * @return a scenario validating against {@code simple.xsd} of the shared test repository
     */
    public static Scenario createScenario(final String name, final @Nullable String match) {
        final org.kosit.validator.scenario.generic.Scenario declaration = new org.kosit.validator.scenario.generic.Scenario(
                EScenarioKind.XML, name).setMatch(match);
        declaration.addXmlSchema(new ScenarioResource("default").setLocation(getRepositoryLocation(TestResources.Simple.SCHEMA)));
        return Scenario.of(declaration, ScenarioXml.toXmlBytes(declaration), createRepository(), null);
    }

    /**
     * Returns the name of the artifact relative to the repository it lives in, which is what a scenario configuration
     * carries as {@code location}. Deliberately not {@link URI#getRawPath()}: that returns {@code null} for the opaque
     * "jar:" URIs the test data has when it is read straight from the artifact.
     *
     * @param resource the absolute URI of the artifact
     * @return the last segment of the URI, never {@code null}
     */
    private static String getRepositoryLocation(final URI resource) {
        final String uri = resource.toASCIIString();
        return uri.substring(uri.lastIndexOf('/') + 1);
    }

}
