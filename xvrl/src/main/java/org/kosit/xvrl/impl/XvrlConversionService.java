package org.kosit.xvrl.impl;

import java.util.HashMap;
import java.util.Map;

import org.kosit.jaxb.AbstractJaxbConverter;
import org.kosit.xvrl.model.ObjectFactory;
import org.kosit.xvrl.model.XvrlReportsType;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

/**
 * Convenience {@link AbstractJaxbConverter} preconfigured for the XVRL JAXB model package
 * ({@code org.kosit.xvrl.model}).
 */
public final class XvrlConversionService extends AbstractJaxbConverter<XvrlReportsType> {

    public static final String XSD_PATH = "/xsd";

    public static final String XVRL_XSD_PATH = XSD_PATH + "/xvrl-1.0.xsd";

    public static final JAXBContext JAXB_CTX;

    public static final String NS_URI = "http://www.xproc.org/ns/xvrl";

    private static final Map<String, String> NS_PREFIX = new HashMap<>();

    static {
        try {
            JAXB_CTX = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName(), XvrlConversionService.class.getClassLoader());
        } catch (final JAXBException e) {
            throw new IllegalStateException("Can not create XVRL JAXB context", e);
        }
        NS_PREFIX.put(NS_URI, "");
    }

    /**
     * Creates a new conversion service for the XVRL model.
     *
     * @throws IllegalStateException if the JAXB context for the XVRL model package can not be created
     */
    public XvrlConversionService() {
        super(JAXB_CTX, XvrlReportsType.class, new ObjectFactory()::createReports);
        withNamespacePrefixMap(NS_PREFIX);
    }
}
