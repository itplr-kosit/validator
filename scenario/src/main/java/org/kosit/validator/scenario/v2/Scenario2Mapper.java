package org.kosit.validator.scenario.v2;

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
import org.kosit.validator.scenario.generic.EScenarioErrorLevel;
import org.kosit.validator.scenario.generic.EScenarioKind;
import org.kosit.validator.scenario.generic.Scenario;
import org.kosit.validator.scenario.generic.ScenarioConfiguration;
import org.kosit.validator.scenario.generic.ScenarioCreateReport;
import org.kosit.validator.scenario.generic.ScenarioCustomErrorLevel;
import org.kosit.validator.scenario.generic.ScenarioDescription;
import org.kosit.validator.scenario.generic.ScenarioDescriptionBlock;
import org.kosit.validator.scenario.generic.ScenarioNamespace;
import org.kosit.validator.scenario.generic.ScenarioResource;
import org.kosit.validator.scenario.generic.ScenarioSchematron;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBElement;

/**
 * Converts between the JAXB model of scenario configuration version 2 and the version independent
 * {@link ScenarioConfiguration}.
 * <p>
 * Reading version 2 is lossless. Writing version 2 can not express everything the generic model can hold, so
 * {@link #fromGeneric(ScenarioConfiguration, List)} reports every dropped value and every value that version 2 requires
 * but that is not set. The following data is lost when writing version 2:
 * <ul>
 * <li>the DVR coordinates of the scenarios and of the resources</li>
 * <li>the {@code validFromDate} of the configuration</li>
 * <li>all {@link EScenarioKind#PDF} scenarios including their requirements and their XML scenario reference</li>
 * </ul>
 *
 * @author Philip Helger
 * @see Scenario2Converter
 */
public final class Scenario2Mapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(Scenario2Mapper.class);

    private static final ObjectFactory OF = new ObjectFactory();

    private Scenario2Mapper() {
    }

    // ---------- version 2 to generic ----------

    private static @Nullable ScenarioDescription _toGeneric(final @Nullable DescriptionType src) {
        if (src == null) {
            return null;
        }
        final ScenarioDescription ret = new ScenarioDescription();
        for (final JAXBElement<? extends Serializable> element : src.getPOrOlOrUl()) {
            final Serializable value = element.getValue();
            switch (value) {
                case final DescriptionType.Ol ol -> {
                    if (!ol.getLi().isEmpty()) {
                        ret.addOrderedList(ol.getLi());
                    }
                }
                case final DescriptionType.Ul ul -> {
                    if (!ul.getLi().isEmpty()) {
                        ret.addUnorderedList(ul.getLi());
                    }
                }
                case final String text -> {
                    if (StringHelper.isNotBlank(text)) {
                        ret.addParagraph(text);
                    }
                }
                case null, default -> LOGGER.warn("Ignoring unsupported description content of type " + value);
            }
        }
        return ret.isNotEmpty() ? ret : null;
    }

    private static @NonNull ScenarioResource _toGeneric(@NonNull final ResourceType src) {
        return new ScenarioResource(src.getName()).setLocation(src.getLocation());
    }

    private static @NonNull Scenario _toGeneric(@NonNull final ScenarioType src) {
        final Scenario ret = new Scenario(EScenarioKind.XML, src.getName());
        ret.setDescription(_toGeneric(src.getDescription()));
        for (final NamespaceType ns : src.getNamespace()) {
            ret.addNamespace(ScenarioNamespace.of(ns.getPrefix(), ns.getValue()));
        }
        ret.setMatch(src.getMatch());
        if (src.getValidateWithXmlSchema() != null) {
            for (final ResourceType resource : src.getValidateWithXmlSchema().getResource()) {
                ret.addXmlSchema(_toGeneric(resource));
            }
        }
        for (final ValidateWithSchematron schematron : src.getValidateWithSchematron()) {
            ret.addSchematron(new ScenarioSchematron(_toGeneric(schematron.getResource())).setPsvi(schematron.isPsvi())
                    .setCompiler(schematron.getCompiler()));
        }
        for (final CreateReportType report : src.getCreateReport()) {
            final ScenarioCreateReport dst = new ScenarioCreateReport(_toGeneric(report.getResource())).setID(report.getId());
            for (final CustomErrorLevel customLevel : report.getCustomLevel()) {
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

    /**
     * Convert the JAXB model of scenario configuration version 2 to the version independent model. This conversion is
     * lossless.
     *
     * @param src the source object to convert. May not be <code>null</code>.
     * @return the created generic configuration. Never <code>null</code>.
     */
    public static @NonNull ScenarioConfiguration toGeneric(@NonNull final Scenarios src) {
        ObjectHelper.requireNonNull(src, "Scenarios");

        final ScenarioConfiguration ret = new ScenarioConfiguration(src.getName());
        ret.setAuthor(src.getAuthor());
        ret.setLastModificationDate(JaxbHelper.getAsLocalDate(src.getDate()));
        ret.setFrameworkVersion(src.getFrameworkVersion());
        ret.setDescription(_toGeneric(src.getDescription()));
        for (final ScenarioType scenario : src.getScenario()) {
            ret.addScenario(_toGeneric(scenario));
        }
        return ret;
    }

    // ---------- generic to version 2 ----------

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
                // Version 2 has no free text, so it becomes a paragraph
                case TEXT, PARAGRAPH -> ret.getPOrOlOrUl().add(OF.createDescriptionTypeP(block.getText()));
                case ORDERED_LIST -> {
                    final DescriptionType.Ol ol = new DescriptionType.Ol();
                    ol.getLi().addAll(block.getItems());
                    ret.getPOrOlOrUl().add(OF.createDescriptionTypeOl(ol));
                }
                case UNORDERED_LIST -> {
                    final DescriptionType.Ul ul = new DescriptionType.Ul();
                    ul.getLi().addAll(block.getItems());
                    ret.getPOrOlOrUl().add(OF.createDescriptionTypeUl(ul));
                }
            }
        }
        return ret;
    }

    private static @NonNull ResourceType _fromGeneric(@NonNull final ScenarioResource src, @NonNull final ConversionState state) {
        final ResourceType ret = new ResourceType();
        ret.setName(src.getName());
        if (src.hasLocation()) {
            ret.setLocation(src.getLocation());
        } else {
            state.error("The resource '" + src.getName() + "' has no location, but scenario version 2 requires it");
        }
        if (src.hasCoordinate()) {
            state.droppedResourceCoordinates++;
        }
        return ret;
    }

    private static @NonNull ScenarioType _fromGeneric(@NonNull final Scenario src, @NonNull final ConversionState state) {
        final ScenarioType ret = new ScenarioType();
        ret.setName(src.getName());
        ret.setDescription(_fromGeneric(src.getDescription()));
        for (final ScenarioNamespace ns : src.getNamespaces()) {
            final NamespaceType dst = new NamespaceType();
            dst.setPrefix(ns.getPrefix());
            dst.setValue(ns.getNamespaceURI());
            ret.getNamespace().add(dst);
        }
        if (src.hasMatch()) {
            ret.setMatch(src.getMatch());
        } else {
            state.error("The scenario '" + src.getName() + "' has no match expression, but scenario version 2 requires it");
        }

        if (src.getXmlSchemas().isEmpty()) {
            state.error("The scenario '" + src.getName() + "' has no XML Schema resource, but scenario version 2 requires at least one");
        } else {
            final ValidateWithXmlSchema dst = new ValidateWithXmlSchema();
            for (final ScenarioResource resource : src.getXmlSchemas()) {
                dst.getResource().add(_fromGeneric(resource, state));
            }
            ret.setValidateWithXmlSchema(dst);
        }

        for (final ScenarioSchematron schematron : src.getSchematrons()) {
            final ValidateWithSchematron dst = new ValidateWithSchematron();
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
                state.error("The report of scenario '" + src.getName() + "' has no ID, but scenario version 2 requires it");
            }
            dst.setResource(_fromGeneric(report.getResource(), state));
            for (final ScenarioCustomErrorLevel customLevel : report.getCustomLevels()) {
                final CustomErrorLevel dstLevel = new CustomErrorLevel();
                dstLevel.setLevel(ErrorLevelType.fromValue(customLevel.getLevel().getID()));
                dstLevel.getValue().addAll(customLevel.getRuleIDs());
                dst.getCustomLevel().add(dstLevel);
            }
            ret.getCreateReport().add(dst);
        }
        ret.setAcceptMatch(src.getAcceptMatch());

        if (src.hasCoordinate()) {
            state.droppedScenarioCoordinates++;
        }
        if (!src.getRequirements().isEmpty()) {
            state.warn("Dropping the " + src.getRequirements().size() + " requirement(s) of scenario '" + src.getName()
                    + "', because scenario version 2 can not express them");
        }
        if (src.hasXmlScenarioRef()) {
            state.warn("Dropping the XML scenario reference of scenario '" + src.getName()
                    + "', because scenario version 2 can not express it");
        }
        return ret;
    }

    /**
     * Convert the version independent model to the JAXB model of scenario configuration version 2. All losses and all
     * values that version 2 requires but that are not set are collected in the provided list.
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
            ret.setDate(JaxbHelper.getAsXmlDate(src.getLastModificationDate()));
        } else {
            state.error("The configuration has no last modification date, but scenario version 2 requires it");
        }
        if (src.hasFrameworkVersion()) {
            ret.setFrameworkVersion(src.getFrameworkVersion());
        } else {
            state.error("The configuration has no framework version, but scenario version 2 requires it");
        }
        final DescriptionType description = _fromGeneric(src.getDescription());
        if (description != null) {
            ret.setDescription(description);
        } else {
            state.error("The configuration has no description, but scenario version 2 requires a non-empty one");
        }
        if (src.getValidFromDate() != null) {
            state.info("Dropping the valid from date '" + src.getValidFromDate() + "', because scenario version 2 can not express it");
        }

        for (final Scenario scenario : src.getScenarios()) {
            if (scenario.isXml()) {
                ret.getScenario().add(_fromGeneric(scenario, state));
            } else {
                state.warn("Dropping the " + scenario.getKind().getID() + " scenario '" + scenario.getName()
                        + "', because scenario version 2 only supports " + EScenarioKind.XML.getID() + " scenarios");
            }
        }
        if (ret.getScenario().isEmpty()) {
            state.error("The configuration has no XML scenario, but scenario version 2 requires at least one");
        }

        state.finish();
        return ret;
    }

    /**
     * Convert the version independent model to the JAXB model of scenario configuration version 2, logging all
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
     * Mutable state of a single "generic to version 2" conversion, collecting the aggregated losses.
     */
    private static final class ConversionState {

        private final @Nullable List<SimpleError> errors;

        private int droppedScenarioCoordinates;

        private int droppedResourceCoordinates;

        private ConversionState(final @Nullable List<SimpleError> errors) {
            this.errors = errors;
        }

        private void error(@NonNull final String message) {
            _add(this.errors, CTStandardSeverity.ERROR, message);
        }

        private void warn(@NonNull final String message) {
            _add(this.errors, CTStandardSeverity.WARNING, message);
        }

        private void info(@NonNull final String message) {
            // The project uses NONE as the "information" severity
            _add(this.errors, CTStandardSeverity.NONE, message);
        }

        private void finish() {
            if (this.droppedScenarioCoordinates > 0 || this.droppedResourceCoordinates > 0) {
                info("Dropping the DVR coordinates of " + this.droppedScenarioCoordinates + " scenario(s) and of "
                        + this.droppedResourceCoordinates + " resource(s), because scenario version 2 can not express them");
            }
        }
    }
}
