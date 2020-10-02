/*
 * Copyright 2017-2020  Koordinierungsstelle für IT-Standards (KoSIT)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.kosit.validationtool.api;

import java.net.URI;

import javax.xml.transform.URIResolver;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import net.sf.saxon.lib.UnparsedTextURIResolver;
import net.sf.saxon.s9api.Processor;

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
     * leverages the saxon s9api for internal processing e.g. xml reading and writing. So this is the main object to
     * secure for reading, transforming and writing xml files.
     *
     * Note: you need exactly one instance for all validator related processing.
     *
     * @return a preconfigured {@link Processor}
     */
    Processor getProcessor();

    /**
     * Creates a specific implementation for resolving referenced objects in XML files. The URIResolver is used for
     * dereferencing an absolute URI (after resolution) to return a {@link javax.xml.transform.Source}. It <b>can</b> be
     * used for resolving relative URIs against a base URI or restrict access to certain URIs.
     * <p>
     * This URIResolver is used to dereference the URIs appearing in <code>xsl:import</code>, <code>xsl:include</code>,
     * and <code>xsl:import-schema</code> declarations.
     * </p>
     *
     * @param scenarioRepository an optional repository, your implementation might not need this
     * @return a preconfigured {@link URIResolver}
     */
    URIResolver createResolver(URI scenarioRepository);

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
