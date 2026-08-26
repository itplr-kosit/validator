package org.kosit.svrl.impl;

import org.kosit.jaxb.JaxbConversionService;
import org.oclc.purl.dsdl.svrl.ObjectFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

/**
 * Convenience {@link JaxbConversionService} preconfigured for the SVRL JAXB model package
 * ({@code org.oclc.purl.dsdl.svrl}).
 */
public class SvrlConversionService extends JaxbConversionService {

    private static final JAXBContext JAXB_CTX;

    static {
        try {
            JAXB_CTX = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName(), SvrlConversionService.class.getClassLoader());
        } catch (final JAXBException e) {
            throw new IllegalStateException("Can not create SVRL JAXB context", e);
        }
    }

    /**
     * Creates a new conversion service for the SVRL model.
     *
     * @throws IllegalStateException if the JAXB context for the SVRL model package can not be created
     */
    public SvrlConversionService() {
        super(JAXB_CTX);
        // Don't use XML Schema to read SVRL - the created outcomes are very different
    }
}
