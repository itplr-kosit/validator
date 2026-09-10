package org.kosit.validator.scenario.v2;

import javax.xml.namespace.QName;
import javax.xml.validation.Schema;

import org.kosit.base.xml.SchemaResolver;
import org.kosit.jaxb.AbstractJaxbConverter;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;

/**
 * Convenience {@link AbstractJaxbConverter} preconfigured for the scenario JAXB model package of version 2
 * ({@code org.kosit.validator.scenario.v2}).
 */
public final class Scenario2Converter extends AbstractJaxbConverter<Scenarios> {

    /** The XML namespace URI of the scenario configuration version 2 */
    public static final String NS_URI = "http://www.xoev.de/de/validator/framework/2/scenarios";

    /** XSD for the scenarios.xml definition of version 2 */
    public static final String SCENARIOS_V2_XSD_PATH = "/xsd/scenarios-v2.xsd";

    private static final JAXBContext JAXB_CTX;

    private static final Schema SCHEMA;

    static {
        try {
            JAXB_CTX = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName(), Scenario2Converter.class.getClassLoader());
        } catch (final JAXBException e) {
            throw new IllegalStateException("Can not create scenario JAXB context", e);
        }

        SCHEMA = SchemaResolver.createParsedSchema(Scenario2Converter.class.getResource(SCENARIOS_V2_XSD_PATH));
    }

    /**
     * Creates a new conversion service for the scenario model.
     *
     * @throws IllegalStateException if the JAXB context for the scenario model package can not be created
     */
    public Scenario2Converter() {
        super(JAXB_CTX, Scenarios.class, x -> new JAXBElement<>(new QName(NS_URI, "scenarios"), Scenarios.class, x));
        // Always use XML Schema
        withSchema(SCHEMA);
    }
}
