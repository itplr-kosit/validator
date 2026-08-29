package org.kosit.validator.scenario.v3;

import java.io.Serializable;
import java.util.List;

import org.conformatron.api.model.detection.CTStandardSeverity;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kosit.base.ObjectHelper;
import org.kosit.base.error.SimpleError;
import org.kosit.base.error.SimpleErrorBuilder;
import org.kosit.base.string.StringHelper;
import org.kosit.jaxb.JaxbHelper;
import org.kosit.validator.scenario.generic.EScenarioDescriptionBlockKind;
import org.kosit.validator.scenario.generic.EScenarioErrorLevel;
import org.kosit.validator.scenario.generic.EScenarioKind;
import org.kosit.validator.scenario.generic.Scenario;
import org.kosit.validator.scenario.generic.ScenarioConfiguration;
import org.kosit.validator.scenario.generic.ScenarioCoordinate;
import org.kosit.validator.scenario.generic.ScenarioCreateReport;
import org.kosit.validator.scenario.generic.ScenarioCustomErrorLevel;
import org.kosit.validator.scenario.generic.ScenarioDescription;
import org.kosit.validator.scenario.generic.ScenarioDescriptionBlock;
import org.kosit.validator.scenario.generic.ScenarioNamespace;
import org.kosit.validator.scenario.generic.ScenarioRequirement;
import org.kosit.validator.scenario.generic.ScenarioResource;
import org.kosit.validator.scenario.generic.ScenarioSchematron;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBElement;

/**
 * Converts between the JAXB model of scenario configuration version 3 and the version independent
 * {@link ScenarioConfiguration}.
 * <p>
 * Reading version 3 is lossless, except for whitespace only text between the child elements of a description, which
 * carries no meaning. Writing version 3 does not lose anything either, but version 3 requires a DVR coordinate for
 * every scenario and for every resource - a configuration that was read from version 2 has none, so
 * {@link #fromGeneric(ScenarioConfiguration, List)} reports every missing coordinate.
 *
 * @author Philip Helger
 * @see Scenario3Converter
 */
public final class Scenario3Mapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(Scenario3Mapper.class);

    private static final ObjectFactory OF = new ObjectFactory();

    /** The local name of the description element that carries an ordered list */
    private static final String ELEMENT_OL = "ol";

    private Scenario3Mapper() {
    }

    // ---------- version 3 to generic ----------

    private static @Nullable ScenarioDescription _toGeneric(final @Nullable DescriptionType src) {
        if (src == null) {
            return null;
        }
        final ScenarioDescription ret = new ScenarioDescription();
        for (final Serializable content : src.getContent()) {
            switch (content) {
                // Mixed content: the raw text between the child elements
                case final String text -> {
                    if (StringHelper.isNotBlank(text)) {
                        ret.addText(text.strip());
                    }
                }
                case final JAXBElement<?> element -> {
                    final Object value = element.getValue();
                    if (value instanceof final DescriptionListType list) {
                        if (!list.getLi().isEmpty()) {
                            ret.add(ScenarioDescriptionBlock
                                    .list(ELEMENT_OL.equals(element.getName().getLocalPart()) ? EScenarioDescriptionBlockKind.ORDERED_LIST
                                            : EScenarioDescriptionBlockKind.UNORDERED_LIST, list.getLi()));
                        }
                    } else if (value instanceof final String text) {
                        if (StringHelper.isNotBlank(text)) {
                            ret.addParagraph(text);
                        }
                    } else {
                        LOGGER.warn("Ignoring unsupported description content of type " + value);
                    }
                }
                case null, default -> LOGGER.warn("Ignoring unsupported description content of type " + content);
            }
        }
        return ret.isNotEmpty() ? ret : null;
    }

    private static @NonNull ScenarioCoordinate _toGeneric(@NonNull final AbstractCoordinateType src) {
        return ScenarioCoordinate.of(src.getGroupId(), src.getArtifactId(), src.getVersion(), src.getClassifier());
    }

    private static @NonNull ScenarioResource _toGeneric(@NonNull final ResourceType src) {
        return new ScenarioResource(src.getName()).setCoordinate(_toGeneric((AbstractCoordinateType) src)).setLocation(src.getLocation());
    }

    private static @NonNull Scenario _toGeneric(@NonNull final ScenarioXmlType src) {
        final Scenario ret = new Scenario(EScenarioKind.XML, src.getName());
        for (final NamespaceType ns : src.getNamespace()) {
            ret.addNamespace(ScenarioNamespace.of(ns.getPrefix(), ns.getValue()));
        }
        ret.setMatch(src.getMatch());
        if (src.getValidateWithXmlSchema() != null) {
            for (final ResourceType resource : src.getValidateWithXmlSchema().getResource()) {
                ret.addXmlSchema(_toGeneric(resource));
            }
        }
        for (final ValidateWithSchematronType schematron : src.getValidateWithSchematron()) {
            ret.addSchematron(new ScenarioSchematron(_toGeneric(schematron.getResource())).setPsvi(schematron.isPsvi())
                    .setCompiler(schematron.getCompiler()));
        }
        for (final CreateReportType report : src.getCreateReport()) {
            final ScenarioCreateReport dst = new ScenarioCreateReport(_toGeneric(report.getResource())).setID(report.getId());
            for (final CustomErrorLevelType customLevel : report.getCustomLevel()) {
                final ScenarioCustomErrorLevel level = new ScenarioCustomErrorLevel(
                        EScenarioErrorLevel.getFromIDOrNull(customLevel.getLevel().value()));
                customLevel.getValue().forEach(level::addRuleID);
                dst.addCustomLevel(level);
            }
            ret.addCreateReport(dst);
        }
        ret.setAcceptMatch(src.getAcceptMatch());
        return ret;
    }

    private static @NonNull Scenario _toGeneric(@NonNull final ScenarioPdfType src) {
        final Scenario ret = new Scenario(EScenarioKind.PDF, src.getName());
        if (src.getRequirements() != null) {
            for (final RequirementType requirement : src.getRequirements().getRequirement()) {
                ret.addRequirement(ScenarioRequirement.of(requirement.getId()));
            }
        }
        if (src.getXmlScenarioRef() != null) {
            ret.setXmlScenarioRef(ScenarioCoordinate.parseOrNull(src.getXmlScenarioRef().getId()));
        }
        return ret;
    }

    /**
     * Convert the JAXB model of scenario configuration version 3 to the version independent model.
     *
     * @param src the source object to convert. May not be <code>null</code>.
     * @return the created generic configuration. Never <code>null</code>.
     */
    public static @NonNull ScenarioConfiguration toGeneric(@NonNull final Scenarios src) {
        ObjectHelper.requireNonNull(src, "Scenarios");

        final ScenarioConfiguration ret = new ScenarioConfiguration(src.getName());
        ret.setAuthor(src.getAuthor());
        ret.setLastModificationDate(JaxbHelper.getAsLocalDate(src.getLastModificationDate()));
        ret.setValidFromDate(JaxbHelper.getAsLocalDate(src.getValidFromDate()));
        ret.setFrameworkVersion(src.getFrameworkVersion());
        ret.setDescription(_toGeneric(src.getDescription()));
        for (final AbstractScenarioType scenario : src.getScenarioXmlOrScenarioPdf()) {
            final Scenario dst = switch (scenario) {
                case final ScenarioXmlType xml -> _toGeneric(xml);
                case final ScenarioPdfType pdf -> _toGeneric(pdf);
                default -> throw new IllegalStateException("Unsupported scenario type " + scenario.getClass().getName());
            };
            dst.setCoordinate(_toGeneric(scenario));
            dst.setDescription(_toGeneric(scenario.getDescription()));
            ret.addScenario(dst);
        }
        return ret;
    }

    // ---------- generic to version 3 ----------

    private static void _add(final @Nullable List<SimpleError> errors, @NonNull final CTStandardSeverity severity,
            @NonNull final String message) {
        final SimpleError error = new SimpleErrorBuilder().severity(severity).message(message).build();
        if (errors != null) {
            errors.add(error);
        } else {
            error.log(LOGGER);
        }
    }

    private static @Nullable DescriptionType _fromGeneric(final @Nullable ScenarioDescription src) {
        if (src == null || !src.isNotEmpty()) {
            return null;
        }
        final DescriptionType ret = new DescriptionType();
        for (final ScenarioDescriptionBlock block : src.getBlocks()) {
            switch (block.getKind()) {
                // Version 3 has mixed content, so free text is written as it is
                case TEXT -> ret.getContent().add(block.getText());
                case PARAGRAPH -> ret.getContent().add(OF.createDescriptionTypeP(block.getText()));
                case ORDERED_LIST -> {
                    final DescriptionListType list = new DescriptionListType();
                    list.getLi().addAll(block.getItems());
                    ret.getContent().add(OF.createDescriptionTypeOl(list));
                }
                case UNORDERED_LIST -> {
                    final DescriptionListType list = new DescriptionListType();
                    list.getLi().addAll(block.getItems());
                    ret.getContent().add(OF.createDescriptionTypeUl(list));
                }
            }
        }
        return ret;
    }

    private static void _fromGeneric(final @Nullable ScenarioCoordinate src, @NonNull final AbstractCoordinateType dst,
            @NonNull final String what, @NonNull final ConversionState state) {
        if (src == null) {
            state.error(what + " has no DVR coordinate, but scenario version 3 requires it");
            return;
        }
        dst.setGroupId(src.getGroupID());
        dst.setArtifactId(src.getArtifactID());
        dst.setVersion(src.getVersion());
        dst.setClassifier(src.getClassifier());
        if (!src.hasCoordinate()) {
            state.warn(what + " uses the DVR coordinate '" + src.getAsSingleID()
                    + "' which is written as it is, but which can not be parsed: " + src.getCoordinateError());
        }
    }

    private static @NonNull ResourceType _fromGeneric(@NonNull final ScenarioResource src, @NonNull final ConversionState state) {
        final ResourceType ret = new ResourceType();
        _fromGeneric(src.getCoordinate(), ret, "The resource '" + src.getName() + "'", state);
        ret.setName(src.getName());
        ret.setLocation(src.getLocation());
        return ret;
    }

    private static @NonNull ScenarioXmlType _fromGenericXml(@NonNull final Scenario src, @NonNull final ConversionState state) {
        final ScenarioXmlType ret = new ScenarioXmlType();
        for (final ScenarioNamespace ns : src.getNamespaces()) {
            final NamespaceType dst = new NamespaceType();
            dst.setPrefix(ns.getPrefix());
            dst.setValue(ns.getNamespaceURI());
            ret.getNamespace().add(dst);
        }
        ret.setMatch(src.getMatch());

        if (src.getXmlSchemas().isEmpty()) {
            state.error("The scenario '" + src.getName() + "' has no XML Schema resource, but scenario version 3 requires at least one");
        } else {
            final ValidateWithXmlSchemaType dst = new ValidateWithXmlSchemaType();
            for (final ScenarioResource resource : src.getXmlSchemas()) {
                dst.getResource().add(_fromGeneric(resource, state));
            }
            ret.setValidateWithXmlSchema(dst);
        }

        for (final ScenarioSchematron schematron : src.getSchematrons()) {
            final ValidateWithSchematronType dst = new ValidateWithSchematronType();
            dst.setResource(_fromGeneric(schematron.getResource(), state));
            // Only write the attribute if it differs from the default
            if (schematron.isPsvi()) {
                dst.setPsvi(Boolean.TRUE);
            }
            dst.setCompiler(schematron.getCompiler());
            ret.getValidateWithSchematron().add(dst);
        }

        for (final ScenarioCreateReport report : src.getCreateReports()) {
            final CreateReportType dst = new CreateReportType();
            if (report.hasID()) {
                dst.setId(report.getID());
            } else {
                state.error("The report of scenario '" + src.getName() + "' has no ID, but scenario version 3 requires it");
            }
            dst.setResource(_fromGeneric(report.getResource(), state));
            for (final ScenarioCustomErrorLevel customLevel : report.getCustomLevels()) {
                final CustomErrorLevelType dstLevel = new CustomErrorLevelType();
                dstLevel.setLevel(ErrorLevelType.fromValue(customLevel.getLevel().getID()));
                dstLevel.getValue().addAll(customLevel.getRuleIDs());
                dst.getCustomLevel().add(dstLevel);
            }
            ret.getCreateReport().add(dst);
        }
        ret.setAcceptMatch(src.getAcceptMatch());

        if (!src.getRequirements().isEmpty()) {
            state.warn("Dropping the " + src.getRequirements().size() + " requirement(s) of the XML scenario '" + src.getName()
                    + "', because only PDF scenarios can express them");
        }
        if (src.hasXmlScenarioRef()) {
            state.warn("Dropping the XML scenario reference of the XML scenario '" + src.getName()
                    + "', because only PDF scenarios can express it");
        }
        return ret;
    }

    private static @NonNull ScenarioPdfType _fromGenericPdf(@NonNull final Scenario src, @NonNull final ConversionState state) {
        final ScenarioPdfType ret = new ScenarioPdfType();
        if (!src.getRequirements().isEmpty()) {
            final RequirementsType requirements = new RequirementsType();
            for (final ScenarioRequirement requirement : src.getRequirements()) {
                final RequirementType dst = new RequirementType();
                dst.setId(requirement.getID());
                requirements.getRequirement().add(dst);
            }
            ret.setRequirements(requirements);
        }
        if (src.hasXmlScenarioRef()) {
            final ScenarioRefType dst = new ScenarioRefType();
            dst.setId(src.getXmlScenarioRef().getAsSingleID());
            ret.setXmlScenarioRef(dst);
        }

        if (!src.getNamespaces().isEmpty() || src.hasMatch() || !src.getXmlSchemas().isEmpty() || !src.getSchematrons().isEmpty()
                || !src.getCreateReports().isEmpty() || src.hasAcceptMatch()) {
            state.warn("Dropping the XML specific content of the PDF scenario '" + src.getName()
                    + "', because a PDF scenario delegates it to the referenced XML scenario");
        }
        return ret;
    }

    /**
     * Convert the version independent model to the JAXB model of scenario configuration version 3. All losses and all
     * values that version 3 requires but that are not set are collected in the provided list.
     *
     * @param src the source object to convert. May not be <code>null</code>.
     * @param errors the list to add all findings to. May be <code>null</code>, in which case all findings are logged
     *            instead.
     * @return the created JAXB object. Never <code>null</code>. It is only guaranteed to be schema valid if no error of
     *         severity {@link CTStandardSeverity#ERROR} was collected.
     */
    public static @NonNull Scenarios fromGeneric(@NonNull final ScenarioConfiguration src, final @Nullable List<SimpleError> errors) {
        ObjectHelper.requireNonNull(src, "Configuration");
        final ConversionState state = new ConversionState(errors);

        final Scenarios ret = new Scenarios();
        ret.setName(src.getName());
        ret.setAuthor(src.getAuthor());
        if (src.getLastModificationDate() != null) {
            ret.setLastModificationDate(JaxbHelper.getAsXmlDate(src.getLastModificationDate()));
        } else {
            state.error("The configuration has no last modification date, but scenario version 3 requires it");
        }
        ret.setValidFromDate(JaxbHelper.getAsXmlDate(src.getValidFromDate()));
        ret.setFrameworkVersion(src.getFrameworkVersion());
        ret.setDescription(_fromGeneric(src.getDescription()));

        for (final Scenario scenario : src.getScenarios()) {
            final AbstractScenarioType dst = switch (scenario.getKind()) {
                case XML -> _fromGenericXml(scenario, state);
                case PDF -> _fromGenericPdf(scenario, state);
            };
            _fromGeneric(scenario.getCoordinate(), dst, "The scenario '" + scenario.getName() + "'", state);
            dst.setName(scenario.getName());
            dst.setDescription(_fromGeneric(scenario.getDescription()));
            ret.getScenarioXmlOrScenarioPdf().add(dst);
        }
        if (ret.getScenarioXmlOrScenarioPdf().isEmpty()) {
            state.error("The configuration has no scenario, but scenario version 3 requires at least one");
        }
        return ret;
    }

    /**
     * Convert the version independent model to the JAXB model of scenario configuration version 3, logging all
     * findings.
     *
     * @param src the source object to convert. May not be <code>null</code>.
     * @return the created JAXB object. Never <code>null</code>.
     * @see #fromGeneric(ScenarioConfiguration, List)
     */
    public static @NonNull Scenarios fromGeneric(@NonNull final ScenarioConfiguration src) {
        return fromGeneric(src, null);
    }

    /**
     * Mutable state of a single "generic to version 3" conversion.
     */
    private static final class ConversionState {

        private final @Nullable List<SimpleError> errors;

        private ConversionState(final @Nullable List<SimpleError> errors) {
            this.errors = errors;
        }

        private void error(@NonNull final String message) {
            _add(this.errors, CTStandardSeverity.ERROR, message);
        }

        private void warn(@NonNull final String message) {
            _add(this.errors, CTStandardSeverity.WARNING, message);
        }
    }
}
