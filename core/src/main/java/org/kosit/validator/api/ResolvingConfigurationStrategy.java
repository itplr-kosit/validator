package org.kosit.validator.api;

import java.net.URI;

import javax.xml.transform.URIResolver;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import net.sf.saxon.lib.ResourceResolver;
import net.sf.saxon.lib.UnparsedTextURIResolver;

/**
 * Centralized construction and configuration of XML related infrastructure components. This interface allows to use
 * custom implementations and configurations of internal xml related factories and objects.
 * 
 * The KoSIT Validator provides out of the box implementations with various security levels based on openjdk SAX stack.
 * 
 * If you decide to implement a custom strategy, please be aware of XML security within your stack. The validator
 * components beyond this strategy asume secured implementation of the interfaces provided by this strategy. There is no
 * effort to mitigate or prevent xml related security issues such as XXE, loading external sources etc. Your would be
 * responsible for this!
 * 
 * @see org.kosit.validator.impl.ResolvingMode
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
     * Creates a specific implementation for resolving referenced objects in XML files. The URIResolver is used for
     * de-referencing an absolute URI (after resolution) to return a {@link javax.xml.transform.Source}. It <b>can</b>
     * be used for resolving relative URIs against a base URI or restrict access to certain URIs.
     * <p>
     * This URIResolver is used to dereference the URIs appearing in <code>xsl:import</code>, <code>xsl:include</code>,
     * and <code>xsl:import-schema</code> declarations.
     * </p>
     *
     * @deprecated since Saxon deprecates the using in favor of {@link ResourceResolver}. Support is removed, when Saxon
     *             removes it.
     * @param scenarioRepository an optional repository, your implementation might not need this
     * @return a preconfigured {@link URIResolver}
     */
    @Deprecated
    default URIResolver createResolver(final URI scenarioRepository) {
        // intentionally return null, so no subclass needs to implement it.
        return null;
    }

    /**
     * Creates a specific implementation for resolving referenced objects in XML files. The ResourceResolver is used for
     * de-referencing an absolute URI (after resolution) to return a {@link javax.xml.transform.Source}. It <b>can</b>
     * be used for resolving relative URIs against a base URI or restrict access to certain URIs.
     * <p>
     * This ResourceResolver is used to de-reference the URIs appearing in <code>xsl:import</code>,
     * <code>xsl:include</code>, and <code>xsl:import-schema</code> declarations.
     * </p>
     *
     * @param scenarioRepository an optional repository, your implementation might not need this
     * @return a preconfigured {@link ResourceResolver}
     */
    ResourceResolver createResourceResolver(URI scenarioRepository);

    /**
     * Creates a specific implementation for resolving objects referenced via XSLT's <code>unparsed-text()</code>
     * function.
     * 
     * @param scenarioRepository an optional repository, your implementation might not need this
     * @return a preconfigured {@link net.sf.saxon.lib.UnparsedTextURIResolver} or null for using saxons default
     */
    UnparsedTextURIResolver createUnparsedTextURIResolver(URI scenarioRepository);

    /**
     * Creates a preconfigured {@link Validator } instance for a given schema for xml file validation. The
     * implementation takes care about security and reference resolving strategies.
     * 
     * @param schema the scheme to create a {@link Validator} for
     * @return a preconfigured {@link Validator}
     */
    Validator createValidator(Schema schema);

}
