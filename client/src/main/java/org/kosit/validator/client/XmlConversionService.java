package org.kosit.validator.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.StringJoiner;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.kosit.jaxb.JaxbConversionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlRegistry;

/**
 * JAXB Conversion Utility.
 */
public class XmlConversionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlConversionService.class);

    private static final int MAX_LOG_CONTENT = 50;

    // context setup
    private JAXBContext jaxbContext;

    public JAXBContext getJaxbContext() {
        if (this.jaxbContext == null) {
            initialize();
        }
        return this.jaxbContext;
    }

    private void checkInputEmpty(final File xml) {
        if (xml == null) {
            throw new JaxbConversionException("Can not unmarshal from empty input file");
        }
    }

    private <T> void checkTypeEmpty(final Class<T> type) {
        if (type == null) {
            throw new JaxbConversionException("Can not unmarshal without type information. Need to specify a target type");
        }
    }

    /**
     * Initializes the default context; all packages with {@link XmlRegistry XmlRegistries}.
     */
    public void initialize() {
        final Collection<Package> p = new ArrayList<>();
        p.add(org.kosit.validator.model.ObjectFactory.class.getPackage());
        p.add(org.kosit.xvrl.model.ObjectFactory.class.getPackage());
        p.add(org.kosit.validator.model.scenarios.ObjectFactory.class.getPackage());
        initialize(p);
    }

    public void initialize(final Package... context) {
        initialize(Arrays.asList(context));
    }

    /**
     * Initializes the conversion service with the given packages.
     *
     * @param context packages for the JAXB context
     */
    public void initialize(final Collection<Package> context) {
        final String[] packages = context != null ? context.stream().map(Package::getName).toArray(String[]::new) : new String[0];
        final StringJoiner joiner = new StringJoiner(":");
        Arrays.stream(packages).forEach(joiner::add);
        initialize(joiner.toString());
    }

    /**
     * Initializes the conversion service with the given context path.
     *
     * @param contextPath the context path
     */
    private void initialize(final String contextPath) {
        try {
            this.jaxbContext = JAXBContext.newInstance(contextPath, XmlConversionService.class.getClassLoader());
        } catch (final JAXBException e) {
            throw new IllegalStateException("Can not create JAXB context for given context: " + contextPath, e);
        }
    }

    public <T> T readXml(final File xml, final Class<T> type) {
        checkInputEmpty(xml);
        checkTypeEmpty(type);
        try ( InputStream is = new FileInputStream(xml) ) {
            final XMLInputFactory inputFactory = XMLInputFactory.newFactory();
            inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            inputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
            inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            final XMLStreamReader xsr = inputFactory.createXMLStreamReader(is);
            final Unmarshaller u = getJaxbContext().createUnmarshaller();
            return u.unmarshal(xsr, type).getValue();
        } catch (final JAXBException | XMLStreamException | IOException e) {
            throw new JaxbConversionException("Can not unmarshal to type " + type.getSimpleName() + " from " + xml, e);
        }
    }
}
