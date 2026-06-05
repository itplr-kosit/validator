package org.kosit.validator.impl;

import org.kosit.jaxb.JaxbConversionService;
import org.kosit.validator.model.scenarios.ObjectFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

/**
 * Convenience {@link JaxbConversionService} preconfigured for the Scenarios JAXB model package
 * ({@code org.kosit.validator.model.scenarios}).
 */
public class ScenariosConversionService extends JaxbConversionService {

    private static final JAXBContext JAXB_CTX;

    static {
        try {
            JAXB_CTX = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName(),
                    ScenariosConversionService.class.getClassLoader());
        } catch (final JAXBException e) {
            throw new IllegalStateException("Can not create Scenarios JAXB context", e);
        }
    }

    /**
     * Creates a new conversion service for the Scenarios model.
     *
     * @throws IllegalStateException if the JAXB context for the Scenarios model package can not be created
     */
    public ScenariosConversionService() {
        super(JAXB_CTX);
        withSchema(SchemaProvider.getScenarioSchema());
    }
}
