package de.kosit.validationtool.api;

import java.net.URI;

import javax.xml.transform.URIResolver;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import net.sf.saxon.s9api.Processor;

/**
 * Centralized construction and configuration of XML related infrastructore components. The KoSIT Validator provides out
 * of the box implementaions with various security levels.
 * 
 * If you decide to implement a custom strategy, please be aware of XML security within your stack. The validator
 * components beyond this strategy asume secured implementation of the interfaces provided by this strategy. There is no
 * effort to mitigate or prevent xml related security issues such as XXE, loading external sources etc.
 * 
 * @see de.kosit.validationtool.impl.ResolvingMode
 * @author Andreas Penski
 */
public interface ResolvingConfigurationStrategy {

    /**
     * Creates a preconfigured {@link SchemaFactory} for loading {@link javax.xml.validation.Schema} objects. The
     * implementation is responsible for xml security. Take care
     * 
     * @return preconfigured {@link SchemaFactory}
     */
    SchemaFactory createSchemaFactory();

    /**
     * Returns a preconfigured {@link Processor Saxon Processor} for various tasks within the Validator. The validator
     * leverages the saxon s9api for internal processing e.g. xml reading and writing. So this is the main object to secure
     * for reading, transforming and writing xml files.
     * 
     * @return a preconfigured {@link Processor}
     */
    Processor getProcessor();

    /**
     * Creates a specific implementation for resolving referenced objects in XML files. The URIResolver, it is used for
     * dereferencing an absolute URI (after resolution) to return a {@link javax.xml.transform.Source}. It <b>can</b> be
     * used for resolving relative URIs against a base URI or restrict access to certain URIs.
     * <p>
     * This URIResolver is used to dereference the URIs appearing in <code>xsl:import</code>, <code>xsl:include</code>, and
     * <code>xsl:import-schema</code> declarations.
     * </p>
     * 
     * @return a preconfigured {@link URIResolver}
     */
    URIResolver createResolver(URI scenarioRepository);

    /**
     * Creates a preconfigured {@link Validator } instance for a given schema for xml file validation. The implementation
     * takes care about security and reference resolving strategies.
     * 
     * @param schema the scheme to create a {@link Validator} for
     * @return a preconfigured {@link Validator}
     */
    Validator createValidator(Schema schema);

}
