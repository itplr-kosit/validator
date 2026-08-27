package org.kosit.svrl.impl;

import org.kosit.jaxb.AbstractJaxbConverter;
import org.oclc.purl.dsdl.svrl.ObjectFactory;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

/**
 * Convenience {@link AbstractJaxbConverter} preconfigured for the SVRL JAXB model package
 * ({@code org.oclc.purl.dsdl.svrl}).
 */
public final class SvrlConverter extends AbstractJaxbConverter<SchematronOutputType> {

    private static final JAXBContext JAXB_CTX;

    static {
        try {
            JAXB_CTX = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName(), SvrlConverter.class.getClassLoader());
        } catch (final JAXBException e) {
            throw new IllegalStateException("Can not create SVRL JAXB context", e);
        }
    }

    /**
     * Creates a new conversion service for the SVRL model.
     *
     * @throws IllegalStateException if the JAXB context for the SVRL model package can not be created
     */
    public SvrlConverter() {
        super(JAXB_CTX, SchematronOutputType.class, new ObjectFactory()::createSchematronOutput);
        // Don't use XML Schema to read SVRL - the created outcomes are very different
    }
}
