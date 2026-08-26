package org.kosit.validator.scenario.v1;

import javax.xml.validation.Schema;

import org.kosit.jaxb.JaxbConversionService;
import org.kosit.jaxb.xml.SchemaResolver;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

/**
 * Convenience {@link JaxbConversionService} preconfigured for the scenario JAXB model package
 * ({@code org.kosit.validator.scenario.model}).
 */
public class Scenario1ConversionService extends JaxbConversionService {

    private static final JAXBContext JAXB_CTX;

    private static final Schema SCHEMA;

    static {
        try {
            JAXB_CTX = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName(),
                    Scenario1ConversionService.class.getClassLoader());
        } catch (final JAXBException e) {
            throw new IllegalStateException("Can not create scenario JAXB context", e);
        }

        SCHEMA = SchemaResolver.createParsedSchema(Scenario1ConversionService.class.getResource(ScenarioSchemas.SCENARIOS_V1_XSD_PATH));
    }

    /**
     * Creates a new conversion service for the scenario model.
     *
     * @throws IllegalStateException if the JAXB context for the scenario model package can not be created
     */
    public Scenario1ConversionService() {
        super(JAXB_CTX);
        withSchema(SCHEMA);
    }
}
