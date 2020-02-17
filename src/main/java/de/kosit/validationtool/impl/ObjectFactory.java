/*
 * Licensed to the Koordinierungsstelle für IT-Standards (KoSIT) under
 * one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  KoSIT licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.kosit.validationtool.impl;

import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.XMLConstants;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import lombok.extern.slf4j.Slf4j;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.CollectionFinder;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.lib.OutputURIResolver;
import net.sf.saxon.lib.ResourceCollection;
import net.sf.saxon.lib.UnparsedTextURIResolver;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.trans.XPathException;

/**
 * Eine Factory für häufig verwendete Objekte mit XML. Zentralisiert die XML Security Konfiguration. Die Konfiguration
 * basiert auf den <a href="https://www.owasp.org/index.php/XML_Security_Cheat_Sheet">OWASP-Empfehlungen</a>.
 *
 * Diese Klasse ist stark abhängig von der Verwendung eines Oracle JDK. Alternative JDKs haben u.U. eine andere SAX- /
 * StAX- / XML-Implementierug und profitieren entsprechend NICHT von den hier getroffenen Einstellungen.
 * 
 * @author Andreas Penski
 */
@Slf4j
public class ObjectFactory {

    private static class SecureUriResolver implements CollectionFinder, OutputURIResolver, UnparsedTextURIResolver {

        public static final String MESSAGE = "Configuration error. Resolving ist not allowed";

        @Override
        public OutputURIResolver newInstance() {
            return this;
        }

        @Override
        public Result resolve(final String href, final String base) throws TransformerException {
            throw new IllegalStateException(MESSAGE);
        }

        @Override
        public void close(final Result result) throws TransformerException {
            throw new IllegalStateException(MESSAGE);
        }

        @Override
        public Reader resolve(final URI absoluteURI, final String encoding, final Configuration config) throws XPathException {
            throw new IllegalStateException(MESSAGE);
        }

        @Override
        public ResourceCollection findCollection(final XPathContext context, final String collectionURI) throws XPathException {
            throw new IllegalStateException(MESSAGE);
        }
    }
    private static final String ORACLE_XERCES_CLASS = "com.sun.org.apache.xerces.internal.impl.Constants";
    private static final String DISSALLOW_DOCTYPE_DECL_FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
    private static final String LOAD_EXTERNAL_DTD_FEATURE = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
    private static final String FEATURE_SECURE_PROCESSING = "http://javax.xml.XMLConstants/feature/secure-processing";
    private static Processor processor;

    static {
        try {
            Class.forName(ORACLE_XERCES_CLASS);
        } catch (final ClassNotFoundException e) {
            log.warn("No oracle JDK version of XERCES found. Configured security features may not have any effect.");
            log.warn("Please take care of XML security while checking your xml contents");
        }
    }

    private ObjectFactory() {
        // hide, it's a factory
    }

    private static DocumentBuilderFactory createDocumentBuilderFactory(final boolean validating) {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            dbf.setValidating(validating);
            dbf.setNamespaceAware(true);

            // explicitly enable secure processing
            dbf.setFeature(FEATURE_SECURE_PROCESSING, true);

            // This is the PRIMARY defense. If DTDs (doctypes) are disallowed, almost all XML entity attacks are prevented
            dbf.setFeature(DISSALLOW_DOCTYPE_DECL_FEATURE, true);

            // Disable external DTDs as well
            dbf.setFeature(LOAD_EXTERNAL_DTD_FEATURE, false);

            // and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks"
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);
            return dbf;
        } catch (final ParserConfigurationException e) {
            throw new IllegalStateException("Can not create DocumentBuilderFactory due to underlying configuration error", e);
        }

    }

    /**
     * Transformer für die Ausgabe. Nutzt nicht Saxon!
     *
     * @param prettyPrint pretty-printing der Ausgabe
     * @return einen vorkonfigurierten Transformer
     */
    public static Transformer createTransformer(final boolean prettyPrint) {
        Transformer transformer = null;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
            if (prettyPrint) {
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            }
            return transformer;
        } catch (final TransformerConfigurationException e) {
            throw new IllegalStateException("Can not create Transformer due to underlying configuration error", e);
        }
    }

    /**
     * Erzeugt einen Zeitstempel zur Verwendung in XML-Objekten
     *
     * @return eine Instanz {@link XMLGregorianCalendar}
     */
    public static XMLGregorianCalendar createTimestamp() {
        try {
            final GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(new Date());
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);

        } catch (final DatatypeConfigurationException e) {
            throw new IllegalStateException("Can not create timestamp", e);
        }
    }

    public static DocumentBuilder createDocumentBuilder(final boolean validating) {
        try {
            return createDocumentBuilderFactory(validating).newDocumentBuilder();
        } catch (final ParserConfigurationException e) {
            throw new IllegalStateException("Can not create DocumentFactory due to underlying configuration error", e);
        }
    }

    private static String encode(final String input) {
        try {
            return URLEncoder.encode(input, StandardCharsets.UTF_8.name());
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException("Error encoding property while initializing saxon", e);
        }
    }

    public static Processor createProcessor() {
        if (processor == null) {
            processor = new Processor(false);
            //verhindere  global im Prinzip alle resolving strategien
            final SecureUriResolver resolver = new SecureUriResolver();
            processor.getUnderlyingConfiguration().setCollectionFinder(resolver);
            processor.getUnderlyingConfiguration().setOutputURIResolver(resolver);
            //hier fehlt eigentlich noch der UriResolver für unparsed text, wird erst ab Saxon 9.8 unterstützt

            //grundsätzlich Feature-konfiguration:
            processor.setConfigurationProperty(FeatureKeys.DTD_VALIDATION, false);
            processor.setConfigurationProperty(FeatureKeys.ENTITY_RESOLVER_CLASS, "");
            processor.setConfigurationProperty(FeatureKeys.XINCLUDE, false);
            processor.setConfigurationProperty(FeatureKeys.ALLOW_EXTERNAL_FUNCTIONS, false);

            // Konfiguration des zu verwendenden Parsers, wenn Saxon selbst einen erzeugen muss, bspw. beim XSL parsen
            processor.setConfigurationProperty(FeatureKeys.XML_PARSER_FEATURE + encode(FEATURE_SECURE_PROCESSING), true);
            processor.setConfigurationProperty(FeatureKeys.XML_PARSER_FEATURE + encode(DISSALLOW_DOCTYPE_DECL_FEATURE), true);
            processor.setConfigurationProperty(FeatureKeys.XML_PARSER_FEATURE + encode(LOAD_EXTERNAL_DTD_FEATURE), false);
        }
        return processor;
    }

    /**
     * Erzeugt einen Validier für das angegebenen Schema.
     *
     * @param schema das Schema mit dem validiert werden soll
     * @return einen vorkonfigurierten Validator
     */
    public static Validator createValidator(final Schema schema) {
        final Validator validator = schema.newValidator();
        try {
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        } catch (final SAXNotRecognizedException | SAXNotSupportedException e) {
            log.warn("Can not disable external DTD access. Maybe an unsupported JAXP implementation is used.");
            log.debug(e.getMessage(), e);
        }
        try {
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        } catch (final SAXNotRecognizedException | SAXNotSupportedException e) {
            log.warn("Can not disable external DTD access. Maybe an unsupported JAXP implementation is used.");
            log.debug(e.getMessage(), e);

        }
        return validator;
    }

    public static SchemaFactory createSchemaFactory() {
        final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            sf.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            sf.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "file");
        } catch (final SAXException e) {
            log.warn("Can not disable external DTD access, maybe an unsupported JAXP implementation is used", e);
        }
        return sf;
    }
}
