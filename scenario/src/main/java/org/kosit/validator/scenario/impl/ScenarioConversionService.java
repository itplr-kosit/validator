package org.kosit.validator.scenario.impl;

import org.kosit.jaxb.JaxbConversionService;
import org.kosit.validator.scenario.model.ObjectFactory;
import org.kosit.validator.scenario.xsd.ScenarioSchemaProvider;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

/**
 * Convenience {@link JaxbConversionService} preconfigured for the scenario JAXB model package
 * ({@code org.kosit.validator.scenario.model}).
 */
public class ScenarioConversionService extends JaxbConversionService {

    private static final JAXBContext JAXB_CTX;

    static {
        try {
            JAXB_CTX = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName(),
                    ScenarioConversionService.class.getClassLoader());
        } catch (final JAXBException e) {
            throw new IllegalStateException("Can not create scenario JAXB context", e);
        }
    }

    /**
     * Creates a new conversion service for the scenario model.
     *
     * @throws IllegalStateException if the JAXB context for the scenario model package can not be created
     */
    public ScenarioConversionService() {
        super(JAXB_CTX);
        withSchema(ScenarioSchemaProvider.getScenarioSchema());
    }
}
