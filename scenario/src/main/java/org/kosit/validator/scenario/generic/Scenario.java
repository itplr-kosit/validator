package org.kosit.validator.scenario.generic;

import java.util.ArrayList;
import java.util.List;

import org.conformatron.api.annotation.Nonempty;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kosit.base.ObjectHelper;
import org.kosit.base.string.StringHelper;

/**
 * A single scenario of a {@link ScenarioConfiguration}.
 * <p>
 * Which properties are relevant depends on {@link #getKind()}:
 * <ul>
 * <li>{@link EScenarioKind#XML} uses the namespaces, the match, the XML Schemas, the Schematrons, the reports and the
 * accept match. This is the only kind that scenario configuration version 2 knows.</li>
 * <li>{@link EScenarioKind#PDF} uses the requirements and the XML scenario reference. It was introduced with scenario
 * configuration version 3 and is silently dropped when writing version 2.</li>
 * </ul>
 * The {@link #getCoordinate() coordinate} is required by version 3 and has no representation in version 2.
 *
 * @author Philip Helger
 */
public class Scenario {

    private final EScenarioKind kind;

    private @Nullable ScenarioCoordinate coordinate;

    private String name;

    private @Nullable ScenarioDescription description;

    private final List<ScenarioNamespace> namespaces = new ArrayList<>();

    private @Nullable String match;

    private final List<ScenarioResource> xmlSchemas = new ArrayList<>();

    private final List<ScenarioSchematron> schematrons = new ArrayList<>();

    private final List<ScenarioCreateReport> createReports = new ArrayList<>();

    private @Nullable String acceptMatch;

    private final List<ScenarioRequirement> requirements = new ArrayList<>();

    private @Nullable ScenarioCoordinate xmlScenarioRef;

    /**
     * Constructor.
     *
     * @param kind the kind of this scenario. May not be <code>null</code>.
     * @param name the human readable name of this scenario. May neither be <code>null</code> nor empty.
     */
    public Scenario(@NonNull final EScenarioKind kind, @NonNull @Nonempty final String name) {
        this.kind = ObjectHelper.requireNonNull(kind, "Kind");
        setName(name);
    }

    /**
     * @return the kind of this scenario. Never <code>null</code>.
     */
    public @NonNull EScenarioKind getKind() {
        return this.kind;
    }

    /**
     * @return <code>true</code> if this is an XML scenario, <code>false</code> if not.
     */
    public boolean isXml() {
        return this.kind == EScenarioKind.XML;
    }

    /**
     * @return <code>true</code> if this is a PDF scenario, <code>false</code> if not.
     */
    public boolean isPdf() {
        return this.kind == EScenarioKind.PDF;
    }

    /**
     * @return the DVR coordinate of this scenario. May be <code>null</code>, e.g. if the scenario was read from a
     *         version 2 configuration.
     */
    public @Nullable ScenarioCoordinate getCoordinate() {
        return this.coordinate;
    }

    /**
     * @return <code>true</code> if a coordinate is present, <code>false</code> if not.
     */
    public boolean hasCoordinate() {
        return this.coordinate != null;
    }

    /**
     * Set the DVR coordinate of this scenario.
     *
     * @param coordinate the coordinate to use. May be <code>null</code>.
     * @return this for chaining
     */
    public @NonNull Scenario setCoordinate(final @Nullable ScenarioCoordinate coordinate) {
        this.coordinate = coordinate;
        return this;
    }

    /**
     * @return the human readable name of this scenario. Never <code>null</code> nor empty.
     */
    public @NonNull @Nonempty String getName() {
        return this.name;
    }

    /**
     * Set the human readable name of this scenario.
     *
     * @param name the name to use. May neither be <code>null</code> nor empty.
     * @return this for chaining
     */
    public @NonNull Scenario setName(@NonNull @Nonempty final String name) {
        ObjectHelper.requireNonNull(name, "Name");
        if (StringHelper.isEmpty(name)) {
            throw new IllegalArgumentException("Name must not be empty");
        }
        this.name = name;
        return this;
    }

    /**
     * @return the description of this scenario. May be <code>null</code>.
     */
    public @Nullable ScenarioDescription getDescription() {
        return this.description;
    }

    /**
     * Set the description of this scenario.
     *
     * @param description the description to use. May be <code>null</code>.
     * @return this for chaining
     */
    public @NonNull Scenario setDescription(final @Nullable ScenarioDescription description) {
        this.description = description;
        return this;
    }

    /**
     * @return the modifiable list of all namespace prefix mappings. Never <code>null</code>. Only relevant for
     *         {@link EScenarioKind#XML}.
     */
    public @NonNull List<ScenarioNamespace> getNamespaces() {
        return this.namespaces;
    }

    /**
     * Add a namespace prefix mapping.
     *
     * @param namespace the namespace to add. May not be <code>null</code>.
     * @return this for chaining
     */
    public @NonNull Scenario addNamespace(@NonNull final ScenarioNamespace namespace) {
        ObjectHelper.requireNonNull(namespace, "Namespace");
        this.namespaces.add(namespace);
        return this;
    }

    /**
     * @return the XPath expression selecting this scenario. May be <code>null</code> in a version 3 configuration,
     *         where a scenario may alternatively be selected by its coordinate.
     */
    public @Nullable String getMatch() {
        return this.match;
    }

    /**
     * @return <code>true</code> if a match expression is present, <code>false</code> if not.
     */
    public boolean hasMatch() {
        return StringHelper.isNotEmpty(this.match);
    }

    /**
     * Set the XPath expression selecting this scenario.
     *
     * @param match the XPath expression to use. May be <code>null</code>.
     * @return this for chaining
     */
    public @NonNull Scenario setMatch(final @Nullable String match) {
        this.match = StringHelper.emptyToNull(match);
        return this;
    }

    /**
     * @return the modifiable list of all XML Schema resources to validate with. Never <code>null</code>. Only relevant
     *         for {@link EScenarioKind#XML}.
     */
    public @NonNull List<ScenarioResource> getXmlSchemas() {
        return this.xmlSchemas;
    }

    /**
     * Add an XML Schema resource to validate with.
     *
     * @param xmlSchema the resource to add. May not be <code>null</code>.
     * @return this for chaining
     */
    public @NonNull Scenario addXmlSchema(@NonNull final ScenarioResource xmlSchema) {
        ObjectHelper.requireNonNull(xmlSchema, "XmlSchema");
        this.xmlSchemas.add(xmlSchema);
        return this;
    }

    /**
     * @return the modifiable list of all Schematron validations. Never <code>null</code>. Only relevant for
     *         {@link EScenarioKind#XML}.
     */
    public @NonNull List<ScenarioSchematron> getSchematrons() {
        return this.schematrons;
    }

    /**
     * Add a Schematron validation.
     *
     * @param schematron the Schematron validation to add. May not be <code>null</code>.
     * @return this for chaining
     */
    public @NonNull Scenario addSchematron(@NonNull final ScenarioSchematron schematron) {
        ObjectHelper.requireNonNull(schematron, "Schematron");
        this.schematrons.add(schematron);
        return this;
    }

    /**
     * @return the modifiable list of all reports to be created. Never <code>null</code>. Only relevant for
     *         {@link EScenarioKind#XML}.
     */
    public @NonNull List<ScenarioCreateReport> getCreateReports() {
        return this.createReports;
    }

    /**
     * Add a report to be created.
     *
     * @param createReport the report to add. May not be <code>null</code>.
     * @return this for chaining
     */
    public @NonNull Scenario addCreateReport(@NonNull final ScenarioCreateReport createReport) {
        ObjectHelper.requireNonNull(createReport, "CreateReport");
        this.createReports.add(createReport);
        return this;
    }

    /**
     * @return the XPath expression that is applied to the generated report to decide on acceptance. May be
     *         <code>null</code>.
     */
    public @Nullable String getAcceptMatch() {
        return this.acceptMatch;
    }

    /**
     * @return <code>true</code> if an accept match expression is present, <code>false</code> if not.
     */
    public boolean hasAcceptMatch() {
        return StringHelper.isNotEmpty(this.acceptMatch);
    }

    /**
     * Set the XPath expression that is applied to the generated report to decide on acceptance.
     *
     * @param acceptMatch the XPath expression to use. May be <code>null</code>.
     * @return this for chaining
     */
    public @NonNull Scenario setAcceptMatch(final @Nullable String acceptMatch) {
        this.acceptMatch = StringHelper.emptyToNull(acceptMatch);
        return this;
    }

    /**
     * @return the modifiable list of all requirements. Never <code>null</code>. Only relevant for
     *         {@link EScenarioKind#PDF} and only supported by scenario configuration version 3.
     */
    public @NonNull List<ScenarioRequirement> getRequirements() {
        return this.requirements;
    }

    /**
     * Add a requirement.
     *
     * @param requirement the requirement to add. May not be <code>null</code>.
     * @return this for chaining
     */
    public @NonNull Scenario addRequirement(@NonNull final ScenarioRequirement requirement) {
        ObjectHelper.requireNonNull(requirement, "Requirement");
        this.requirements.add(requirement);
        return this;
    }

    /**
     * @return the coordinate of the {@link EScenarioKind#XML} scenario that validates the XML document embedded in the
     *         PDF. May be <code>null</code>. Only relevant for {@link EScenarioKind#PDF} and only supported by scenario
     *         configuration version 3.
     */
    public @Nullable ScenarioCoordinate getXmlScenarioRef() {
        return this.xmlScenarioRef;
    }

    /**
     * @return <code>true</code> if an XML scenario reference is present, <code>false</code> if not.
     */
    public boolean hasXmlScenarioRef() {
        return this.xmlScenarioRef != null;
    }

    /**
     * Set the coordinate of the XML scenario that validates the embedded XML document.
     *
     * @param xmlScenarioRef the coordinate to use. May be <code>null</code>.
     * @return this for chaining
     */
    public @NonNull Scenario setXmlScenarioRef(final @Nullable ScenarioCoordinate xmlScenarioRef) {
        this.xmlScenarioRef = xmlScenarioRef;
        return this;
    }

    @Override
    public String toString() {
        return "Scenario[kind=" + this.kind + "; coordinate=" + this.coordinate + "; name=" + this.name + "; description="
                + this.description + "; namespaces=" + this.namespaces + "; match=" + this.match + "; xmlSchemas=" + this.xmlSchemas
                + "; schematrons=" + this.schematrons + "; createReports=" + this.createReports + "; acceptMatch=" + this.acceptMatch
                + "; requirements=" + this.requirements + "; xmlScenarioRef=" + this.xmlScenarioRef + "]";
    }
}
